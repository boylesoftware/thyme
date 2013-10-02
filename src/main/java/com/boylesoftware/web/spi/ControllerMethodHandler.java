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

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;

import com.boylesoftware.web.RequestedResourceException;


/**
 * Handler associated with a {@link ControllerHandler} and used to process a
 * particular HTTP request method. The method handler is the ultimate object
 * that calls the custom controller.
 *
 * @author Lev Himmelfarb
 */
public interface ControllerMethodHandler {

	/**
	 * Process user input: build the user input beans in the controller method
	 * arguments, validate them, create user input validation errors and save it
	 * all in request attributes to later pass to the controller method.
	 *
	 * @param request The HTTP request.
	 *
	 * @return {@code true} if user input has been successfully validated.
	 *
	 * @throws ServletException If an application error happens.
	 */
	boolean prepareUserInput(RouterRequest request)
		throws ServletException;

	/**
	 * Tell if the expected response is a redirect in case the method call is
	 * successful. The {@link #call} method returns the redirect URL in that
	 * case. If the {@link #call} method returns {@code null} and this method
	 * returns {@code true}, it means that the controller has found problems
	 * with the request and the response should be a bad request response with
	 * the current view allowing the user to correct the problems and resubmit
	 * the request.
	 *
	 * @return {@code true} if successful response is expected to be a redirect.
	 */
	boolean redirectOnSuccess();

	/**
	 * Tell if the method needs an entity manager and a transaction.
	 *
	 * @return {@code true} if one of the method arguments is the entity
	 * manager.
	 */
	boolean needsEntityManager();

	/**
	 * Call the controller method.
	 *
	 * @param request The HTTP request.
	 * @param em Entity manager to use inside the controller to access
	 * persistent objects, or {@code null} if {@link #needsEntityManager()}
	 * returned {@code false}.
	 *
	 * @return Result of the method call, which can be a URL to which to
	 * redirect, or {@code null} to show the view associated with the route. If
	 * the returned URL starts with "/", it is considered to be server root
	 * relative.
	 *
	 * @throws RequestedResourceException If the controller throws it.
	 * @throws ServletException If an error happens calling the controller.
	 */
	String call(RouterRequest request, EntityManager em)
		throws RequestedResourceException, ServletException;

	/**
	 * Get method argument handlers.
	 *
	 * @return Read-only list of controller method argument handlers.
	 */
	List<ControllerMethodArgHandler> getArgHandlers();
}
