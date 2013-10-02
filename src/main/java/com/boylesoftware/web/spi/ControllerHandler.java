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

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


/**
 * Interface for controller handlers, that encapsulate information about a
 * controller associated with a route.
 *
 * @author Lev Himmelfarb
 */
public interface ControllerHandler {

	/**
	 * Get handler for the controller method corresponding to the request HTTP
	 * method.
	 *
	 * @param request The HTTP request.
	 *
	 * @return Corresponding controller method handler, or {@code null} if
	 * controller does not handle the request's HTTP method.
	 */
	ControllerMethodHandler getMethodHandler(HttpServletRequest request);

	/**
	 * Tell if the controller has additional view preparation logic. If this
	 * method returns {@code false}, the {@link #prepareView} method is not
	 * called for the request.
	 *
	 * @param request The HTTP request.
	 *
	 * @return {@code true} if controller has view preparation logic.
	 *
	 * @throws ServletException If an error happens processing the request.
	 */
	boolean hasPrepareView(RouterRequest request)
		throws ServletException;

	/**
	 * Tell if {@link #prepareView} method uses the entity manager. The method
	 * is called only after calling {@link #hasPrepareView} and only it that
	 * call returned {@code true}.
	 *
	 * @return {@code true} if it uses the entity manager.
	 */
	boolean prepareViewNeedsEntityManager();

	/**
	 * Call controller's view preparation logic. If controller does not provide
	 * any view preparation logic, the method must do nothing. The method is
	 * called by the framework each time before sending the view to the client
	 * and after the view's script (see {@link ViewHandler#getScript}), if any,
	 * is executed.
	 *
	 * @param request The HTTP request.
	 * @param em Entity manager to use to access persistent objects, or
	 * {@code null} if the {@link #prepareViewNeedsEntityManager()} method
	 * returned {@code false}.
	 *
	 * @throws ServletException If an error happens processing the request.
	 */
	void prepareView(RouterRequest request, EntityManager em)
		throws ServletException;
}
