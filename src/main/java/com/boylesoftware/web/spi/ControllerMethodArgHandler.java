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


/**
 * Interface for handlers that bind controller method arguments to their values.
 *
 * @author Lev Himmelfarb
 */
public interface ControllerMethodArgHandler {

	/**
	 * Tell if in order to determine the argument value, the
	 * {@link #getArgValue} method uses the entity manager.
	 *
	 * @return {@code true} if needs entity manager.
	 */
	boolean usesEntityManager();

	/**
	 * Get argument value.
	 *
	 * @param request The HTTP request.
	 * @param em Entity manager, or {@code null} if all method argument handlers
	 * returned {@code false} from their {@link #usesEntityManager()} methods.
	 *
	 * @return Value to pass to the controller method.
	 *
	 * @throws ServletException If an error happens getting the argument value.
	 */
	Object getArgValue(RouterRequest request, EntityManager em)
		throws ServletException;

	/**
	 * Invoked by the framework when the request processing is finished. The
	 * implementation can use this method to recycle and cleanup any objects
	 * that it created for the request. Any exception thrown from this method is
	 * logged but otherwise ignored.
	 *
	 * @param request The HTTP request.
	 */
	void onComplete(RouterRequest request);
}
