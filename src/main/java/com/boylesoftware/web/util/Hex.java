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
package com.boylesoftware.web.util;

import com.boylesoftware.web.util.pool.PooledStringBuffer;
import com.boylesoftware.web.util.pool.StringBufferPool;


/**
 * Hexadecimal codec.
 *
 * @author Lev Himmelfarb
 */
public final class Hex {

	/**
	 * Hexadecimal alphabet.
	 */
	private static final char[] HEX = "0123456789ABCDEF".toCharArray();


	/**
	 * All methods are static.
	 */
	private Hex() {}


	/**
	 * Encode bytes to a hexadecimal string.
	 *
	 * @param bytes Bytes to encode.
	 *
	 * @return The encoded string.
	 */
	public static String encode(final byte[] bytes) {

		try (final PooledStringBuffer buf = StringBufferPool.get()) {
			final StringBuilder res = buf.getStringBuilder();
			for (final byte b : bytes) {
				res.append(HEX[(b >> 4) & 0xF]);
				res.append(HEX[(b & 0xF)]);
			}
			return res.toString();
		}
	}

	/**
	 * Decode hexadecimal string.
	 *
	 * @param str Hexadecimal string.
	 *
	 * @return The decoded bytes.
	 *
	 * @throws IllegalArgumentException If the string is not a valid hexadecimal
	 * string.
	 */
	public static byte[] decode(final String str) {

		final int strLen = str.length();
		if (strLen % 2 != 0)
			throw new IllegalArgumentException(
					"Hexadecimal string needs to be even-length.");

		final byte[] res = new byte[strLen / 2];
		for (int i = 0; i < strLen; i += 2)
			res[i / 2] = (byte) (hexToBin(str.charAt(i)) * 16 +
					hexToBin(str.charAt(i + 1)));

		return res;
	}

	/**
	 * Convert hexadecimal character to its value.
	 *
	 * @param c The character.
	 *
	 * @return The value.
	 *
	 * @throws IllegalArgumentException If the character is invalid.
	 */
	private static int hexToBin(final char c) {

		if ((c >= '0') && (c <= '9'))
			return c - '0';
		if ((c >= 'A') && (c <= 'F'))
			return c - 'A' + 10;
		if ((c >= 'a') && (c <= 'f'))
			return c - 'a' + 10;

		throw new IllegalArgumentException(
				"Illegal character in hexadecimal string.");
	}
}
