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

import java.io.IOException;

import javax.servlet.ServletException;


/**
 * Interface for view handlers, that encapsulate information about a view
 * associated with a route.
 *
 * @author Lev Himmelfarb
 */
public interface ViewHandler {

	/**
	 * Get script associated with the view. The script is executed by the
	 * framework each time before the view is sent to the client.
	 *
	 * @return The script, or {@code null} if none.
	 */
	Script getScript();

	/**
	 * Send the view to the client. The method implementation must not execute
	 * the view script (see {@link #getScript()}).
	 *
	 * @param request The HTTP request.
	 *
	 * @throws IOException If an I/O error happens sending the view to the
	 * client.
	 * @throws ServletException If an application error happens sending the view
	 * to the client.
	 */
	void sendView(RouterRequest request)
		throws IOException, ServletException;
}
