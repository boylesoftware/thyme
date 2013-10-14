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
package com.boylesoftware.web.impl.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.spi.ViewSender;


/**
 * View sender implementation that wraps a collection of other view senders and
 * picks one depending on the view id pattern.
 *
 * @author Lev Himmelfarb
 */
public class MultiplexViewSender
	implements ViewSender {

	/**
	 * View id pattern rule.
	 */
	private static final class Rule {

		/**
		 * View id pattern.
		 */
		private final Pattern pattern;

		/**
		 * Corresponding view sender.
		 */
		private final ViewSender sender;


		/**
		 * Create new rule.
		 *
		 * @param pattern View id pattern.
		 * @param sender Corresponding view sender.
		 */
		Rule(final Pattern pattern, final ViewSender sender) {

			this.pattern = pattern;
			this.sender = sender;
		}


		/**
		 * Get pattern.
		 *
		 * @return The pattern.
		 */
		Pattern getPattern() {

			return this.pattern;
		}

		/**
		 * Get view sender.
		 *
		 * @return The view sender.
		 */
		ViewSender getSender() {

			return this.sender;
		}
	}


	/**
	 * View id matching rules.
	 */
	private final ArrayList<Rule> rules = new ArrayList<>();


	/**
	 * Associate the specified view id pattern with the corresponding view
	 * sender. The order in which this method is called is important. Patterns
	 * added earlier are matched first.
	 *
	 * <p>This method is not thread-safe and is supposed to be called only
	 * during the application initialization.
	 *
	 * @param pattern View id regular expression.
	 * @param sender View sender.
	 *
	 * @return This multiplex view sender.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	public MultiplexViewSender addPattern(final String pattern,
			final ViewSender sender)
		throws UnavailableException {

		try {
			this.rules.add(new Rule(Pattern.compile(pattern), sender));
		} catch (final PatternSyntaxException e) {
			throw new UnavailableException("Invalid view id pattern: " +
					e.getMessage());
		}

		return this;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ViewSender#send(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void send(final String viewId, final HttpServletRequest request,
			final HttpServletResponse response)
		throws IOException, ServletException {

		ViewSender sender = null;
		final Iterator<Rule> i = this.rules.iterator();
		Rule rule = i.next();
		final Matcher m = rule.getPattern().matcher(viewId);
		if (m.matches()) {
			sender = rule.getSender();
		} else {
			while (i.hasNext()) {
				rule = i.next();
				m.usePattern(rule.getPattern());
				m.reset();
				if (m.matches()) {
					sender = rule.getSender();
					break;
				}
			}
		}

		if (sender == null)
			throw new ServletException("No matching view sender for view id " +
					viewId + ".");

		sender.send(viewId, request, response);
	}
}
