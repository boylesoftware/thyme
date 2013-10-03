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
package com.boylesoftware.web;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.spi.RouterRequest;


/**
 * Listener of the router request lifecycle used to ensure framework integrity.
 *
 * @author Lev Himmelfarb
 */
@WebListener
public class RouterRequestLifecycle
	implements ServletRequestListener {

	/**
	 * Name of request attribute used to store the router request.
	 */
	private static final String ROUTER_REQ_ATTNAME =
		(RouterRequestLifecycle.class).getName() + ".ROUTER_REQ";


	/**
	 * The log.
	 */
	private final Log log = LogFactory.getLog(this.getClass());


	/**
	 * Associate router request with the container request.
	 *
	 * @param routerReq The router request.
	 */
	static void associate(final RouterRequest routerReq) {

		routerReq.setAttribute(ROUTER_REQ_ATTNAME, routerReq);
	}

	/**
	 * Get router request associated with the container request.
	 *
	 * @param request Container request.
	 *
	 * @return Associated router request, or {@code null}.
	 */
	static RouterRequest restore(final ServletRequest request) {

		return (RouterRequest) request.getAttribute(ROUTER_REQ_ATTNAME);
	}

	/**
	 * Complete router request active lifecycle. Called before sending the view.
	 *
	 * @param routerReq The router request.
	 */
	static void complete(final RouterRequest routerReq) {

		routerReq.removeAttribute(ROUTER_REQ_ATTNAME);

		routerReq.commitFlashAttributes();
	}

	/**
	 * Disassociate router request from the container request and recycle it.
	 *
	 * @param routerReq The router request.
	 */
	static void recycle(final RouterRequest routerReq) {

		routerReq.removeAttribute(ROUTER_REQ_ATTNAME);
		try {
			routerReq.recycle();
		} catch (final Exception e) {
			LogFactory.getLog(RouterRequestLifecycle.class).fatal(
					"error recycling router request", e);
			// TODO: do we have to be that brutal?
			System.exit(1);
		}
	}


	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
	 */
	@Override
	public void requestInitialized(final ServletRequestEvent sre) {

		if (this.log.isDebugEnabled())
			this.log.debug(">>> request initialized");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestListener#requestDestroyed(javax.servlet.ServletRequestEvent)
	 */
	@Override
	public void requestDestroyed(final ServletRequestEvent sre) {

		if (this.log.isDebugEnabled())
			this.log.debug("<<< request destroyed");
	}
}
