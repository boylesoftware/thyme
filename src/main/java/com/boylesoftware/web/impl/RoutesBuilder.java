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
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.Script;
import com.boylesoftware.web.spi.ViewSender;
import com.boylesoftware.web.spi.Route.SecurityMode;
import com.boylesoftware.web.util.StringUtils;


/**
 * Routes builder used by the {@link AbstractRouterConfiguration#buildRoutes}
 * method implementation.
 *
 * @author Lev Himmelfarb
 */
public class RoutesBuilder {

	/**
	 * Servlet context.
	 */
	private final ServletContext sc;

	/**
	 * Controller method argument handler provider.
	 */
	private final ControllerMethodArgHandlerProvider argHandlerProvider;

	/**
	 * View sender.
	 */
	private final ViewSender viewSender;

	/**
	 * The routes.
	 */
	private final Collection<RouteImpl> routes;

	/**
	 * Login page URI.
	 */
	private String loginPageURI;

	/**
	 * Protected URIs pattern.
	 */
	private Pattern protectedURIPattern;

	/**
	 * Public URIs pattern.
	 */
	private Pattern publicURIPattern;


	/**
	 * Create new builder.
	 *
	 * @param sc Servlet context.
	 * @param argHandlerProvider Controller method argument handler provider.
	 * @param viewSender View sender.
	 */
	RoutesBuilder(final ServletContext sc,
			final ControllerMethodArgHandlerProvider argHandlerProvider,
			final ViewSender viewSender) {

		this.sc = sc;
		this.argHandlerProvider = argHandlerProvider;
		this.viewSender = viewSender;

		this.routes = new ArrayList<>(128);
	}


	/**
	 * Add route mapping.
	 *
	 * @param id Route id, or {@code null} to auto-generate id from the URI
	 * pattern. The generated id is the URI pattern with all URI parameter
	 * placeholders, if any, replaced with "%". Note, that no two mappings in
	 * a single routes configuration can have the same id.
	 * @param uriPattern Request URI pattern, which is a context relative URI
	 * that can optionally contain URI parameter placeholders. Each placeholder
	 * is an expression surrounded by curly braces. Inside curly braces there is
	 * the parameter name and optionally, separated with a colon, the value
	 * regular expression. The regular expression must not contain any capturing
	 * groups and if the expression contains curly braces, they must be
	 * balanced. If the parameter name is empty, the value is not converted to a
	 * request parameter. If the regular expression is unspecified, regular
	 * expression that matches anything except "/" is used. Each URI parameter
	 * is converted to a regular request parameter with the specified name.
	 * @param securityMode Security mode.
	 * @param routeScript Additional logic associated with the mapping, or
	 * {@code null} if none. If specified, the script is executed each time for
	 * the matched request before the controller is called and the view is sent
	 * back to the client.
	 * @param controller Controller, or {@code null} if no controller is
	 * associated with the mapped route.
	 * @param viewIdPattern Mapped resource view id. The id may contain
	 * placeholders for request attributes and parameters in curly braces with
	 * the attribute or parameter name inside. Attributes take precedence over
	 * parameters.
	 * @param viewScript Additional logic associated with the view, or
	 * {@code null} if none. If specified, the script is executed each time
	 * before the view is sent to the client. The script's purpose it to prepare
	 * data used by the view.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	public void addRoute(final String id, final String uriPattern,
			final SecurityMode securityMode, final Script routeScript,
			final Object controller, final String viewIdPattern,
			final Script viewScript)
		throws UnavailableException {

		final String contextPath =
			StringUtils.emptyIfNull(this.sc.getContextPath());

		this.routes.add(new RouteImpl(this.sc, id, contextPath + uriPattern,
				securityMode, routeScript, controller,
				this.argHandlerProvider, viewIdPattern, this.viewSender,
				viewScript));
	}

	/**
	 * Set URI of the dedicated user login page. The login page URI must be set
	 * using this method in the {@link AbstractRouterConfiguration#buildRoutes}
	 * method implementation.
	 *
	 * <p>Note, that URI patterns set using {@link #setProtectedURIPattern} and
	 * {@link #setPublicURIPattern} methods must be configured in such a way
	 * that makes the login page URI public.
	 *
	 * @param loginPageURI Context-relative login page URI.
	 */
	public void setLoginPageURI(final String loginPageURI) {

		this.loginPageURI = loginPageURI;
	}

	/**
	 * Set pattern for URIs of resources, access to which requires an
	 * authenticated user. If {@code null}, which is the default, and pattern
	 * set using {@link #setPublicURIPattern} method is not {@code null}, all
	 * URIs that do not match the public URI pattern are considered protected.
	 * If both protected and public URI patterns are {@code null}, all URIs are
	 * considered public.
	 *
	 * @param protectedURIPattern Pattern matching context-relative request URIs
	 * of protected pages.
	 */
	public void setProtectedURIPattern(final String protectedURIPattern) {

		this.protectedURIPattern = Pattern.compile(
				StringUtils.emptyIfNull(this.sc.getContextPath()) +
				"(?:" + protectedURIPattern + ")");
	}

	/**
	 * Set pattern for URIs of resources, access to which does not require an
	 * authenticated user. If {@code null}, which is the default, all URIs that
	 * do not match pattern specified using {@link #setProtectedURIPattern}
	 * method are considered public. If a URI matches both protected and public
	 * URI patterns, the resource is considered public.
	 *
	 * @param publicURIPattern Pattern matching context-relative request URIs
	 * of public pages.
	 */
	public void setPublicURIPattern(final String publicURIPattern) {

		this.publicURIPattern = Pattern.compile(
				StringUtils.emptyIfNull(this.sc.getContextPath()) +
				"(?:" + publicURIPattern + ")");
	}


	/**
	 * Get the routes.
	 *
	 * @return The routes.
	 */
	RouteImpl[] getRoutes() {

		return this.routes.toArray(new RouteImpl[this.routes.size()]);
	}

	/**
	 * Get login page URI.
	 *
	 * @return Context-relative login page URI.
	 */
	String getLoginPageURI() {

		return this.loginPageURI;
	}

	/**
	 * Get protected page URIs pattern.
	 *
	 * @return Protected page URIs pattern, or {@code null}.
	 */
	Pattern getProtectedURIPattern() {

		return this.protectedURIPattern;
	}

	/**
	 * Get public page URIs pattern.
	 *
	 * @return Public page URIs pattern, or {@code null}.
	 */
	Pattern getPublicURIPattern() {

		return this.publicURIPattern;
	}
}
