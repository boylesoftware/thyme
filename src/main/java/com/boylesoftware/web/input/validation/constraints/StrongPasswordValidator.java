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
package com.boylesoftware.web.input.validation.constraints;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * {@link StrongPassword} constraint validator.
 *
 * @author Lev Himmelfarb
 */
public class StrongPasswordValidator
	implements ConstraintValidator<StrongPassword, String> {

	/**
	 * Minimum required length.
	 */
	private int minLength;

	/**
	 * Must contain patterns.
	 */
	private Pattern[] mustContain;


	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
	 */
	@Override
	public void initialize(final StrongPassword constraintAnnotation) {

		this.minLength = constraintAnnotation.minLength();
		final String[] mustContain = constraintAnnotation.mustContain();
		final int numPatterns = mustContain.length;
		this.mustContain = new Pattern[numPatterns];
		for (int i = 0; i < numPatterns; i++)
			this.mustContain[i] = Pattern.compile(mustContain[i]);
	}

	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(final String value,
			final ConstraintValidatorContext context) {

		if (value == null)
			return true;

		if (value.length() < this.minLength)
			return false;

		if (this.mustContain.length > 0) {
			final Matcher m = this.mustContain[0].matcher(value);
			for (final Pattern p : this.mustContain)
				if (!m.usePattern(p).reset().find())
					return false;
		}

		return true;
	}
}
