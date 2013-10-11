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

import com.boylesoftware.web.NotFoundException;
import com.boylesoftware.web.RequestedResourceException;


/**
 * Implementation of the entity by id expression.
 *
 * @author Lev Himmelfarb
 */
class EntityValueExpression
	implements ValueExpression {

	/**
	 * Entity class.
	 */
	private final Class<?> entityClass;

	/**
	 * Entity id expression.
	 */
	private final ValueExpression entityIdExpr;


	/**
	 * Create new expression.
	 *
	 * @param entityClass Entity class.
	 * @param entityIdExpr Entity id expression.
	 */
	EntityValueExpression(final Class<?> entityClass,
			final ValueExpression entityIdExpr) {

		this.entityClass = entityClass;
		this.entityIdExpr = entityIdExpr;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.ValueExpression#getValue(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public Object getValue(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		final Object res =
			em.find(this.entityClass, this.entityIdExpr.getValue(request, em));

		if (res == null)
			throw new NotFoundException();

		return res;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return this.entityClass.getName() + "(" + this.entityIdExpr + ")";
	}
}
