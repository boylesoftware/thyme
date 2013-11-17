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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.MessageInterpolator;

import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.api.FlashAttributes;
import com.boylesoftware.web.api.RouterRequestContext;
import com.boylesoftware.web.api.UserInputErrors;


/**
 * Extended SPI version of the {@link RouterRequestContext} interface used by
 * the framework and framework component implementations.
 *
 * @author Lev Himmelfarb
 */
public interface RouterRequest
	extends RouterRequestContext {

	/**
	 * Get route associated with the request.
	 *
	 * @return The route descriptor.
	 */
	Route getRoute();

	/**
	 * Tell if processing of the request requires an authenticated user. The
	 * implementation must not take into account the route's security mode,
	 * which is processed by the framework separately.
	 *
	 * @return {@code true} if an authenticated user is required.
	 */
	boolean isAuthenticationRequired();

	/**
	 * Get authenticator API.
	 *
	 * @param <T> Authenticated user object class. This is the class of the
	 * object returned by the {@link AuthenticationService#getAuthenticatedUser}
	 * method.
	 *
	 * @return The authenticator API.
	 */
	<T> Authenticator<T> getAuthenticator();

	/**
	 * Get controller method handler for processing the request.
	 *
	 * @return Controller method handler, or {@code null} if controller does not
	 * have custom logic for the request's HTTP method.
	 */
	ControllerMethodHandler getControllerMethodHandler();

	/**
	 * Get request validation message interpolator. The interpolator is used
	 * to interpolate messages generated during user input validation.
	 *
	 * @return The interpolator.
	 */
	MessageInterpolator getMessageInterpolator();

	/**
	 * Get user input validation errors API.
	 *
	 * @return User input validation errors API.
	 */
	UserInputErrors getUserInputErrors();

	/**
	 * Get flash attributes API.
	 *
	 * @return The flash attributes API.
	 */
	FlashAttributes getFlashAttributes();

	/**
	 * Commit flash attributes. Called by the framework before sending a
	 * successful response (either redirect or the view) back to the client.
	 */
	void commitFlashAttributes();

	/**
	 * Change underlying original request and response.
	 *
	 * @param request The new underlying request.
	 * @param response The new response.
	 */
	void rewrap(HttpServletRequest request, HttpServletResponse response);

	/**
	 * Recycle the request object. This method is called by the framework when
	 * the request is no longer needed.
	 */
	void recycle();
}
