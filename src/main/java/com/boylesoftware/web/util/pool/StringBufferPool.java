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
package com.boylesoftware.web.util.pool;


/**
 * Pool for string buffers/builders and regular expression matchers.
 *
 * @author Lev Himmelfarb
 */
public final class StringBufferPool {

	/**
	 * The pool.
	 */
	private static final FastPool<PooledStringBuffer> pool =
		new FastPool<>(new PoolableObjectFactory<PooledStringBuffer>() {

			@Override
			public PooledStringBuffer makeNew(
					final FastPool<PooledStringBuffer> pool,
					final int pooledObjectId) {

				return new PooledStringBuffer(pool, pooledObjectId);
			}
		}, "StringBuffersPool");


	/**
	 * All methods are static.
	 */
	private StringBufferPool() {}


	/**
	 * Borrow a buffer. The method is thread-safe.
	 *
	 * @return A poolable wrapper containing the buffer.
	 */
	public static PooledStringBuffer get() {

		return pool.getSync();
	}
}
