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
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.boylesoftware.web.NotFoundException;
import com.boylesoftware.web.RequestedResourceException;


/**
 * Implementation of the entity query expression.
 *
 * @author Lev Himmelfarb
 */
class EntityQueryValueExpression
	implements ValueExpression {

	/**
	 * Entity class.
	 */
	private final Class<?> entityClass;

	/**
	 * Query name.
	 */
	private final String queryName;

	/**
	 * Query tweaks.
	 */
	private final EntityQueryTweak[] tweaks;

	/**
	 * Tells if list is requested instead of a single result.
	 */
	private final boolean listMode;


	/**
	 * Create new expression.
	 *
	 * @param entityClass Entity class.
	 * @param queryName Query name.
	 * @param tweaks Query tweaks.
	 * @param listMode {@code true} if list is requested instead of a single
	 * result.
	 */
	EntityQueryValueExpression(final Class<?> entityClass,
			final String queryName, final EntityQueryTweak[] tweaks,
			final boolean listMode) {

		this.entityClass = entityClass;
		this.queryName = queryName;
		this.tweaks = tweaks;
		this.listMode = listMode;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.ValueExpression#getValue(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public Object getValue(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		final TypedQuery<?> query =
			em.createNamedQuery(this.queryName, this.entityClass);

		for (final EntityQueryTweak tweak : this.tweaks)
			tweak.apply(query, request, em);

		if (this.listMode)
			return query.getResultList();

		try {
			return query.getSingleResult();
		} catch (final NoResultException | NonUniqueResultException e) {
			throw new NotFoundException();
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder(256);
		sb.append(this.entityClass.getName()).append(':')
			.append(this.queryName).append("()");
		for (final EntityQueryTweak tweak : this.tweaks)
			sb.append('.').append(tweak);
		if (this.listMode)
			sb.append(".list");

		return sb.toString();
	}
}
