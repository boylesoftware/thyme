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
import com.boylesoftware.web.impl.AbstractRouterConfigurationProvider;
import com.boylesoftware.web.impl.StandardControllerMethodArgHandlerProvider;
import com.boylesoftware.web.impl.view.DispatchViewSenderProvider;
import com.boylesoftware.web.impl.view.MultiplexViewSenderProvider;
import com.boylesoftware.web.spi.ViewSenderProvider;

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
public class RoutesRouterConfigurationProvider
	extends AbstractRouterConfigurationProvider {

	/**
	 * Context-relative path to the router configuration file.
	 */
	public static final String ROUTES_PATH = "/WEB-INF/routes";


	/**
	 * Configuration file parser.
	 */
	private RoutesParser parser;

	/**
	 * Login page URI.
	 */
	private String loginPageURI;

	/**
	 * Protected pages URI pattern.
	 */
	private String protectedURIPattern;

	/**
	 * Public pages URI pattern.
	 */
	private String publicURIPattern;


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

		try (final InputStream in = sc.getResourceAsStream(ROUTES_PATH)) {

			if (in == null)
				throw new UnavailableException("No " + ROUTES_PATH +
						" found in the web-application.");

			this.parser = new RoutesParser(new CommonTokenStream(
					new RoutesLexer(new ANTLRInputStream(in))));
			this.parser.setErrorHandler(new BailErrorStrategy());
			this.parser.setProvider(this);
			try {
				this.parser.config();
			} catch (final Exception e) {
				this.error(e);
			}

			this.loginPageURI = this.parser.getLoginPageURI();
			this.protectedURIPattern = this.parser.getProtectedURIPattern();
			this.publicURIPattern = this.parser.getPublicURIPattern();

		} catch (final IOException e) {
			this.error(e);
		} finally {
			this.parser = null;
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

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.AbstractRouterConfigurationProvider#getLoginPageURI(javax.servlet.ServletContext)
	 */
	@Override
	protected String getLoginPageURI(final ServletContext sc) {

		return this.loginPageURI;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.AbstractRouterConfigurationProvider#getProtectedURIPattern(javax.servlet.ServletContext)
	 */
	@Override
	protected String getProtectedURIPattern(final ServletContext sc) {

		return this.protectedURIPattern;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.AbstractRouterConfigurationProvider#getPublicURIPattern(javax.servlet.ServletContext)
	 */
	@Override
	protected String getPublicURIPattern(final ServletContext sc) {

		return this.publicURIPattern;
	}
}
