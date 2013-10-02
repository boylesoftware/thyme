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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.ServletContext;

import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.ApplicationConfiguration;
import com.boylesoftware.web.spi.ExecutorServiceProvider;


/**
 * Executor service provider that providers a fixed size thread pool with number
 * of threads specified by the {@link ApplicationConfiguration#ASYNC_THREADS}
 * application configuration property. If the application configuration property
 * is undefined, default number of threads is {@value #DEFAULT_NUM_THREADS}.
 *
 * @author Lev Himmelfarb
 */
public class FixedThreadPoolExecutorServiceProvider
	implements ExecutorServiceProvider {

	/**
	 * Default number of threads.
	 */
	public static final int DEFAULT_NUM_THREADS = 10;


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ExecutorServiceProvider#getExecutorService(javax.servlet.ServletContext, com.boylesoftware.web.ApplicationConfiguration)
	 */
	@Override
	public ExecutorService getExecutorService(final ServletContext sc,
			final ApplicationConfiguration config) {

		final ThreadGroup threadGroup = new ThreadGroup("AsyncExecutors");

		final int numThreads = config.getConfigProperty(
				ApplicationConfiguration.ASYNC_THREADS, Integer.class,
				Integer.valueOf(DEFAULT_NUM_THREADS)).intValue();

		return Executors.newFixedThreadPool(numThreads, new ThreadFactory() {

			private int nextThreadNum = 0;

			@Override
			public Thread newThread(final Runnable r) {

				final String threadName =
					"async-executor-" + (this.nextThreadNum++);

				LogFactory.getLog(this.getClass()).debug(
						"starting asynchronous request processing thread " +
								threadName);

				return new Thread(threadGroup, r, threadName);
			}
		});
	}
}
