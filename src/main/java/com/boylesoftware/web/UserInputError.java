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
 * User input validation error.
 *
 * @author Lev Himmelfarb
 */
public class UserInputError {

	/**
	 * Input field name, or {@code null}.
	 */
	private String fieldName;

	/**
	 * The message.
	 */
	private String message;


	/**
	 * Get name of the input field, with which the error is associated.
	 *
	 * @return Input field name, or {@code null} if the error is not associated
	 * with any particular field.
	 */
	public String getFieldName() {

		return this.fieldName;
	}

	/**
	 * Set name of the input field, with which the error is associated.
	 *
	 * @param fieldName Input field name, or {@code null} if the error is not
	 * associated with any particular field.
	 */
	public void setFieldName(final String fieldName) {

		this.fieldName = fieldName;
	}

	/**
	 * Get the message.
	 *
	 * @return The message.
	 */
	public String getMessage() {

		return this.message;
	}

	/**
	 * Set the message.
	 *
	 * @param message The message.
	 */
	public void setMessage(final String message) {

		this.message = message;
	}
}
