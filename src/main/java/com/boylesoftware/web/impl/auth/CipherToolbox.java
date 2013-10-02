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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Random;

import javax.crypto.Cipher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.api.Authenticator;
import com.boylesoftware.web.util.Base64;
import com.boylesoftware.web.util.pool.AbstractPoolable;
import com.boylesoftware.web.util.pool.FastPool;

/**
 * Poolable collection of objects needed for encrypting/decrypting
 * authentication cookie.
 *
 * @author Lev Himmelfarb
 */
class CipherToolbox
	extends AbstractPoolable {

	/**
	 * Algorithm used to encrypt the authentication cookie.
	 */
	static final String ALGORITHM = "AES";

	/**
	 * Random number generator.
	 */
	private static final Random RAND = new Random();


	/**
	 * The log.
	 */
	private final Log log = LogFactory.getLog(Authenticator.class);

	/**
	 * The key.
	 */
	private final Key secretKey;

	/**
	 * The cipher.
	 */
	private final Cipher cipher;

	/**
	 * Buffer for the clear value.
	 */
	private final ByteBuffer clearBuf;

	/**
	 * Buffer for the encrypted value.
	 */
	private final ByteBuffer cipherBuf;

	/**
	 * Character array for Base64 representation of the encrypted value.
	 */
	private final char[] base64Chars;

	/**
	 * Buffer wrapper for the Base64 character array.
	 */
	private final CharBuffer base64Buf;

	/**
	 * Decrypted user id.
	 */
	private int userId;

	/**
	 * Decrypted salt.
	 */
	private int salt;

	/**
	 * Decrypted timestamp.
	 */
	private long timestamp;


	/**
	 * Create new instance.
	 *
	 * @param pool Reference to the owning pool.
	 * @param pooledObjectId Pooled object id.
	 * @param secretKey The key.
	 */
	CipherToolbox(final FastPool<CipherToolbox> pool,
			final int pooledObjectId, final Key secretKey) {
		super(pool, pooledObjectId);

		this.secretKey = secretKey;

		try {
			this.cipher = Cipher.getInstance(ALGORITHM);
		} catch (final GeneralSecurityException e) {
			throw new RuntimeException("Error creating cipher.", e);
		}

		this.clearBuf = ByteBuffer.allocate(48);
		this.cipherBuf = ByteBuffer.allocate(48);
		this.base64Chars = new char[64];
		this.base64Buf = CharBuffer.wrap(this.base64Chars);
	}


	/**
	 * Encrypt specified authentication data into a Base64 cookie value.
	 *
	 * @param userId User id.
	 * @param salt Salt.
	 *
	 * @return Encrypted value as a Base64 encoded string.
	 */
	String encrypt(final int userId, final int salt) {

		this.clearBuf.clear();
		this.clearBuf.putInt(salt).putLong(RAND.nextLong()).putInt(userId)
			.putLong(RAND.nextLong()).putLong(System.currentTimeMillis());
		this.clearBuf.flip();

		this.cipherBuf.clear();
		try {
			this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
			this.cipher.doFinal(this.clearBuf, this.cipherBuf);
		} catch (final GeneralSecurityException e) {
			throw new RuntimeException(
					"Error encrypting authentication cookie.", e);
		}
		this.cipherBuf.flip();

		this.base64Buf.clear();
		Base64.encode(this.cipherBuf, this.base64Buf);
		this.base64Buf.flip();

		return this.base64Buf.toString();
	}

	/**
	 * Decrypt specified Base64 value. The decrypted values can be retrieved
	 * immediately after calling this method using {@link #getUserId},
	 * {@link #getSalt} and {@link #getTimestamp}.
	 *
	 * @param base64Val The value to decrypt in Base64 encoding.
	 *
	 * @return {@code true} if decrypted successfully, {@code false} if
	 * could not decrypt.
	 */
	boolean decrypt(final String base64Val) {

		final boolean debug = this.log.isDebugEnabled();
		if (debug)
			this.log.debug("decrypting [" + base64Val +
					"], pooled cipher " + this);

		try {
			final int len = base64Val.length();
			base64Val.getChars(0, len, this.base64Chars, 0);
			this.base64Buf.limit(len);
			this.base64Buf.rewind();
		} catch (final IndexOutOfBoundsException e) {
			if (debug)
				this.log.debug("decryption error", e);
			return false;
		}

		this.cipherBuf.clear();
		Base64.decode(this.base64Buf, this.cipherBuf);
		this.cipherBuf.flip();

		this.clearBuf.clear();
		try {
			this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey);
			this.cipher.doFinal(this.cipherBuf, this.clearBuf);
		} catch (final GeneralSecurityException e) {
			if (debug)
				this.log.debug("decryption error", e);
			return false;
		}
		this.clearBuf.flip();

		this.salt = this.clearBuf.getInt();
		this.clearBuf.getLong();
		this.userId = this.clearBuf.getInt();
		this.clearBuf.getLong();
		this.timestamp = this.clearBuf.getLong();

		if (debug)
			this.log.debug("decrypted: userId=" + this.userId +
					", salt=" + this.salt + ", ts=" + this.timestamp +
					" (" + (new java.util.Date(this.timestamp)) + ")");

		return true;
	}


	/**
	 * Get decrypted user id.
	 *
	 * @return User id.
	 */
	int getUserId() {

		return this.userId;
	}

	/**
	 * Get decrypted salt.
	 *
	 * @return The salt.
	 */
	int getSalt() {

		return this.salt;
	}

	/**
	 * Get decrypted timestamp.
	 *
	 * @return The timestamp.
	 */
	long getTimestamp() {

		return this.timestamp;
	}
}
