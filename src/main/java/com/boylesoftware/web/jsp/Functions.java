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
package com.boylesoftware.web.jsp;

import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import com.boylesoftware.web.AbstractWebApplication;
import com.boylesoftware.web.api.Routes;
import com.boylesoftware.web.api.Routes.URIType;


/**
 * JSP function implementations.
 *
 * @author Lev Himmelfarb
 */
public final class Functions {

	/**
	 * Empty string array.
	 */
	private static final String[] EMPTY_STRING_ARRAY = new String[0];


	/**
	 * All methods are static.
	 */
	private Functions() {}


	/**
	 * Get parameterless route URI using {@link Routes#getRouteURI} with
	 * {@link URIType#DEFAULT}.
	 *
	 * @param pageContext Page context.
	 * @param routeId Route id.
	 *
	 * @return The URI.
	 */
	public static String uri(final PageContext pageContext,
			final String routeId) {

		return uri(pageContext, routeId, URIType.DEFAULT, null);
	}

	/**
	 * Get parameterless route URI using {@link Routes#getRouteURI} and force it
	 * to be either {@link URIType#FORCE_SSL} or {@link URIType#FORCE_PLAIN}.
	 *
	 * @param pageContext Page context.
	 * @param routeId Route id.
	 * @param forceSSL {@code true} for {@link URIType#FORCE_SSL}, {@code false}
	 * for {@link URIType#FORCE_PLAIN}.
	 *
	 * @return The URI.
	 */
	public static String uriForce(final PageContext pageContext,
			final String routeId, final boolean forceSSL) {

		return uri(pageContext, routeId,
				(forceSSL ? URIType.FORCE_SSL : URIType.FORCE_PLAIN), null);
	}

	/**
	 * Get parameterless route URI using {@link Routes#getRouteURI} with
	 * {@link URIType#FORCE_PLAIN}.
	 *
	 * @param pageContext Page context.
	 * @param routeId Route id.
	 *
	 * @return The URI.
	 */
	public static String uriForcePlain(final PageContext pageContext,
			final String routeId) {

		return uri(pageContext, routeId, URIType.FORCE_PLAIN, null);
	}

	/**
	 * Get parameterless route URI using {@link Routes#getRouteURI} with
	 * {@link URIType#FORCE_SSL}.
	 *
	 * @param pageContext Page context.
	 * @param routeId Route id.
	 *
	 * @return The URI.
	 */
	public static String uriForceSSL(final PageContext pageContext,
			final String routeId) {

		return uri(pageContext, routeId, URIType.FORCE_SSL, null);
	}

	/**
	 * Get route URI using {@link Routes#getRouteURI} with
	 * {@link URIType#DEFAULT}.
	 *
	 * @param pageContext Page context.
	 * @param routeId Route id.
	 * @param params Comma-separated URI parameter values.
	 *
	 * @return The URI.
	 */
	public static String uriParams(final PageContext pageContext,
			final String routeId, final String params) {

		return uri(pageContext, routeId, URIType.DEFAULT, params);
	}


	/**
	 * Call {@link Routes#getRouteURI}.
	 *
	 * @param pageContext Page context.
	 * @param routeId Route id.
	 * @param type URI type.
	 * @param params Comma-separated URI parameter values, or {@code null} or
	 * empty string.
	 *
	 * @return The route URI.
	 */
	private static String uri(final PageContext pageContext,
			final String routeId, final URIType type, final String params) {

		final String[] uriParams = ((params != null) && (params.length() > 0) ?
				params.split(",") : EMPTY_STRING_ARRAY);

		try {
			return AbstractWebApplication
					.getApplication(pageContext.getServletContext())
					.getRoutes()
					.getRouteURI((HttpServletRequest) pageContext.getRequest(),
							routeId, type, uriParams);
		} catch (final UnavailableException e) {
			throw new RuntimeException(e);
		}
	}
}
