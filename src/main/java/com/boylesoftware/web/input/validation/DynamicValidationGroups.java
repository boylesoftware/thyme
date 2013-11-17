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
package com.boylesoftware.web.input.validation;

import com.boylesoftware.web.api.RouterRequestContext;


/**
 * A user input bean can implement this interface to provide dynamic validation
 * groups list for itself.
 *
 * @author Lev Himmelfarb
 */
public interface DynamicValidationGroups {

	/**
	 * Get validation groups to use to validate this user input bean.
	 *
	 * @param request The request context.
	 *
	 * @return Validation groups, or empty array to use the default group.
	 */
	Class<?>[] getValidationGroups(RouterRequestContext request);
}
