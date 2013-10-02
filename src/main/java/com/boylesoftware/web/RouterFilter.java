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
package com.boylesoftware.web;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The router filter.
 *
 * @author Lev Himmelfarb
 */
@WebFilter(
		filterName="RouterFilter",
		asyncSupported=true,
		dispatcherTypes={ DispatcherType.REQUEST, DispatcherType.ASYNC },
		urlPatterns={ "/*" }
)
public class RouterFilter
	implements Filter {

	/**
	 * The router.
	 */
	private final Router router = new Router();

	/**
	 * The web-application.
	 */
	private AbstractWebApplication webapp;


	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(final FilterConfig filterConfig)
		throws UnavailableException {

		this.webapp = AbstractWebApplication.getApplication(
				filterConfig.getServletContext());

		this.webapp.onRouterInit();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {

		this.webapp.onRouterDestroy();

		this.webapp = null;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(final ServletRequest request,
			final ServletResponse response, final FilterChain chain)
		throws IOException, ServletException {

		if (!this.router.route(this.webapp, (HttpServletRequest) request,
				(HttpServletResponse) response))
			chain.doFilter(request, response);
	}
}
