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

import java.util.Collection;
import java.util.Map;

import com.boylesoftware.web.UserInputError;


/**
 * User input validation errors. As a collection, contains all errors regardless
 * of with what field they are associated. Also provides a map returned by the
 * {@link #getByField} method that groups errors by corresponding input fields.
 *
 * <p>A {@link UserInputErrors} reference can be included in the list of a
 * controller method arguments and used for in-transaction request validation.
 *
 * @author Lev Himmelfarb
 */
public interface UserInputErrors
	extends Collection<UserInputError> {

	/**
	 * Add error.
	 *
	 * @param fieldName Input field name, or {@code null} if the error is not
	 * associated with any particular field (a.k.a. global error).
	 * @param messageTmpl The message template. The message template is
	 * interpolated using the same message interpolator used for validating user
	 * input beans.
	 */
	void add(final String fieldName, final String messageTmpl);

	/**
	 * Get form errors grouped by form fields.
	 *
	 * @return Unmodifiable map with field names as keys and collections of
	 * corresponding errors as values. Special key {@code null} is used
	 * for errors that are not associated with any particular field.
	 */
	Map<String, Collection<UserInputError>> getByField();

	/**
	 * Get first error added to this errors object.
	 *
	 * @return The error, or {@code null} if object contains no errors.
	 */
	UserInputError getFirstError();

	/**
	 * Get first field (not global) error added to this errors object.
	 *
	 * @return Field error, or {@code null} if no field errors were ever added.
	 */
	UserInputError getFirstFieldError();
}
