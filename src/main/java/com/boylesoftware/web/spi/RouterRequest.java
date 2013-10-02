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
import javax.servlet.http.HttpServletResponse;
import javax.validation.MessageInterpolator;

import com.boylesoftware.web.api.Attributes;
import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.api.FlashAttributes;
import com.boylesoftware.web.api.UserInputErrors;


/**
 * HTTP servlet request that matched a certain router mapping and is ready to be
 * processed by the framework request processing logic.
 *
 * <p>On top of the additional methods, the router request implementation
 * modifies the behavior of some of the original request's methods. Namely,
 * methods that return request parameters include parameters coming from the
 * mapping's URI pattern.
 *
 * @author Lev Himmelfarb
 */
public interface RouterRequest
	extends HttpServletRequest {

	/**
	 * Get route associated with the request.
	 *
	 * @return The route descriptor.
	 */
	Route getRoute();

	/**
	 * Get response object associated with the request.
	 *
	 * @return The response.
	 */
	HttpServletResponse getResponse();

	/**
	 * Tell if processing of the request requires an authenticated user. The
	 * implementation must not take into account the route's security mode,
	 * which is processed by the framework separately.
	 *
	 * @return {@code true} if an authenticated user is required.
	 */
	boolean isAuthenticationRequired();

	/**
	 * Get authenticated user associated with the request. Normally, the method
	 * simply attempts to get the authenticated user from the
	 * {@link Attributes#AUTHED_USER} request attribute.
	 *
	 * @param <T> Authenticated user object class, the same returned by the
	 * {@link AuthenticationService#getAuthenticatedUserObjectClass} method.
	 *
	 * @return Authenticated user, or {@code null} if the request is anonymous.
	 */
	<T> T getAuthenticatedUser();

	/**
	 * Get authenticator API.
	 *
	 * @param <T> Authenticated user object class, the same returned by the
	 * {@link AuthenticationService#getAuthenticatedUserObjectClass} method.
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
	 * Get user locale. As opposed to the {@link #getLocale()} method, this
	 * method uses configured {@link UserLocaleFinder}. If there is a
	 * {@link Locale} argument in a controller method's argument list, this is
	 * the locale passed to the controller.
	 *
	 * @return User locale, if request has an authenticated user, or the locale
	 * provided by the user agent, the same returned by {@link #getLocale()}
	 * method.
	 */
	Locale getUserLocale();

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
