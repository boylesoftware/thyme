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
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.boylesoftware.web.RequestedResourceException;


/**
 * Set named parameter on the query.
 *
 * @author Lev Himmelfarb
 */
class PositionalParamEntityQueryTweak
	implements EntityQueryTweak {

	/**
	 * Parameter position.
	 */
	private final int paramPos;

	/**
	 * Number of results expression.
	 */
	private final ValueExpression valueExpr;


	/**
	 * Create new tweak.
	 *
	 * @param paramPos Parameter position, starting from 1.
	 * @param valueExpr Parameter value expression.
	 */
	PositionalParamEntityQueryTweak(final int paramPos,
			final ValueExpression valueExpr) {

		this.paramPos = paramPos;
		this.valueExpr = valueExpr;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.routes.EntityQueryTweak#apply(javax.persistence.Query, javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public void apply(final Query query, final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		query.setParameter(this.paramPos,
				this.valueExpr.getValue(request, em));
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "setParameter(" + this.paramPos + ", " + this.valueExpr + ")";
	}
}
