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


/**
 * Descriptor of a route, which maps request URI to corresponding application
 * logic, which includes a controller, a view, additional logic scripts and
 * mapped resource security requirements.
 *
 * @author Lev Himmelfarb
 */
public interface Route {

	/**
	 * Security mode for the mapped resource(s).
	 */
	enum SecurityMode {

		/**
		 * Default mode. If {@link RouterRequest#isAuthenticationRequired}
		 * returns {@code false} for the request, plain HTTP requests to the
		 * mapped resource(s) are allowed, no authenticated user is required. If
		 * it returns {@code true}, the effect is the same as having security
		 * mode {@link #FORCE_REQUIRE_AUTH}.
		 *
		 * <p>If configured login page URI matched a mapping with security mode
		 * {@link #DEFAULT}, the mapping's security mode is automatically
		 * replaced with {@link #FORCE_SSL}.
		 *
		 * <p>Note, that the framework provides the route with an authenticated
		 * user, if there is any, <em>only</em> if the request is received over
		 * HTTPS.
		 */
		DEFAULT,

		/**
		 * The mapped resource(s) may be accessed only over HTTPS. If a plain
		 * HTTP request is submitted, the framework responds with a 301 (Moved
		 * Permanently) redirect to the same URI, but with HTTPS.
		 */
		FORCE_SSL,

		/**
		 * Access to the mapped resource(s) requires an authenticated user. This
		 * also implies that HTTPS is required. If a plain HTTP request is
		 * submitted, the framework responds with a 301 (Moved Permanently)
		 * redirect to the same URI, but with HTTPS. If the request is HTTPS,
		 * but there is no authenticated user, the framework responds with a
		 * 303 (See Other) redirect to the login page. This security mode
		 * overrides whatever {@link RouterRequest#isAuthenticationRequired}
		 * method tells about the request.
		 */
		FORCE_REQUIRE_AUTH
	}


	/**
	 * Get script associated with the route. The script is executed each time
	 * the mapping is used before attempting to call the controller and send the
	 * view.
	 *
	 * @return The script, or {@code null} if none.
	 */
	Script getScript();

	/**
	 * Get handler of the controller associated with the route.
	 *
	 * @return Controller handler, or {@code null} if no controller is
	 * associated with the route.
	 */
	ControllerHandler getControllerHandler();

	/**
	 * Get handler of the view associated with the route.
	 *
	 * @return View handler.
	 */
	ViewHandler getViewHandler();

	/**
	 * Get mapped resource security mode.
	 *
	 * @return The security mode.
	 */
	SecurityMode getSecurityMode();
}
