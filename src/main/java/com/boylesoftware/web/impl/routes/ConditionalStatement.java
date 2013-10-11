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
 * Implementation of the script "if/else" statement.
 *
 * @author Lev Himmelfarb
 */
class ConditionalStatement
	implements Script {

	/**
	 * The condition.
	 */
	private final Condition condition;

	/**
	 * The "if" script.
	 */
	private final Script ifScript;

	/**
	 * The "else" script.
	 */
	private final Script elseScript;


	/**
	 * Create new statement.
	 *
	 * @param condition The condition.
	 * @param ifScript Subscript to execute if the condition is true.
	 * @param elseScript Subscript to execute if the condition is false, or
	 * {@code null}.
	 */
	ConditionalStatement(final Condition condition, final Script ifScript,
			final Script elseScript) {

		this.condition = condition;
		this.ifScript = ifScript;
		this.elseScript = elseScript;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.Script#execute(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public void execute(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException, ServletException {

		if (this.condition.isTrue(request, em))
			this.ifScript.execute(request, em);
		else if (this.elseScript != null)
			this.elseScript.execute(request, em);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder(256);
		sb.append("if (").append(this.condition).append(") ")
			.append(this.ifScript);
		if (this.elseScript != null)
			sb.append(" else ").append(this.elseScript);

		return sb.toString();
	}
}
