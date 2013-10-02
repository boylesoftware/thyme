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

import java.lang.annotation.Annotation;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;


/**
 * Interface for {@link ControllerMethodArgHandler}s provider.
 *
 * @author Lev Himmelfarb
 */
public interface ControllerMethodArgHandlerProvider {

	/**
	 * Get handler for an argument with the specified type and annotations. This
	 * method is called during the application initialization.
	 *
	 * @param sc Servlet context.
	 * @param argInd Zero-based argument index in the list of controller
	 * method arguments.
	 * @param argType Argument type.
	 * @param argAnnos Argument annotations, never {@code null}, but can
	 * be empty.
	 *
	 * @return Handler, or {@code null} if this provider can not handle the
	 * specified argument.
	 *
	 * @throws UnavailableException If an error happens. Throwing this exception
	 * makes the web-application fail to start.
	 */
	ControllerMethodArgHandler getHandler(ServletContext sc, int argInd,
			Class<?> argType, Annotation[] argAnnos)
		throws UnavailableException;
}
