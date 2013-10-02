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

import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import com.boylesoftware.web.RequestedResourceException;
import com.boylesoftware.web.spi.ControllerHandler;
import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.RouterRequest;


/**
 * {@link ControllerHandler} implementation.
 *
 * @author Lev Himmelfarb
 */
class ControllerHandlerImpl
	implements ControllerHandler {

	/**
	 * The controller.
	 */
	private final Object controller;

	/**
	 * Handler of the controller's method for processing HTTP GET requests, or
	 * {@code null}.
	 */
	private final ControllerMethodHandlerImpl getMethodHandler;

	/**
	 * Handler of the controller's method for processing HTTP POST requests, or
	 * {@code null}.
	 */
	private final ControllerMethodHandlerImpl postMethodHandler;

	/**
	 * Handler of the controller's method for processing HTTP DELETE requests,
	 * or {@code null}.
	 */
	private final ControllerMethodHandlerImpl deleteMethodHandler;

	/**
	 * Handler of the controller's method used to prepare the view, or
	 * {@code null}.
	 */
	private final ControllerMethodHandlerImpl prepareViewMethodHandler;


	/**
	 * Create new handler for the specified controller.
	 *
	 * @param sc Servlet context.
	 * @param controller The controller.
	 * @param argHandlerProvider Controller method argument handler provider to
	 * use.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	ControllerHandlerImpl(final ServletContext sc, final Object controller,
			final ControllerMethodArgHandlerProvider argHandlerProvider)
		throws UnavailableException {

		this.controller = controller;

		Method getMethod = null;
		Method postMethod = null;
		Method deleteMethod = null;
		Method prepareViewMethod = null;
		for (final Method m : this.controller.getClass().getDeclaredMethods()) {
			switch (m.getName()) {
			case "get":
				getMethod = checkControllerMethod(getMethod, m, Void.TYPE);
				break;
			case "post":
				postMethod = checkControllerMethod(postMethod, m, String.class);
				break;
			case "delete":
				deleteMethod =
					checkControllerMethod(deleteMethod, m, String.class);
				break;
			case "prepareView":
				prepareViewMethod =
					checkControllerMethod(prepareViewMethod, m, Void.TYPE);
			}
		}
		this.getMethodHandler = (getMethod != null ?
				new ControllerMethodHandlerImpl(sc, this.controller, getMethod,
						argHandlerProvider, false, false) : null);
		this.postMethodHandler = (postMethod != null ?
				new ControllerMethodHandlerImpl(sc, this.controller, postMethod,
						argHandlerProvider, true, true) : null);
		this.deleteMethodHandler = (deleteMethod != null ?
				new ControllerMethodHandlerImpl(sc, this.controller,
						deleteMethod, argHandlerProvider, false, true) : null);
		this.prepareViewMethodHandler = (prepareViewMethod != null ?
				new ControllerMethodHandlerImpl(sc, this.controller,
						prepareViewMethod, argHandlerProvider, false, false) :
							null);
	}

	/**
	 * Check the candidate controller method.
	 *
	 * @param existingMethod Controller method with the same name found before,
	 * or {@code null}. Must be {@code null} for the call to succeed.
	 * @param method The candidate method.
	 * @param requiredReturnType Required method return type.
	 *
	 * @return The method specified in the {@code method} parameter.
	 *
	 * @throws UnavailableException If the method is invalid or the
	 * {@code existingMethod} parameter is not {@code null}.
	 */
	private static Method checkControllerMethod(final Method existingMethod,
			final Method method, final Class<?> requiredReturnType)
		throws UnavailableException {

		if (existingMethod != null)
			throw new UnavailableException("Controller has more than one " +
					existingMethod.getName() + " method.");

		if (!method.getReturnType().equals(requiredReturnType))
			throw new UnavailableException("Controller's " + method.getName() +
					" method does not have required return type " +
					requiredReturnType.getName() + ".");

		return method;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerHandler#getMethodHandler(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public ControllerMethodHandlerImpl getMethodHandler(
			final HttpServletRequest request) {

		switch (request.getMethod()) {
		case "GET":
			return this.getMethodHandler;
		case "POST":
			return this.postMethodHandler;
		case "DELETE":
			return this.deleteMethodHandler;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerHandler#hasPrepareView(com.boylesoftware.web.spi.RouterRequest)
	 */
	@Override
	public boolean hasPrepareView(final RouterRequest request) {

		return (this.prepareViewMethodHandler != null);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerHandler#prepareViewNeedsEntityManager()
	 */
	@Override
	public boolean prepareViewNeedsEntityManager() {

		return this.prepareViewMethodHandler.needsEntityManager();
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerHandler#prepareView(com.boylesoftware.web.spi.RouterRequest, javax.persistence.EntityManager)
	 */
	@Override
	public void prepareView(final RouterRequest request, final EntityManager em)
		throws ServletException {

		if (this.prepareViewMethodHandler == null)
			return;

		try {
			this.prepareViewMethodHandler.call(request, em);
		} catch (final RequestedResourceException e) {
			throw new ServletException(
					"Error in the controller's view preparation method.", e);
		}
	}
}
