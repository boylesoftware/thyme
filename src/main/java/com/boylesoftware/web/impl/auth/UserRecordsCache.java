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
 * Interface for authenticated user records cache that can be used by
 * authentication service implementations to cache user records fetched from the
 * back-end user record storage. The authentication service implementations are
 * assumed to be those that use {@link UserRecordHandler}.
 *
 * <p>Cache implementations must be thread-safe.
 *
 * @param <T> User record type.
 *
 * @author Lev Himmelfarb
 */
public interface UserRecordsCache<T> {

	/**
	 * Get user record from the cache.
	 *
	 * @param id User id.
	 *
	 * @return Cached user record, or {@code null} if not in the cache.
	 */
	T getUser(int id);

	/**
	 * Store user record in the cache.
	 *
	 * @param id User id.
	 * @param user User record.
	 */
	void storeUser(int id, T user);

	/**
	 * Evict user record from the cache, if present. Do nothing otherwise.
	 *
	 * @param id User id.
	 */
	void evictUser(int id);

	/**
	 * Purge all cached user records.
	 */
	void evictAllUsers();
}
