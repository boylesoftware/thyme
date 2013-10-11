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
 * Implementation of the "equals" script condition.
 *
 * @author Lev Himmelfarb
 */
class EqualsCondition
	implements Condition {

	/**
	 * Value 1.
	 */
	private final ValueExpression value1;

	/**
	 * Value 2.
	 */
	private final ValueExpression value2;


	/**
	 * Create new condition.
	 *
	 * @param value1 Value 1.
	 * @param value2 Value 2.
	 */
	EqualsCondition(final ValueExpression value1,
			final ValueExpression value2) {

		this.value1 = value1;
		this.value2 = value2;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.Condition#isTrue(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public boolean isTrue(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		final Object v1 = this.value1.getValue(request, em);
		final Object v2 = this.value2.getValue(request, em);

		return (v1 != null ? v1.equals(v2) : (v2 == null));
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "(" + this.value1 + " == " + this.value2 + ")";
	}
}
