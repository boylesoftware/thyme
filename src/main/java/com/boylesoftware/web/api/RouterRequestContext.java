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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * HTTP servlet request that matched a certain router mapping and is ready to be
 * processed by the framework request processing logic.
 *
 * <p>On top of the additional methods, the router request context
 * implementation modifies behavior of some of the original request's methods.
 * Namely, methods that return request parameters include parameters coming from
 * the mapping's URI pattern.
 *
 * @author Lev Himmelfarb
 */
public interface RouterRequestContext
	extends HttpServletRequest {

	/**
	 * Get response object associated with the request.
	 *
	 * @return The response.
	 */
	HttpServletResponse getResponse();

	/**
	 * Get authenticated user associated with the request. Normally, the method
	 * simply attempts to get the authenticated user from the
	 * {@link Attributes#AUTHED_USER} request attribute.
	 *
	 * @param <T> Authenticated user object class. This is the user object class
	 * of associated with the {@link Authenticator}.
	 *
	 * @return Authenticated user, or {@code null} if the request is anonymous.
	 */
	<T> T getAuthenticatedUser();

	/**
	 * Get user locale. As opposed to the {@link #getLocale()} method, this
	 * method uses configured locale finder. If there is a {@link Locale}
	 * argument in a controller method's argument list, this is the locale
	 * passed to the controller.
	 *
	 * @return User locale, if request has an authenticated user, or the locale
	 * provided by the user agent, the same returned by {@link #getLocale()}
	 * method.
	 */
	Locale getUserLocale();
}
