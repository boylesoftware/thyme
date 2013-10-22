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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.UnavailableException;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.api.Routes;
import com.boylesoftware.web.impl.RequestUserLocaleFinder;
import com.boylesoftware.web.impl.StandardControllerMethodArgHandlerProvider;
import com.boylesoftware.web.impl.auth.LocalUserRecordsCache;
import com.boylesoftware.web.impl.auth.SessionlessAuthenticationService;
import com.boylesoftware.web.impl.routes.RoutesRouterConfiguration;
import com.boylesoftware.web.impl.view.DispatchViewSender;
import com.boylesoftware.web.impl.view.MultiplexViewSender;
import com.boylesoftware.web.spi.AuthenticationService;
import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.RouterConfiguration;
import com.boylesoftware.web.spi.UserLocaleFinder;
import com.boylesoftware.web.spi.UserRecordHandler;
import com.boylesoftware.web.spi.UserRecordsCache;
import com.boylesoftware.web.spi.ViewSender;


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
	 * Default number of threads.
	 */
	public static final int DEFAULT_ASYNC_THREADS = 10;

	/**
	 * Default persistence unit name.
	 */
	public static final String DEFAULT_PU_NAME = "pu";

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
					this.getAuthenticationService(sc, this));

			// get validator factory
			log.debug("creating validator factory");
			this.services.setValidatorFactory(
					this.getValidatorFactory(sc, this));

			// get user locale finder
			log.debug("creating user locale finder");
			this.services.setUserLocaleFinder(
					this.getUserLocaleFinder(sc, this));

			// create entity manager factory
			log.debug("creating persistence manager factory");
			this.services.setEntityManagerFactory(
					this.getEntityManagerFactory(sc, this));

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
				this.getRouterConfiguration(sc, this, this.services);

			// initialize custom application
			log.debug("initializing custom application");
			this.init();

			// get the executor service
			log.debug("creating request processing executor service");
			this.executors = this.getExecutorService(sc, this);

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
	 * Get executor service. This method is called once during the application
	 * initialization. The executor service is automatically shut down by the
	 * framework when the application goes down.
	 *
	 * <p>Default implementation returns a fixed size thread pool with number
	 * of threads specified by the
	 * {@link ApplicationConfiguration#ASYNC_THREADS} application configuration
	 * property. If the application configuration property is undefined, default
	 * number of threads is {@value #DEFAULT_ASYNC_THREADS}.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return The executor service.
	 *
	 * @throws UnavailableException If the executor service is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	@SuppressWarnings("unused")
	protected ExecutorService getExecutorService(final ServletContext sc,
			final ApplicationConfiguration config)
		throws UnavailableException {

		final ThreadGroup threadGroup = new ThreadGroup("AsyncExecutors");

		final int numThreads = config.getConfigProperty(
				ApplicationConfiguration.ASYNC_THREADS, Integer.class,
				Integer.valueOf(DEFAULT_ASYNC_THREADS)).intValue();

		return Executors.newFixedThreadPool(numThreads, new ThreadFactory() {

			private int nextThreadNum = 0;

			@Override
			public Thread newThread(final Runnable r) {

				final String threadName =
					"async-executor-" + (this.nextThreadNum++);

				LogFactory.getLog(this.getClass()).debug(
						"starting asynchronous request processing thread " +
								threadName);

				return new Thread(threadGroup, r, threadName);
			}
		});
	}

	/**
	 * Get the authentication service. This method is called once during the
	 * application initialization.
	 *
	 * <p>Default implementation returns a
	 * {@link SessionlessAuthenticationService}.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return The authenticator.
	 *
	 * @throws UnavailableException If authentication service is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected AuthenticationService<?> getAuthenticationService(
			final ServletContext sc, final ApplicationConfiguration config)
		throws UnavailableException {

		return new SessionlessAuthenticationService(
				this.getUserRecordHandler(sc, config),
				this.getUserRecordsCache(sc, config));
	}

	/**
	 * Get user record handler used by the authentication service. This method
	 * is called once during the application initialization.
	 *
	 * <p>Since there is no generic user record handler implementation, this
	 * method, unless overridden, throws an {@link UnavailableException}, so
	 * that if the application uses an authentication service that works with
	 * persistent user account records, it must override this method and provide
	 * an application-specific implementation of the user record handler.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return User record handler.
	 *
	 * @throws UnavailableException If user record handler is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	@SuppressWarnings("unused")
	protected UserRecordHandler<?> getUserRecordHandler(final ServletContext sc,
			final ApplicationConfiguration config)
		throws UnavailableException {

		throw new UnavailableException("Application uses user record" +
				" authentication service implementation, but user record" +
				" handler is not provided.");
	}

	/**
	 * Get user records cache implementation used by the authentication service.
	 * This method is called once during the application initialization.
	 *
	 * <p>Default implementation returns a {@link LocalUserRecordsCache}.
	 * <b>Note, that local cache does is not suitable for a distributed
	 * environment.</b>
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return User records cache implementation.
	 *
	 * @throws UnavailableException If user records cache implementation is
	 * unavailable. Throwing this exception makes the web-application fail to
	 * start.
	 */
	@SuppressWarnings("unused")
	protected UserRecordsCache<?> getUserRecordsCache(final ServletContext sc,
			final ApplicationConfiguration config)
		throws UnavailableException {

		return new LocalUserRecordsCache<>();
	}

	/**
	 * Get entity manager factory. This method is called once during the
	 * application initialization. The entity manager factory is automatically
	 * closed by the framework when the application shuts down.
	 *
	 * <p>Default implementation uses
	 * {@link Persistence#createEntityManagerFactory(String)} method to create
	 * the entity manager factory. The persistence unit name is taken from the
	 * {@link ApplicationConfiguration#PU_NAME} application configuration
	 * property with default name {@value #DEFAULT_PU_NAME}.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return Entity manager factory.
	 *
	 * @throws UnavailableException If entity manager factory is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	@SuppressWarnings("unused")
	protected EntityManagerFactory getEntityManagerFactory(
			final ServletContext sc, final ApplicationConfiguration config)
		throws UnavailableException {

		return Persistence.createEntityManagerFactory(
				config.getConfigProperty(ApplicationConfiguration.PU_NAME,
						String.class, DEFAULT_PU_NAME));
	}

	/**
	 * Get validator factory used for validating user input (such as submitted
	 * HTML forms). This method is called once during the application
	 * initialization.
	 *
	 * <p>Default implementation uses default validation provider (see
	 * {@link Validation#byDefaultProvider()} and optionally a custom
	 * {@link MessageInterpolator} returned by the
	 * {@link #getValidatorMessageInterpolator} method.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return Validator factory.
	 *
	 * @throws UnavailableException If validator factory is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	protected ValidatorFactory getValidatorFactory(final ServletContext sc,
			final ApplicationConfiguration config)
		throws UnavailableException {

		final MessageInterpolator messageInterpolator =
			this.getValidatorMessageInterpolator(sc, config);

		if (messageInterpolator == null)
			return Validation.buildDefaultValidatorFactory();

		return Validation
				.byDefaultProvider()
				.configure()
				.messageInterpolator(messageInterpolator)
				.buildValidatorFactory();
	}

	/**
	 * Get message interpolator for the user input validator. The method is
	 * called once during the application initialization.
	 *
	 * <p>Default implementation returns {@code null} to use the default
	 * message interpolator.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return Message interpolator, or {@code null} to use the default.
	 *
	 * @throws UnavailableException If an error happens getting the message
	 * interpolator. Throwing this exception makes the web-application fail to
	 * start.
	 */
	@SuppressWarnings("unused")
	protected MessageInterpolator getValidatorMessageInterpolator(
			final ServletContext sc, final ApplicationConfiguration config)
		throws UnavailableException {

		return null;
	}

	/**
	 * Get user locale finder. This method is called during the application
	 * initialization.
	 *
	 * <p>Default implementation returns {@link RequestUserLocaleFinder}.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return User locale finder.
	 *
	 * @throws UnavailableException If user locale finder is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	@SuppressWarnings("unused")
	protected UserLocaleFinder<?> getUserLocaleFinder(final ServletContext sc,
			final ApplicationConfiguration config)
		throws UnavailableException {

		return new RequestUserLocaleFinder();
	}

	/**
	 * Get the router configuration. This method is called once during the
	 * application initialization.
	 *
	 * <p>Default implementation returns {@link RoutesRouterConfiguration}
	 * configured with view sender and controller method argument handler
	 * provider returned by the {@link #getViewSender} and
	 * {@link #getControllerMethodArgHandlerProvider} methods.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 * @param appServices Application services.
	 *
	 * @return The routes.
	 *
	 * @throws UnavailableException If routing configuration is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	protected RouterConfiguration getRouterConfiguration(
			final ServletContext sc, final ApplicationConfiguration config,
			final ApplicationServices appServices)
		throws UnavailableException {

		return new RoutesRouterConfiguration(sc, appServices,
				this.getControllerMethodArgHandlerProvider(sc, config,
						appServices),
				this.getViewSender(sc, config, appServices));
	}

	/**
	 * Get view sender. This method is called once during the application
	 * initialization.
	 *
	 * <p>Default implementation returns a {@link MultiplexViewSender}
	 * configured with all view senders provided by the framework out-of-the-box
	 * mapped using corresponding template file extensions.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 * @param appServices Application services.
	 *
	 * @return View sender provider.
	 *
	 * @throws UnavailableException If view sender provider is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	@SuppressWarnings("unused")
	protected ViewSender getViewSender(final ServletContext sc,
			final ApplicationConfiguration config,
			final ApplicationServices appServices)
		throws UnavailableException {

		final MultiplexViewSender sender = new MultiplexViewSender();
		sender.addPattern(".*\\.jspx?", new DispatchViewSender());

		return sender;
	}

	/**
	 * Get controller method argument handlers provider. This method is called
	 * once during the application initialization.
	 *
	 * <p>Default implementation returns a
	 * {@link StandardControllerMethodArgHandlerProvider}.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 * @param appServices Application services.
	 *
	 * @return Controller method argument handlers provider.
	 *
	 * @throws UnavailableException If controller method argument handlers
	 * provider is unavailable. Throwing this exception makes the
	 * web-application fail to start.
	 */
	@SuppressWarnings("unused")
	protected ControllerMethodArgHandlerProvider
	getControllerMethodArgHandlerProvider(final ServletContext sc,
			final ApplicationConfiguration config,
			final ApplicationServices appServices)
		throws UnavailableException {

		return new StandardControllerMethodArgHandlerProvider(appServices);
	}
}
