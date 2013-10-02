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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.boylesoftware.web.input.WithNewPassword;


/**
 * {@link PasswordsMatch} constraint validator.
 *
 * @author Lev Himmelfarb
 */
public class PasswordsMatchValidator
	implements ConstraintValidator<PasswordsMatch, WithNewPassword> {

	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
	 */
	@Override
	public void initialize(final PasswordsMatch constraintAnnotation) {

		// nothing
	}

	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(final WithNewPassword value,
			final ConstraintValidatorContext context) {

		if (value == null)
			return true;

		final String p1 = value.getPassword();
		final String p2 = value.getPassword2();

		final boolean isValid = (p1 == null ? p2 == null : p1.equals(p2));
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
					context.getDefaultConstraintMessageTemplate())
				.addPropertyNode("password")
				.addConstraintViolation();
		}

		return isValid;
	}
}
