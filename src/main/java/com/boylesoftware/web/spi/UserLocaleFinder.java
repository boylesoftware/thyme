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
package com.boylesoftware.web.spi;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;


/**
 * Interface for the user locale finder, which is responsible for determining
 * the locale associated with a given request. Some applications may have locale
 * as part of the user profile. A user locale finder implementation may access
 * this information in the user profile. It may also default to the locate
 * provided by the user agent (the browser) and available via the
 * {@link HttpServletRequest#getLocale()} method. The application maintains a
 * single instance of the user locale finder for its use.
 *
 * @param <T> Authenticated user object type.
 *
 * @author Lev Himmelfarb
 */
public interface UserLocaleFinder<T> {

	/**
	 * Get locale.
	 *
	 * @param request The HTTP request.
	 * @param user Authenticated user, or {@code null} if the request is not
	 * authenticated. This is the user object returned by
	 * {@link AuthenticationService#getAuthenticatedUser} method.
	 *
	 * @return The locale.
	 */
	Locale getLocale(HttpServletRequest request, T user);
}
