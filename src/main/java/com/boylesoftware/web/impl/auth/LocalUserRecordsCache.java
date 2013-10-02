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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * {@link UserRecordsCache} implementation that uses local hash map and soft
 * references to cache user records. This cache implementation cannot be used in
 * clustered environments as the cache instances do not communicate between each
 * other.
 *
 * @param <T> User record type.
 *
 * @author Lev Himmelfarb
 */
public class LocalUserRecordsCache<T>
	implements UserRecordsCache<T> {

	/**
	 * Cache record.
	 *
	 * @param <T> User record type.
	 */
	private static final class CacheRecord<T>
		extends SoftReference<T> {

		/**
		 * User id.
		 */
		private final Integer id;


		/**
		 * Create new record.
		 *
		 * @param id User id.
		 * @param user User record.
		 * @param q Reference queue.
		 */
		CacheRecord(final Integer id, final T user,
				final ReferenceQueue<T> q) {
			super(user, q);

			this.id = id;
		}


		/**
		 * Get user id.
		 *
		 * @return User id.
		 */
		Integer getId() {

			return this.id;
		}
	}


	/**
	 * The cache.
	 */
	private final ConcurrentMap<Integer, CacheRecord<T>> cache =
		new ConcurrentHashMap<>();

	/**
	 * Soft reference cleanup queue.
	 */
	private final ReferenceQueue<T> refQueue = new ReferenceQueue<>();


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.auth.UserRecordsCache#getUser(int)
	 */
	@Override
	public T getUser(final int id) {

		this.expungeStaleRecords();

		final Integer key = Integer.valueOf(id);
		final CacheRecord<T> ref = this.cache.get(key);
		if (ref == null)
			return null;

		final T user = ref.get();
		if (user == null)
			this.cache.remove(key);

		return user;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.auth.UserRecordsCache#storeUser(int, java.lang.Object)
	 */
	@Override
	public void storeUser(final int id, final T user) {

		final Integer key = Integer.valueOf(id);
		this.cache.put(key, new CacheRecord<>(key, user, this.refQueue));
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.auth.UserRecordsCache#evictUser(int)
	 */
	@Override
	public void evictUser(final int id) {

		this.cache.remove(Integer.valueOf(id));

		this.expungeStaleRecords();
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.auth.UserRecordsCache#evictAllUsers()
	 */
	@Override
	public void evictAllUsers() {

		this.cache.clear();

		while (this.refQueue.poll() != null) { /* nothing */ }
	}


	/**
	 * Expunge stale cache records.
	 */
	private void expungeStaleRecords() {

		for (Object x; (x = this.refQueue.poll()) != null;) {
			@SuppressWarnings("unchecked")
			final CacheRecord<T> rec = (CacheRecord<T>) x;
			this.cache.remove(rec.getId());
		}
	}
}
