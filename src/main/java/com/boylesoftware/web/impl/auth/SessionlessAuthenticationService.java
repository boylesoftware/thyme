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

import java.security.Key;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.spi.AuthenticationService;
import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.util.StringUtils;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;


/**
 * Authentication service implementation provided by the
 * {@link SessionlessAuthenticationServiceProvider}.
 *
 * @param <T> User record type.
 *
 * @author Lev Himmelfarb
 */
class SessionlessAuthenticationService<T>
	implements AuthenticationService<T> {

	/**
	 * Name of the authentication cookie.
	 */
	private static final String AUTH_COOKIE_NAME = "BSWEBAT";

	/**
	 * Name of request attribute used for the list of pending authenticated user
	 * records cache evictions.
	 */
	private static final String PENDING_CACHE_EVICTIONS_ATTNAME =
		(SessionlessAuthenticationService.class).getName() +
			".PENDING_CACHE_EVICTIONS";

	/**
	 * Special object used to indicate that full authenticated user records
	 * cache purge was requested.
	 */
	private static final Object EVICT_ALL = new Object();


	/**
	 * The log.
	 */
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * User record class.
	 */
	private final Class<T> userRecordClass;

	/**
	 * User record handler.
	 */
	private final UserRecordHandler<T> userRecordHandler;

	/**
	 * Authenticated user records cache.
	 */
	private final UserRecordsCache<T> userRecordsCache;

	/**
	 * Cipher pool.
	 */
	private final FastPool<CipherToolbox> cipherPool;


	/**
	 * Create new authenticator.
	 *
	 * @param secretKey Key for encrypting the authentication cookie.
	 * @param userRecordClass User record class.
	 * @param userRecordHandler User record handler.
	 * @param userRecordsCache Authenticated user records cache.
	 */
	SessionlessAuthenticationService(final Key secretKey,
			final Class<T> userRecordClass,
			final UserRecordHandler<T> userRecordHandler,
			final UserRecordsCache<T> userRecordsCache) {

		this.userRecordClass = userRecordClass;
		this.userRecordHandler = userRecordHandler;
		this.userRecordsCache = userRecordsCache;

		this.cipherPool =
			new FastPool<>(new PoolableObjectFactory<CipherToolbox>() {

				@Override
				public CipherToolbox makeNew(final FastPool<CipherToolbox> pool,
						final int pooledObjectId) {

					return new CipherToolbox(pool, pooledObjectId, secretKey);
				}
			}, "AuthenticatorCiphersPool");
	}


	/**
	 * Get cipher toolbox from the service's internal pool.
	 *
	 * @return Cipher toolbox.
	 */
	CipherToolbox getCipherToolbox() {

		return this.cipherPool.getSync();
	}

	/**
	 * Get user record handler.
	 *
	 * @return The user record handler.
	 */
	UserRecordHandler<T> getUserRecordHandler() {

		return this.userRecordHandler;
	}

	/**
	 * Create authentication token cookie.
	 *
	 * @param request The request.
	 * @param value Value for the cookie.
	 *
	 * @return The cookie.
	 */
	Cookie createAuthCookie(final HttpServletRequest request,
			final String value) {

		final Cookie authCookie = new Cookie(AUTH_COOKIE_NAME, value);
		authCookie.setPath(
				StringUtils.emptyIfNull(request.getContextPath()) + "/");

		return authCookie;
	}

	/**
	 * Pend eviction of the specified user from the authenticated user records
	 * cache.
	 *
	 * @param request The request.
	 * @param user The user to evict.
	 */
	@SuppressWarnings("unchecked")
	void pendCacheEviction(final RouterRequest request, final T user) {

		final Object attVal =
			request.getAttribute(PENDING_CACHE_EVICTIONS_ATTNAME);

		if (attVal == EVICT_ALL)
			return;

		Set<Integer> pendingList;
		if (attVal == null) {
			pendingList = new HashSet<>();
			request.setAttribute(PENDING_CACHE_EVICTIONS_ATTNAME, pendingList);
		} else {
			pendingList = (Set<Integer>) attVal;
		}

		pendingList.add(Integer.valueOf(
				this.userRecordHandler.getUserId(user)));
	}

	/**
	 * Pend full authenticated user records cache purge.
	 *
	 * @param request The request.
	 */
	void pendCachePurge(final RouterRequest request) {

		request.setAttribute(PENDING_CACHE_EVICTIONS_ATTNAME, EVICT_ALL);
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.AuthenticationService#getAuthenticatedUserObjectClass()
	 */
	@Override
	public Class<T> getAuthenticatedUserObjectClass() {

		return this.userRecordClass;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.AuthenticationService#getAuthenticatedUser(javax.servlet.http.HttpServletRequest, javax.persistence.EntityManagerFactory)
	 */
	@Override
	public T getAuthenticatedUser(final HttpServletRequest request,
			final EntityManagerFactory emf) {

		final boolean debug = this.log.isDebugEnabled();

		// get authentication cookie
		final String cookieVal = this.getAuthCookieValue(request);
		if (cookieVal == null) {
			if (debug)
				this.log.debug("no authentication cookie");
			return null;
		}

		// decrypt the cookie value
		int userId;
		int salt;
		long timestamp;
		final CipherToolbox cipher = this.getCipherToolbox();
		try {
			if (!cipher.decrypt(cookieVal))
				return null;
			userId = cipher.getUserId();
			salt = cipher.getSalt();
			timestamp = cipher.getTimestamp();
		} finally {
			cipher.recycle();
		}

		// check if timestamp is way too old
		final long now = System.currentTimeMillis();
		if ((timestamp < now - 365L * 24L * 3600000L) || (timestamp > now)) {
			if (debug)
				this.log.debug("timestamp out of allowed range");
			return null;
		}

		// find the user record
		T user = this.userRecordsCache.getUser(userId);
		if (user == null) {
			if (debug)
				this.log.debug("user id " + userId +
						" is not in the authenticated user records cache," +
						" will attempt to fetch from the storage");
			user = this.userRecordHandler.getUser(userId, salt, emf);
			this.userRecordsCache.storeUser(userId, user);
		} else {
			if (debug)
				this.log.debug("user id " + userId +
						" found in the authenticated user records cache");
		}
		if (debug)
			this.log.debug("authenticated user: " + user);
		return user;
	}

	/**
	 * Get authentication cookie value.
	 *
	 * @param request The HTTP request.
	 *
	 * @return The cookie value, or {@code null} if not found.
	 */
	private String getAuthCookieValue(final HttpServletRequest request) {

		final Cookie[] cookies = request.getCookies();
		if (cookies != null)
			for (final Cookie cookie : request.getCookies())
				if (cookie.getName().equals(AUTH_COOKIE_NAME))
					return cookie.getValue();

		return null;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.AuthenticationService#getAuthenticator(com.boylesoftware.web.spi.RouterRequest)
	 */
	@Override
	public Authenticator<T> getAuthenticator(final RouterRequest routerReq) {

		return new SessionlessAuthenticator<>(this, routerReq);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.AuthenticationService#performCacheEvictions(com.boylesoftware.web.spi.RouterRequest)
	 */
	@Override
	public void performCacheEvictions(final RouterRequest routerReq) {

		final boolean debug = this.log.isDebugEnabled();

		final Object attVal =
			routerReq.getAttribute(PENDING_CACHE_EVICTIONS_ATTNAME);

		if (attVal == null)
			return;

		routerReq.removeAttribute(PENDING_CACHE_EVICTIONS_ATTNAME);

		if (attVal == EVICT_ALL) {
			if (debug)
				this.log.debug("purging all cached authenticated user records");
			this.userRecordsCache.evictAllUsers();
		} else {
			@SuppressWarnings("unchecked")
			final Set<Integer> pendingList = (Set<Integer>) attVal;
			for (Integer userId : pendingList) {
				if (debug)
					this.log.debug("evicting user id " + userId +
							" from the authenticated user records cache");
				this.userRecordsCache.evictUser(userId.intValue());
			}
		}
	}
}
