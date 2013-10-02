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
package com.boylesoftware.web.util.pool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Pooled string buffer/builder wrapper. Also includes a re-usable regular
 * expression matcher.
 *
 * @author Lev Himmelfarb
 */
public class PooledStringBuffer
	extends AbstractPoolable
	implements AutoCloseable {

	/**
	 * Buffer's initial capacity.
	 */
	private static final int INITIAL_CAPACITY = 256;

	/**
	 * Pattern plug.
	 */
	private static final Pattern PATTERN_PLUG = Pattern.compile("");


	/**
	 * Wrapped string buffer.
	 */
	private final StringBuffer buffer;

	/**
	 * Wrapper string builder.
	 */
	private final StringBuilder builder;

	/**
	 * Regular expression matcher.
	 */
	private final Matcher matcher;


	/**
	 * Create new wrapper.
	 *
	 * @param pool Reference to the pool.
	 * @param pooledObjectId Pooled object id.
	 */
	PooledStringBuffer(final FastPool<PooledStringBuffer> pool,
			final int pooledObjectId) {
		super(pool, pooledObjectId);

		this.buffer = new StringBuffer(INITIAL_CAPACITY);
		this.builder = new StringBuilder(INITIAL_CAPACITY);

		this.matcher = PATTERN_PLUG.matcher("");
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.util.pool.AbstractPoolable#recycle()
	 */
	@Override
	public void recycle() {

		this.buffer.setLength(0);
		this.builder.setLength(0);

		super.recycle();
	}

	/**
	 * Calls {@link #recycle}.
	 */
	@Override
	public void close() {

		this.recycle();
	}


	/**
	 * Get wrapped string buffer.
	 *
	 * @return The string buffer. The buffer is empty for a newly borrowed
	 * object.
	 */
	public StringBuffer getStringBuffer() {

		return this.buffer;
	}

	/**
	 * Get wrapped string builder.
	 *
	 * @return The string builder. The builder is empty for a newly borrowed
	 * object.
	 */
	public StringBuilder getStringBuilder() {

		return this.builder;
	}

	/**
	 * Get regular expression matcher.
	 *
	 * @param pattern The pattern.
	 * @param input The input.
	 *
	 * @return Initialized matcher.
	 */
	public Matcher getMatcher(final Pattern pattern, final CharSequence input) {

		this.matcher.usePattern(pattern);
		this.matcher.reset(input);

		return this.matcher;
	}
}
