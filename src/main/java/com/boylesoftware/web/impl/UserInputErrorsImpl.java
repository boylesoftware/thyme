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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.MessageInterpolator;
import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.validation.metadata.ConstraintDescriptor;

import com.boylesoftware.web.UserInputError;
import com.boylesoftware.web.api.UserInputErrors;
import com.boylesoftware.web.util.pool.FastPool;
import com.boylesoftware.web.util.pool.Poolable;
import com.boylesoftware.web.util.pool.PoolableObjectFactory;


/**
 * User input validation errors implementation.
 *
 * @author Lev Himmelfarb
 */
class UserInputErrorsImpl
	implements UserInputErrors {

	/**
	 * Dummy annotation type.
	 */
	private static @interface Dummy {

		// nothing
	}

	/**
	 * Constraint descriptor plug.
	 */
	@Dummy
	static final class ConstraintDescriptorPlug
		implements ConstraintDescriptor<Dummy> {

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#getAnnotation()
		 */
		@Override
		public Dummy getAnnotation() {

			return this.getClass().getAnnotation(Dummy.class);
		}

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#getMessageTemplate()
		 */
		@Override
		public String getMessageTemplate() {

			return "";
		}

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#getGroups()
		 */
		@Override
		public Set<Class<?>> getGroups() {

			return Collections.emptySet();
		}

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#getPayload()
		 */
		@Override
		public Set<Class<? extends Payload>> getPayload() {

			return Collections.emptySet();
		}

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#getValidationAppliesTo()
		 */
		@Override
		public ConstraintTarget getValidationAppliesTo() {

			return ConstraintTarget.IMPLICIT;
		}

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#getConstraintValidatorClasses()
		 */
		@Override
		public List<Class<? extends ConstraintValidator<Dummy, ?>>>
		getConstraintValidatorClasses() {

			return Collections.emptyList();
		}

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#getAttributes()
		 */
		@Override
		public Map<String, Object> getAttributes() {

			return Collections.emptyMap();
		}

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#getComposingConstraints()
		 */
		@Override
		public Set<ConstraintDescriptor<?>> getComposingConstraints() {

			return Collections.emptySet();
		}

		/* (non-Javadoc)
		 * @see javax.validation.metadata.ConstraintDescriptor#isReportAsSingleViolation()
		 */
		@Override
		public boolean isReportAsSingleViolation() {

			return false;
		}
	}

	/**
	 * Message interpolator context plug.
	 */
	static final class MessageInterpolatorContextPlug
		implements MessageInterpolator.Context {

		/**
		 * Constraint descriptor plug.
		 */
		private static final ConstraintDescriptor<?> CDESC =
			new ConstraintDescriptorPlug();


		/* (non-Javadoc)
		 * @see javax.validation.MessageInterpolator.Context#getConstraintDescriptor()
		 */
		@Override
		public ConstraintDescriptor<?> getConstraintDescriptor() {

			return CDESC;
		}

		/* (non-Javadoc)
		 * @see javax.validation.MessageInterpolator.Context#getValidatedValue()
		 */
		@Override
		public Object getValidatedValue() {

			return null;
		}

		/* (non-Javadoc)
		 * @see javax.validation.MessageInterpolator.Context#unwrap(java.lang.Class)
		 */
		@Override
		public <T> T unwrap(Class<T> type) {

			throw new ValidationException("Unsupported.");
		}
	}

	/**
	 * Message interpolator context plug.
	 */
	private static final MessageInterpolator.Context MSG_INTRPLTR_PLUG =
		new MessageInterpolatorContextPlug();

	/**
	 * User input error objects pool.
	 */
	private static final FastPool<PoolableUserInputError> ERRORS_POOL =
		new FastPool<>(new PoolableObjectFactory<PoolableUserInputError>() {

			@Override
			public PoolableUserInputError makeNew(
					final FastPool<PoolableUserInputError> pool,
					final int pooledObjectId) {

				return new PoolableUserInputError(pool, pooledObjectId);
			}
		}, "UserInputErrorsPool");


	/**
	 * Owning request object.
	 */
	private final RouterRequestImpl request;

	/**
	 * All errors as a flat list.
	 */
	private final List<UserInputError> allErrors = new ArrayList<>();

	/**
	 * Errors by field names.
	 */
	private final Map<String, Collection<UserInputError>> byField =
		new HashMap<>();

	/**
	 * Read-only view of the errors by field names map.
	 */
	private final Map<String, Collection<UserInputError>> byFieldRO =
		Collections.unmodifiableMap(this.byField);

	/**
	 * First field error.
	 */
	private UserInputError firstFieldError;


	/**
	 * Create new error object.
	 *
	 * @param request Owning request object.
	 */
	UserInputErrorsImpl(final RouterRequestImpl request) {

		this.request = request;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.UserInputErrors#add(java.lang.String, java.lang.String)
	 */
	@Override
	public void add(final String fieldName, final String messageTmpl) {

		final PoolableUserInputError error = ERRORS_POOL.getSync();
		boolean success = false;
		try {
			error.setFieldName(fieldName);
			error.setMessage(this.request.getMessageInterpolator().interpolate(
					messageTmpl, MSG_INTRPLTR_PLUG,
					this.request.getUserLocale()));

			this.add(error);

			success = true;

		} finally {
			if (!success)
				error.recycle();
		}
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.UserInputErrors#getByField()
	 */
	@Override
	public Map<String, Collection<UserInputError>> getByField() {

		return this.byFieldRO;
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.UserInputErrors#getFirstError()
	 */
	@Override
	public UserInputError getFirstError() {

		try {
			return this.allErrors.get(0);
		} catch (final IndexOutOfBoundsException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.boylesoftware.web.api.UserInputErrors#getFirstFieldError()
	 */
	@Override
	public UserInputError getFirstFieldError() {

		return this.firstFieldError;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {

		return this.allErrors.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {

		return this.allErrors.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object o) {

		return this.allErrors.contains(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<UserInputError> iterator() {

		return this.allErrors.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {

		return this.allErrors.toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(final T[] a) {

		return this.allErrors.toArray(a);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(final UserInputError e) {

		final String fieldName = e.getFieldName();
		Collection<UserInputError> fieldErrors = this.byField.get(fieldName);
		if (fieldErrors == null) {
			fieldErrors = new ArrayList<>();
			this.byField.put(fieldName, fieldErrors);
		}
		fieldErrors.add(e);

		this.allErrors.add(e);

		if ((this.firstFieldError == null) && (fieldName != null))
			this.firstFieldError = e;

		return true;
	}

	/**
	 * Method unsupported.
	 */
	@Override
	public boolean remove(final Object o) {

		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {

		return this.allErrors.containsAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends UserInputError> c) {

		if (c.size() == 0)
			return false;

		for (final UserInputError e : c)
			this.add(e);

		return true;
	}

	/**
	 * Method unsupported.
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {

		throw new UnsupportedOperationException();
	}

	/**
	 * Method unsupported.
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {

		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {

		for (final UserInputError error : this.allErrors)
			if (error instanceof Poolable)
				((Poolable) error).recycle();

		this.allErrors.clear();
		this.byField.clear();
		this.firstFieldError = null;
	}
}
