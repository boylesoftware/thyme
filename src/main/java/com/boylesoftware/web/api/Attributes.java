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
package com.boylesoftware.web.api;


/**
 * Standard request attribute names.
 *
 * @author Lev Himmelfarb
 */
public final class Attributes {

	/**
	 * All members are static.
	 */
	private Attributes() {}


	/**
	 * Name of request attribute used to store the authenticated user object.
	 */
	public static final String AUTHED_USER = "authedUser";

	/**
	 * Name of request attribute used to store user input errors object.
	 */
	public static final String USER_INPUT_ERRORS = "userInputErrors";

	/**
	 * Name use request attribute used to store user input bean.
	 */
	public static final String USER_INPUT = "userInput";
}
