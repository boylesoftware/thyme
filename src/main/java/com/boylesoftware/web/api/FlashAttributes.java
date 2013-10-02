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
package com.boylesoftware.web.api;


/**
 * Flash scope attributes API. A flash attributes object can be passed to a
 * controller method as an argument. The controller can add attributes to it.
 * The flash attributes will be converted to request attributes during the next
 * request received from the same client.
 *
 * @author Lev Himmelfarb
 */
public interface FlashAttributes {

	/**
	 * Set flash attribute.
	 *
	 * @param name Attribute name.
	 * @param value Attribute value.
	 */
	void setAttribute(String name, String value);
}
