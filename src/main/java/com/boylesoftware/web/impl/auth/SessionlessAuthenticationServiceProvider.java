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

import javax.crypto.spec.SecretKeySpec;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import com.boylesoftware.web.ApplicationConfiguration;
import com.boylesoftware.web.spi.AuthenticationService;
import com.boylesoftware.web.spi.AuthenticationServiceProvider;
import com.boylesoftware.web.util.Hex;


/**
 * Provider of {@link AuthenticationService} that does not rely on servlet
 * container's HTTP session tracking and stores all necessary authenticated user
 * information in an encrypted cookie.
 *
 * <p>The provider implementation requires a 128-bit AES encryption algorithm
 * secret key in the JNDI environment. The name of the environment entry is
 * "java:comp/env/secretKey" and it should be a string in hexadecimal encoding.
 *
 * @param <T> User record type.
 *
 * @author Lev Himmelfarb
 */
public class SessionlessAuthenticationServiceProvider<T>
	implements AuthenticationServiceProvider<T> {

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
	 * Create new factory.
	 *
	 * @param userRecordClass User record class.
	 * @param userRecordHandler User record handler.
	 * @param userRecordsCache Authenticated user records cache.
	 */
	public SessionlessAuthenticationServiceProvider(
			final Class<T> userRecordClass,
			final UserRecordHandler<T> userRecordHandler,
			final UserRecordsCache<T> userRecordsCache) {

		this.userRecordClass = userRecordClass;
		this.userRecordHandler = userRecordHandler;
		this.userRecordsCache = userRecordsCache;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.AuthenticationServiceProvider#getAuthenticationService(javax.servlet.ServletContext, com.boylesoftware.web.ApplicationConfiguration)
	 */
	@Override
	public AuthenticationService<T> getAuthenticationService(
			final ServletContext sc, final ApplicationConfiguration config)
		throws UnavailableException {

		// get configured secret key from the JNDI
		String secretKeyStr;
		try {
			final InitialContext jndi = new InitialContext();
			try {
				secretKeyStr = (String) jndi.lookup("java:comp/env/secretKey");
			} finally {
				jndi.close();
			}
		} catch (final NamingException e) {
			throw new UnavailableException(
					"Error looking up secret key in the JNDI: " + e);
		}
		if (!secretKeyStr.matches("[0-9A-Fa-f]{32}"))
			throw new UnavailableException("Configured secret key is" +
					" invalid. The key must be a 16 bytes value" +
					" encoded as a hexadecimal string.");
		final Key secretKey = new SecretKeySpec(
				Hex.decode(secretKeyStr), CipherToolbox.ALGORITHM);

		// create and return authenticator instance
		return new SessionlessAuthenticationService<>(secretKey,
				this.userRecordClass, this.userRecordHandler,
				this.userRecordsCache);
	}
}
