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

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import com.boylesoftware.web.RequestedResourceException;


/**
 * Interface for implementations of logic associated with different phases of
 * the request processing by the framework.
 *
 * @author Lev Himmelfarb
 */
public interface Script {

	/**
	 * Execute the script.
	 *
	 * @param request The HTTP request.
	 * @param em Entity manager to use to access persistent objects.
	 *
	 * @throws RequestedResourceException If there is a problem with the
	 * request.
	 */
	void execute(HttpServletRequest request, EntityManager em)
		throws RequestedResourceException;
}
