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
package com.boylesoftware.web.input.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.boylesoftware.web.util.EntityUtils;


/**
 * Field-level validation constraint used to check if the value is good for the
 * specified persistent entity field, length-wise. The target field maximum
 * length is retrieved using {@link EntityUtils#getColumnLength} method.
 *
 * @author Lev Himmelfarb
 */
@Constraint(validatedBy=FitsValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
	ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Fits {

	/**
	 * Constraint violation message.
	 *
	 * @return Constraint violation message.
	 */
	String message() default
		"{com.boylesoftware.web.validation.constraints.Fits.message}";

	/**
	 * Constraint groups.
	 *
	 * @return Constraint groups.
	 */
	Class<?>[] groups() default {};

	/**
	 * Constraint payload.
	 *
	 * @return Constraint payload.
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * The target entity class.
	 *
	 * @return The target entity class.
	 */
	Class<?> entity();

	/**
	 * The target field name.
	 *
	 * @return The target field name.
	 */
	String field();
}
