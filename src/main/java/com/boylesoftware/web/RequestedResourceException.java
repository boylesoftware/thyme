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


/**
 * Parent for exceptions that indicate a requested resource unavailability and
 * result in an HTTP error response.
 *
 * @author Lev Himmelfarb
 */
public abstract class RequestedResourceException
	extends Exception {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Get HTTP response status code that corresponds to the error.
	 *
	 * @return HTTP status code.
	 */
	public abstract int getHTTPErrorCode();
}
