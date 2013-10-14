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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


/**
 * Implementation of model component reference.
 *
 * @author Lev Himmelfarb
 */
class ModelReferenceValueExpression
	implements ValueExpression {

	/**
	 * Model component name.
	 */
	private final String name;

	/**
	 * Name parts.
	 */
	private final String[] nameParts;


	/**
	 * Create new expression.
	 *
	 * @param name Model component name.
	 */
	ModelReferenceValueExpression(final String name) {

		this.name = name;
		this.nameParts = name.split("\\.");
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.ValueExpression#getValue(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public Object getValue(final HttpServletRequest request,
			final EntityManager em)
		throws ServletException {

		String paramVal = request.getParameter(this.name);
		if (paramVal != null)
			return paramVal;

		return this.getBeanProperty(request.getAttribute(this.nameParts[0]), 1);
	}

	/**
	 * Recursively get bean property.
	 *
	 * @param bean The bean.
	 * @param namePartInd Index of the bean property name.
	 *
	 * @return The property value, or {@code null}.
	 *
	 * @throws ServletException If an error happens.
	 */
	private Object getBeanProperty(final Object bean, final int namePartInd)
		throws ServletException {

		if (bean == null)
			return null;

		if (namePartInd >= this.nameParts.length)
			return bean;

		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());

			final String propName = this.nameParts[namePartInd];
			for (final PropertyDescriptor propDesc :
				beanInfo.getPropertyDescriptors()) {
				if (propDesc.getName().equals(propName))
					return this.getBeanProperty(
							propDesc.getReadMethod().invoke(bean),
							namePartInd + 1);
			}

			return null;

		} catch (final IntrospectionException e) {
			throw new ServletException(
					"Error introspecting model component bean.", e);
		} catch (final ReflectiveOperationException e) {
			throw new ServletException(
					"Error getting model component bean property.", e);
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return this.name;
	}
}
