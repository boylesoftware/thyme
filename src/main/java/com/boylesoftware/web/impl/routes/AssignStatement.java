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
import com.boylesoftware.web.spi.Script;


/**
 * Implementation of the assignment statement used in a script.
 *
 * @author Lev Himmelfarb
 */
class AssignStatement
	implements Script {

	/**
	 * Model component name.
	 */
	private final String name;

	/**
	 * Value expression.
	 */
	private final ValueExpression valueExpression;


	/**
	 * Create new statement.
	 *
	 * @param name Model component name.
	 * @param valueExpression Value expression.
	 */
	public AssignStatement(final String name,
			final ValueExpression valueExpression) {

		this.name = name;
		this.valueExpression = valueExpression;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.Script#execute(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public void execute(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		final Object v = this.valueExpression.getValue(request, em);
		if (v != null)
			request.setAttribute(this.name, v);
		else
			request.removeAttribute(this.name);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return this.name + " = " + this.valueExpression;
	}
}
