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
import javax.servlet.http.HttpServletRequest;


/**
 * Implementation of the new entity expression.
 *
 * @author Lev Himmelfarb
 */
class NewEntityValueExpression
	implements ValueExpression {

	/**
	 * Entity class.
	 */
	private final Class<?> entityClass;


	/**
	 * Create new expression.
	 *
	 * @param entityClass Entity class.
	 */
	NewEntityValueExpression(final Class<?> entityClass) {

		this.entityClass = entityClass;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.ValueExpression#getValue(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public Object getValue(final HttpServletRequest request,
			final EntityManager em)
		throws ServletException {

		try {
			return this.entityClass.newInstance();
		} catch (final ReflectiveOperationException e) {
			throw new ServletException("Error instatiating entity.", e);
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "new " + this.entityClass.getName();
	}
}
