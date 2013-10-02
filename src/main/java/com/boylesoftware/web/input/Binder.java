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

import com.boylesoftware.web.spi.RouterRequest;


/**
 * Interface for classes responsible for converting request parameters to user
 * input bean field values.
 *
 * @author Lev Himmelfarb
 */
public interface Binder {

	/**
	 * Convert request parameter value to the target user input bean field
	 * value.
	 *
	 * @param request The request.
	 * @param paramVal Request parameter value, may be {@code null}.
	 * @param format Optional binder implementation specific format string from
	 * the annotation.
	 * @param targetType Target field type.
	 *
	 * @return Value for user input bean field.
	 *
	 * @throws BindingException If the request parameter value is invalid.
	 */
	Object convert(RouterRequest request, String paramVal, String format,
			Class<?> targetType)
		throws BindingException;
}
