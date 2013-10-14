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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import com.boylesoftware.web.ApplicationServices;
import com.boylesoftware.web.impl.AbstractRouterConfiguration;
import com.boylesoftware.web.impl.RoutesBuilder;
import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.ViewSender;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.logging.LogFactory;


/**
 * Router configuration provider implementation that loads the configuration
 * from {@value #ROUTES_PATH} file.
 *
 * @author Lev Himmelfarb
 */
public class RoutesRouterConfiguration
	extends AbstractRouterConfiguration {

	/**
	 * Context-relative path to the router configuration file.
	 */
	public static final String ROUTES_PATH = "/WEB-INF/routes";


	/**
	 * Create new provider.
	 *
	 * @param sc Servlet context.
	 * @param appServices The application services.
	 * @param argHandlerProvider Controller method argument handler provider.
	 * @param viewSender View sender.
	 *
	 * @throws UnavailableException If configuration is incorrect.
	 */
	public RoutesRouterConfiguration(final ServletContext sc,
			final ApplicationServices appServices,
			final ControllerMethodArgHandlerProvider argHandlerProvider,
			final ViewSender viewSender)
		throws UnavailableException {
		super(sc, appServices, argHandlerProvider, viewSender);
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.AbstractRouterConfiguration#buildRoutes(javax.servlet.ServletContext, com.boylesoftware.web.impl.RoutesBuilder)
	 */
	@Override
	protected void buildRoutes(final ServletContext sc,
			final RoutesBuilder routes)
		throws UnavailableException {

		try (final InputStream in = sc.getResourceAsStream(ROUTES_PATH)) {

			if (in == null)
				throw new UnavailableException("No " + ROUTES_PATH +
						" found in the web-application.");

			final RoutesParser parser = new RoutesParser(new CommonTokenStream(
					new RoutesLexer(new ANTLRInputStream(in))));
			parser.setErrorHandler(new BailErrorStrategy());
			parser.setRoutesBuilder(routes);
			try {
				parser.config();
			} catch (final Exception e) {
				this.error(e);
			}

		} catch (final IOException e) {
			this.error(e);
		}
	}

	/**
	 * Convert exception to {@link UnavailableException} and throw it.
	 *
	 * @param e Original exception.
	 *
	 * @throws UnavailableException Always.
	 */
	private void error(final Exception e)
		throws UnavailableException {

		LogFactory.getLog(this.getClass()).error(
				"error loading router configuration", e);
		throw new UnavailableException("Error loading router configuration: " +
				e.getMessage());
	}
}
