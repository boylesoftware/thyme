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
import javax.persistence.EntityManagerFactory;


/**
 * Interface for application specific handler of user account records. The
 * handler presents the application specific user records to the framework in a
 * standardized way.
 *
 * @param <T> User record type.
 *
 * @author Lev Himmelfarb
 */
public interface UserRecordHandler<T> {

	/**
	 * Get user record given the user id and salt.
	 *
	 * @param id User id.
	 * @param salt Salt.
	 * @param emf Entity manager factory to use to lookup the user record in the
	 * persistent storage.
	 *
	 * @return User record, or {@code null} if none match the specified id and
	 * salt.
	 */
	T getUser(int id, int salt, EntityManagerFactory emf);

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

	/**
	 * Given user record, get user id.
	 *
	 * @param user User record.
	 *
	 * @return User id.
	 */
	int getUserId(T user);

	/**
	 * Given user record, get salt associated with it.
	 *
	 * @param user User record.
	 *
	 * @return Salt value.
	 */
	int getUserSalt(T user);
}
