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
package com.boylesoftware.web.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * Collection of static utility methods for persistent entities.
 *
 * @author Lev Himmelfarb
 */
public final class EntityUtils {

	/**
	 * Cached getters for entity fields.
	 */
	private static final ConcurrentMap<String, Method> READ_METHODS =
		new ConcurrentHashMap<>();

	/**
	 * Cached entity fields.
	 */
	private static final ConcurrentMap<String, Field> FIELDS =
		new ConcurrentHashMap<>();


	/**
	 * Only static methods.
	 */
	private EntityUtils() {}


	/**
	 * Get entity field value.
	 *
	 * @param entity The entity object.
	 * @param fieldName Field name.
	 *
	 * @return Field value.
	 *
	 * @throws IllegalArgumentException If field does not exist or is not
	 * readable.
	 */
	public static Object getFieldValue(final Object entity,
			final String fieldName) {

		final String cacheKey = entity.getClass().getName() + "#" + fieldName;
		Method readMethod = READ_METHODS.get(cacheKey);
		if (readMethod == null) {
			try {
				final BeanInfo bi =
					Introspector.getBeanInfo(entity.getClass(), Object.class);

				PropertyDescriptor fieldDesc = null;
				for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
					if (pd.getName().equals(fieldName)) {
						fieldDesc = pd;
						break;
					}
				}
				if (fieldDesc == null)
					throw new IllegalArgumentException("Property " + fieldName +
							" not found in bean class " +
							entity.getClass().getName() + ".");

				readMethod = fieldDesc.getReadMethod();
				if (readMethod == null)
					throw new IllegalArgumentException("Property " + fieldName +
							" in bean class " + entity.getClass().getName() +
							" is not readable.");

				READ_METHODS.putIfAbsent(cacheKey, readMethod);

			} catch (final IntrospectionException e) {
				throw new RuntimeException("Error introspecting entity object.",
						e);
			}
		}

		try {
			return readMethod.invoke(entity);
		} catch (final InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException("Error getting entity field value.", e);
		}
	}

	/**
	 * Get field maximum length using its {@link Column} annotation. If the
	 * field does not have a {@link Column} annotation, returns the default
	 * value of 255. If the field has a {@link Lob} annotation, always returns
	 * {@link Integer#MAX_VALUE}.
	 *
	 * @param entityClass The entity class.
	 * @param fieldName The field name.
	 *
	 * @return The field's maximum length.
	 *
	 * @throws IllegalArgumentException If field does not exist.
	 */
	public static int getColumnLength(final Class<?> entityClass,
			final String fieldName) {

		final Field field = getField(entityClass, fieldName);

		if (field.isAnnotationPresent(Lob.class))
			return Integer.MAX_VALUE;

		final Column anno = field.getAnnotation(Column.class);
		if (anno == null)
			return 255;

		return anno.length();
	}

	/**
	 * Get temporal field type.
	 *
	 * @param entityClass The entity class.
	 * @param fieldName The field name.
	 *
	 * @return Field temporal type, or {@code null} if the field does not have
	 * a {@link Temporal} annotation
	 *
	 * @throws IllegalArgumentException If field does not exist.
	 */
	public static TemporalType getColumnTemporalType(final Class<?> entityClass,
			final String fieldName) {

		final Field field = getField(entityClass, fieldName);
		final Temporal anno = field.getAnnotation(Temporal.class);
		if (anno == null)
			return null;

		return anno.value();
	}

	/**
	 * Tell if the field is nullable using its {@link Column} or
	 * {@link JoinColumn} annotation.
	 *
	 * @param entityClass The entity class.
	 * @param fieldName The field name.
	 *
	 * @return {@code true} if the field is nullable;
	 *
	 * @throws IllegalArgumentException If field does not exist.
	 */
	public static boolean isColumnNullable(final Class<?> entityClass,
			final String fieldName) {

		final Field field = getField(entityClass, fieldName);

		final Column anno1 = field.getAnnotation(Column.class);
		if (anno1 != null)
			return anno1.nullable();

		final JoinColumn anno2 = field.getAnnotation(JoinColumn.class);
		if (anno2 != null)
			return anno2.nullable();

		return true;
	}

	/**
	 * Get class of the entity referred by a many-to-one field.
	 *
	 * @param entityClass The class of the entity containing the many-to-one
	 * field.
	 * @param fieldName The field name.
	 *
	 * @return The target entity class.
	 *
	 * @throws IllegalArgumentException If field does not exist or is not a
	 * many-to-one field (does not have a {@link ManyToOne} annotation).
	 */
	public static Class<?> getReferredEntityClass(final Class<?> entityClass,
			final String fieldName) {

		final Field field = getField(entityClass, fieldName);
		if (!field.isAnnotationPresent(ManyToOne.class))
			throw new IllegalArgumentException("Field " + fieldName +
					" in entity class " + entityClass.getName() +
					" is not a many-to-one field.");

		return field.getType();
	}


	/**
	 * Get field by name.
	 *
	 * @param entityClass The entity class.
	 * @param fieldName Field name.
	 *
	 * @return Field descriptor.
	 *
	 * @throws IllegalArgumentException If field does not exist.
	 */
	private static Field getField(final Class<?> entityClass,
			final String fieldName) {

		final String cacheKey = entityClass.getName() + "#" + fieldName;
		Field field = FIELDS.get(cacheKey);
		if (field == null) {
			Class<?> searchType = entityClass;
			LOOP: while ((searchType != null)
					&& !Object.class.equals(searchType)) {
				final Field[] fields = searchType.getDeclaredFields();
				final int fieldsLen = fields.length;
				for (int i = 0; i < fieldsLen; i++) {
					final Field f = fields[i];
					if (f.getName().equals(fieldName)) {
						field = f;
						break LOOP;
					}
				}
				searchType = searchType.getSuperclass();
			}
			if (field == null)
				throw new IllegalArgumentException("No field " + fieldName +
						" in entity class " + entityClass.getName() + ".");
			FIELDS.putIfAbsent(cacheKey, field);
		}

		return field;
	}
}
