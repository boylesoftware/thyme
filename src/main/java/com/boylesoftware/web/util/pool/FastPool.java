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

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * Fast object pool implementation that uses soft references for pooled objects.
 * The implementation is not thread-safe. Synchronization can be done using the
 * pool instance object monitor.
 *
 * @param <T> Type of the pooled objects.
 *
 * @author Lev Himmelfarb
 */
public class FastPool<T extends Poolable> {

	/**
	 * Initial number of pool slots.
	 */
	private static final int INIT_SIZE = 10;


	/**
	 * Pooled object wrapper.
	 */
	private static final class PooledObject
		extends SoftReference<Object> {

		/**
		 * Index of the pool slot assigned to the object.
		 */
		private boolean idle;


		/**
		 * Create new wrapper for the specified object. The wrapper is marked as
		 * borrowed.
		 *
		 * @param referent Pooled object.
		 */
		PooledObject(final Object referent) {
			super(referent);

			this.idle = false;
		}


		/**
		 * Tell if the pooled object is idle.
		 *
		 * @return {@code true} if idle, {@code false} if borrowed.
		 */
		boolean isIdle() {

			return this.idle;
		}

		/**
		 * Set flags telling if the pooled object is idle.
		 *
		 * @param idle {@code true} if idle, {@code false} if borrowed.
		 */
		void setIdle(final boolean idle) {

			this.idle = idle;
		}
	}


	/**
	 * All pools.
	 */
	private static final Map<FastPool<?>, Boolean> ALL_POOLS =
		new WeakHashMap<>();


	/**
	 * Get all existing pools in the JVM at the moment.
	 *
	 * @return All pools.
	 */
	public static FastPool<?>[] getAllPools() {

		synchronized (ALL_POOLS) {
			return ALL_POOLS.keySet().toArray(
					new FastPool<?>[ALL_POOLS.size()]);
		}
	}


	/**
	 * Pool name.
	 */
	private final String name;

	/**
	 * Pooled objects factory.
	 */
	private final PoolableObjectFactory<T> factory;

	/**
	 * The pool.
	 */
	private PooledObject[] pool;

	/**
	 * Number of slots in the pool.
	 */
	private int poolSize;

	/**
	 * Stack for idle slot indexes.
	 */
	private int[] idleObjIndStack;

	/**
	 * Head of the idle slot indexes stack.
	 */
	private int idleObjIndStackHead;

	/**
	 * Stack for free slot indexes.
	 */
	private int[] freeSlotIndStack;

	/**
	 * Head of the free slot indexes stack.
	 */
	private int freeSlotIndStackHead;


	/**
	 * Create new pool.
	 *
	 * @param name Pool name for reports and debugging.
	 * @param factory Pooled objects factory.
	 */
	public FastPool(final PoolableObjectFactory<T> factory, final String name) {

		this.name = name;
		this.factory = factory;

		this.pool = new PooledObject[INIT_SIZE];
		this.poolSize = 0;
		this.idleObjIndStack = new int[INIT_SIZE];
		this.idleObjIndStackHead = -1;
		this.freeSlotIndStack = new int[INIT_SIZE];
		this.freeSlotIndStackHead = -1;

		synchronized (ALL_POOLS) {
			ALL_POOLS.put(this, Boolean.TRUE);
		}
	}


	/**
	 * Get an object from the pool.
	 *
	 * @return The object.
	 */
	public T get() {

		Object obj = null;
		do {

			if (this.idleObjIndStackHead < 0) {

				int slotInd;
				if (this.freeSlotIndStackHead >= 0) {
					slotInd =
						this.freeSlotIndStack[this.freeSlotIndStackHead--];
				} else {
					slotInd = this.poolSize++;
					final int poolCapacity = this.pool.length;
					if (slotInd >= poolCapacity)
						this.pool = Arrays.copyOf(this.pool,
								poolCapacity + poolCapacity/2);
				}

				obj = this.factory.makeNew(this, slotInd);

				this.pool[slotInd] = new PooledObject(obj);

			} else {

				int slotInd = this.idleObjIndStack[this.idleObjIndStackHead--];
				PooledObject pobj = this.pool[slotInd];
				obj = pobj.get();
				if (obj == null) {
					this.pool[slotInd] = null;
					this.freeSlotIndStackHead++;
					final int stackCapacity = this.freeSlotIndStack.length;
					if (this.freeSlotIndStackHead >= stackCapacity)
						this.freeSlotIndStack = Arrays.copyOf(
								this.freeSlotIndStack,
								stackCapacity + stackCapacity/2);
					this.freeSlotIndStack[this.freeSlotIndStackHead] = slotInd;
				} else {
					pobj.setIdle(false);
				}
			}

		} while (obj == null);

		return this.cast(obj);
	}

	/**
	 * Synchronized version of the {@link #get} method.
	 *
	 * @return The object.
	 */
	public synchronized T getSync() {

		return this.get();
	}

	/**
	 * Return object to the pool.
	 *
	 * @param obj Previously borrowed from the pool object.
	 *
	 * @throws IllegalArgumentException If the object has been already returned.
	 */
	public void recycle(final T obj) {

		final int slotInd = obj.getPooledObjectId();

		final PooledObject pobj = this.pool[slotInd];
		if (pobj.isIdle())
			throw new IllegalStateException("The object is already idle.");

		pobj.setIdle(true);

		this.idleObjIndStackHead++;
		final int stackCapacity = this.idleObjIndStack.length;
		if (this.idleObjIndStackHead >= stackCapacity)
			this.idleObjIndStack = Arrays.copyOf(this.idleObjIndStack,
					stackCapacity + stackCapacity/2);
		this.idleObjIndStack[this.idleObjIndStackHead] = slotInd;
	}

	/**
	 * Synchronized version of the {@link #recycle} method.
	 *
	 * @param obj The object.
	 */
	public synchronized void recycleSync(final T obj) {

		this.recycle(obj);
	}

	/**
	 * Cast specified object to the pooled object type.
	 *
	 * @param obj Object to cast.
	 *
	 * @return The same object but cast to the pooled object type.
	 */
	@SuppressWarnings("unchecked")
	private T cast(final Object obj) {

		return (T) obj;
	}


	/**
	 * Get snapshot of the pool internals for debugging.
	 *
	 * @return Pool internal state description.
	 */
	public synchronized String printStats() {

		final StringBuilder sb = new StringBuilder(256);

		sb.append("FastPool ").append(this.name).append(" stats:\n");
		sb.append("* Pool Size: ").append(this.poolSize).append('\n');
		sb.append("* Pool Slots:\n");
		for (int i = 0; i < this.pool.length; i++) {
			sb.append("    #").append(i).append(": ");
			final PooledObject pooledObj = this.pool[i];
			if (pooledObj == null) {
				sb.append("null\n");
			} else {
				sb.append(pooledObj.isIdle() ? "[idle] " : "[allocated] ");
				final Object o = pooledObj.get();
				sb.append(o != null ? o.getClass().getName() : "null")
					.append('\n');
			}
		}
		sb.append("* Idle Stack:");
		for (int i = this.idleObjIndStackHead; i >= 0; i--)
			sb.append(' ').append(this.idleObjIndStack[i]);
		sb.append('\n');
		sb.append("* Free Slot Stack:");
		for (int i = this.freeSlotIndStackHead; i >= 0; i--)
			sb.append(' ').append(this.freeSlotIndStack[i]);
		sb.append('\n');

		return sb.toString();
	}
}
