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

import com.boylesoftware.web.ApplicationConfiguration;
import com.boylesoftware.web.ApplicationServices;
import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.Route.SecurityMode;
import com.boylesoftware.web.spi.RouterConfiguration;
import com.boylesoftware.web.spi.RouterConfigurationProvider;
import com.boylesoftware.web.spi.Script;
import com.boylesoftware.web.spi.ViewSender;
import com.boylesoftware.web.spi.ViewSenderProvider;
import com.boylesoftware.web.util.StringUtils;


/**
 * Abstract convenience implementation of the
 * {@link RouterConfigurationProvider}.
 *
 * @author Lev Himmelfarb
 */
public abstract class AbstractRouterConfigurationProvider
	implements RouterConfigurationProvider {

	/**
	 * Controller method argument handler provider.
	 */
	private final ControllerMethodArgHandlerProvider argHandlerProvider;

	/**
	 * View sender factory.
	 */
	private final ViewSenderProvider viewSenderProvider;

	/**
	 * View sender.
	 */
	private ViewSender viewSender;

	/**
	 * Temporary collection of route mappings used during the
	 * {@link #buildRoutes} method call.
	 */
	private Collection<RouteImpl> mappings;


	/**
	 * Create new provider.
	 *
	 * @param argHandlerProvider Controller method argument handler provider.
	 * @param viewSenderProvider View sender provider.
	 */
	public AbstractRouterConfigurationProvider(
			final ControllerMethodArgHandlerProvider argHandlerProvider,
			final ViewSenderProvider viewSenderProvider) {

		this.argHandlerProvider = argHandlerProvider;
		this.viewSenderProvider = viewSenderProvider;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.RouterConfigurationProvider#getRouterConfiguration(javax.servlet.ServletContext, com.boylesoftware.web.ApplicationConfiguration, com.boylesoftware.web.ApplicationServices)
	 */
	@Override
	public final RouterConfiguration getRouterConfiguration(
			final ServletContext sc, final ApplicationConfiguration config,
			final ApplicationServices appServices)
		throws UnavailableException {

		this.viewSender = this.viewSenderProvider.getViewSender(sc);

		this.mappings = new ArrayList<>();
		try {
			this.buildRoutes(sc);

			final String contextPath =
				StringUtils.emptyIfNull(sc.getContextPath());

			return new RouterConfigurationImpl(appServices,
					this.mappings, contextPath, this.getLoginPageURI(sc),
					Pattern.compile(contextPath + "(?:" +
							this.getProtectedURIPattern(sc) + ")"),
					Pattern.compile(contextPath + "(?:" +
							this.getPublicURIPattern(sc) + ")"));

		} finally {
			this.mappings = null;
		}
	}


	/**
	 * Add route mapping.
	 *
	 * @param sc Servlet context.
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
	 * @param commonScript Additional logic associated with the mapping, or
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
	protected final void addRoute(final ServletContext sc, final String id,
			final String uriPattern, final SecurityMode securityMode,
			final Script commonScript, final Object controller,
			final String viewIdPattern, final Script viewScript)
		throws UnavailableException {

		final String contextPath = StringUtils.emptyIfNull(sc.getContextPath());

		this.mappings.add(new RouteImpl(sc, id, contextPath + uriPattern,
				securityMode, commonScript, controller,
				this.argHandlerProvider, viewIdPattern, viewScript,
				this.viewSender));
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
	 *
	 * @throws UnavailableException If an error happens.
	 */
	protected abstract void buildRoutes(ServletContext sc)
		throws UnavailableException;

	/**
	 * Get URI of the application's user login page. This is the value that
	 * {@link RouterConfiguration#getLoginPageURI} method will be returning.
	 *
	 * <p>Note, that patterns returned by {@link #getProtectedURIPattern} and
	 * {@link #getPublicURIPattern} methods must be such that make the login
	 * page URI public.
	 *
	 * @param sc Servlet context.
	 *
	 * @return Login page URI, which is a context relative URL without a query
	 * string.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	protected abstract String getLoginPageURI(ServletContext sc)
		throws UnavailableException;

	/**
	 * Get pattern for URIs of resources, access to which requires an
	 * authenticated user. If this method returns {@code null}, which is the
	 * default unless overridden, and {@link #getPublicURIPattern} returns a
	 * non-null value, all URIs that do not match the public URI pattern are
	 * considered protected. If both this and {@link #getPublicURIPattern}
	 * methods return {@code null}, all URIs are considered public. The pattern,
	 * if returned, must match context relative request URIs.
	 *
	 * @param sc Servlet context.
	 *
	 * @return Regular expression for protected URIs, or {@code null}.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	@SuppressWarnings("unused")
	protected String getProtectedURIPattern(ServletContext sc)
		throws UnavailableException {

		return null;
	}

	/**
	 * Get pattern for URIs of resources, access to which does not require an
	 * authenticated user. If this method returns {@code null}, which is the
	 * default unless overridden, all URIs that do not match pattern returned by
	 * {@link #getProtectedURIPattern} method are considered public. If a URI
	 * matches both protected and public URI patterns, the resource is
	 * considered public. The pattern, if returned, must match context relative
	 * request URIs.
	 *
	 * @param sc Servlet context.
	 *
	 * @return Regular expression for public URIs, or {@code null}.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	@SuppressWarnings("unused")
	protected String getPublicURIPattern(ServletContext sc)
		throws UnavailableException {

		return null;
	}
}
