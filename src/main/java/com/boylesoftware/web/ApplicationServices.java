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

import javax.mail.Session;
import javax.persistence.EntityManagerFactory;
import javax.validation.ValidatorFactory;

import com.boylesoftware.web.spi.AuthenticationService;
import com.boylesoftware.web.spi.UserLocaleFinder;


/**
 * Encapsulates a number of essential application services.
 *
 * @author Lev Himmelfarb
 */
public class ApplicationServices {

	/**
	 * Default JNDI name of optional JavaMail session.
	 */
	public static final String DEFAULT_MAIL_SESSION_JNDI_NAME =
		"java:comp/env/mail/session";


	/**
	 * The application.
	 */
	private final AbstractWebApplication webapp;

	/**
	 * The authenticator.
	 */
	private AuthenticationService<?> authenticationService;

	/**
	 * The entity manager factory.
	 */
	private EntityManagerFactory emf;

	/**
	 * Validator factory.
	 */
	private ValidatorFactory validatorFactory;

	/**
	 * User locale finder.
	 */
	private UserLocaleFinder<?> userLocaleFinder;

	/**
	 * Optional JavaMail session.
	 */
	private Session mailSession;


	/**
	 * Create new services container.
	 *
	 * @param webapp The application.
	 */
	ApplicationServices(final AbstractWebApplication webapp) {

		this.webapp = webapp;
	}


	/**
	 * Get the application.
	 *
	 * @return The application.
	 */
	public AbstractWebApplication getApplication() {

		return this.webapp;
	}

	/**
	 * Get authentication service.
	 *
	 * @return The authentication service.
	 */
	public AuthenticationService<?> getAuthenticationService() {

		return this.authenticationService;
	}

	/**
	 * Set authentication service.
	 *
	 * @param authenticationService The authentication service.
	 */
	void setAuthenticationService(
			final AuthenticationService<?> authenticationService) {

		this.authenticationService = authenticationService;
	}

	/**
	 * Get entity manager factory.
	 *
	 * @return The entity manager factory.
	 */
	public EntityManagerFactory getEntityManagerFactory() {

		return this.emf;
	}

	/**
	 * Set entity manager factory.
	 *
	 * @param emf The entity manager factory.
	 */
	void setEntityManagerFactory(final EntityManagerFactory emf) {

		this.emf = emf;
	}

	/**
	 * Get validator factory used to validate user input.
	 *
	 * @return The validator factory.
	 */
	public ValidatorFactory getValidatorFactory() {

		return this.validatorFactory;
	}

	/**
	 * Set validator factory used to validate user input.
	 *
	 * @param validatorFactory The validator factory.
	 */
	void setValidatorFactory(final ValidatorFactory validatorFactory) {

		this.validatorFactory = validatorFactory;
	}

	/**
	 * Get user locale finder.
	 *
	 * @return User locale finder.
	 */
	public UserLocaleFinder<?> getUserLocaleFinder() {

		return this.userLocaleFinder;
	}

	/**
	 * Set user locale finder.
	 *
	 * @param userLocaleFinder User locale finder.
	 */
	void setUserLocaleFinder(final UserLocaleFinder<?> userLocaleFinder) {

		this.userLocaleFinder = userLocaleFinder;
	}

	/**
	 * Get optional JavaMail session. The JavaMail session, if configured, is
	 * taken from the JNDI. The JNDI name is specified by the
	 * {@link ApplicationConfiguration#MAIL_SESSION_JNDI_NAME} application
	 * configuration property. If property is not specified, the default is
	 * {@value #DEFAULT_MAIL_SESSION_JNDI_NAME}. If no JavaMail session can be
	 * found in the JNDI, it is assumed that the application does not use
	 * JavaMail to send e-mails and this method returns {@code null}.
	 *
	 * @return The session, or {@code null} if not configured.
	 */
	public Session getMailSession() {

		return this.mailSession;
	}

	/**
	 * Set JavaMail session.
	 *
	 * @param mailSession The session.
	 */
	void setMailSession(final Session mailSession) {

		this.mailSession = mailSession;
	}
}
