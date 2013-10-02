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

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;

import com.boylesoftware.web.api.Attributes;
import com.boylesoftware.web.api.Authenticator;


/**
 * Interface for the authenticator service. The application uses a single
 * authenticator instance responsible for user authentication. An HTTP request
 * may be associated by the authenticator with a particular application user.
 * The authenticator implements the mechanism of that association. It is able to
 * tell if a given request is "authenticated" and it is also able to establish
 * and end authenticated sessions, that consist of series of requests from the
 * same user from the moment the session is established (the user logs in) until
 * it ends (either, because the user logs out or the session expires).
 *
 * @param <T> Authenticated user object type.
 *
 * @author Lev Himmelfarb
 */
// TODO: caching provider
public interface AuthenticationService<T> {

	/**
	 * Get authenticated user object class.
	 *
	 * @return The authenticated user object class.
	 */
	Class<T> getAuthenticatedUserObjectClass();

	/**
	 * Get authenticated user for the request. The framework calls this method
	 * only if the request's {@link HttpServletRequest#isSecure} method returns
	 * {@code true}. Otherwise, it assumes there is no authenticated user.
	 *
	 * <p>This method is for the framework use. If a controller needs the
	 * authenticated user, it is made available in a request attribute under
	 * {@link Attributes#AUTHED_USER} name.
	 *
	 * @param request The HTTP request.
	 * @param emf Entity manager factory to use to access the user record.
	 *
	 * @return Authenticated user record, or {@code null} if the request does
	 * not contain information about the authenticated user.
	 */
	T getAuthenticatedUser(HttpServletRequest request,
			EntityManagerFactory emf);

	/**
	 * Get authentication service API for controllers.
	 *
	 * @param routerReq Request being processed.
	 *
	 * @return The authenticator.
	 */
	Authenticator<T> getAuthenticator(RouterRequest routerReq);

	/**
	 * Execute any requested via the {@link Authenticator} API authenticated
	 * users cache evictions. The method is called after the current transaction
	 * has been successfully committed.
	 *
	 * @param routerReq Request being processed.
	 */
	void performCacheEvictions(RouterRequest routerReq);
}
