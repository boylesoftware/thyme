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
package com.boylesoftware.web.input.binders;

import com.boylesoftware.web.input.Binder;
import com.boylesoftware.web.input.BindingException;
import com.boylesoftware.web.spi.RouterRequest;


/**
 * Binder for enumeration fields.
 *
 * @author Lev Himmelfarb
 */
public class EnumBinder
	implements Binder {

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.input.Binder#convert(com.boylesoftware.web.spi.RouterRequest, java.lang.String, java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object convert(final RouterRequest request, final String paramVal,
			final String format,
			@SuppressWarnings("rawtypes") final Class targetType)
		throws BindingException {

		try {
			return (paramVal != null ?
					Enum.valueOf(targetType, paramVal) : null);
		} catch (final IllegalArgumentException e) {
			throw new BindingException(e, null);
		}
	}
}
