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
import javax.servlet.http.HttpServletRequest;


/**
 * Implementation of the script condition that checks the request HTTP method.
 *
 * @author Lev Himmelfarb
 */
class RequestMethodCondition
	implements Condition {

	/**
	 * The method.
	 */
	private final String method;


	/**
	 * Create new condition.
	 *
	 * @param method The method.
	 */
	RequestMethodCondition(final String method) {

		this.method = method;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.Condition#isTrue(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public boolean isTrue(final HttpServletRequest request,
			final EntityManager em) {

		return request.getMethod().equalsIgnoreCase(this.method);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return this.method;
	}
}
