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


/**
 * Application configuration.
 *
 * @author Lev Himmelfarb
 */
public interface ApplicationConfiguration {

	/**
	 * Standard name of application configuration property used to configure the
	 * asynchronous request processing timeout. The value is expected to be of
	 * type {@link Long} and express the timeout in milliseconds. The default is
	 * 10 seconds.
	 */
	String ASYNC_TIMEOUT = "com.boylesoftware.web.async.timeout";

	/**
	 * Standard name of application configuration property used to configure the
	 * number of threads in the thread pool used to asynchronously process
	 * requests.
	 */
	String ASYNC_THREADS = "com.boylesoftware.web.async.threads";

	/**
	 * Standard name of application configuration property used to specify the
	 * JPA persistence unit name.
	 */
	String PU_NAME = "com.boylesoftware.web.pu.name";

	/**
	 * Standard name of application configuration property used to specified
	 * JNDI name of the optional JavaMail session.
	 */
	String MAIL_SESSION_JNDI_NAME =
		"com.boylesoftware.web.mail.sessionJndiName";


	/**
	 * Get application configuration property.
	 *
	 * @param propName Property name.
	 * @param propClass Expected property value type.
	 * @param defaultValue Default value to return if the property is undefined.
	 *
	 * @return Property value.
	 */
	<T> T getConfigProperty(String propName, Class<T> propClass,
			T defaultValue);
}
