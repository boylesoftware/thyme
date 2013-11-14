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

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.spi.AuthenticationService;
import com.boylesoftware.web.spi.RouterRequest;


/**
 * Stub authentication service implementation that assumes that the application
 * does not need user authentication and reports to the rest of the framework
 * that all requests are anonymous.
 *
 * @author Lev Himmelfarb
 */
public class NopAuthenticationService
	implements AuthenticationService<Object>, Authenticator<Object> {

	/**
	 * Always returns {@code null}.
	 */
	@Override
	public Object getAuthenticatedUser(final HttpServletRequest request,
			final EntityManagerFactory emf) {

		return null;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.AuthenticationService#getAuthenticator(com.boylesoftware.web.spi.RouterRequest)
	 */
	@Override
	public Authenticator<Object> getAuthenticator(
			final RouterRequest routerReq) {

		return this;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void performCacheEvictions(final RouterRequest routerReq) {

		// nothing
	}

	/**
	 * Throws {@link UnsupportedOperationException}.
	 */
	@Override
	public void authenticate(final HttpServletRequest request,
			final HttpServletResponse response, final Object user,
			final boolean rememberUser) {

		throw new UnsupportedOperationException();
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void logout(final HttpServletRequest request,
			final HttpServletResponse response) {

		// nothing
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void evictFromCache(Object user) {

		// nothing
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void evictAllFromCache() {

		// nothing
	}
}
