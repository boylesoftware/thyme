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
package com.boylesoftware.web.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.api.FlashAttributes;
import com.boylesoftware.web.util.StringUtils;
import com.boylesoftware.web.util.pool.PooledStringBuffer;
import com.boylesoftware.web.util.pool.StringBufferPool;


/**
 * Flash attributes implementation.
 *
 * @author Lev Himmelfarb
 */
class FlashAttributesImpl
	implements FlashAttributes {

	/**
	 * Name of the HTTP cookie used to store flash scope attributes on the
	 * client side.
	 */
	private static final String FLASH_COOKIE = "BSWEBFL";

	/**
	 * Pattern used to extract attributes from the cookie value.
	 */
	private static final Pattern ATTR_PATTERN =
		Pattern.compile("(?:^|\\G&)([^=]+)=([^&]*(?:(?<=\\\\)&[^&]*)*)");

	/**
	 * Pattern used to replace escaped ampersands in attribute values.
	 */
	private static final Pattern ESCAPED_AMP_PATTERN =
		Pattern.compile("\\\\&");

	/**
	 * Pattern used to escape ampersands in attribute values.
	 */
	private static final Pattern AMP_PATTERN =
		Pattern.compile("&");


	/**
	 * Matcher used to extract attributes from the cookie value.
	 */
	private final Matcher attrMatcher;

	/**
	 * Matcher used to replace escaped ampersands in attribute values.
	 */
	private final Matcher escapedAmpMatcher;

	/**
	 * Matcher used to escape ampersands in attribute values.
	 */
	private final Matcher ampMatcher;

	/**
	 * The attributes.
	 */
	private final Map<String, String> attributes;

	/**
	 * Tells if there is a flash attributes cookie sent with the current
	 * request.
	 */
	private boolean hasFlashCookie;


	/**
	 * Create new instance.
	 */
	FlashAttributesImpl() {

		this.attrMatcher = ATTR_PATTERN.matcher("");
		this.escapedAmpMatcher = ESCAPED_AMP_PATTERN.matcher("");
		this.ampMatcher = AMP_PATTERN.matcher("");

		this.attributes = new HashMap<>();
	}


	/**
	 * Convert flash cookie to request attributes.
	 *
	 * @param request The HTTP request.
	 *
	 * @throws ServletException If the cookie value has invalid syntax.
	 */
	void flashCookieToAttributes(final HttpServletRequest request)
		throws ServletException {

		Cookie cookie = null;
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (final Cookie c : cookies) {
				if (c.getName().equals(FLASH_COOKIE)) {
					cookie = c;
					break;
				}
			}
		}

		if (cookie == null) {
			this.hasFlashCookie = false;
			return;
		}
		this.hasFlashCookie = true;

		final String cookieVal = cookie.getValue();
		this.attrMatcher.reset(cookieVal);
		int lastEnd = 0;
		while (this.attrMatcher.find()) {
			final String attrName = this.attrMatcher.group(1);
			final String attrValRaw = this.attrMatcher.group(2);
			final String attrVal = (attrValRaw.indexOf("\\&") >= 0 ?
					this.escapedAmpMatcher.reset(attrValRaw).replaceAll("&") :
						attrValRaw);
			request.setAttribute(attrName, attrVal);
			lastEnd = this.attrMatcher.end();
		}

		if (lastEnd != cookieVal.length())
			throw new ServletException(
					"Invalid flash attributes cookie value.");
	}

	/**
	 * Convert flash attributes to a cookie.
	 *
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 */
	void flashAttributesToCookie(final HttpServletRequest request,
			final HttpServletResponse response) {

		if (this.attributes.isEmpty() && !this.hasFlashCookie)
			return;

		String cookieVal;
		try (final PooledStringBuffer buf = StringBufferPool.get()) {
			final StringBuffer sb = buf.getStringBuffer();

			for (final Map.Entry<String, String> entry :
					this.attributes.entrySet()) {
				if (sb.length() > 0)
					sb.append('&');
				sb.append(entry.getKey()).append('=');
				final String valRaw = entry.getValue();
				if (valRaw.indexOf('&') < 0) {
					sb.append(valRaw);
				} else {
					this.ampMatcher.reset(valRaw);
					while (this.ampMatcher.find())
						this.ampMatcher.appendReplacement(sb, "\\&");
					this.ampMatcher.appendTail(sb);
				}
			}

			cookieVal = sb.toString();
		}

		final Cookie cookie = new Cookie(FLASH_COOKIE, cookieVal);
		cookie.setPath(StringUtils.emptyIfNull(request.getContextPath()) + "/");
		if (cookieVal.length() == 0)
			cookie.setMaxAge(0);

		response.addCookie(cookie);
	}

	/**
	 * Clear attributes.
	 */
	void clear() {

		this.attributes.clear();
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.FlashAttributes#setAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute(final String name, final String value) {

		if (name.indexOf('=') >= 0)
			throw new IllegalArgumentException(
					"Flash attribute name cannot contain \"=\" characters.");

		this.attributes.put(name, value);
	}
}
