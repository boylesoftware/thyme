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
package com.boylesoftware.web;

import javax.servlet.http.HttpServletResponse;


/**
 * Requested resource does not support the HTTP request method. Results in a
 * 405 HTTP response.
 *
 * @author Lev Himmelfarb
 */
public class MethodNotAllowedException
	extends RequestedResourceException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.RequestedResourceException#getHTTPErrorCode()
	 */
	@Override
	public int getHTTPErrorCode() {

		return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
	}
}
