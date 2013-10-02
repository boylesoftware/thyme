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

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * {@link AsyncContext} wrapper.
 *
 * @author Lev Himmelfarb
 */
public class AsyncContextWrapper
	implements AsyncContext {

	/**
	 * Wrapper context.
	 */
	private AsyncContext asyncContext;


	/**
	 * Set wrapped context.
	 *
	 * @param asyncContext Context to wrap.
	 */
	protected void setAsyncContext(final AsyncContext asyncContext) {

		this.asyncContext = asyncContext;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#getRequest()
	 */
	@Override
	public ServletRequest getRequest() {

		return this.asyncContext.getRequest();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#getResponse()
	 */
	@Override
	public ServletResponse getResponse() {

		return this.asyncContext.getResponse();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#hasOriginalRequestAndResponse()
	 */
	@Override
	public boolean hasOriginalRequestAndResponse() {

		return this.asyncContext.hasOriginalRequestAndResponse();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#dispatch()
	 */
	@Override
	public void dispatch() {

		this.asyncContext.dispatch();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#dispatch(java.lang.String)
	 */
	@Override
	public void dispatch(final String path) {

		this.asyncContext.dispatch(path);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#dispatch(javax.servlet.ServletContext, java.lang.String)
	 */
	@Override
	public void dispatch(final ServletContext context, final String path) {

		this.asyncContext.dispatch(context, path);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#complete()
	 */
	@Override
	public void complete() {

		this.asyncContext.complete();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#start(java.lang.Runnable)
	 */
	@Override
	public void start(final Runnable run) {

		this.asyncContext.start(run);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#addListener(javax.servlet.AsyncListener)
	 */
	@Override
	public void addListener(final AsyncListener listener) {

		this.asyncContext.addListener(listener);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#addListener(javax.servlet.AsyncListener, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	@Override
	public void addListener(final AsyncListener listener,
			final ServletRequest servletRequest,
			final ServletResponse servletResponse) {

		this.asyncContext.addListener(listener, servletRequest,
				servletResponse);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#createListener(java.lang.Class)
	 */
	@Override
	public <T extends AsyncListener> T createListener(final Class<T> clazz)
		throws ServletException {

		return this.asyncContext.createListener(clazz);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#setTimeout(long)
	 */
	@Override
	public void setTimeout(final long timeout) {

		this.asyncContext.setTimeout(timeout);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.AsyncContext#getTimeout()
	 */
	@Override
	public long getTimeout() {

		return this.asyncContext.getTimeout();
	}
}
