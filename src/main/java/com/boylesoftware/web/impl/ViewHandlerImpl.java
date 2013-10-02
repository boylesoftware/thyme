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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.spi.Script;
import com.boylesoftware.web.spi.ViewHandler;
import com.boylesoftware.web.spi.ViewSender;
import com.boylesoftware.web.util.pool.PooledStringBuffer;
import com.boylesoftware.web.util.pool.StringBufferPool;


/**
 * {@link ViewHandler} implementation.
 *
 * @author Lev Himmelfarb
 */
class ViewHandlerImpl
	implements ViewHandler {

	/**
	 * Pattern for placeholders in the view id pattern.
	 */
	private static final Pattern PH_PATTERN = Pattern.compile("\\{[^}]+\\}");


	/**
	 * View id pattern.
	 */
	private final String viewIdPattern;

	/**
	 * Tells if the view id pattern has any placeholders in it.
	 */
	private final boolean viewIdPatternHasPlaceholders;

	/**
	 * If the view id pattern contains placeholders, this array contains
	 * corresponding placeholder names.
	 */
	private final String[] viewIdPatternPHNames;

	/**
	 * View script, or {@code null}.
	 */
	private final Script script;

	/**
	 * View sender.
	 */
	private final ViewSender sender;


	/**
	 * Create new view handler.
	 *
	 * @param viewIdPattern View id pattern.
	 * @param script View script, or {@code null}.
	 * @param sender View sender.
	 */
	ViewHandlerImpl(final String viewIdPattern, final Script script,
			final ViewSender sender) {

		this.viewIdPattern = viewIdPattern;
		final List<String> phNames = new ArrayList<>();
		final Matcher m = PH_PATTERN.matcher(this.viewIdPattern);
		this.viewIdPatternHasPlaceholders = m.find();
		if (this.viewIdPatternHasPlaceholders) {
			do {
				phNames.add(this.viewIdPattern.substring(m.start() + 1,
						m.end() - 1));
			} while (m.find());
		}
		this.viewIdPatternPHNames = phNames.toArray(new String[phNames.size()]);
		this.script = script;
		this.sender = sender;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ViewHandler#getScript()
	 */
	@Override
	public Script getScript() {

		return this.script;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ViewHandler#sendView(com.boylesoftware.web.spi.RouterRequest)
	 */
	@Override
	public void sendView(final RouterRequest request)
		throws IOException, ServletException {

		// prepare the final view id
		String viewId;
		if (this.viewIdPatternHasPlaceholders) {
			try (final PooledStringBuffer buf = StringBufferPool.get()) {
				final StringBuffer viewIdSB = buf.getStringBuffer();
				final Matcher m =
					buf.getMatcher(PH_PATTERN, this.viewIdPattern);
				int i = 0;
				while (m.find()) {
					final String name = this.viewIdPatternPHNames[i++];
					Object replacement = request.getAttribute(name);
					if (replacement == null)
						replacement = request.getParameter(name);
					if (replacement == null)
						replacement = "";
					m.appendReplacement(viewIdSB, replacement.toString());
				}
				m.appendTail(viewIdSB);
				viewId = viewIdSB.toString();
			}
		} else {
			viewId = this.viewIdPattern;
		}

		// call the sender
		this.sender.send(viewId, request, request.getResponse());
	}
}
