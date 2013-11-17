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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.boylesoftware.web.input.validation.DynamicValidationGroups;


/**
 * Used to mark user input beans (such as beans behind HTML forms) in the list
 * of controller method arguments.
 *
 * @author Lev Himmelfarb
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserInput {

	/**
	 * Constraint groups to apply during validation. If the annotated user input
	 * bean implements {@link DynamicValidationGroups}, the groups specified by
	 * this annotation override the dynamic groups.
	 *
	 * @return Constraint groups to apply during validation.
	 */
	Class<?>[] groups() default {};
}
