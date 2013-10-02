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
package com.boylesoftware.web.api;

import javax.servlet.http.HttpServletRequest;


/**
 * Router configuration API.
 *
 * @author Lev Himmelfarb
 */
public interface Routes {

	/**
	 * Requested route URI type.
	 */
	enum URIType {

		/**
		 * URI is generated according to the route mapping security mode.
		 */
		DEFAULT,

		/**
		 * Force the URI to be an HTTPS URL. If current request is already
		 * HTTPS, the resulting URI is server root relative.
		 */
		FORCE_SSL,

		/**
		 * Force the URI to be a plain HTTP URL. If current request is already
		 * plain HTTP, the resulting URI is server root relative.
		 */
		FORCE_PLAIN,

		/**
		 * Force the URI to be an absolute HTTPS URL.
		 */
		FORCE_ABSOLUTE_SSL,

		/**
		 * Force the URI to be an absolute plain HTTP URL.
		 */
		FORCE_ABSOLUTE_PLAIN
	}


	/**
	 * Get URI for the specified route. The URI can be used to generate links
	 * and send redirects.
	 *
	 * @param request Request, used to determine current HTTP or HTTPS mode.
	 * @param routeId Route id.
	 * @param type Required URI type.
	 * @param uriParams Values for URI parameters in the route mapping's URI
	 * pattern.
	 *
	 * @return The URI.
	 *
	 * @throws IllegalArgumentException If specified id is unknown or the number
	 * of provided URI parameters does not match the number of placeholders in
	 * the pattern.
	 */
	String getRouteURI(HttpServletRequest request, String routeId, URIType type,
			String... uriParams);
}
