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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.AbstractWebApplication;
import com.boylesoftware.web.ApplicationServices;
import com.boylesoftware.web.MethodNotAllowedException;
import com.boylesoftware.web.api.Attributes;
import com.boylesoftware.web.api.Routes;
import com.boylesoftware.web.spi.Route.SecurityMode;
import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.RouterConfiguration;
import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.spi.ViewSender;
import com.boylesoftware.web.util.StringUtils;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;
import com.boylesoftware.web.util.pool.PooledStringBuffer;
import com.boylesoftware.web.util.pool.StringBufferPool;


/**
 * Abstract {@link RouterConfiguration} implementation.
 *
 * @author Lev Himmelfarb
 */
public abstract class AbstractRouterConfiguration
	implements RouterConfiguration, Routes {

	/**
	 * Pattern that matches any request URI.
	 */
	private static final Pattern ANY_URI = Pattern.compile(".*");

	/**
	 * Pattern that does not match any request URI.
	 */
	private static final Pattern NO_URI = Pattern.compile("");


	/**
	 * The log.
	 */
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * The application.
	 */
	private final AbstractWebApplication webapp;

	/**
	 * The mappings.
	 */
	private final RouteImpl[] mappings;

	/**
	 * Lock used to synchronized concurrent access to the {@code mappings}
	 * field.
	 */
	private final ReadWriteLock mappingsLock = new ReentrantReadWriteLock();

	/**
	 * Mappings by mapping id.
	 */
	private final Map<String, RouteImpl> mappingsById;

	/**
	 * Login page URI.
	 */
	private final String loginPageURI;

	/**
	 * Protected URIs pattern.
	 */
	private final Pattern protectedURIPattern;

	/**
	 * Public URIs pattern.
	 */
	private final Pattern publicURIPattern;

	/**
	 * Router request objects pool.
	 */
	private final FastPool<RouterRequestImpl> routerRequestPool;


	/**
	 * Create new router configuration object.
	 *
	 * @param sc Servlet context.
	 * @param appServices The application services.
	 * @param argHandlerProvider Controller method argument handler provider.
	 * @param viewSender View sender.
	 *
	 * @throws UnavailableException If configuration is incorrect.
	 */
	public AbstractRouterConfiguration(final ServletContext sc,
			final ApplicationServices appServices,
			final ControllerMethodArgHandlerProvider argHandlerProvider,
			final ViewSender viewSender)
		throws UnavailableException {

		this.webapp = appServices.getApplication();

		final RoutesBuilder routesBuilder =
			new RoutesBuilder(sc, argHandlerProvider, viewSender);
		this.buildRoutes(sc, routesBuilder);
		this.mappings = routesBuilder.getRoutes();

		this.loginPageURI = routesBuilder.getLoginPageURI();

		final Pattern protectedURIPattern =
			routesBuilder.getProtectedURIPattern();
		final Pattern publicURIPattern =
			routesBuilder.getPublicURIPattern();
		this.protectedURIPattern = (protectedURIPattern != null ?
				protectedURIPattern :
					(publicURIPattern != null ? ANY_URI : NO_URI));
		this.publicURIPattern = (publicURIPattern != null ?
				publicURIPattern :
					(protectedURIPattern != null ? NO_URI : ANY_URI));
		final String fullLoginPageURI = (this.loginPageURI != null ?
				StringUtils.emptyIfNull(sc.getContextPath()) +
				this.loginPageURI : null);
		if (fullLoginPageURI != null) {
			if (this.protectedURIPattern.matcher(fullLoginPageURI).matches() &&
					!this.publicURIPattern.matcher(fullLoginPageURI).matches())
				throw new UnavailableException("Provided login page URI" +
					" requires an authenticated user. Check the protected and" +
					" public URI patterns.");
		}

		final int numMappings = this.mappings.length;
		this.mappingsById = new HashMap<>(numMappings);
		for (int i = 0; i < numMappings; i++) {
			RouteImpl mapping = this.mappings[i];
			if ((fullLoginPageURI != null) &&
					mapping.getURIPattern().matcher(fullLoginPageURI)
						.matches()) {
				switch (mapping.getSecurityMode()) {
				case DEFAULT:
					this.mappings[i] = mapping =
						new RouteImpl(mapping, SecurityMode.FORCE_SSL);
					break;
				case FORCE_SSL:
					break;
				case FORCE_REQUIRE_AUTH:
					throw new UnavailableException("Provided login page URI" +
						" requires an authenticated user. Check the mapping's" +
						" security mode.");
				}
			}
			if (this.mappingsById.put(mapping.getId(), mapping) != null)
				throw new UnavailableException(
						"More than one mapping share route id " +
						mapping.getId() + ".");
		}

		this.routerRequestPool = new FastPool<>(
				new PoolableObjectFactory<RouterRequestImpl>() {

					@Override
					public RouterRequestImpl makeNew(
							final FastPool<RouterRequestImpl> pool,
							final int pooledObjectId) {

						return new RouterRequestImpl(pool, pooledObjectId,
								appServices);
					}
				}, "RouterRequestsPool");
	}


	/**
	 * Build the route mappings. The implementation must call one of the
	 * {@code addRoute} protected methods to add the route mappings.
	 *
	 * <p>Note, that the mappings must be configured in a way that no request
	 * URI can ever match more than one mapping. If it does, the choice of the
	 * mapping by the framework is unpredictable.
	 *
	 * @param sc Servlet context.
	 * @param routes Builder, to which to add the routes.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	protected abstract void buildRoutes(ServletContext sc, RoutesBuilder routes)
		throws UnavailableException;


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterConfiguration#findRoute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public RouterRequest findRoute(final HttpServletRequest request,
			final HttpServletResponse response)
		throws MethodNotAllowedException, ServletException {

		// check if we have mappings
		if (this.mappings.length == 0)
			return null;

		// try to find the matching route mapping
		final Lock readLock = this.mappingsLock.readLock();
		readLock.lock();
		try {

			// test request URI against the mappings
			RouteImpl mapping = this.mappings[0];
			final String requestURI = request.getRequestURI();
			// TODO: reusable matcher?
			final Matcher m = mapping.getURIPattern().matcher(requestURI);
			int mappingInd = 0;
			do {

				// try to match the mapping
				if (m.matches()) {

					// log the match
					if (this.log.isDebugEnabled())
						this.log.debug("found mapping for URI " + requestURI +
								" on attempt " + (mappingInd + 1));

					// move the mapping higher if matched more frequently
					final long numMatched = mapping.incrementNumMatched();
					if (mappingInd > 0) {
						final RouteImpl prevMapping =
							this.mappings[mappingInd - 1];
						if (numMatched > prevMapping.getNumMatched()) {
							final Lock writeLock =
								this.mappingsLock.writeLock();
							readLock.unlock();
							writeLock.lock();
							try {
								this.mappings[mappingInd] = prevMapping;
								this.mappings[mappingInd - 1] = mapping;
							} finally {
								readLock.lock();
								writeLock.unlock();
							}
						}
					}

					// wrap the request
					final RouterRequestImpl routerRequest =
						this.routerRequestPool.getSync();
					boolean success = false;
					try {

						// initialize the router request
						routerRequest.wrap(request, response, mapping,
								this.isAuthenticationRequired(requestURI));

						// add parameters made from the URI components
						final int numURIParams = m.groupCount();
						for (int i = 0; i < numURIParams; i++) {
							final String uriParamName =
								mapping.getURIParamName(i);
							if (uriParamName != null)
								routerRequest.addParameter(uriParamName,
										m.group(i + 1));
						}

						// convert flash attributes cookie to request attributes
						routerRequest.flashCookieToAttributes();

						// return the router request
						success = true;
						return routerRequest;

					} finally {
						if (!success)
							routerRequest.recycle();
					}
				}

				// next mapping for next iteration
				if (++mappingInd >= this.mappings.length)
					break;
				mapping = this.mappings[mappingInd];

				// reuse the matcher
				m.reset();
				m.usePattern(mapping.getURIPattern());

			} while (true);

		} finally {
			readLock.unlock();
		}

		// no mapping matched
		return null;
	}

	/**
	 * Tell if specified request URI requires authenticated user.
	 *
	 * @param requestURI Server root relative request URI.
	 *
	 * @return {@code true} if requires authenticated user.
	 */
	private boolean isAuthenticationRequired(final String requestURI) {

		return (this.protectedURIPattern.matcher(requestURI).matches() &&
				!this.publicURIPattern.matcher(requestURI).matches());
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterConfiguration#getLoginPageURI()
	 */
	@Override
	public String getLoginPageURI() {

		return this.loginPageURI;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterConfiguration#getRoutes()
	 */
	@Override
	public Routes getRoutes() {

		return this;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.Routes#getRouteURI(javax.servlet.http.HttpServletRequest, java.lang.String, com.boylesoftware.web.api.Routes.URIType, java.lang.String[])
	 */
	@Override
	public String getRouteURI(final HttpServletRequest request,
			final String routeId, final URIType type,
			final String... uriParams) {

		// find mapping
		final RouteImpl mapping = this.mappingsById.get(routeId);
		if (mapping == null)
			throw new IllegalArgumentException("No mapping for route id " +
					routeId + ".");

		// check number of provided URI parameters
		final int numURIParams = mapping.getNumURIParams();
		if (numURIParams != uriParams.length)
			throw new IllegalArgumentException("Number of URI parameters does" +
					" not match the route mapping's URI pattern.");

		// build the URI
		try (final PooledStringBuffer buf = StringBufferPool.get()) {

			// build server root relative URI
			final String uriTmpl = mapping.getURITemplate();
			final String relativeURI = (numURIParams > 0 ?
					this.replaceURIPlaceholders(uriTmpl, uriParams, buf) :
						uriTmpl);

			// determine if SSL is needed
			final boolean needsSSL = (
				(type == URIType.FORCE_SSL) ||
				(type == URIType.FORCE_ABSOLUTE_SSL) || (
					(type == URIType.DEFAULT) && (
						(mapping.getSecurityMode() != SecurityMode.DEFAULT) ||
							this.isAuthenticationRequired(relativeURI)
					)
				)
			);

			// is server root relative URI OK?
			final boolean secureRequest = request.isSecure();
			final boolean authedRequest =
				(request.getAttribute(Attributes.AUTHED_USER) != null);
			if ((type != URIType.FORCE_ABSOLUTE_PLAIN) &&
				(type != URIType.FORCE_ABSOLUTE_SSL) &&
				((!needsSSL && !(secureRequest &&
						((type == URIType.FORCE_PLAIN) || !authedRequest))) ||
					(needsSSL && secureRequest)))
				return relativeURI;

			// build full URL
			final StringBuilder urlSB = buf.getStringBuilder();
			urlSB.setLength(0);
			if (needsSSL)
				urlSB.append("https://");
			else
				urlSB.append("http://");
			urlSB.append(request.getServerName());
			final int httpsPort = this.webapp.getHTTPSPort();
			final int httpPort = this.webapp.getHTTPPort();
			if (needsSSL && (httpsPort != 443))
				urlSB.append(':').append(httpsPort);
			else if (!needsSSL && (httpPort != 80))
				urlSB.append(':').append(httpPort);
			urlSB.append(relativeURI);

			// return it
			return urlSB.toString();
		}
	}

	/**
	 * Replace parameter placeholders in a URI template with values.
	 *
	 * @param uriTmpl URI template.
	 * @param uriParams URI parameter values.
	 * @param buf Buffer to use.
	 *
	 * @return The URI with placeholders replaced with values.
	 */
	private String replaceURIPlaceholders(final String uriTmpl,
			final String[] uriParams, final PooledStringBuffer buf) {

		final char[] uriTmplChars = uriTmpl.toCharArray();
		final StringBuilder sb = buf.getStringBuilder();
		int phInd, lastPHInd = 0, paramInd = 0;
		while ((phInd = uriTmpl.indexOf('%', lastPHInd)) >= 0) {
			sb.append(uriTmplChars, lastPHInd, phInd - lastPHInd);
			sb.append(uriParams[paramInd++]);
			lastPHInd = phInd + 1;
		}
		sb.append(uriTmplChars, lastPHInd, uriTmplChars.length - lastPHInd);

		return sb.toString();
	}
}
