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
package com.boylesoftware.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.api.Attributes;
import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.spi.Route.SecurityMode;
import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.util.LooseCannon;
import com.boylesoftware.web.util.pool.PooledStringBuffer;
import com.boylesoftware.web.util.pool.StringBufferPool;


/**
 * Routing logic implementation.
 *
 * @author Lev Himmelfarb
 */
class Router {

	/**
	 * Name of request attribute used to pass exception from the asynchronous
	 * transaction to the router.
	 */
	private static final String ASYNC_EXCEPTION_ATTNAME =
		(Router.class).getName() + ".ASYNC_EXCEPTION";

	/**
	 * Name of request attribute used to pass authenticated user to the router.
	 */
	private static final String AUTHED_USER_ATTNAME =
		(Router.class).getName() + ".AUTHED_USER";

	/**
	 * Plug for anonymous user.
	 */
	private static final Object ANONYMOUS_USER = new Object();


	/**
	 * The log.
	 */
	private final Log log = LogFactory.getLog(this.getClass());


	/**
	 * Set asynchronous transaction exception in the request.
	 *
	 * @param request The request.
	 * @param e The exception.
	 */
	static void setAsyncException(final ServletRequest request,
			final Exception e) {

		request.setAttribute(ASYNC_EXCEPTION_ATTNAME, e);
	}

	/**
	 * Set authenticated user in the request.
	 *
	 * @param request The request.
	 * @param authedUser The user, or {@code null} if the request is
	 * unauthenticated.
	 */
	static void setAuthenticatedUser(final HttpServletRequest request,
			final Object authedUser) {

		request.setAttribute(AUTHED_USER_ATTNAME,
				(authedUser != null ? authedUser : ANONYMOUS_USER));
	}


	/**
	 * Route request.
	 *
	 * @param webapp The application.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 *
	 * @return {@code true} if the router took care of the request processing
	 * and no further action is required, {@code false} if the router did not
	 * process the request because it does not have a route for it.
	 *
	 * @throws ServletException If an error happens.
	 * @throws IOException If an I/O error happens reading the request or
	 * sending the response.
	 */
	boolean route(final AbstractWebApplication webapp,
			final HttpServletRequest request,
			final HttpServletResponse response)
		throws ServletException, IOException {

		if (this.log.isDebugEnabled())
			this.log.debug("received " + request.getDispatcherType() + " " +
					request.getMethod() + " " + request.getRequestURI() +
					" (" + request.getClass().getName() + ")");

		// execute corresponding logic
		return (request.getDispatcherType() == DispatcherType.ASYNC ?
				this.doAsync(webapp, request, response) :
					this.doRequest(webapp, request, response));
	}

	/**
	 * Process direct request.
	 *
	 * @param webapp The application.
	 * @param request The request.
	 * @param response The response.
	 *
	 * @return {@code true} if request processed.
	 *
	 * @throws ServletException If an error happens.
	 * @throws IOException If an I/O error happens reading the request or
	 * sending the response.
	 */
	private boolean doRequest(final AbstractWebApplication webapp,
			final HttpServletRequest request,
			final HttpServletResponse response)
		throws ServletException, IOException {

		final boolean debug = this.log.isDebugEnabled();

		// find matching route
		RouterRequest q;
		try {
			q = webapp.getRouterConfiguration().findRoute(request, response);
			if (q == null) {
				if (debug)
					this.log.debug("no route for request URI " +
							request.getRequestURI());
				return false;
			}
			if (debug)
				this.log.debug("found route for request URI " +
						request.getRequestURI());
		} catch (final MethodNotAllowedException e) {
			if (debug)
				this.log.debug("request method " + request.getMethod() +
						" is not allowed for request URI " +
						request.getRequestURI(), e);
			response.sendError(e.getHTTPErrorCode());
			return true;
		}
		final RouterRequest routerReq = q;
		RouterRequestLifecycle.associate(routerReq);

		// process request
		boolean recycleReq = true;
		try {

			// check if SSL is required
			final SecurityMode securityMode =
				routerReq.getRoute().getSecurityMode();
			final boolean requireAuthedUser =
				((securityMode == SecurityMode.FORCE_REQUIRE_AUTH) ||
						routerReq.isAuthenticationRequired());
			if ((requireAuthedUser ||
						(securityMode == SecurityMode.FORCE_SSL)) &&
					!routerReq.isSecure()) {
				if (debug)
					this.log.debug("non-SSL request using mapping with" +
							" security mode " + securityMode +
							" and require auth: " + requireAuthedUser);
				this.sendRedirectToSecureURI(webapp, routerReq, response);
				return true;
			}

			// fix request character encoding
			if (routerReq.getCharacterEncoding() == null)
				routerReq.setCharacterEncoding("UTF-8");

			// get authenticated user
			final AuthenticatorExecutor exec =
				AuthenticatorExecutor.getExecutor();
			boolean recycleExec = true;
			try {
				exec.init(webapp, routerReq);
				recycleReq = recycleExec = false;
				final ExecutorService execPool = webapp.getExecutorService();
				if (debug)
					this.log.debug("executor service status: " + execPool);
				execPool.execute(exec);
			} finally {
				if (recycleExec)
					exec.recycle();
			}

		} finally {
			if (recycleReq) {
				if (debug)
					this.log.debug("recycling router request " + routerReq);
				RouterRequestLifecycle.recycle(routerReq);
			}
		}

		// done
		return true;
	}

