/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.reference.impl;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.sf.taverna.t2.reference.ListServiceException;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;

/**
 * Abstract superclass for all service implementation objects, will be used to
 * allow injection of thread pooling logic as and when we implement it.
 * 
 * @author Tom Oinn
 */
public class AbstractServiceImpl {

	private static ThreadGroup defaultThreadGroup = new ThreadGroup(
			"Reference subsystem service thread group");
	private static Executor defaultExecutor = Executors
			.newCachedThreadPool(new ThreadFactory() {

				private int threadCount = 0;

				private synchronized String nextThreadName() {
					return "Thread" + (threadCount++);
				}

				public Thread newThread(Runnable r) {
					Thread result = new Thread(defaultThreadGroup, r,
							nextThreadName());
					new Thread();
					result.setDaemon(true);
					return result;
				}

			});

	/**
	 * Schedule a runnable for execution using the default executor
	 * 
	 * @param r
	 */
	protected void executeRunnable(Runnable r) {
		defaultExecutor.execute(r);
	}

	private T2ReferenceGenerator t2ReferenceGenerator = null;

	/**
	 * Inject the T2Reference generator used to allocate new IDs when
	 * registering ErrorDocuments
	 */
	public final void setT2ReferenceGenerator(T2ReferenceGenerator t2rg) {
		this.t2ReferenceGenerator = t2rg;
	}

	protected final T2ReferenceGenerator getGenerator() {
		return this.t2ReferenceGenerator;
	}

	/**
	 * Check that the t2reference generator is configured
	 * 
	 * @throws ListServiceException
	 *             if the generator is still null
	 */
	protected final void checkGenerator() throws ListServiceException {
		if (t2ReferenceGenerator == null) {
			throw new ListServiceException(
					"T2ReferenceGenerator not initialized, list "
							+ "service operations not available");
		}
	}

	protected final T2ReferenceGenerator getGenerator(ReferenceContext context) {
		List<? extends T2ReferenceGenerator> generators = context
				.getEntities(T2ReferenceGenerator.class);
		if (generators.isEmpty()) {
			return this.t2ReferenceGenerator;
		} else {
			return generators.get(0);
		}
	}
}
