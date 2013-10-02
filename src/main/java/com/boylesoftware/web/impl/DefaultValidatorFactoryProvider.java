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

import javax.servlet.ServletContext;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import com.boylesoftware.web.ApplicationConfiguration;
import com.boylesoftware.web.spi.ValidatorFactoryProvider;


/**
 * {@link ValidatorFactoryProvider} implementation that uses
 * {@link Validation#buildDefaultValidatorFactory()} to create validator
 * factory.
 *
 * @author Lev Himmelfarb
 */
public class DefaultValidatorFactoryProvider
	implements ValidatorFactoryProvider {

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ValidatorFactoryProvider#getValidatorFactory(javax.servlet.ServletContext, com.boylesoftware.web.ApplicationConfiguration)
	 */
	@Override
	public ValidatorFactory getValidatorFactory(final ServletContext sc,
			final ApplicationConfiguration config) {

		return Validation.buildDefaultValidatorFactory();
	}
}
