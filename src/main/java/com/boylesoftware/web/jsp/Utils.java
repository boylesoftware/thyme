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
package com.boylesoftware.web.jsp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.boylesoftware.web.util.pool.PooledStringBuffer;
import com.boylesoftware.web.util.pool.StringBufferPool;


/**
 * Utilities for tag implementations.
 *
 * @author Lev Himmelfarb
 */
final class Utils {

	/**
	 * Pattern for special characters in HTML.
	 */
	private static final Pattern HTML_SPEC_CHAR_PATTERN =
		Pattern.compile("[&<>]");

	/**
	 * Pattern for special characters in HTML tag attribute values.
	 */
	private static final Pattern ATT_SPEC_CHAR_PATTERN =
		Pattern.compile("[\"&<>]");


	/**
	 * All methods are static.
	 */
	private Utils() {}


	/**
	 * Escape HTML.
	 *
	 * @param src The original value.
	 *
	 * @return Value, safe for HTML.
	 */
	static String escapeHtml(final String src) {

		try (final PooledStringBuffer buf = StringBufferPool.get()) {
			final StringBuffer sb = buf.getStringBuffer();

			boolean replaced = false;
			final Matcher m = buf.getMatcher(HTML_SPEC_CHAR_PATTERN, src);
			while (m.find()) {
				replaced = true;
				switch (m.group().charAt(0)) {
				case '&':
					m.appendReplacement(sb, "&amp;");
					break;
				case '<':
					m.appendReplacement(sb, "&lt;");
					break;
				case '>':
					m.appendReplacement(sb, "&gt;");
				}
			}

			if (replaced) {
				m.appendTail(sb);
				return sb.toString();
			}
		}

		return src;
	}

	/**
	 * Escape HTML tag attribute value.
	 *
	 * @param src The original value.
	 *
	 * @return Value, safe for an HTML tag attribute.
	 */
	static String escapeHtmlAttr(final String src) {

		try (final PooledStringBuffer buf = StringBufferPool.get()) {
			final StringBuffer sb = buf.getStringBuffer();

			boolean replaced = false;
			final Matcher m = buf.getMatcher(ATT_SPEC_CHAR_PATTERN, src);
			while (m.find()) {
				replaced = true;
				switch (m.group().charAt(0)) {
				case '"':
					m.appendReplacement(sb, "&quot;");
					break;
				case '&':
					m.appendReplacement(sb, "&amp;");
					break;
				case '<':
					m.appendReplacement(sb, "&lt;");
					break;
				case '>':
					m.appendReplacement(sb, "&gt;");
				}
			}

			if (replaced) {
				m.appendTail(sb);
				return sb.toString();
			}
		}

		return src;
	}
}
