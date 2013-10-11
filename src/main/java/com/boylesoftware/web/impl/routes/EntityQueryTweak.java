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
 * Operation on a {@link EntityQueryValueExpression}.
 *
 * @author Lev himmelfarb
 */
interface EntityQueryTweak {

	/**
	 * Apply the tweak to the specified query.
	 *
	 * @param query The query.
	 * @param request The HTTP request.
	 * @param em Entity manager to use to access persistent objects.
	 *
	 * @throws RequestedResourceException If there is a problem with the
	 * request.
	 * @throws ServletException If an application error happens.
	 */
	void apply(Query query, HttpServletRequest request, EntityManager em)
		throws RequestedResourceException, ServletException;
}
