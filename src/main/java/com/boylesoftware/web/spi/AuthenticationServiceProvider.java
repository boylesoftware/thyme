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

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import com.boylesoftware.web.ApplicationConfiguration;


/**
 * Interface for the provider that is used to get the application's
 * {@link AuthenticationService}.
 *
 * @param <T> Authenticated user object type.
 *
 * @author Lev Himmelfarb
 */
public interface AuthenticationServiceProvider<T> {

	/**
	 * Create and return the authentication service. This method is called once
	 * during the application initialization.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return The authenticator.
	 *
	 * @throws UnavailableException If authentication service is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	AuthenticationService<T> getAuthenticationService(ServletContext sc,
			ApplicationConfiguration config)
		throws UnavailableException;
}
