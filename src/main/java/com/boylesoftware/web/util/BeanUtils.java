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
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Collection of static utility methods for working with Java beans.
 *
 * @author Lev Himmelfarb
 */
public final class BeanUtils {

	/**
	 * Bean property value getter that supports nested beans.
	 */
	private static final class Getter {

		/**
		 * Getters for all nested bean properties.
		 */
		private final Method[] getters;


		/**
		 * Construct new getter.
		 *
		 * @param beanClass Top bean class.
		 * @param propName Property name, possibly nested.
		 *
		 * @throws IllegalArgumentException If the property is invalid.
		 */
		Getter(final Class<?> beanClass, final String propName) {

			Class<?> subBeanClass = beanClass;
			try {
				final String[] subPropNames = propName.split("\\.");
				this.getters = new Method[subPropNames.length];
				for (int i = 0; i < subPropNames.length; i++) {
					final BeanInfo bi =
						Introspector.getBeanInfo(subBeanClass, Object.class);
					final String subPropName = subPropNames[i];
					PropertyDescriptor propDesc = null;
					for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
						if (pd.getName().equals(subPropName)) {
							propDesc = pd;
							break;
						}
					}
					if (propDesc == null)
						throw new IllegalArgumentException("Property " +
								subPropName + " not found in bean class " +
								subBeanClass.getName() + ".");
					if ((this.getters[i] = propDesc.getReadMethod()) == null)
						throw new IllegalArgumentException("Property " +
								subPropName + " in bean class " +
								subBeanClass.getName() + " is not readable.");
					Class<?> prevBeanClass = subBeanClass;
					if (i < subPropNames.length - 1) {
						subBeanClass = propDesc.getPropertyType();
						if (subBeanClass == null)
							throw new IllegalArgumentException(
									"Type of nested bean property " +
									subPropName + " in bean class " +
									prevBeanClass.getName() +
									" cannot be determined.");
					}
				}
			} catch (final IntrospectionException e) {
				throw new RuntimeException("Error introspecting bean class " +
						subBeanClass.getName() + ".", e);
			}
		}


		/**
		 * Get bean property value.
		 *
		 * @param bean The bean.
		 *
		 * @return The property value, or {@code null} if the property or any
		 * nested bean in its path is {@code null}.
		 */
		Object getValue(final Object bean) {

			try {
				Object res = bean;
				for (int i = 0; i < this.getters.length; i++) {
					if ((res = this.getters[i].invoke(res)) == null)
						return null;
				}
				return res;
			} catch (final ReflectiveOperationException e) {
				throw new RuntimeException("Error getting bean property value.",
						e);
			}
		}
	}


	/**
	 * Cached getters for properties.
	 */
	private static final ConcurrentMap<String, Getter> GETTERS_CACHE =
		new ConcurrentHashMap<>();


	/**
	 * Only static methods.
	 */
	private BeanUtils() {}


	/**
	 * Get bean property value.
	 *
	 * @param bean The bean.
	 * @param propName Property name. May be nested bean property path that uses
	 * dots to separate nested bean properties.
	 *
	 * @return Property value.
	 *
	 * @throws IllegalArgumentException If field does not exist or is not
	 * readable.
	 */
	public static Object getPropertyValue(final Object bean,
			final String propName) {

		final Class<?> beanClass = bean.getClass();
		final String cacheKey = beanClass.getName() + "#" + propName;
		Getter getter = GETTERS_CACHE.get(cacheKey);
		if (getter == null) {
			getter = new Getter(beanClass, propName);
			GETTERS_CACHE.putIfAbsent(cacheKey, getter);
		}

		return getter.getValue(bean);
	}
}
