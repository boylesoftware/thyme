/*
 * Copyright 2013 Boyle Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.boylesoftware.web.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import com.boylesoftware.web.RequestedResourceException;
import com.boylesoftware.web.spi.ControllerMethodArgHandler;
import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.ControllerMethodHandler;
import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.spi.UserInputHandler;
import com.boylesoftware.web.util.pool.AbstractPoolable;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;


/**
 * {@link ControllerMethodHandler} implementation.
 *
 * @author Lev Himmelfarb
 */
class ControllerMethodHandlerImpl
	implements ControllerMethodHandler {

	/**
	 * Method arguments container.
	 */
	private static final class Args
		extends AbstractPoolable {

		/**
		 * Argument values.
		 */
		private final Object[] values;


		/**
		 * Create new container.
		 *
		 * @param pool Reference to the pool.
		 * @param pooledObjectId Pooled object id.
		 * @param numArgs Number of method arguments.
		 */
		Args(final FastPool<Args> pool, final int pooledObjectId,
				final int numArgs) {
			super(pool, pooledObjectId);

			this.values = new Object[numArgs];
		}


		/* (non-Javadoc)
		 * @see com.boylesoftware.web.util.pool.AbstractPoolable#recycle()
		 */
		@Override
		public void recycle() {

			Arrays.fill(this.values, null);

			super.recycle();
		}


		/**
		 * Get argument values array.
		 *
		 * @return Argument values array.
		 */
		Object[] getValues() {

			return this.values;
		}
	}


	/**
	 * The controller.
	 */
	private final Object controller;

	/**
	 * The method.
	 */
	private final Method method;

	/**
	 * Handlers for the method arguments.
	 */
	private final ControllerMethodArgHandler[] argHandlers;

	/**
	 * List view of the argument handlers array.
	 */
	private final List<ControllerMethodArgHandler> argHandlersList;

	/**
	 * Argument arrays pool.
	 */
	private final FastPool<Args> argsPool;

	/**
	 * Redirect on success.
	 */
	private final boolean redirectOnSuccess;

	/**
	 * Tells if the method needs entity manager.
	 */
	private final boolean needsEntityManager;

	/**
	 * Handler for the user input method parameter, or {@code null} if none.
	 */
	private final UserInputHandler userInputHandler;


	/**
	 * Create new handler for the specified controller method.
	 *
	 * @param sc Servlet context.
	 * @param controller The controller.
	 * @param method Controller method to wrap.
	 * @param argHandlerProvider Controller method argument handler provider to
	 * use.
	 * @param allowUserInput {@code true} if the method allows user input beans
	 * as parameters.
	 * @param redirectOnSuccess {@code true} if the response should be a
	 * redirect response if the method is successful. The {@link #call} method
	 * returns the redirect URL in that case.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	ControllerMethodHandlerImpl(final ServletContext sc,
			final Object controller, final Method method,
			final ControllerMethodArgHandlerProvider argHandlerProvider,
			final boolean allowUserInput, final boolean redirectOnSuccess)
		throws UnavailableException {

		this.controller = controller;

		this.method = method;
		if (this.method.isVarArgs())
			throw new UnavailableException("Controller's " +
					this.method.getName() +
					" method has variable number of arguments.");
		this.method.setAccessible(true);

		final Class<?>[] argTypes = this.method.getParameterTypes();
		final Annotation[][] argAnnos = this.method.getParameterAnnotations();
		this.argHandlers = new ControllerMethodArgHandler[argTypes.length];
		this.argHandlersList = Collections.unmodifiableList(
				Arrays.asList(this.argHandlers));
		UserInputHandler userInputHandler = null;
		boolean needsEntityManager = false;
		for (int i = 0; i < argTypes.length; i++) {
			final ControllerMethodArgHandler h =
				argHandlerProvider.getHandler(sc, i, argTypes[i], argAnnos[i]);
			if (h == null)
				throw new UnavailableException("Controller's " +
						this.method.getName() +
						" method has unsupported argument type " +
						argTypes[i].getName() + " in position " + i + ".");
			needsEntityManager |= h.usesEntityManager();
			if (h instanceof UserInputHandler) {
				if (!allowUserInput)
					throw new UnavailableException("Controller's " +
							this.method.getName() +
							" method does not support user input arguments.");
				if (userInputHandler != null)
					throw new UnavailableException("Controller method cannot" +
							" have more than one user input bean argument.");
				userInputHandler = (UserInputHandler) h;
			}
			this.argHandlers[i] = h;
		}
		this.needsEntityManager = needsEntityManager;
		this.userInputHandler = userInputHandler;

		this.argsPool = new FastPool<>(new PoolableObjectFactory<Args>() {

			@Override
			public Args makeNew(final FastPool<Args> pool,
					final int pooledObjectId) {

				return new Args(pool, pooledObjectId, argTypes.length);
			}
		}, "ControllerMethodArgumentListsPool");

		this.redirectOnSuccess = redirectOnSuccess;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodHandler#prepareUserInput(com.boylesoftware.web.spi.RouterRequest)
	 */
	@Override
	public boolean prepareUserInput(final RouterRequest request)
		throws ServletException {

		return (this.userInputHandler != null ?
				this.userInputHandler.prepareUserInput(request) : true);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodHandler#redirectOnSuccess()
	 */
	@Override
	public boolean redirectOnSuccess() {

		return this.redirectOnSuccess;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodHandler#needsEntityManager()
	 */
	@Override
	public boolean needsEntityManager() {

		return this.needsEntityManager;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodHandler#call(com.boylesoftware.web.spi.RouterRequest, javax.persistence.EntityManager)
	 */
	@Override
	public String call(final RouterRequest request, final EntityManager em)
		throws RequestedResourceException, ServletException {

		final Args pooledArgs = this.argsPool.getSync();
		try {

			final Object[] args = pooledArgs.getValues();
			int i = 0;
			for (final ControllerMethodArgHandler h : this.argHandlers)
				args[i++] = h.getArgValue(request, em);

			return (String) this.method.invoke(this.controller, args);

		} catch (final InvocationTargetException e) {
			try {
				throw e.getCause();
			} catch (final RequestedResourceException | ServletException |
					RuntimeException | Error cause) {
				throw cause;
			} catch (final Throwable cause) {
				throw new ServletException("Error calling controller method.",
						e);
			}
		} catch (final IllegalAccessException e) {
			throw new ServletException("Error calling controller method.", e);
		} finally {
			pooledArgs.recycle();
		}
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodHandler#getArgHandlers()
	 */
	@Override
	public List<ControllerMethodArgHandler> getArgHandlers() {

		return this.argHandlersList;
	}
}
