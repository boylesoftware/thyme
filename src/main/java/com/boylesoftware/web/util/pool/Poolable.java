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
 * Interface to be added to objects that can be pooled by the {@link FastPool}.
 *
 * @author Lev Himmelfarb
 */
public interface Poolable {

	/**
	 * Get pooled object id originally provided by the pool to the
	 * {@link PoolableObjectFactory#makeNew} method.
	 *
	 * @return The poolable object id.
	 */
	int getPooledObjectId();

	/**
	 * Return the object back to the original pool. The pool reference is passed
	 * to the {@link PoolableObjectFactory#makeNew} method. The implementation
	 * can simply call {@link FastPool#recycle} method. It can also perform the
	 * necessary clean up before calling it. Note, that in some cases the
	 * {@link FastPool#recycle} method needs to be called from within a
	 * synchronized block.
	 */
	void recycle();
}
