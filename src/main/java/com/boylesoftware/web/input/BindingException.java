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
package com.boylesoftware.web.input;


/**
 * Request parameter value cannot be converted to the target user input bean
 * field because it is invalid.
 *
 * @author Lev Himmelfarb
 */
public class BindingException
	extends Exception {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Target field default value.
	 */
	private final Object defaultValue;


	/**
	 * Create new exception.
	 *
	 * @param defaultValue Value to assign to the target field.
	 */
	public BindingException(final Object defaultValue) {
		super();

		this.defaultValue = defaultValue;
	}

	/**
	 * Create new exception.
	 *
	 * @param message Error description.
	 * @param cause Error cause.
	 * @param defaultValue Value to assign to the target field.
	 */
	public BindingException(final String message, final Throwable cause,
			final Object defaultValue) {
		super(message, cause);

		this.defaultValue = defaultValue;
	}

	/**
	 * Create new exception.
	 *
	 * @param message Error description.
	 * @param defaultValue Value to assign to the target field.
	 */
	public BindingException(final String message, final Object defaultValue) {
		super(message);

		this.defaultValue = defaultValue;
	}

	/**
	 * Create new exception.
	 *
	 * @param cause Error cause.
	 * @param defaultValue Value to assign to the target field.
	 */
	public BindingException(final Throwable cause, final Object defaultValue) {
		super(cause);

		this.defaultValue = defaultValue;
	}


	/**
	 * Get value to be assigned to the target field.
	 *
	 * @return The value, may be {@code null} for reference types.
	 */
	public Object getDefaultValue() {

		return this.defaultValue;
	}
}
