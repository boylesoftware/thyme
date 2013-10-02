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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.spi.RouterRequest;


/**
 * Authenticator API implementation provided by the
 * {@link SessionlessAuthenticationServiceProvider}.
 *
 * @param <T> User record type.
 *
 * @author Lev Himmelfarb
 */
class SessionlessAuthenticator<T>
	implements UserRecordAuthenticator<T> {

	/**
	 * The authentication service.
	 */
	private final SessionlessAuthenticationService<T> service;

	/**
	 * The user record handler.
	 */
	private final UserRecordHandler<T> userRecordHandler;

	/**
	 * Router request.
	 */
	private final RouterRequest routerReq;


	/**
	 * Create new authenticator for the specified request.
	 *
	 * @param service Reference to the owning authentication service.
	 * @param routerReq The request.
	 */
	SessionlessAuthenticator(final SessionlessAuthenticationService<T> service,
			final RouterRequest routerReq) {

		this.service = service;
		this.userRecordHandler = this.service.getUserRecordHandler();
		this.routerReq = routerReq;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.Authenticator#authenticate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, boolean)
	 */
	@Override
	public void authenticate(final HttpServletRequest request,
			final HttpServletResponse response, final T user,
			final boolean rememberUser) {

		String cookieVal;
		final CipherToolbox cipher = this.service.getCipherToolbox();
		try {
			cookieVal = cipher.encrypt(this.userRecordHandler.getUserId(user),
					this.userRecordHandler.getUserSalt(user));
		} finally {
			cipher.recycle();
		}

		final Cookie cookie = this.service.createAuthCookie(request, cookieVal);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		if (rememberUser)
			cookie.setMaxAge(Integer.MAX_VALUE);

		response.addCookie(cookie);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.Authenticator#logout(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void logout(final HttpServletRequest request,
			final HttpServletResponse response) {

		final Cookie cookie = this.service.createAuthCookie(request, "");
		cookie.setMaxAge(0);

		response.addCookie(cookie);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.Authenticator#evictFromCache(java.lang.Object)
	 */
	@Override
	public void evictFromCache(final T user) {

		this.service.pendCacheEviction(this.routerReq, user);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.Authenticator#evictAllFromCache()
	 */
	@Override
	public void evictAllFromCache() {

		this.service.pendCachePurge(this.routerReq);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.impl.auth.UserRecordAuthenticator#getUserByLoginNameAndPassword(javax.persistence.EntityManager, java.lang.String, java.lang.String)
	 */
	@Override
	public T getUserByLoginNameAndPassword(final EntityManager em,
			final String loginName, final String password) {

		return this.userRecordHandler.getUserByLoginNameAndPassword(em,
				loginName, password);
	}
}
