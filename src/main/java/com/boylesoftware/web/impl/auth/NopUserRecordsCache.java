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


/**
 * Stub {@link UserRecordsCache} implementation that does not cache anything.
 *
 * @param <T> User record type.
 *
 * @author Lev Himmelfarb
 */
public class NopUserRecordsCache<T>
	implements UserRecordsCache<T> {

	/**
	 * Always returns {@code null}.
	 */
	@Override
	public T getUser(final int id) {

		return null;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void storeUser(final int id, final T user) {

		// nothing
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void evictUser(final int id) {

		// nothing
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void evictAllUsers() {

		// nothing
	}
}
