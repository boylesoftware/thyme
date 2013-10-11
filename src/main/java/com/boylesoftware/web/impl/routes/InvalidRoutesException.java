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
package com.boylesoftware.web.impl.routes;


/**
 * Router configuration error.
 *
 * @author Lev Himmelfarb
 */
class InvalidRoutesException
	extends RuntimeException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Create new exception.
	 *
	 * @param message Error description.
	 */
	InvalidRoutesException(final String message) {
		super(message);
	}

	/**
	 * Create new exception.
	 *
	 * @param message Error description.
	 * @param cause Error original cause.
	 */
	InvalidRoutesException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
