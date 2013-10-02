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
 * Interface for poolable objects used with {@link FastPool}.
 *
 * @param <T> Type of the poolable objects produced by this factory.
 *
 * @author Lev Himmelfarb
 */
public interface PoolableObjectFactory<T extends Poolable> {

	/**
	 * Make new poolable object instance.
	 *
	 * @param pool Reference to the pool.
	 * @param pooledObjectId Pooled object id that needs to be saved in the new
	 * object so that it can be used in its {@link Poolable} interface
	 * implementation.
	 *
	 * @return New poolable object instance.
	 */
	T makeNew(FastPool<T> pool, int pooledObjectId);
}
