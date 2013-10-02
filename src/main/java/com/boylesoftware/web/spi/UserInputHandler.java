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


/**
 * Interface for handlers of controller method arguments that are user input
 * beans.
 *
 * @author Lev Himmelfarb
 */
public interface UserInputHandler
	extends ControllerMethodArgHandler {

	/**
	 * Process user input: build the user input bean, validate it, create user
	 * input validation errors object, save it all in request attributes to
	 * later pass to the controller method.
	 *
	 * @param request The HTTP request.
	 *
	 * @return {@code true} if user input has been successfully validated.
	 *
	 * @throws ServletException If an error happens.
	 */
	boolean prepareUserInput(final RouterRequest request)
		throws ServletException;
}
