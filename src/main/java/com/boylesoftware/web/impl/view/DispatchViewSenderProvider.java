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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.spi.ViewSender;
import com.boylesoftware.web.spi.ViewSenderProvider;


/**
 * View sender implementation that dispatches the request to another resource to
 * send the view. This sender implementation can be typically used for JSP-based
 * views. The sender interprets the view id as a context-relative URI, to which
 * it forwards the request.
 *
 * <p>This implementation is also a {@link ViewSenderProvider}, which simply
 * returns its own instance.
 *
 * @author Lev Himmelfarb
 */
public class DispatchViewSenderProvider
	implements ViewSenderProvider, ViewSender {

	/**
	 * The log.
	 */
	private final Log log = LogFactory.getLog(this.getClass());


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ViewSenderProvider#getViewSender(javax.servlet.ServletContext)
	 */
	@Override
	public ViewSender getViewSender(final ServletContext sc) {

		return this;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ViewSender#send(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void send(final String viewId, final HttpServletRequest request,
			final HttpServletResponse response)
		throws IOException, ServletException {

		if (request.isAsyncStarted()) {
			if (this.log.isDebugEnabled())
				this.log.debug("dispatching to " + viewId +
						" using async context");
			request.getAsyncContext().dispatch(viewId);
		} else {
			if (this.log.isDebugEnabled())
				this.log.debug("dispatching to " + viewId +
						" using request dispatcher");
			request.getRequestDispatcher(viewId).forward(request, response);
		}
	}
}
