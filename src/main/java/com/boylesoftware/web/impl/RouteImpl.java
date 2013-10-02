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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider;
import com.boylesoftware.web.spi.Route;
import com.boylesoftware.web.spi.Script;
import com.boylesoftware.web.spi.ViewSender;


/**
 * {@link Route} implementation.
 *
 * @author Lev Himmelfarb
 */
class RouteImpl
	implements Route {

	/**
	 * Route id.
	 */
	private final String id;

	/**
	 * Request URI pattern.
	 */
	private final Pattern uriPattern;

	/**
	 * Names of parameters extracted from the URI pattern.
	 */
	private final String[] uriParamNames;

	/**
	 * Request URI template.
	 */
	private final String uriTemplate;

	/**
	 * Security mode.
	 */
	private final SecurityMode securityMode;

	/**
	 * Script associated with the mapping, or {@code null}.
	 */
	private final Script script;

	/**
	 * Associated controller handler, or {@code null}.
	 */
	private final ControllerHandlerImpl controllerHandler;

	/**
	 * Associated view handler.
	 */
	private final ViewHandlerImpl viewHandler;

	/**
	 * Number of times this mapping has been matched.
	 */
	private AtomicLong numMatched;


	/**
	 * Create new mapping.
	 *
	 * @param sc Servlet context.
	 * @param id Route id, or {@code null} to auto-generate based on the URI
	 * pattern.
	 * @param uriPattern Request URI pattern, which is a server root relative
	 * URI that can optionally contain URI parameter placeholders. Each
	 * placeholder is an expression surrounded by curly braces. Inside curly
	 * braces there is the parameter name and optionally, separated by a colon,
	 * the value regular expression. The regular expression must not contain any
	 * capturing groups and if the expression contains curly braces, they must
	 * be balanced. If the parameter name is empty, the value is not converted
	 * to a request parameter. If the regular expression is unspecified, regular
	 * expression that matches anything except "/" is used. Each URI parameter
	 * is converted to a regular request parameter with the specified name.
	 * @param securityMode Security mode.
	 * @param commonScript Additional logic associated with the mapping, or
	 * {@code null} if none. If specified, the script is executed each time for
	 * the matched request before the controller is called and the view is sent
	 * back to the client.
	 * @param controller Controller, or {@code null} if no controller is
	 * associated with the mapped resource.
	 * @param argHandlerProvider Controller method argument handler provider to
	 * use.
	 * @param viewIdPattern Mapped resource view id. The id may contain
	 * placeholders for request attributes and parameters in curly braces with
	 * the attribute or parameter name inside. Attributes take precedence over
	 * parameters.
	 * @param viewScript Additional logic associated with the view, or
	 * {@code null} if none. If specified, the script is executed each time
	 * before the view is sent to the client. The script's purpose it to prepare
	 * data used by the view.
	 * @param viewSender View sender to use to send the view to the client.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	RouteImpl(final ServletContext sc, final String id,
			final String uriPattern, final SecurityMode securityMode,
			final Script commonScript, final Object controller,
			final ControllerMethodArgHandlerProvider argHandlerProvider,
			final String viewIdPattern, final Script viewScript,
			final ViewSender viewSender)
		throws UnavailableException {

		// parse URI pattern and template
		final StringBuilder uriTemplateSB = new StringBuilder();
		final StringBuilder uriPatternSB = new StringBuilder();
		final List<String> uriParamNamesList = new ArrayList<>();
		final StringBuilder paramNameSB = new StringBuilder();
		final StringBuilder paramPatternSB = new StringBuilder();
		final int patternLen = uriPattern.length();
		uriPatternSB.append("\\Q");
		for (int cInd = 0; cInd < patternLen; cInd++) {
			final char c = uriPattern.charAt(cInd);
			if (c == '{') {

				try {
					paramNameSB.setLength(0);
					paramPatternSB.setLength(0);
					StringBuilder curSB = paramNameSB;
					int balance = 1;
					while (balance > 0) {
						final char c1 = uriPattern.charAt(++cInd);
						switch (c1) {
						case '{':
							balance++;
							curSB.append(c1);
							break;
						case '}':
							if (--balance > 0)
								curSB.append(c1);
							break;
						case ':':
							if (curSB != paramPatternSB)
								curSB = paramPatternSB;
							else
								curSB.append(c1);
							break;
						default:
							curSB.append(c1);
						}
					}
				} catch (final IndexOutOfBoundsException e) {
					LogFactory.getLog(this.getClass()).error(
							"error parsing mapping URI pattern", e);
					throw new UnavailableException(
							"Unbalanced curly braces in URI pattern \"" +
							uriPattern + "\" at position " + cInd + ".");
				}

				uriParamNamesList.add(paramNameSB.length() > 0 ?
						paramNameSB.toString() : null);

				uriPatternSB.append("\\E(")
				.append(paramPatternSB.length() > 0 ? paramPatternSB : "[^/]+")
				.append(")\\Q");

				uriTemplateSB.append("%");

			} else { // character outside parameter placeholder
				uriPatternSB.append(c);
				uriTemplateSB.append(c);
			}
		}
		uriPatternSB.append("\\E");
		this.uriPattern = Pattern.compile(uriPatternSB.toString());
		this.uriParamNames =
			uriParamNamesList.toArray(new String[uriParamNamesList.size()]);
		this.uriTemplate = uriTemplateSB.toString();

		// route id
		this.id = (id != null ? id :
			this.uriTemplate.substring(sc.getContextPath().length()));

		// save security mode
		this.securityMode = securityMode;

		// save the script
		this.script = commonScript;

		// create controller handler
		this.controllerHandler = (controller != null ?
				new ControllerHandlerImpl(sc, controller, argHandlerProvider) :
					null);

		// create view handler
		this.viewHandler =
			new ViewHandlerImpl(viewIdPattern, viewScript, viewSender);

		// initially the mapping has not been matched
		this.numMatched = new AtomicLong();
	}

	/**
	 * Create new mapping using an existing mapping, but with a different
	 * security mode.
	 *
	 * @param baseMapping Base mapping.
	 * @param securityMode New security mode.
	 */
	RouteImpl(final RouteImpl baseMapping, final SecurityMode securityMode) {

		this.id = baseMapping.id;
		this.uriPattern = baseMapping.uriPattern;
		this.uriParamNames = baseMapping.uriParamNames;
		this.uriTemplate = baseMapping.uriTemplate;
		this.script = baseMapping.script;
		this.controllerHandler = baseMapping.controllerHandler;
		this.viewHandler = baseMapping.viewHandler;

		this.numMatched = new AtomicLong();

		this.securityMode = securityMode;
	}


	/**
	 * Atomically get number of times this mapping has been matched.
	 *
	 * @return Number of times this mapping has been matched.
	 */
	long getNumMatched() {

		return this.numMatched.get();
	}

	/**
	 * Atomically increment the number of times this mapping has been matched.
	 *
	 * @return The new value.
	 */
	long incrementNumMatched() {

		return this.numMatched.incrementAndGet();
	}

	/**
	 * Get route id.
	 *
	 * @return Route id.
	 */
	String getId() {

		return this.id;
	}

	/**
	 * Get pattern for matching against the request URI.
	 *
	 * @return Request URI pattern.
	 */
	Pattern getURIPattern() {

		return this.uriPattern;
	}

	/**
	 * Get URI parameter name for the specified index.
	 *
	 * @param ind Zero-based parameter index.
	 *
	 * @return Parameter name, or {@code null} if placeholder in the URI pattern
	 * does not contain a name.
	 */
	String getURIParamName(final int ind) {

		return this.uriParamNames[ind];
	}

	/**
	 * Get number of URI parameters in the URI pattern.
	 *
	 * @return Number of URI parameters.
	 */
	int getNumURIParams() {

		return this.uriParamNames.length;
	}

	/**
	 * Get URI template, which the is the URI pattern with all URI parameter
	 * placeholders, if any, replaced with "%". The template may be used to
	 * construct request URIs that would match the mapping.
	 *
	 * @return The URI template.
	 */
	String getURITemplate() {

		return this.uriTemplate;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.Route#getScript()
	 */
	@Override
	public Script getScript() {

		return this.script;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.Route#getControllerHandler()
	 */
	@Override
	public ControllerHandlerImpl getControllerHandler() {

		return this.controllerHandler;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.Route#getViewHandler()
	 */
	@Override
	public ViewHandlerImpl getViewHandler() {

		return this.viewHandler;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.Route#getSecurityMode()
	 */
	@Override
	public SecurityMode getSecurityMode() {

		return this.securityMode;
	}
}
