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
package com.boylesoftware.web.impl;

import javax.servlet.http.HttpServletRequest;

import com.boylesoftware.web.api.RouteURIBuilder;
import com.boylesoftware.web.api.Routes;
import com.boylesoftware.web.api.Routes.URIType;
import com.boylesoftware.web.util.pool.AbstractPoolable;
import com.boylesoftware.web.util.pool.FastPool;


/**
 * {@link RouteURIBuilder} implementation.
 *
 * @author Lev Himmelfarb
 */
class RouteURIBuilderImpl
	extends AbstractPoolable
	implements RouteURIBuilder {

	/**
	 * The routes.
	 */
	private final Routes routes;

	/**
	 * The request.
	 */
	private HttpServletRequest request;

	/**
	 * The route id.
	 */
	private String routeId;

	/**
	 * URI type.
	 */
	private URIType type;


	/**
	 * Create new builder.
	 *
	 * @param pool Reference to the owning pool.
	 * @param pooledObjectId Pooled object id.
	 * @param routes The routes configuration.
	 */
	RouteURIBuilderImpl(final FastPool<RouteURIBuilderImpl> pool,
			final int pooledObjectId, final Routes routes) {
		super(pool, pooledObjectId);

		this.routes = routes;
	}


	/**
	 * Initialize the builder.
	 *
	 * @param request The request.
	 * @param routeId Route id.
	 * @param type URI type.
	 *
	 * @return Itself.
	 */
	RouteURIBuilderImpl init(final HttpServletRequest request,
			final String routeId, final URIType type) {

		this.request = request;
		this.routeId = routeId;
		this.type = type;

		return this;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.RouteURIBuilder#getURI(java.lang.String[])
	 */
	@Override
	public String getURI(String... params) {

		return this.routes.getRouteURI(this.request, this.routeId, this.type,
				params);
	}
}
