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

import com.boylesoftware.web.BadRequestException;
import com.boylesoftware.web.RequestedResourceException;
import com.boylesoftware.web.spi.Script;


/**
 * Implementation of the "abort if/unless" script statement.
 *
 * @author Lev Himmelfarb
 */
class AbortStatement
	implements Script {

	/**
	 * Execute if condition evaluates to this value.
	 */
	private final boolean expectedConditionResult;

	/**
	 * Condition to evaluate.
	 */
	private final Condition condition;


	/**
	 * Create new statement.
	 *
	 * @param expectedConditionResult Execute if condition evaluates to this
	 * value. That is {@code true} for "abort if" and {@code false} for "abort
	 * unless".
	 * @param condition Condition to evaluate.
	 */
	AbortStatement(final boolean expectedConditionResult,
			final Condition condition) {

		this.expectedConditionResult = expectedConditionResult;
		this.condition = condition;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.Script#execute(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public void execute(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		if (this.condition.isTrue(request, em) == this.expectedConditionResult)
			throw new BadRequestException();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder(256);
		sb.append("abort ");
		if (this.expectedConditionResult == true)
			sb.append("if");
		else
			sb.append("unless");
		sb.append(" (").append(this.condition).append(")");

		return sb.toString();
	}
}
