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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.UnavailableException;
import javax.validation.ValidatorFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.api.Routes;
import com.boylesoftware.web.impl.DefaultEntityManagerFactoryProvider;
import com.boylesoftware.web.impl.DefaultValidatorFactoryProvider;
import com.boylesoftware.web.impl.FixedThreadPoolExecutorServiceProvider;
import com.boylesoftware.web.impl.RequestUserLocaleFinderProvider;
import com.boylesoftware.web.spi.AuthenticationService;
import com.boylesoftware.web.spi.AuthenticationServiceProvider;
import com.boylesoftware.web.spi.EntityManagerFactoryProvider;
import com.boylesoftware.web.spi.ExecutorServiceProvider;
import com.boylesoftware.web.spi.RouterConfiguration;
import com.boylesoftware.web.spi.RouterConfigurationProvider;
import com.boylesoftware.web.spi.UserLocaleFinderProvider;
import com.boylesoftware.web.spi.ValidatorFactoryProvider;


/**
 * Base class for the custom application object. Custom web-application must
 * provide a concrete subclass of this base class and register it as a servlet
 * context listener.
 *
 * @author Lev Himmelfarb
 */
public abstract class AbstractWebApplication
	implements ApplicationConfiguration, ServletContextListener {

	/**
	 * Name of servlet context attribute used to store the web application
	 * object.
	 */
	private static final String WEBAPP_ATTNAME =
		(AbstractWebApplication.class).getName() + ".WEBAPP";


	/**
	 * Servlet context.
	 */
	protected ServletContext servletContext;

	/**
	 * The HTTP port.
	 */
	@Resource(name="httpPort")
	private int httpPort;

	/**
	 * The HTTPS port.
	 */
	@Resource(name="httpsPort")
	private int httpsPort;

	/**
	 * Application configuration properties.
	 */
	protected final Map<String, Object> configProperties =
		new ConcurrentHashMap<>();

	/**
	 * Application services.
	 */
	protected final ApplicationServices services =
		new ApplicationServices(this);

	/**
	 * The router configuration.
	 */
	private RouterConfiguration routerConfiguration;

	/**
	 * Executor service used to asynchronously process requests.
	 */
	private ExecutorService executors;


	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent sce) {

		this.servletContext = sce.getServletContext();

		sce.getServletContext().setAttribute(WEBAPP_ATTNAME, this);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {

		sce.getServletContext().removeAttribute(WEBAPP_ATTNAME);

		this.servletContext = null;
	}


	/**
	 * Initialize the web-application. Called by the framework just before the
	 * application is made available in the container.
	 *
	 * @throws UnavailableException If application cannot be initialized.
	 * Throwing the exception makes the application fail to start.
	 */
	void onRouterInit()
		throws UnavailableException {

		// get the log
		final Log log = LogFactory.getLog(AbstractWebApplication.class);
		log.debug("initializing the web-application");

		// initialize the application
		boolean success = false;
		try {

			// create configuration
			log.debug("creating configuration");
			this.configure(this.configProperties);

			// get the authenticator
			final ServletContext sc = this.servletContext;
			log.debug("creating authenticator");
			this.services.setAuthenticationService(
					this.getAuthenticationServiceProvider()
					.getAuthenticationService(sc, this));

			// get validator factory
			log.debug("creating validator factory");
			this.services.setValidatorFactory(
					this.getValidatorFactoryProvider()
					.getValidatorFactory(sc, this));

			// get user locale finder
			log.debug("creating user locale finder");
			this.services.setUserLocaleFinder(
					this.getUserLocaleFinderProvider()
					.getUserLocaleFinder(sc, this));

			// create entity manager factory
			log.debug("creating persistence manager factory");
			this.services.setEntityManagerFactory(
					this.getEntityManagerFactoryProvider()
					.getEntityManagerFactory(sc, this));

			// get JavaMail session from the JNDI
			log.debug("attempting to find JavaMail session in the JNDI");
			try {
				final InitialContext jndi = new InitialContext();
				try {
					this.services.setMailSession((Session) jndi.lookup(
							this.getConfigProperty(MAIL_SESSION_JNDI_NAME,
									String.class,
									ApplicationServices.
										DEFAULT_MAIL_SESSION_JNDI_NAME)));
				} catch (final NameNotFoundException e) {
					log.debug("no JavaMail session in the JNDI, JavaMail will" +
							" be unavailable", e);
				}
			} catch (final NamingException e) {
				log.error("Error accessing JNDI.", e);
			}

			// get the router configuration
			log.debug("creating routes configuration");
			this.routerConfiguration =
				this.getRouterConfigurationProvider()
				.getRouterConfiguration(sc, this, this.services);

			// initialize custom application
			log.debug("initializing custom application");
			this.init();

			// get the executor service
			log.debug("creating request processing executor service");
			this.executors =
				this.getExecutorServiceProvider().getExecutorService(sc, this);

			// done
			log.debug("initialized successfully");
			success = true;

		} finally {
			if (!success) {
				log.debug("initialization error");
				this.cleanup();
			}
		}
	}

	/**
	 * Destroy the web-application. Called by the framework right after the
	 * application is made unavailable in the container.
	 */
	void onRouterDestroy() {

		this.cleanup();
	}

	/**
	 * Clean-up the application object.
	 */
	private void cleanup() {

		// get the log
		final Log log = LogFactory.getLog(AbstractWebApplication.class);
		log.debug("destroying the web-application");

		// shutdown the executors
		if (this.executors != null) {
			log.debug("shutting down the request processing executors...");
			this.executors.shutdown();
			try {
				boolean success = true;
				if (!this.executors.awaitTermination(30, TimeUnit.SECONDS)) {
					log.warn("could not shutdown the request processing" +
							" executors in 30 seconds, trying to force" +
							" shutdown...");
					this.executors.shutdownNow();
					if (!this.executors.awaitTermination(30,
							TimeUnit.SECONDS)) {
						log.error("could not shutdown the request processing" +
								" executors");
						success = false;
					}
				}
				if (success)
					log.debug("request processing executors shut down");
			} catch (final InterruptedException e) {
				log.warn("waiting for the request processing executors to" +
						" shutdown was interrupted");
				this.executors.shutdownNow();
				Thread.currentThread().interrupt();
			} finally {
				this.executors = null;
			}
		}

		// destroy custom application
		log.debug("destroying custom application");
		try {
			this.destroy();
		} catch (final Exception e) {
			log.error("error destroying custom application", e);
		}

		// forget the router configuration
		this.routerConfiguration = null;

		// close and forget the entity manager factory
		final EntityManagerFactory emf =
			this.services.getEntityManagerFactory();
		if (emf != null) {
			this.services.setEntityManagerFactory(null);
			try {
				log.debug("closing persistence manager factory");
				emf.close();
			} catch (final Exception e) {
				log.error("error shutting down the application", e);
			}
		}

		// forget user locale finder
		this.services.setUserLocaleFinder(null);

		// close and forget the validator factory
		final ValidatorFactory vf = this.services.getValidatorFactory();
		if (vf != null) {
			this.services.setValidatorFactory(null);
			try {
				log.debug("closing validator factory");
				vf.close();
			} catch (final Exception e) {
				log.error("error shutting down the application", e);
			}
		}

		// forget the authentication service
		this.services.setAuthenticationService(null);
	}

	/**
	 * Create application configuration properties. The method is called during
	 * the application initialization before the {@link #init()}. The
	 * configuration properties are made available during the application
	 * runtime via the {@link #getConfigProperty(String, Class, Object)} method.
	 *
	 * <p>Can be overridden by the subclass. The default implementation does
	 * nothing.
	 *
	 * @param config The configuration properties. The implementation can add
	 * configuration properties to this map.
	 *
	 * @throws UnavailableException If an error happens. Throwing this exception
	 * will cause the application to fail to start.
	 */
	@SuppressWarnings("unused")
	protected void configure(final Map<String, Object> config)
		throws UnavailableException {

		// nothing
	}

	/**
	 * Initialize custom application.
	 *
	 * <p>Can be overridden by the subclass. Default implementation does
	 * nothing.
	 *
	 * @throws UnavailableException If an error happens. Throwing this exception
	 * will cause the application to fail to start.
	 */
	@SuppressWarnings("unused")
	protected void init()
		throws UnavailableException {

		// nothing
	}

	/**
	 * Destroy custom application. Can be overridden by the subclass. Default
	 * implementation does nothing.
	 */
	protected void destroy() {

		// nothing
	}


	/**
	 * Get web-application object for the specified servlet context. The method
	 * can be used in custom application components that run outside controllers
	 * (controllers can get the application object as a controller method
	 * argument), such as JSP views.
	 *
	 * @param sc The servlet context.
	 *
	 * @return The web-application object.
	 *
	 * @throws UnavailableException If web-application object is not available
	 * for the specified servlet context.
	 */
	public static AbstractWebApplication getApplication(final ServletContext sc)
		throws UnavailableException {

		final AbstractWebApplication webapp =
			(AbstractWebApplication) sc.getAttribute(WEBAPP_ATTNAME);
		if (webapp == null)
			throw new UnavailableException(
					"WebApplication listener has not been configured.");

		return webapp;
	}


	/**
	 * Get application's HTTPS port.
	 *
	 * @return The HTTPS port.
	 */
	public int getHTTPSPort() {

		return this.httpsPort;
	}

	/**
	 * Get application's plan HTTP port.
	 *
	 * @return The HTTP port.
	 */
	public int getHTTPPort() {

		return this.httpPort;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.ApplicationConfiguration#getConfigProperty(java.lang.String, java.lang.Class, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getConfigProperty(final String propName,
			final Class<T> propClass, final T defaultValue) {

		final Object valObj = this.configProperties.get(propName);
		if (valObj == null)
			return defaultValue;

		if (propClass.isAssignableFrom(valObj.getClass()))
			return (T) valObj;

		if (valObj instanceof String) {
			if ((String.class).isAssignableFrom(propClass))
				return (T) valObj;
			final String valStr = (String) valObj;
			if ((Integer.class).isAssignableFrom(propClass)) {
				final T val = (T) Integer.valueOf(valStr);
				this.configProperties.put(propName, val);
				return val;
			}
			if ((Long.class).isAssignableFrom(propClass)) {
				final T val = (T) Long.valueOf(valStr);
				this.configProperties.put(propName, val);
				return val;
			}
			if ((Boolean.class).isAssignableFrom(propClass)) {
				final T val = (T) Boolean.valueOf(valStr);
				this.configProperties.put(propName, val);
				return val;
			}
			throw new IllegalArgumentException(
					"Unsupported application configuration property type " +
					propClass.getName() + ".");
		}

		throw new RuntimeException("Application configuration property " +
				propName + " has unexpected type " +
				valObj.getClass().getName() + ".");
	}

	/**
	 * Get router configuration API.
	 *
	 * @return The router configuration API.
	 */
	public Routes getRoutes() {

		return this.routerConfiguration.getRoutes();
	}


	/**
	 * Get entity manager factory.
	 *
	 * @return Entity manager factory.
	 */
	EntityManagerFactory getEntityManagerFactory() {

		return this.services.getEntityManagerFactory();
	}

	/**
	 * Get authentication service.
	 *
	 * @return Authentication service.
	 */
	AuthenticationService<?> getAuthenticationService() {

		return this.services.getAuthenticationService();
	}

	/**
	 * Get router configuration.
	 *
	 * @return The router configuration.
	 */
	RouterConfiguration getRouterConfiguration() {

		return this.routerConfiguration;
	}

	/**
	 * Get executor service for asynchronous request processing.
	 *
	 * @return The executor service.
	 */
	ExecutorService getExecutorService() {

		return this.executors;
	}


	/**
	 * Get provider of the executor service used to asynchronously process
	 * requests. The method is called once during the application
	 * initialization.
	 *
	 * <p>The default implementation returns
	 * {@link FixedThreadPoolExecutorServiceProvider}.
	 *
	 * @return The executor service provider.
	 */
	protected ExecutorServiceProvider getExecutorServiceProvider() {

		return new FixedThreadPoolExecutorServiceProvider();
	}

	/**
	 * Get authentication service provider. The method is called once during the
	 * application initialization.
	 *
	 * @return The authentication service provider.
	 */
	protected abstract AuthenticationServiceProvider<?>
	getAuthenticationServiceProvider();

	/**
	 * Get entity manager factory provider. The method is called once during the
	 * application initialization.
	 *
	 * <p>The default implementation returns
	 * {@link DefaultEntityManagerFactoryProvider}.
	 *
	 * @return Entity manager factory provider.
	 */
	protected EntityManagerFactoryProvider getEntityManagerFactoryProvider() {

		return new DefaultEntityManagerFactoryProvider();
	}

	/**
	 * Get validator factory provider. The method is called once during the
	 * application initialization.
	 *
	 * <p>The default implementation returns
	 * {@link DefaultValidatorFactoryProvider}.
	 *
	 * @return Validator factory provider.
	 */
	protected ValidatorFactoryProvider getValidatorFactoryProvider() {

		return new DefaultValidatorFactoryProvider();
	}

	/**
	 * Get user locale finder provider. The method is called once during the
	 * application initialization.
	 *
	 * <p>The default implementation returns
	 * {@link RequestUserLocaleFinderProvider}.
	 *
	 * @return User locale finder provider.
	 */
	protected UserLocaleFinderProvider getUserLocaleFinderProvider() {

		return new RequestUserLocaleFinderProvider();
	}

	/**
	 * Get routes provider. The method is called once during the application
	 * initialization.
	 *
	 * @return Routes provider.
	 */
	protected abstract RouterConfigurationProvider
	getRouterConfigurationProvider();
}
