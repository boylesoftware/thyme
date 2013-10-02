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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.MethodNotAllowedException;
import com.boylesoftware.web.api.Routes;


/**
 * Interface for the request routing configuration. Request routing
 * configuration is a collection is route mappings, which map request URIs to
 * certain custom application logic. The logic is defined by a collection of
 * specialized components associated with the route and, therefore, the request
 * URI. The components include the controller, the view, and additional scripts
 * executed outside the the controller and the view at various defined points in
 * the request processing sequence.
 *
 * @author Lev Himmelfarb
 */
public interface RouterConfiguration {

	/**
	 * Find route matching the specified request URI. The route must be
	 * discarded using its {@link RouterRequest#recycle} method after it is no
	 * longer needed.
	 *
	 * @param request Original request to route.
	 * @param response The response. If route is found, this response is made
	 * available via the router request's {@link RouterRequest#getResponse}
	 * method.
	 *
	 * @return Original request wrapped as a router request object, or
	 * {@code null} if no matching route found.
	 *
	 * @throws MethodNotAllowedException If route exists, but it cannot handle
	 * the request's HTTP method.
	 * @throws ServletException If an error happens.
	 */
	RouterRequest findRoute(HttpServletRequest request,
			HttpServletResponse response)
		throws MethodNotAllowedException, ServletException;

	/**
	 * Get URI of the application's user login page.
	 *
	 * @return Login page URI, which is a context relative URL without a query
	 * string.
	 */
	String getLoginPageURI();

	/**
	 * Get router configuration API.
	 *
	 * @return Router configuration API.
	 */
	Routes getRoutes();
}
