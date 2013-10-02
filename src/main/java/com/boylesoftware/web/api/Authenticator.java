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
import javax.servlet.http.HttpServletResponse;


/**
 * The authentication service API, which allows controllers to implement logic
 * that includes logging users in and out. An {@link Authenticator} can be
 * included in the list of a controller method arguments.
 *
 * @param <T> Authenticated user object type.
 *
 * @author Lev Himmelfarb
 */
public interface Authenticator<T> {

	/**
	 * Name of request parameter used to pass the URI of the original protected
	 * resource request to the user login page. Upon successful authentication,
	 * the login page redirects to the this URI. The URI is a context relative
	 * URL of the page, including the query string if any.
	 */
	String TARGET_URI = "uri";


	/**
	 * Establish authenticated session.
	 *
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @param user The authenticated user.
	 * @param rememberUser {@code true} to make authenticated session permanent
	 * (if supported, otherwise - ignored).
	 */
	void authenticate(HttpServletRequest request, HttpServletResponse response,
			T user, boolean rememberUser);

	/**
	 * Invalidate authenticated session.
	 *
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 */
	void logout(HttpServletRequest request, HttpServletResponse response);

	/**
	 * If the authentication service caches authenticated user records, evict
	 * the specified user from the cache. This method needs to be called when
	 * the user record gets updated and the updates affect the authenticated
	 * user record. The actual eviction is delayed until the transaction is
	 * successfully committed.
	 *
	 * @param user The user record.
	 */
	void evictFromCache(T user);

	/**
	 * If the authentication service caches authenticated user records, evict
	 * all cached records from the cache. This method may be useful when bulk
	 * updates are made to the user records. The actual eviction is delayed
	 * until the transaction is successfully committed.
	 */
	void evictAllFromCache();
}
