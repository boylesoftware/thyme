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

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;

import com.boylesoftware.web.ApplicationConfiguration;
import com.boylesoftware.web.spi.EntityManagerFactoryProvider;


/**
 * Implementation of {@link EntityManagerFactoryProvider} that uses
 * {@link Persistence#createEntityManagerFactory(String)} method to create the
 * entity manager factory. The persistence unit name is taken from the
 * {@link ApplicationConfiguration#PU_NAME} application configuration property
 * with default name {@value #DEFAULT_PU_NAME}.
 *
 * @author Lev Himmelfarb
 */
public class DefaultEntityManagerFactoryProvider
	implements EntityManagerFactoryProvider {

	/**
	 * Default persistence unit name.
	 */
	public static final String DEFAULT_PU_NAME = "pu";


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.EntityManagerFactoryProvider#getEntityManagerFactory(javax.servlet.ServletContext, com.boylesoftware.web.ApplicationConfiguration)
	 */
	@Override
	public EntityManagerFactory getEntityManagerFactory(
			final ServletContext sc, final ApplicationConfiguration config) {

		return Persistence.createEntityManagerFactory(
				config.getConfigProperty(ApplicationConfiguration.PU_NAME,
						String.class, DEFAULT_PU_NAME));
	}
}
