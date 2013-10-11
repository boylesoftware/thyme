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
 * Implementation of the "or" script condition.
 *
 * @author Lev Himmelfarb
 */
class OrCondition
	implements Condition {

	/**
	 * Condition 1.
	 */
	private final Condition condition1;

	/**
	 * Condition 2.
	 */
	private final Condition condition2;


	/**
	 * Create new condition.
	 *
	 * @param condition1 Condition 1.
	 * @param condition2 Condition 2.
	 */
	OrCondition(final Condition condition1, final Condition condition2) {

		this.condition1 = condition1;
		this.condition2 = condition2;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.Condition#isTrue(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public boolean isTrue(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		return (this.condition1.isTrue(request, em) ||
				this.condition2.isTrue(request, em));
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "(" + this.condition1 + " | " + this.condition2 + ")";
	}
}
