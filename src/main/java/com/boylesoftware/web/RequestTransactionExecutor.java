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

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.boylesoftware.web.spi.ControllerHandler;
import com.boylesoftware.web.spi.ControllerMethodHandler;
import com.boylesoftware.web.spi.Route;
import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.spi.Script;
import com.boylesoftware.web.spi.ViewHandler;
import com.boylesoftware.web.util.LooseCannon;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;


/**
 * Executes the request processing transaction.
 *
 * @author Lev Himmelfarb
 */
class RequestTransactionExecutor
	extends AsynchronousExecutor {

	/**
	 * The instance pool.
	 */
	private static final FastPool<RequestTransactionExecutor> POOL =
		new FastPool<>(new PoolableObjectFactory<RequestTransactionExecutor>() {

			@Override
			public RequestTransactionExecutor makeNew(
					final FastPool<RequestTransactionExecutor> pool,
					final int pooledObjectId) {

				return new RequestTransactionExecutor(pooledObjectId);
			}
		}, "RequestTransactionExecutorsPool");

	/**
	 * Get executor instance from the internal pool. The instance must be
	 * recycled after it is no longer needed.
	 *
	 * @return Executor instance.
	 */
	static RequestTransactionExecutor getExecutor() {

		return POOL.getSync();
	}


	/**
	 * Finisher used to send redirect responses.
	 */
	private static final class RedirectFinisher {

		/**
		 * Redirection target URL.
		 */
		private String location;


		/**
		 * Create new finisher.
		 */
		RedirectFinisher() {}


		/**
		 * Set redirection target URL.
		 *
		 * @param location Target URL.
		 */
		void setLocation(final String location) {

			this.location = location;
		}

		/**
		 * Execute the finisher.
		 *
		 * @param executor The executor.
		 */
		void execute(final AsynchronousExecutor executor) {

			final HttpServletResponse response =
				(HttpServletResponse) executor.asyncContext.getResponse();
			response.setStatus(HttpServletResponse.SC_SEE_OTHER);
			response.setHeader("Location", this.location);

			LooseCannon.heel();

			executor.asyncContext.complete();
		}
	}


	/**
	 * Finisher used to send views.
	 */
	private static final class SendViewFinisher {

		/**
		 * Tells if response needs to be "bad request".
		 */
		private boolean badRequest;


		/**
		 * Create new finisher.
		 */
		SendViewFinisher() {}


		/**
		 * Set flag telling if the response needs to be a "bad request"
		 * response.
		 *
		 * @param badRequest {@code true} to send "bad request" response.
		 */
		void setBadRequest(final boolean badRequest) {

			this.badRequest = badRequest;
		}

		/**
		 * Execute the finisher.
		 *
		 * @param executor The executor.
		 *
		 * @throws ServletException If view handler throws it.
		 * @throws IOException If view handler throws it.
		 */
		void execute(final AsynchronousExecutor executor)
			throws ServletException, IOException {

			if (this.badRequest) {
				final HttpServletResponse response =
					(HttpServletResponse) executor.asyncContext.getResponse();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

			LooseCannon.heel();

			executor.routerReq.getRoute().getViewHandler().sendView(
					executor.routerReq);
		}
	}


	/**
	 * Redirect response finisher.
	 */
	private final RedirectFinisher redirectFinisher = new RedirectFinisher();

	/**
	 * View response finisher.
	 */
	private final SendViewFinisher sendViewFinisher = new SendViewFinisher();

	/**
	 * The route.
	 */
	private Route route;

	/**
	 * Route script, if any.
	 */
	private Script routeScript;

	/**
	 * Controller handler, if any.
	 */
	private ControllerHandler controllerHandler;

	/**
	 * Controller method handler, if any.
	 */
	private ControllerMethodHandler methodHandler;

	/**
	 * Tells if user input has been validated successfully.
	 */
	private boolean userInputValid;

	/**
	 * Tells if controller has view preparation method.
	 */
	private boolean hasViewPrep;

	/**
	 * View script, if any.
	 */
	private Script viewScript;


	/**
	 * Create new executor. This constructor is for internal use only. Use
	 * {@link #getExecutor()} to get instances from the pool.
	 *
	 * @param pooledObjectId Pooled object id.
	 */
	RequestTransactionExecutor(final int pooledObjectId) {
		super(POOL, pooledObjectId);
	}


	/**
	 * Prepare for asynchronous request processing transaction.
	 *
	 * @param webapp The application.
	 * @param request The request.
	 *
	 * @return {@code true} if needs to proceed with the asynchronous
	 * processing, {@code false} if no need for transaction.
	 *
	 * @throws ServletException If an error happens.
	 * @throws IOException If an I/O error happens sending the response.
	 */
	boolean prepare(final AbstractWebApplication webapp,
			final RouterRequest request)
		throws ServletException, IOException {

		final boolean debug = this.log.isDebugEnabled();

		// retrieve objects that we'll need for request processing
		this.route = request.getRoute();
		this.routeScript = this.route.getScript();
		this.controllerHandler = this.route.getControllerHandler();
		this.methodHandler = request.getControllerMethodHandler();
		final ViewHandler viewHandler = this.route.getViewHandler();
		this.viewScript = viewHandler.getScript();

		// prepare and validate user input
		this.userInputValid = ((this.methodHandler == null) ||
				this.methodHandler.prepareUserInput(request));
		if (debug && !this.userInputValid)
			this.log.debug("user input is invalid");

		// controller has view preparation logic?
		this.hasViewPrep = (this.controllerHandler != null ?
				this.controllerHandler.hasPrepareView(request) : false);

		// no need for transaction?
		if ((this.routeScript == null) && (this.viewScript == null) &&
				!this.hasViewPrep &&
				(!this.userInputValid || (this.methodHandler == null))) {
			if (debug)
				this.log.debug("no need for transaction, sending the view");

			RouterRequestLifecycle.complete(request);

			if (!this.userInputValid)
				request.getResponse().setStatus(
						HttpServletResponse.SC_BAD_REQUEST);

			LooseCannon.heel();

			viewHandler.sendView(request);

			return false;
		}

		// need transaction, start asynchronous processing
		this.init(webapp, request);

		// need transaction
		return true;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.AsynchronousExecutor#cleanup()
	 */
	@Override
	protected void cleanup() {

		this.redirectFinisher.setLocation(null);
		this.sendViewFinisher.setBadRequest(false);

		this.route = null;
		this.routeScript = null;
		this.controllerHandler = null;
		this.methodHandler = null;
		this.viewScript = null;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.AsynchronousExecutor#execute()
	 */
	@Override
	protected void execute()
		throws RequestedResourceException, ServletException, IOException {

		final boolean debug = this.log.isDebugEnabled();

		// flag telling whether to send the view or not
		boolean sendView;

		// determine if we need entity manager and transaction
		if ((this.routeScript == null) && (this.viewScript == null) &&
				((this.methodHandler == null) ||
						!this.methodHandler.needsEntityManager()) &&
				(!this.hasViewPrep ||
						!this.controllerHandler
								.prepareViewNeedsEntityManager())) {
			if (debug)
				this.log.debug("entity manager is not used," +
						" no need for transaction");

			// call the controller
			sendView = this.callController(null);

			// perform user cache evictions
			this.webapp.getAuthenticationService().performCacheEvictions(
					this.routerReq);
			LooseCannon.heel();
			this.checkTimeout();

			// call controller's prepare view method
			if (sendView && this.hasViewPrep) {
				this.controllerHandler.prepareView(this.routerReq, null);
				LooseCannon.heel();
				this.checkTimeout();
			}

		} else { // needs transaction

			if (debug)
				this.log.debug("creating entity manager");
			final EntityManager em =
				this.webapp.getEntityManagerFactory().createEntityManager();
			try {

				// start transaction
				final EntityTransaction tx = em.getTransaction();
				if (debug)
					this.log.debug("begin transaction");
				tx.begin();
				boolean success = false;
				try {
					this.checkTimeout();

					// execute script associated with the mapping
					if (this.routeScript != null) {
						this.routeScript.execute(this.routerReq, em);
						LooseCannon.heel();
						this.checkTimeout();
					}

					// call the controller
					sendView = this.callController(em);

					// prepare the view
					if (sendView) {

						// call the view's script
						if (this.viewScript != null) {
							this.viewScript.execute(this.routerReq, em);
							LooseCannon.heel();
							this.checkTimeout();
						}

						// call controller's prepare view method
						if (this.hasViewPrep) {
							this.controllerHandler.prepareView(this.routerReq,
									em);
							LooseCannon.heel();
							this.checkTimeout();
						}
					}

					// transaction successful
					success = true;

				} finally {
					if (success) {
						if (debug)
							this.log.debug("commit transaction");
						tx.commit();
						this.webapp.getAuthenticationService()
							.performCacheEvictions(this.routerReq);
						LooseCannon.heel();
						this.checkTimeout();
					} else {
						if (debug)
							this.log.debug("rollback transaction");
						tx.rollback();
						LooseCannon.heel();
					}
				}

			} finally {
				if (debug)
					this.log.debug("closing entity manager");
				em.close();
			}
		}

		// show the view or send the redirect
		RouterRequestLifecycle.complete(this.routerReq);
		if (sendView) {
			if (debug)
				this.log.debug("sending the view");
			this.sendViewFinisher.execute(this);
		} else {
			if (debug)
				this.log.debug("completing async processing");
			this.redirectFinisher.execute(this);
		}
	}

	/**
	 * Check if controller needs to be called, call it if so, and process its
	 * return value.
	 *
	 * @param em The entity manager to pass to the controller.
	 *
	 * @return {@code true} if the view needs to be displayed as a result of the
	 * controller call.
	 *
	 * @throws ServletException If controller throws it.
	 * @throws RequestedResourceException If controller throws it.
	 */
	private boolean callController(final EntityManager em)
		throws ServletException, RequestedResourceException {

		if (this.userInputValid) {
			if (this.methodHandler != null) {

				// call the controller method
				final String redirectTo =
					this.methodHandler.call(this.routerReq, em);
				LooseCannon.heel();
				this.checkTimeout();

				// redirect if necessary
				if (redirectTo != null) {
					this.redirectFinisher.setLocation(redirectTo);
					return false;
				} else if (this.methodHandler.redirectOnSuccess()) {
					this.sendViewFinisher.setBadRequest(true);
				}
			}
		} else {
			this.sendViewFinisher.setBadRequest(true);
		}

		return true;
	}
}
