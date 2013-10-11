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

import com.boylesoftware.web.RequestedResourceException;


/**
 * Implementation of the script condition that evaluates a value expression.
 *
 * @author Lev Himmelfarb
 */
class ValueCondition
	implements Condition {

	/**
	 * The value.
	 */
	private final ValueExpression value;


	/**
	 * Create new condition.
	 *
	 * @param value The value.
	 */
	ValueCondition(final ValueExpression value) {

		this.value = value;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.Condition#isTrue(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public boolean isTrue(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		final Object v = this.value.getValue(request, em);

		if (v == null)
			return false;

		if (v instanceof Boolean)
			return ((Boolean) v).booleanValue();

		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "(" + this.value + ")";
	}
}
