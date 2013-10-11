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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import com.boylesoftware.web.RequestedResourceException;
import com.boylesoftware.web.spi.Script;


/**
 * Script implementation that executes a sequence of nested scripts.
 *
 * @author Lev Himmelfarb
 */
class CombinedScript
	implements Script {

	/**
	 * Nested subscripts.
	 */
	private final List<Script> subscripts = new ArrayList<>();


	/**
	 * Add subscript.
	 *
	 * @param subscript The subscript.
	 */
	void addSubscript(final Script subscript) {

		this.subscripts.add(subscript);
	}

	/**
	 * Tell if the script has no nested subscripts.
	 *
	 * @return {@code true} if the script is empty.
	 */
	boolean isEmpty() {

		return this.subscripts.isEmpty();
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.Script#execute(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManager)
	 */
	@Override
	public void execute(final HttpServletRequest request,
			final EntityManager em)
		throws RequestedResourceException {

		for (final Script subscript : this.subscripts)
			subscript.execute(request, em);
	}
}
