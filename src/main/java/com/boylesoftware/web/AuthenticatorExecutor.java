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
package com.boylesoftware.web;

import com.boylesoftware.web.spi.AuthenticationService;
import com.boylesoftware.web.util.LooseCannon;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;


/**
 * Executes authenticated user lookup.
 *
 * @author Lev Himmelfarb
 */
class AuthenticatorExecutor
	extends AsynchronousExecutor {

	/**
	 * The instance pool.
	 */
	private static final FastPool<AuthenticatorExecutor> POOL =
		new FastPool<>(new PoolableObjectFactory<AuthenticatorExecutor>() {

			@Override
			public AuthenticatorExecutor makeNew(
					final FastPool<AuthenticatorExecutor> pool,
					final int pooledObjectId) {

				return new AuthenticatorExecutor(pooledObjectId);
			}
		}, "RequestAuthenticationExecutorsPool");

	/**
	 * Get executor instance from the internal pool. The instance must be
	 * recycled after it is no longer needed.
	 *
	 * @return Executor instance.
	 */
	static AuthenticatorExecutor getExecutor() {

		return POOL.getSync();
	}


	/**
	 * Create new executor. This constructor is for internal use only. Use
	 * {@link #getExecutor()} to get instances from the pool.
	 *
	 * @param pooledObjectId Pooled object id.
	 */
	AuthenticatorExecutor(final int pooledObjectId) {
		super(POOL, pooledObjectId);
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.AsynchronousExecutor#execute()
	 */
	@Override
	protected void execute() {

		final AuthenticationService<?> auth =
			this.webapp.getAuthenticationService();
		final Object authedUser = (this.routerReq.isSecure() ?
				auth.getAuthenticatedUser(this.routerReq,
						this.webapp.getEntityManagerFactory()) : null);

		LooseCannon.heel();

		this.checkTimeout();

		Router.setAuthenticatedUser(this.routerReq, authedUser);

		this.asyncContext.dispatch();
	}
}
