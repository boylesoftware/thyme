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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.spi.UserRecordHandler;
import com.boylesoftware.web.util.Hex;


/**
 * Simple abstract implementation of {@link UserRecordHandler} that looks up
 * user record in the database.
 *
 * @param <T> User record type. Must be a JPA entity.
 *
 * @author Lev Himmelfarb
 */
public abstract class AbstractUserRecordHandler<T>
	implements UserRecordHandler<T> {

	/**
	 * The log.
	 */
	private final Log log = LogFactory.getLog(AbstractUserRecordHandler.class);

	/**
	 * User record class.
	 */
	private final Class<T> userRecordClass;


	/**
	 * Create new handler.
	 *
	 * @param userRecordClass User record class.
	 */
	public AbstractUserRecordHandler(final Class<T> userRecordClass) {

		this.userRecordClass = userRecordClass;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.UserRecordHandler#getUserObjectClass()
	 */
	@Override
	public Class<T> getUserObjectClass() {

		return this.userRecordClass;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.UserRecordHandler#getUser(int, int, javax.persistence.EntityManagerFactory)
	 */
	@Override
	public T getUser(final int id, final int salt,
			final EntityManagerFactory emf) {

		final boolean debug = this.log.isDebugEnabled();

		if (debug) {
			this.log.debug("looking up user id " + id + ", salt " + salt +
					" in the database");
			this.log.debug("creating entity manager");
		}
		final EntityManager em = emf.createEntityManager();
		try {
			final T user = em.find(this.userRecordClass, Integer.valueOf(id));

			if ((user == null) || (this.getUserSalt(user) != salt)) {
				if (debug)
					this.log.debug("user not found or salt does not match");
				return null;
			}

			if (debug)
				this.log.debug("user found");
			return user;

		} finally {
			if (debug)
				this.log.debug("closing entity manager");
			em.close();
		}
	}

	/**
	 * Digest password. This is a convenience method for some implementations.
	 *
	 * @param password The password.
	 * @param algorithm Digest algorithm (see {@link MessageDigest}).
	 *
	 * @return Password digest in hexadecimal encoding.
	 */
	protected String digestPassword(final String password,
			final String algorithm) {

		try {
			final MessageDigest digester = MessageDigest.getInstance(algorithm);

			return Hex.encode(digester.digest(
					password.getBytes(Charset.forName("UTF-8"))));

		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Invalid digest algorithm.", e);
		}
	}
}
