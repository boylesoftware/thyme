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

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.api.RequestParam;
import com.boylesoftware.web.api.UserInput;
import com.boylesoftware.web.api.UserInputErrors;
import com.boylesoftware.web.api.UserRecordAuthenticator;


/**
 * User login page controller that can be used in applications that use
 * authenticators implementing {@link UserRecordAuthenticator} interface.
 *
 * <p>The controller uses {@link LoginData} instance as the user input. In case
 * of failed login, the validation error message is
 * {@value #INVALID_LOGIN_MESSAGE}.
 *
 * @author Lev Himmelfarb
 */
public class LoginController {

	/**
	 * Invalid login message.
	 */
	public static final String INVALID_LOGIN_MESSAGE =
		"{error.loginName.invalid}";


	/**
	 * Process login data submission and attempt to establish authenticated
	 * session.
	 *
	 * @param request The request.
	 * @param response The response.
	 * @param loginData Login data.
	 * @param errors Login data validation errors.
	 * @param targetURI Target protected page URI.
	 * @param em Entity manager.
	 * @param auth The authenticator.
	 *
	 * @return Target protected page URI.
	 */
	String post(final HttpServletRequest request,
			final HttpServletResponse response,
			@UserInput final LoginData loginData, final UserInputErrors errors,
			@RequestParam(Authenticator.TARGET_URI) final String targetURI,
			final EntityManager em,
			final UserRecordAuthenticator<Object> auth) {

		final Object user =
			auth.getUserByLoginNameAndPassword(em, loginData.getLoginName(),
					loginData.getPassword());
		if (user == null) {
			errors.add("loginName", INVALID_LOGIN_MESSAGE);
			return null;
		}

		auth.authenticate(request, response, user, loginData.isRememberMe());

		return targetURI;
	}
}
