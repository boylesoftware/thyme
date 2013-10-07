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

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import com.boylesoftware.web.ApplicationServices;
import com.boylesoftware.web.impl.AbstractRouterConfigurationProvider;
import com.boylesoftware.web.impl.StandardControllerMethodArgHandlerProvider;
import com.boylesoftware.web.impl.view.DispatchViewSenderProvider;
import com.boylesoftware.web.impl.view.MultiplexViewSenderProvider;
import com.boylesoftware.web.spi.ViewSenderProvider;


/**
 * Router configuration provider implementation that loads the configuration
 * from {@value #ROUTES_PATH} file.
 *
 * @author Lev Himmelfarb
 */
public class RoutesRouterConfigurationProvider
	extends AbstractRouterConfigurationProvider {

	/**
	 * Context-relative path to the router configuration file.
	 */
	public static final String ROUTES_PATH = "/WEB-INF/routes";


	/**
	 * Create new provider.
	 *
	 * @param appServices Application services.
	 */
	public RoutesRouterConfigurationProvider(
			final ApplicationServices appServices) {
		super(new StandardControllerMethodArgHandlerProvider(appServices));
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.AbstractRouterConfigurationProvider#getViewSenderProvider(javax.servlet.ServletContext)
	 */
	@Override
	protected ViewSenderProvider getViewSenderProvider(final ServletContext sc)
		throws UnavailableException {

		final MultiplexViewSenderProvider provider =
			new MultiplexViewSenderProvider();
		provider.addPattern(sc, ".*\\.jspx?", new DispatchViewSenderProvider());

		return provider;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.AbstractRouterConfigurationProvider#buildRoutes(javax.servlet.ServletContext)
	 */
	@Override
	protected void buildRoutes(final ServletContext sc)
		throws UnavailableException {

		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.AbstractRouterConfigurationProvider#getLoginPageURI(javax.servlet.ServletContext)
	 */
	@Override
	protected String getLoginPageURI(final ServletContext sc)
		throws UnavailableException {

		// TODO Auto-generated method stub
		return null;
	}
}
