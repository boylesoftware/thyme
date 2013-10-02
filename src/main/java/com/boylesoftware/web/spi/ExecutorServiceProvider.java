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

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import com.boylesoftware.web.ApplicationConfiguration;


/**
 * Provider for the executor service used to asynchronously process requests.
 *
 * @author Lev Himmelfarb
 */
public interface ExecutorServiceProvider {

	/**
	 * Get executor service. This method is called once during the application
	 * initialization. The executor service is automatically shut down by the
	 * framework when the application goes down.
	 *
	 * @param sc Servlet context.
	 * @param config Application configuration.
	 *
	 * @return The executor service.
	 *
	 * @throws UnavailableException If the executor service is unavailable.
	 * Throwing this exception makes the web-application fail to start.
	 */
	ExecutorService getExecutorService(ServletContext sc,
			ApplicationConfiguration config)
		throws UnavailableException;
}
