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

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.boylesoftware.web.input.NoTrim;


/**
 * User login information.
 *
 * @author Lev Himmelfarb
 */
public class LoginData
	implements Serializable {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * User login name.
	 */
	@NotNull(message="{error.loginName.empty}")
	private String loginName;

	/**
	 * Password.
	 */
	@NoTrim
	@NotNull(message="{error.password.empty}")
	private String password;

	/**
	 * Tells if the login should be remembered after the browser is closed.
	 */
	private boolean rememberMe;


	/**
	 * Get user login name.
	 *
	 * @return User login name.
	 */
	public String getLoginName() {

		return this.loginName;
	}

	/**
	 * Set user login name.
	 *
	 * @param loginName User login name.
	 */
	public void setLoginName(final String loginName) {

		this.loginName = loginName;
	}

	/**
	 * Get password.
	 *
	 * @return The password.
	 */
	public String getPassword() {

		return this.password;
	}

	/**
	 * Set password.
	 *
	 * @param password The password.
	 */
	public void setPassword(final String password) {

		this.password = password;
	}

	/**
	 * Tell if the login should be remembered after the browser is closed.
	 *
	 * @return {@code true} if the login should be remembered.
	 */
	public boolean isRememberMe() {

		return this.rememberMe;
	}

	/**
	 * Set flag telling if the login should be remembered after the browser is
	 * closed.
	 *
	 * @param rememberMe {@code true} if the login should be remembered.
	 */
	public void setRememberMe(final boolean rememberMe) {

		this.rememberMe = rememberMe;
	}
}