	/**
	 * Process asynchronous dispatch.
	 *
	 * @param webapp The application.
	 * @param request The request.
	 * @param response The response.
	 *
	 * @return {@code true} if request processed.
	 *
	 * @throws ServletException If an error happens.
	 * @throws IOException If an I/O error happens reading the request or
	 * sending the response.
	 */
	private boolean doAsync(final AbstractWebApplication webapp,
			final HttpServletRequest request,
			final HttpServletResponse response)
		throws ServletException, IOException {

		final boolean debug = this.log.isDebugEnabled();

		// check if asynchronous executor error
		final Exception asyncError =
			(Exception) request.getAttribute(ASYNC_EXCEPTION_ATTNAME);
		if (asyncError != null) {
			if (debug)
				this.log.debug("received error from async transaction",
						asyncError);
			if (asyncError instanceof RequestedResourceException) {
				if (asyncError instanceof ServiceUnavailableException)
					this.log.error("service unavailable error", asyncError);
				response.sendError(
						((RequestedResourceException) asyncError)
							.getHTTPErrorCode());
				return true;
			}
			if (asyncError instanceof ServletException)
				throw (ServletException) asyncError;
			if (asyncError instanceof IOException)
				throw (IOException) asyncError;
			if (asyncError instanceof RuntimeException)
				throw (RuntimeException) asyncError;
			throw new ServletException(asyncError);
		}

		// restore the route
		final RouterRequest routerReq = RouterRequestLifecycle.restore(request);
		if (!routerReq.getRequestURI().equals(request.getRequestURI())) {
			if (debug)
				this.log.debug("request is not a router request");
			return false;
		}

		// re-wrap the router request
		routerReq.rewrap(request, response);

		// check if route requires authenticated user
		final Object authedUser =
			routerReq.getAttribute(AUTHED_USER_ATTNAME);
		if ((authedUser != null) && (authedUser != ANONYMOUS_USER)) {
			routerReq.setAttribute(Attributes.AUTHED_USER, authedUser);
		} else if ((routerReq.getRoute().getSecurityMode() ==
							SecurityMode.FORCE_REQUIRE_AUTH) ||
						routerReq.isAuthenticationRequired()) {
			this.sendRedirectToLoginPage(webapp, routerReq, response);
			return true;
		}

		// execute request processing logic
		final RequestTransactionExecutor exec =
			RequestTransactionExecutor.getExecutor();
		boolean recycleExec = true;
		try {
			if (exec.prepare(webapp, routerReq)) {
				recycleExec = false;
				final ExecutorService execPool = webapp.getExecutorService();
				if (debug)
					this.log.debug("executor service status: " + execPool);
				execPool.execute(exec);
			}
		} finally {
			if (recycleExec)
				exec.recycle();
		}

		// done
		return true;
	}

	/**
	 * Send redirect to the login page.
	 *
	 * @param webapp The web-application.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 *
	 * @throws ServletException If an error happens.
	 */
	private void sendRedirectToLoginPage(final AbstractWebApplication webapp,
			final HttpServletRequest request,
			final HttpServletResponse response)
		throws ServletException {

		LooseCannon.heel();

		try (final PooledStringBuffer buf = StringBufferPool.get()) {
			final StringBuilder sb = buf.getStringBuilder();

			sb.append(request.getRequestURI());
			final String queryString = request.getQueryString();
			if (queryString != null)
				sb.append('?').append(queryString);
			final String targetURI = sb.toString();
			sb.setLength(0);

			sb.append("https://").append(request.getServerName());

			final int httpsPort = webapp.getHTTPSPort();
			if (httpsPort != 443)
				sb.append(':').append(httpsPort);

			sb.append(request.getContextPath())
				.append(webapp.getRouterConfiguration().getLoginPageURI())
				.append('?').append(Authenticator.TARGET_URI)
				.append('=');

			try {
				sb.append(
						URLEncoder.encode(targetURI, "UTF-8"));
			} catch (final UnsupportedEncodingException e) {
				throw new ServletException("UTF-8 is unsupported.", e);
			}

			response.setStatus(HttpServletResponse.SC_SEE_OTHER);
			response.setHeader("Location", sb.toString());
		}
	}

	/**
	 * Send redirect to the same request URI (including the query string), but
	 * over HTTPS.
	 *
	 * @param webapp The web-application.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 */
	private void sendRedirectToSecureURI(final AbstractWebApplication webapp,
			final HttpServletRequest request,
			final HttpServletResponse response) {

		LooseCannon.heel();

		try (final PooledStringBuffer buf = StringBufferPool.get()) {
			final StringBuilder redirectURL = buf.getStringBuilder();

			redirectURL.append("https://").append(request.getServerName());

			final int httpsPort = webapp.getHTTPSPort();
			if (httpsPort != 443)
				redirectURL.append(':').append(httpsPort);

			redirectURL.append(request.getRequestURI());

			final String queryString = request.getQueryString();
			if (queryString != null)
				redirectURL.append('?').append(queryString);

			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			response.setHeader("Location", redirectURL.toString());
		}
	}
}
