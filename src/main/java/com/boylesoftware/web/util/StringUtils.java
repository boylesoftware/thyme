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


/**
 * Collection of static utility methods for strings.
 *
 * @author Lev Himmelfarb
 */
public final class StringUtils {

	/**
	 * Only static methods.
	 */
	private StringUtils() {}


	/**
	 * Check if specified string is {@code null} and return an empty string if
	 * so.
	 *
	 * @param str String to test, may be {@code null}.
	 *
	 * @return The original string, or an empty string if the original string
	 * was {@code null}.
	 */
	public static String emptyIfNull(final String str) {

		return (str != null ? str : "");
	}

	/**
	 * Trim the specified string and make it {@code null} if the result is an
	 * empty string.
	 *
	 * @param str String to trim, may be {@code null}.
	 *
	 * @return Trimmed string, or {@code null} if the string was {@code null},
	 * empty or blank.
	 */
	public static String trimToNull(final String str) {

		if (str == null)
			return str;

		final String res = str.trim();
		if (res.length() == 0)
			return null;

		return res;
	}

	/**
	 * Check if specified string is empty and return {@code null} if so.
	 *
	 * @param str String to test, may be {@code null}.
	 *
	 * @return The original string, or {@code null} if the string was
	 * {@code null} or empty.
	 */
	public static String nullIfEmpty(final String str) {

		if (str == null)
			return str;

		return (str.length() > 0 ? str : null);
	}
}
