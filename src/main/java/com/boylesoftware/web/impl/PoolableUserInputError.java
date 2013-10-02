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
package com.boylesoftware.web.impl;

import com.boylesoftware.web.UserInputError;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.Poolable;


/**
 * Poolable extension of the user input error object.
 *
 * @author Lev Himmelfarb
 */
class PoolableUserInputError
	extends UserInputError
	implements Poolable {

	/**
	 * The pool.
	 */
	private final FastPool<PoolableUserInputError> pool;

	/**
	 * Pooled object id.
	 */
	private final int pooledObjectId;


	/**
	 * Create new instance.
	 *
	 * @param pool Reference to the owning pool.
	 * @param pooledObjectId The pooled object id.
	 */
	PoolableUserInputError(final FastPool<PoolableUserInputError> pool,
			final int pooledObjectId) {

		this.pool = pool;
		this.pooledObjectId = pooledObjectId;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.util.pool.Poolable#getPooledObjectId()
	 */
	@Override
	public int getPooledObjectId() {

		return this.pooledObjectId;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.util.pool.Poolable#recycle()
	 */
	@Override
	public void recycle() {

		this.pool.recycleSync(this);
	}
}
