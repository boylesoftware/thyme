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


/**
 * Field-level validation constraint used to ensure user password strength.
 *
 * @author Lev Himmelfarb
 */
@Constraint(validatedBy=StrongPasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
	ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StrongPassword {

	/**
	 * Constraint violation message.
	 *
	 * @return Constraint violation message.
	 */
	String message() default
		"{com.boylesoftware.web.validation.constraints.StrongPassword.message}";

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
	 * Minimum password length. Default is 8.
	 *
	 * @return Minimum password length.
	 */
	int minLength() default 8;

	/**
	 * List of regular expressions all of which the password must contain. By
	 * default, must contain at least one of each: lower case alphabetic
	 * character, upper case alphabetic character and either a digit or a
	 * punctuation.
	 *
	 * @return List of regular expressions all of which the password must
	 * contain.
	 */
	String[] mustContain() default
		{ "\\p{Lower}", "\\p{Upper}", "\\p{Digit}|\\p{Punct}" };
}
