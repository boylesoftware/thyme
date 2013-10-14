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
package com.boylesoftware.web.impl.routes;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;


/**
 * Type converter utility.
 *
 * @author Lev Himmelfarb
 */
final class TypeConverter {

	/**
	 * All methods are static.
	 */
	private TypeConverter() {}


	/**
	 * Convert value to entity id class.
	 *
	 * @param em Entity manager.
	 * @param entityClass Entity class.
	 * @param val Value to convert.
	 *
	 * @return Id value.
	 *
	 * @throws ServletException If cannot be converted.
	 */
	static Object toEntityId(final EntityManager em, final Class<?> entityClass,
			final Object val)
		throws ServletException {

		final Class<?> idClass =
			em.getMetamodel().entity(entityClass).getIdType().getJavaType();

		final Object id = TypeConverter.convert(val, idClass);

		if (id == null)
			throw new ServletException("Entity id of invalid class " +
					val.getClass().getName() + ".");

		return id;
	}

	/**
	 * Convert value to the target type.
	 *
	 * @param val Value to convert.
	 * @param type Target type.
	 *
	 * @return Converted value, or {@code null} if cannot be converted.
	 */
	static Object convert(final Object val, final Class<?> type) {

		if (type.isAssignableFrom(val.getClass()))
			return val;

		if (val instanceof String) {
			if (type.isAssignableFrom(Integer.class))
				return Integer.valueOf((String) val);
			if (type.isAssignableFrom(Long.class))
				return Long.valueOf((String) val);
			if (type.isAssignableFrom(Short.class))
				return Short.valueOf((String) val);
			if (type.isAssignableFrom(Byte.class))
				return Byte.valueOf((String) val);
		} else if (val instanceof Number) {
			if (type.isAssignableFrom(String.class))
				return val.toString();
			if (type.isAssignableFrom(Integer.class))
				return Integer.valueOf(((Number) val).intValue());
			if (type.isAssignableFrom(Long.class))
				return Long.valueOf(((Number) val).longValue());
			if (type.isAssignableFrom(Short.class))
				return Short.valueOf(((Number) val).shortValue());
			if (type.isAssignableFrom(Byte.class))
				return Byte.valueOf(((Number) val).byteValue());
		}

		return null;
	}
}
