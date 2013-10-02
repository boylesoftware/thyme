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
package com.boylesoftware.web.impl.auth;

import javax.persistence.EntityManager;

import com.boylesoftware.web.api.Authenticator;


/**
 * {@link Authenticator} interface extension that provides additional API backed
 * by the {@link UserRecordHandler} implementation.
 *
 * @param <T> User record type.
 *
 * @author Lev Himmelfarb
 */
public interface UserRecordAuthenticator<T>
	extends Authenticator<T> {

	/**
	 * Get user record given user login name and password.
	 *
	 * @param em Entity manager to use to get the record from the database.
	 * @param loginName User login name. The value is as provided by the user,
	 * so the implementation may perform some implementation-specific
	 * normalization.
	 * @param password User password as provided by the user.
	 *
	 * @return The user record, or {@code null} if not found.
	 */
	T getUserByLoginNameAndPassword(EntityManager em, String loginName,
			String password);
}
