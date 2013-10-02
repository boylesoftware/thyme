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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for user input bean fields used to associate request parameter to
 * field value binder with the field.
 *
 * @author Lev Himmelfarb
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bind {

	/**
	 * Default error message.
	 */
	public static final String DEFAULT_MESSAGE =
		"{com.boylesoftware.web.input.Bind.message}";


	/**
	 * Error message to use in case the request parameter value is invalid. The
	 * message is interpolated using the same message interpolator used for user
	 * input bean validation.
	 *
	 * @return Error message to use in case the request parameter value is
	 * invalid.
	 */
	String message() default DEFAULT_MESSAGE;

	/**
	 * The binder implementation class. By default, a standard binder is picked
	 * depending on the target field type.
	 *
	 * @return The binder implementation class.
	 */
	Class<? extends Binder> binder();

	/**
	 * Format for request parameter value parsing. Applicable only to certain
	 * binder implementations, irrelevant to others. Binders may have their own
	 * default formats.
	 *
	 * @return Format for request parameter value parsing.
	 */
	String format();
}
