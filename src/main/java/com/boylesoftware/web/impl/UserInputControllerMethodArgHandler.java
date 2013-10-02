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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boylesoftware.web.api.Attributes;
import com.boylesoftware.web.api.UserInputErrors;
import com.boylesoftware.web.input.Bind;
import com.boylesoftware.web.input.Binder;
import com.boylesoftware.web.input.BindingException;
import com.boylesoftware.web.input.NoTrim;
import com.boylesoftware.web.input.binders.BooleanBinder;
import com.boylesoftware.web.input.binders.IntegerBinder;
import com.boylesoftware.web.input.binders.StringBinder;
import com.boylesoftware.web.spi.RouterRequest;
import com.boylesoftware.web.spi.UserInputHandler;
import com.boylesoftware.web.util.pool.AbstractPoolable;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;


/**
 * Handler for user input bean controller method arguments.
 *
 * @author Lev Himmelfarb
 */
class UserInputControllerMethodArgHandler
	implements UserInputHandler {

	/**
	 * Name of request attribute used to store the poolable user input bean
	 * wrapper.
	 */
	private static final String POOLED_OBJ_ATTNAME =
		(UserInputControllerMethodArgHandler.class).getName() + ".POOLED_OBJ";


	/**
	 * Poolable wrapper for user input bean.
	 */
	private static final class PoolableUserInput
		extends AbstractPoolable {

		/**
		 * The user input bean.
		 */
		private final Object bean;


		/**
		 * Create new wrapper.
		 *
		 * @param pool The pool.
		 * @param pooledObjectId Pooled object id.
		 * @param bean User input bean.
		 */
		PoolableUserInput(final FastPool<PoolableUserInput> pool,
				final int pooledObjectId, final Object bean) {
			super(pool, pooledObjectId);

			this.bean = bean;
		}


		/**
		 * Get wrapped user input bean.
		 *
		 * @return The bean.
		 */
		Object getBean() {

			return this.bean;
		}
	}


	/**
	 * User input bean field descriptor.
	 */
	private static final class FieldDesc {

		/**
		 * Bean property descriptor.
		 */
		private final PropertyDescriptor propDesc;

		/**
		 * Tells if the field has a {@link NoTrim} annotation.
		 */
		private final boolean noTrim;

		/**
		 * Field value binder.
		 */
		private final Binder binder;

		/**
		 * Format for the binder.
		 */
		private final String format;

		/**
		 * Error message if binder fails.
		 */
		private final String errorMessage;


		/**
		 * Create new descriptor.
		 *
		 * @param propDesc Bean property descriptor.
		 * @param noTrim {@code true} if the field has a {@link NoTrim}
		 * annotation.
		 * @param binder Field value binder.
		 * @param format Format for the binder.
		 * @param errorMessage Error message if bider fails.
		 */
		FieldDesc(final PropertyDescriptor propDesc, final boolean noTrim,
				final Binder binder, final String format,
				final String errorMessage) {

			this.propDesc = propDesc;
			this.noTrim = noTrim;
			this.binder = binder;
			this.format = format;
			this.errorMessage = errorMessage;
		}


		/**
		 * Get bean property descriptor.
		 *
		 * @return Bean property descriptor.
		 */
		PropertyDescriptor getPropDesc() {

			return this.propDesc;
		}

		/**
		 * Tell if the field has a {@link NoTrim} annotation.
		 *
		 * @return {@code true} if the field has a {@link NoTrim} annotation.
		 */
		boolean isNoTrim() {

			return this.noTrim;
		}

		/**
		 * Get the field value bider.
		 *
		 * @return The binder.
		 */
		Binder getBinder() {

			return this.binder;
		}

		/**
		 * Get format for the binder.
		 *
		 * @return The format.
		 */
		String getFormat() {

			return this.format;
		}

		/**
		 * Get error message if binder fails.
		 *
		 * @return The error message template.
		 */
		String getErrorMessage() {

			return this.errorMessage;
		}
	}


	/**
	 * The log.
	 */
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Validator factory.
	 */
	private final ValidatorFactory validatorFactory;

	/**
	 * User input bean class.
	 */
	private final Class<?> beanClass;

	/**
	 * Validation groups.
	 */
	private final Class<?>[] validationGroups;

	/**
	 * Descriptors of the user input bean field.
	 */
	private final FieldDesc[] beanFields;

	/**
	 * Bean pool.
	 */
	private final FastPool<PoolableUserInput> beanPool;


	/**
	 * Create new handler.
	 *
	 * @param validatorFactory Validator factory.
	 * @param beanClass User input bean class.
	 * @param validationGroups Validation groups to apply during bean
	 * validation.
	 *
	 * @throws UnavailableException If an error happens.
	 */
	UserInputControllerMethodArgHandler(final ValidatorFactory validatorFactory,
			final Class<?> beanClass, final Class<?>[] validationGroups)
		throws UnavailableException {

		this.validatorFactory = validatorFactory;

		this.beanClass = beanClass;
		this.validationGroups = validationGroups;

		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(this.beanClass);
			final PropertyDescriptor[] propDescs =
				beanInfo.getPropertyDescriptors();
			final List<FieldDesc> beanFields = new ArrayList<>();
			for (final PropertyDescriptor propDesc : propDescs) {
				final String propName = propDesc.getName();
				final Class<?> propType = propDesc.getPropertyType();
				final Method propGetter = propDesc.getReadMethod();
				final Method propSetter = propDesc.getWriteMethod();

				if ((propGetter == null) || (propSetter == null))
					continue;

				Field propField = null;
				for (Class<?> c = this.beanClass; !c.equals(Object.class);
						c = c.getSuperclass()) {
					try {
						propField = c.getDeclaredField(propName);
						break;
					} catch (final NoSuchFieldException e) {
						// nothing, continue the loop
					}
				}
				final boolean noTrim =
					(((propField != null) &&
							propField.isAnnotationPresent(NoTrim.class)) ||
					(propGetter.isAnnotationPresent(NoTrim.class)));

				Class<? extends Binder> binderClass = null;
				String format = null;
				String errorMessage = Bind.DEFAULT_MESSAGE;
				Bind bindAnno = null;
				if (propField != null)
					bindAnno = propField.getAnnotation(Bind.class);
				if (bindAnno == null)
					bindAnno = propGetter.getAnnotation(Bind.class);
				if (bindAnno != null) {
					binderClass = bindAnno.binder();
					format = bindAnno.format();
					errorMessage = bindAnno.message();
				}
				if (binderClass == null) {
					if ((String.class).isAssignableFrom(propType))
						binderClass = StringBinder.class;
					else if ((Boolean.class).isAssignableFrom(propType) ||
							propType.equals(Boolean.TYPE))
						binderClass = BooleanBinder.class;
					else if ((Integer.class).isAssignableFrom(propType) ||
							propType.equals(Integer.TYPE))
						binderClass = IntegerBinder.class;
					else // TODO: add more standard binders
						throw new UnavailableException(
								"Unsupported user input bean field type " +
										propType.getName() + ".");
				}

				beanFields.add(new FieldDesc(propDesc, noTrim,
						binderClass.newInstance(), format, errorMessage));
			}
			this.beanFields =
				beanFields.toArray(new FieldDesc[beanFields.size()]);
		} catch (final IntrospectionException e) {
			this.log.error("error introspecting user input bean", e);
			throw new UnavailableException("Specified user input bean" +
					" class could not be introspected.");
		} catch (final IllegalAccessException | InstantiationException e) {
			this.log.error("error instatiating binder", e);
			throw new UnavailableException("Used user input bean field binder" +
					" could not be instantiated.");
		}

		this.beanPool =
			new FastPool<>(new PoolableObjectFactory<PoolableUserInput>() {

				@Override
				public PoolableUserInput makeNew(
						final FastPool<PoolableUserInput> pool,
						final int pooledObjectId) {

					try {
						return new PoolableUserInput(pool, pooledObjectId,
								beanClass.newInstance());
					} catch (final InstantiationException |
							IllegalAccessException e) {
						throw new RuntimeException(
								"Error instatiating user input bean.", e);
					}
				}
			}, "UserInputBeansPool_" + beanClass.getSimpleName());
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.UserInputHandler#prepareUserInput(com.boylesoftware.web.spi.RouterRequest)
	 */
	@Override
	public boolean prepareUserInput(final RouterRequest request)
		throws ServletException {

		boolean success = false;
		final PoolableUserInput pooledUserInput = this.beanPool.getSync();
		try {
			request.setAttribute(POOLED_OBJ_ATTNAME, pooledUserInput);
			final Object bean = pooledUserInput.getBean();
			request.setAttribute(Attributes.USER_INPUT, bean);
			final UserInputErrors errors = request.getUserInputErrors();
			request.setAttribute(Attributes.USER_INPUT_ERRORS, errors);

			// bind the bean properties
			final int numProps = this.beanFields.length;
			for (int i = 0; i < numProps; i++) {
				final FieldDesc fieldDesc = this.beanFields[i];
				final PropertyDescriptor propDesc = fieldDesc.getPropDesc();
				final String propName = propDesc.getName();
				final String propValStr = (fieldDesc.isNoTrim() ?
						this.nullIfEmpty(request.getParameter(propName)) :
							this.trimToNull(request.getParameter(propName)));
				final Method propSetter = propDesc.getWriteMethod();
				try {
					propSetter.invoke(bean,
							fieldDesc.getBinder().convert(request, propValStr,
									fieldDesc.getFormat(),
									propDesc.getPropertyType()));
				} catch (final BindingException e) {
					if (this.log.isDebugEnabled())
						this.log.debug("binding error", e);
					propSetter.invoke(bean, e.getDefaultValue());
					errors.add(propName, fieldDesc.getErrorMessage());
				}
			}

			// validate the bean
			final Validator validator = this.validatorFactory
					.usingContext()
					.messageInterpolator(request.getMessageInterpolator())
					.getValidator();
			final Set<ConstraintViolation<Object>> cvs =
				validator.validate(bean, this.validationGroups);
			final boolean valid = cvs.isEmpty();
			if (!valid) {
				for (final ConstraintViolation<Object> cv : cvs)
					errors.add(cv.getPropertyPath().toString(),
							cv.getMessage());
			}

			success = true;

			return valid;

		} catch (final IllegalAccessException | InvocationTargetException e) {
			throw new ServletException("Error working with user input bean.",
					e);
		} finally {
			if (!success)
				pooledUserInput.recycle();
		}
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodArgHandler#usesEntityManager()
	 */
	@Override
	public boolean usesEntityManager() {

		return false;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodArgHandler#getArgValue(com.boylesoftware.web.spi.RouterRequest, javax.persistence.EntityManager)
	 */
	@Override
	public Object getArgValue(final RouterRequest request,
			final EntityManager em) {

		return request.getAttribute(Attributes.USER_INPUT);
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.spi.ControllerMethodArgHandler#onComplete(com.boylesoftware.web.spi.RouterRequest)
	 */
	@Override
	public void onComplete(final RouterRequest request) {

		request.removeAttribute(Attributes.USER_INPUT);
		request.removeAttribute(Attributes.USER_INPUT_ERRORS);

		final PoolableUserInput pooledUserInput =
			(PoolableUserInput) request.getAttribute(POOLED_OBJ_ATTNAME);
		if (pooledUserInput != null)
			pooledUserInput.recycle();
	}


	/**
	 * Trim the specified string and make it {@code null} if the result is an
	 * empty string.
	 *
	 * @param str String to trim, or {@code null}.
	 *
	 * @return Trimmed string, or {@code null} if the string was {@code null},
	 * empty or blank.
	 */
	private String trimToNull(final String str) {

		if (str == null)
			return str;

		final String res = str.trim();
		if (res.length() == 0)
			return null;

		return res;
	}

	/**
	 * Check if specified string is empty.
	 *
	 * @param str String to test, or {@code null}.
	 *
	 * @return The original string, or {@code null} if the string was
	 * {@code null} or empty.
	 */
	private String nullIfEmpty(final String str) {

		if (str == null)
			return str;

		return (str.length() > 0 ? str : null);
	}
}
