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

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Errors producer used to testing.
 *
 * @author Lev Himmelfarb
 */
public final class LooseCannon {

	/**
	 * The log.
	 */
	private static final Log LOG = LogFactory.getLog(LooseCannon.class);

	/**
	 * Random number generator.
	 */
	private static final Random RAND = new Random();


	/**
	 * Tells if the class is on.
	 */
	private static boolean atSea;


	/**
	 * All methods are static.
	 */
	private LooseCannon() {}


	/**
	 * Turns the class on and off.
	 *
	 * @param val {@code true} to turn it on.
	 */
	public static void setAtSea(final boolean val) {

		atSea = val;
	}

	/**
	 * Randomly chooses to do nothing, be slow, be very slow (to cause a
	 * timeout), or throw a runtime exception.
	 */
	public static void heel() {

		if (!atSea)
			return;

		switch (RAND.nextInt(30)) {
		case 0:
			try {
				LOG.info("*** being slow");
				Thread.sleep((5 - RAND.nextInt(5)) * 1000L);
			} catch (final InterruptedException e) {
				LOG.info("*** interrupted");
			}
			break;
		case 1:
			try {
				LOG.info("*** causing timeout");
				Thread.sleep(20 * 1000L);
			} catch (final InterruptedException e) {
				LOG.info("*** interrupted");
			}
			break;
		case 2:
			LOG.info("*** throwing exception");
			throw new RuntimeException("I'm hungry!");
		}
	}
}
