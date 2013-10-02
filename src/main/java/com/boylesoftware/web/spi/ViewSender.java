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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Interface for implementations that send views to the client.
 *
 * @author Lev Himmelfarb
 */
public interface ViewSender {

	/**
	 * Send the identified view to the client. If the request is in asynchronous
	 * mode, the implementation must complete request processing using the
	 * attached to the request asynchronous context.
	 *
	 * @param viewId The implementation specific view id.
	 * @param request The HTTP request. Note, that the implementation must check
	 * if the request is in asynchronous mode and proceed accordingly.
	 * @param response The HTTP response.
	 *
	 * @throws IOException If an I/O error happens sending the view to the
	 * client.
	 * @throws ServletException If an application error happens sending the view
	 * to the client.
	 */
	void send(String viewId, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ServletException;
}
