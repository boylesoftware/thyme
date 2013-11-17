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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.validation.MessageInterpolator;

import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.ApplicationServices;
import com.boylesoftware.web.MethodNotAllowedException;
import com.boylesoftware.web.api.Attributes;
import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.api.FlashAttributes;
import com.boylesoftware.web.api.UserInputErrors;
import com.boylesoftware.web.spi.ControllerMethodArgHandler;
import com.boylesoftware.web.spi.ControllerMethodHandler;
import com.boylesoftware.web.spi.Route;
import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.spi.UserLocaleFinder;
import com.boylesoftware.web.util.pool.AbstractPoolable;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.Poolable;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;


/**
 * {@link RouterRequest} implementation.
 *
 * @author Lev Himmelfarb
 */
class RouterRequestImpl
	extends HttpServletRequestWrapper
	implements RouterRequest, Poolable {

	/**
	 * Poolable single element string array wrapper.
	 */
	private static final class PoolableSingleElementStringArray
		extends AbstractPoolable {

		/**
		 * The wrapped array.
		 */
		private final String[] array;


		/**
		 * Create new object.
		 *
		 * @param pool Reference to the owning pool.
		 * @param pooledObjectId Pooled object id.
		 */
		PoolableSingleElementStringArray(
				final FastPool<PoolableSingleElementStringArray> pool,
				final int pooledObjectId) {
			super(pool, pooledObjectId);

			this.array = new String[1];
		}


		/**
		 * Set specified value as the wrapped array element and return the
		 * array.
		 *
		 * @param value Value for the single array element.
		 *
		 * @return The wrapped array.
		 */
		String[] getArray(final String value) {

			this.array[0] = value;

			return this.array;
		}
	}


	/**
	 * Single element string arrays pool.
	 */
	private static final FastPool<PoolableSingleElementStringArray> SESA_POOL =
		new FastPool<>(
				new PoolableObjectFactory<PoolableSingleElementStringArray>() {

					@Override
					public PoolableSingleElementStringArray makeNew(
							final FastPool<PoolableSingleElementStringArray>
							pool,
							final int pooledObjectId) {

						return new PoolableSingleElementStringArray(pool,
								pooledObjectId);
					}
				},
				"SingleElementStringArraysPool");

	/**
	 * Request plug.
	 */
	private static final HttpServletRequest REQUEST_PLUG =
		new DummyHttpServletRequest();


	/**
	 * The pool.
	 */
	private final FastPool<RouterRequestImpl> pool;

	/**
	 * Pooled object id.
	 */
	private final int pooledObjectId;

	/**
	 * User locale finder.
	 */
	private final UserLocaleFinder<Object> userLocaleFinder;

	/**
	 * Message interpolator.
	 */
	private final MessageInterpolator messageInterpolator;

	/**
	 * User input errors.
	 */
	private final UserInputErrorsImpl userInputErrors;

	/**
	 * Flash attributes.
	 */
	private final FlashAttributesImpl flashAttributes;

	/**
	 * The authenticator.
	 */
	private final Authenticator<?> authenticator;

	/**
	 * The response.
	 */
	private HttpServletResponse response;

	/**
	 * Matched route mapping.
	 */
	private RouteImpl route;

	/**
	 * Tells if authenticated user is required.
	 */
	private boolean authRequired;

	/**
	 * Controller method handler.
	 */
	private ControllerMethodHandlerImpl controllerMethodHandler;

	/**
	 * User locale.
	 */
	private Locale userLocale;

	/**
	 * Attached recyclables.
	 */
	private final Collection<Poolable> attachedRecyclables;

	/**
	 * Additional request parameters.
	 */
	private final Map<String, String[]> addlParams;

	/**
	 * Tells if the request has additional parameters.
	 */
	private boolean hasAddlParams;

	/**
	 * Cached parameters map that includes additional parameters.
	 */
	private final Map<String, String[]> parameterMap;

	/**
	 * Immutable view of the parameter map.
	 */
	private final Map<String, String[]> parameterMapRO;

	/**
	 * Tells if cached parameter map is filled with values.
	 */
	private boolean parameterMapFilled;

	/**
	 * Cached parameter names collection that includes additional parameters.
	 */
	private final Collection<String> parameterNames;

	/**
	 * Tells if the cached parameter names collection is filled with values.
	 */
	private boolean parameterNamesFilled;


	/**
	 * Create new wrapper instance.
	 *
	 * @param pool The pool.
	 * @param pooledObjectId Pooled object id.
	 * @param appServices Application services.
	 */
	@SuppressWarnings("unchecked")
	RouterRequestImpl(final FastPool<RouterRequestImpl> pool,
			final int pooledObjectId,
			final ApplicationServices appServices) {
		super(REQUEST_PLUG);

		this.pool = pool;
		this.pooledObjectId = pooledObjectId;

		this.userLocaleFinder =
			(UserLocaleFinder<Object>) appServices.getUserLocaleFinder();
		final MessageInterpolator defaultMessageInterpolator =
			appServices.getValidatorFactory().getMessageInterpolator();
		this.messageInterpolator = new MessageInterpolator() {

			@Override
			public String interpolate(final String messageTemplate,
					final Context context, final Locale locale) {

				return defaultMessageInterpolator.interpolate(messageTemplate,
						context, locale);
			}

			@Override
			public String interpolate(final String messageTemplate,
					final Context context) {

				return defaultMessageInterpolator.interpolate(messageTemplate,
						context, RouterRequestImpl.this.getUserLocale());
			}
		};
		this.userInputErrors = new UserInputErrorsImpl(this);

		this.flashAttributes = new FlashAttributesImpl();

		this.authenticator =
			appServices.getAuthenticationService().getAuthenticator(this);

		this.attachedRecyclables = new ArrayList<>();

		this.addlParams = new HashMap<>();
		this.hasAddlParams = false;
		this.parameterMap = new HashMap<>();
		this.parameterMapRO = Collections.unmodifiableMap(this.parameterMap);
		this.parameterMapFilled = false;
		this.parameterNames = new HashSet<>();
		this.parameterNamesFilled = false;
	}


	/**
	 * Wrap the specified request and mapping.
	 *
	 * @param request The request.
	 * @param response The response.
	 * @param route The route descriptor.
	 * @param authRequried {@code true} if requires an authenticated user.
	 *
	 * @throws MethodNotAllowedException If controller associated with the route
	 * cannot handle the request's HTTP method.
	 */
	void wrap(final HttpServletRequest request,
			final HttpServletResponse response, final RouteImpl route,
			final boolean authRequried)
		throws MethodNotAllowedException {

		this.setRequest(request);
		this.response = response;
		this.route = route;
		this.authRequired = authRequried;

		final ControllerHandlerImpl controllerHandler =
			this.route.getControllerHandler();
		this.controllerMethodHandler = (controllerHandler != null ?
				controllerHandler.getMethodHandler(request) : null);
		if ((this.controllerMethodHandler == null) &&
				!request.getMethod().equals("GET"))
			throw new MethodNotAllowedException();
	}

	/**
	 * Add parameter to the request. Additional parameters override original
	 * request's parameters that have the same name.
	 *
	 * @param name Parameter name.
	 * @param value Parameter value.
	 */
	void addParameter(final String name, final String value) {

		final PoolableSingleElementStringArray pooledArray =
			SESA_POOL.getSync();
		this.attachedRecyclables.add(pooledArray);
		this.addlParams.put(name, pooledArray.getArray(value));

		this.hasAddlParams = true;
		this.parameterMapFilled = this.parameterNamesFilled = false;
	}

	/**
	 * Convert flash cookie to request attributes.
	 *
	 * @throws ServletException If the cookie value has invalid syntax.
	 */
	void flashCookieToAttributes()
		throws ServletException {

		this.flashAttributes.flashCookieToAttributes(this);
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.util.Poolable#getPooledObjectId()
	 */
	@Override
	public int getPooledObjectId() {

		return this.pooledObjectId;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.RouterRequestContext#getResponse()
	 */
	@Override
	public HttpServletResponse getResponse() {

		return this.response;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.RouterRequestContext#getAuthenticatedUser()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAuthenticatedUser() {

		return (T) this.getAttribute(Attributes.AUTHED_USER);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.RouterRequestContext#getUserLocale()
	 */
	@Override
	public Locale getUserLocale() {

		if (this.userLocale == null)
			this.userLocale = this.userLocaleFinder.getLocale(this,
					this.getAuthenticatedUser());

		return this.userLocale;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#getRoute()
	 */
	@Override
	public Route getRoute() {

		return this.route;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#isAuthenticationRequired()
	 */
	@Override
	public boolean isAuthenticationRequired() {

		return this.authRequired;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#getAuthenticator()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> Authenticator<T> getAuthenticator() {

		return (Authenticator<T>) this.authenticator;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#getControllerMethodHandler()
	 */
	@Override
	public ControllerMethodHandler getControllerMethodHandler() {

		return this.controllerMethodHandler;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#getMessageInterpolator()
	 */
	@Override
	public MessageInterpolator getMessageInterpolator() {

		return this.messageInterpolator;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#getUserInputErrors()
	 */
	@Override
	public UserInputErrors getUserInputErrors() {

		return this.userInputErrors;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#getFlashAttributes()
	 */
	@Override
	public FlashAttributes getFlashAttributes() {

		return this.flashAttributes;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#commitFlashAttributes()
	 */
	@Override
	public void commitFlashAttributes() {

		this.flashAttributes.flashAttributesToCookie(this, this.response);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#rewrap(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void rewrap(final HttpServletRequest request,
			final HttpServletResponse response) {

		this.setRequest(request);
		this.response = response;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterRequest#recycle()
	 */
	@Override
	public void recycle() {

		if (this.controllerMethodHandler != null) {
			for (final ControllerMethodArgHandler h :
					this.controllerMethodHandler.getArgHandlers()) {
				try {
					h.onComplete(this);
				} catch (final Exception e) {
					LogFactory.getLog(this.getClass()).warn(
							"error in controller method argument handler" +
							" finalizing request, will ignore and continue", e);
				}
			}
		}

		for (final Poolable obj : this.attachedRecyclables)
			obj.recycle();
		this.attachedRecyclables.clear();

		this.addlParams.clear();
		this.hasAddlParams = false;
		this.parameterMap.clear();
		this.parameterMapFilled = false;
		this.parameterNames.clear();
		this.parameterNamesFilled = false;

		this.userInputErrors.clear();

		this.flashAttributes.clear();

		this.setRequest(REQUEST_PLUG);
		this.response = null;
		this.route = null;
		this.controllerMethodHandler = null;

		this.userLocale = null;

		this.pool.recycleSync(this);
	}


	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(final String name) {

		if (this.hasAddlParams) {
			final String[] val = this.addlParams.get(name);
			if (val != null)
				return val[0];
		}

		return super.getParameter(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterNames()
	 */
	@Override
	public Enumeration<String> getParameterNames() {

		if (this.parameterNamesFilled)
			return Collections.enumeration(this.parameterNames);

		if (!this.hasAddlParams)
			return super.getParameterNames();

		this.parameterNames.addAll(super.getParameterMap().keySet());
		this.parameterNames.addAll(this.addlParams.keySet());
		this.parameterNamesFilled = true;

		return Collections.enumeration(this.parameterNames);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
	 */
	@Override
	public String[] getParameterValues(final String name) {

		if (this.hasAddlParams) {
			final String[] val = this.addlParams.get(name);
			if (val != null)
				return val;
		}

		return super.getParameterValues(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterMap()
	 */
	@Override
	public Map<String, String[]> getParameterMap() {

		if (this.parameterMapFilled)
			return this.parameterMapRO;

		if (!this.hasAddlParams)
			return super.getParameterMap();

		this.parameterMap.putAll(super.getParameterMap());
		this.parameterMap.putAll(this.addlParams);
		this.parameterMapFilled = true;

		return this.parameterMapRO;
	}
}
