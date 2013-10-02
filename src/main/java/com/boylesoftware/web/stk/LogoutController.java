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
package com.boylesoftware.web.stk;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.api.Routes;
import com.boylesoftware.web.api.Routes.URIType;


/**
 * Controller providing user logout functionality.
 *
 * @author Lev Himmelfarb
 */
public class LogoutController {

	/**
	 * Post logout page route id.
	 */
	private final String postLogoutPageRouteId;


	/**
	 * Create new controller.
	 *
	 * @param postLogoutPageRouteId Id of the route that leads to the page shown
	 * after successful logout.
	 */
	public LogoutController(final String postLogoutPageRouteId) {

		this.postLogoutPageRouteId = postLogoutPageRouteId;
	}


	/**
	 * Process user logout action.
	 *
	 * @param request The request.
	 * @param response The response.
	 * @param auth The authenticator.
	 * @param routes The routes.
	 *
	 * @return URI of the home page.
	 */
	String post(final HttpServletRequest request,
			final HttpServletResponse response, final Authenticator<?> auth,
			final Routes routes) {

		auth.logout(request, response);

		return routes.getRouteURI(request, this.postLogoutPageRouteId,
				URIType.FORCE_PLAIN);
	}
}
