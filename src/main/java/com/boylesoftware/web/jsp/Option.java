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
package com.boylesoftware.web.jsp;


/**
 * An option for a form input.
 *
 * @author Lev Himmelfarb
 */
public class Option {

	/**
	 * Option value.
	 */
	private final String value;

	/**
	 * Option label.
	 */
	private final String label;


	/**
	 * Create new option.
	 *
	 * @param value Option value.
	 * @param label Option label.
	 */
	public Option(final String value, final String label) {

		this.value = value;
		this.label = label;
	}


	/**
	 * Get option value.
	 *
	 * @return Option value.
	 */
	public String getValue() {

		return this.value;
	}

	/**
	 * Get option label.
	 *
	 * @return Option label.
	 */
	public String getLabel() {

		return this.label;
	}
}
