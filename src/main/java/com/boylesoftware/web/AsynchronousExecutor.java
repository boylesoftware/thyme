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

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.util.pool.AbstractPoolable;
import com.boylesoftware.web.util.pool.FastPool;


/**
 * Base class for asynchronous executors used by the {@link Router}.
 *
 * @author Lev Himmelfarb
 */
abstract class AsynchronousExecutor
	extends AbstractPoolable
	implements Runnable, AsyncListener {

	/**
	 * Default timeout in milliseconds.
	 */
	private static final Long DEFAULT_TIMEOUT = Long.valueOf(10000L);


	/**
	 * Timeout exception.
	 */
	private static final class TimeOutException
		extends RuntimeException {

		/**
		 * Serial version id.
		 */
		private static final long serialVersionUID = 1L;


		/**
		 * Create new exception.
		 */
		TimeOutException() {}
	}


	/**
	 * The log.
	 */
	protected final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Tells if the asynchronous processing has timed out.
	 */
	private boolean timedOut;

	/**
	 * Thread running the executor.
	 */
	private Thread executorThread;

	/**
	 * The application.
	 */
	protected AbstractWebApplication webapp;

	/**
	 * Request being processed.
	 */
	protected RouterRequest routerReq;

	/**
	 * Asynchronous request processing context.
	 */
	protected AsyncContext asyncContext;


	/**
	 * Create new executor.
	 *
	 * @param pool Reference to the pool.
	 * @param pooledObjectId Pooled object id.
	 */
	protected AsynchronousExecutor(
			final FastPool<? extends AsynchronousExecutor> pool,
			final int pooledObjectId) {
		super(pool, pooledObjectId);
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.util.pool.AbstractPoolable#recycle()
	 */
	@Override
	public void recycle() {

		if (this.log.isDebugEnabled())
			this.log.debug("recycling async executor for request " +
					this.routerReq);

		this.cleanup();

		this.executorThread = null;
		this.routerReq = null;
		this.asyncContext = null;
		this.webapp = null;

		super.recycle();
	}


	/**
	 * Initialize the executor and put the request to asynchronous mode.
	 *
	 * @param webapp The application.
	 * @param request The request.
	 *
	 * @return The new asynchronous context.
	 */
	protected AsyncContext init(final AbstractWebApplication webapp,
			final RouterRequest request) {

		this.webapp = webapp;
		this.routerReq = request;

		this.timedOut = false;

		if (this.log.isDebugEnabled())
			this.log.debug("starting asynchronous request processing");
		this.asyncContext = this.routerReq.startAsync();
		this.asyncContext.addListener(this);
		this.asyncContext.setTimeout(webapp.getConfigProperty(
				ApplicationConfiguration.ASYNC_TIMEOUT, Long.class,
				DEFAULT_TIMEOUT).longValue());

		this.routerReq.rewrap(
				(HttpServletRequest) this.asyncContext.getRequest(),
				(HttpServletResponse) this.asyncContext.getResponse());

		return this.asyncContext;
	}

	/**
	 * Check if asynchronous processing has timed out. If so, the method throws
	 * a special internal runtime exception.
	 */
	protected void checkTimeout() {

		if (this.timedOut)
			throw new TimeOutException();
	}

	/**
	 * Execute the logic.
	 *
	 * @throws Exception If error happens that needs to be passed back to the
	 * router.
	 */
	protected abstract void execute()
		throws Exception;

	/**
	 * Clean up the instance before recycling it. Can be overridden in a
	 * subclass. Default implementation does nothing.
	 */
	protected void cleanup() {

		// nothing
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		Thread.interrupted();

		this.executorThread = Thread.currentThread();

		final boolean debug = this.log.isDebugEnabled();
		if (debug)
			this.log.debug("started async executor for request " +
					this.routerReq + (this.routerReq != null ?
							" [" + this.routerReq.getRequestURI() + "]" : ""));
		try {

			this.checkTimeout();

			this.execute();

		} catch (final TimeOutException e) {
			if (debug)
				this.log.debug("timeout", e);
		} catch (final Exception e) {
			if (debug)
				this.log.debug("error in async executor", e);
			try {
				Router.setAsyncException(this.routerReq, e);
				if (debug)
					this.log.debug("dispatching back to the router");
				this.asyncContext.dispatch();
			} catch (final Exception e1) {
				if (debug)
					this.log.debug("error dispatching async executor error" +
							" back to the router", e1);
			}
		} finally {
			if (debug)
				this.log.debug("exiting async executor");
		}
	}


	/* (non-Javadoc)
	 * @see javax.servlet.AsyncListener#onComplete(javax.servlet.AsyncEvent)
	 */
	@Override
	public void onComplete(final AsyncEvent event) {

		final boolean debug = this.log.isDebugEnabled();
		if (debug)
			this.log.debug("async event: complete");

		if (this.routerReq != null) {
			if (debug)
				this.log.debug("recycling router request " + this.routerReq);
			RouterRequestLifecycle.recycle(this.routerReq);
		}

		this.recycle();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncListener#onTimeout(javax.servlet.AsyncEvent)
	 */
	@Override
	public void onTimeout(final AsyncEvent event) {

		final boolean debug = this.log.isDebugEnabled();
		if (debug)
			this.log.debug("async event: timeout");

		this.timedOut = true;

		this.cleanup();

		if (debug)
			this.log.debug("recycling router request " + this.routerReq);
		RouterRequestLifecycle.recycle(this.routerReq);

		this.routerReq = null;
		this.asyncContext = null;
		this.webapp = null;

		if (this.executorThread != null)
			this.executorThread.interrupt();

		final AsyncContext asyncCtx = event.getAsyncContext();
		Router.setAsyncException(asyncCtx.getRequest(),
				new ServiceUnavailableException());
		if (debug)
			this.log.debug("dispatching service unavailable exception back to" +
					" the router");
		asyncCtx.dispatch();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncListener#onError(javax.servlet.AsyncEvent)
	 */
	@Override
	public void onError(final AsyncEvent event) {

		if (this.log.isDebugEnabled())
			this.log.debug("async event: error");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncListener#onStartAsync(javax.servlet.AsyncEvent)
	 */
	@Override
	public void onStartAsync(final AsyncEvent event) {

		if (this.log.isDebugEnabled())
			this.log.debug("async event: startAsync");

		this.recycle();
	}
}
