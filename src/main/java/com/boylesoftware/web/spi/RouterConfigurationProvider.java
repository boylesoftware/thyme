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
package com.boylesoftware.web.spi;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import com.boylesoftware.web.ApplicationConfiguration;
import com.boylesoftware.web.ApplicationServices;


/**
 * Interface for the provider of the request routing configuration.
 *
 * @author Lev Himmelfarb
 */
public interface RouterConfigurationProvider {

	/**
	 * Get the routes configuration. This method is called once during the
	 * application initialization.
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
	RouterConfiguration getRouterConfiguration(ServletContext sc,
			ApplicationConfiguration config, ApplicationServices appServices)
		throws UnavailableException;
}
