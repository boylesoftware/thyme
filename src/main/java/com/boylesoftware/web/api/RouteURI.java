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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to mark a route URI in the list of controller method arguments. The URI
 * is retrieved for the controller using {@link Routes#getRouteURI} method. The
 * target argument must be either a {@link String} (only for parameterless URI
 * mappings) or {@link RouteURIBuilder}.
 *
 * @author Lev Himmelfarb
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RouteURI {

	/**
	 * Route id.
	 *
	 * @return Route id.
	 */
	String value();

	/**
	 * URI type. Default is {@link Routes.URIType#DEFAULT}.
	 *
	 * @return URI type.
	 */
	Routes.URIType type() default Routes.URIType.DEFAULT;
}
