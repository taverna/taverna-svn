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
package net.sf.taverna.t2.invocation;

import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Carries the context of a workflow invocation, the necessary data manager,
 * security agents and any other resource shared across the invocation such as
 * provenance injectors.
 * 
 * @author Tom Oinn
 * 
 */
public interface InvocationContext extends ReferenceContext {

	/**
	 * Return the reference service to be used within this invocation context
	 * 
	 * @return a configured instance of ReferenceService to be used to resolve
	 *         and register references to data in the workflow
	 */
	public ReferenceService getReferenceService();

	/**
	 * Get exactly one entity assignable to the specified type, throwing an
	 * exception if either there are no entities in the context assignable to
	 * this type or if there is more than one. If you need to handle multiple
	 * cases then you should be using the getEntities method instead, this
	 * method implicitly assumes that the absence or any ambiguity is a problem
	 * for your calling code.
	 * 
	 * @param entityClass
	 *            the java class corresponding to the entity type to return.
	 * @return the specified entity, provided there is exactly one entity within
	 *         this invocation context which is assignable to the entityClass
	 *         parameter
	 * @throws InvocationContextException
	 *             if there are zero or more than a single assignable entity in
	 *             the context
	 */
	public <T extends Object> T getEntity(Class<T> entityClass);

	/**
	 * Add a new entity to the invocation context, use this to inject facilities
	 * such as workflow monitoring, provenance capture, security agents and the
	 * like when constructing a new workflow instance.
	 * 
	 * @param entity
	 *            the entity to add to the invocation context, this method does
	 *            nothing if the entity is already in the context as determined
	 *            by object equality checks
	 */
	public void addEntity(Object entity);

	/**
	 * Determine whether this invocation context represents an active process
	 * flow given a process identifier. If this returns false then a processing
	 * entity within the dataflow should not act on the corresponding event.
	 * This is used to indicate that a process has been cancelled or failed and
	 * that no further processing should be performed. Implementations should
	 * maintain a set of cancelled process identifiers and return true from this
	 * method if the owning process is prefixed by any member of this set (i.e.
	 * a process is active if and only if all its parents are active)
	 */
	public boolean isActive(ProcessIdentifier owningProcess);

	/**
	 * Explicitly declare that a process has been cancelled. This method is used
	 * by entities within the workflow to indicate that a process has been
	 * failed or cancelled and that entities either within or downstream of that
	 * entity should not perform any further processing
	 */
	public void setProcessInactive(ProcessIdentifier owningProcess);

	/**
	 * Pause the processing of tokens within this invocation context. Setting
	 * this to true will cause all calls to isActive() to block until set to
	 * false
	 */
	public void setPaused(boolean paused);

	/**
	 * Is this context paused? This is initially set to false but can be changed
	 * by the setPaused() method
	 */
	public boolean isPaused();

	/**
	 * Register a dataflow instance with a specific process identifier with this
	 * invocation context, this is used to message any active dataflows about a
	 * state change between RUNNING and PAUSED. This method should never be
	 * called directly from your code and is only to support the pause / resume
	 * notification system.
	 */
	public void registerDataflow(Dataflow dataflow,
			ProcessIdentifier dataflowProcessId);

	/**
	 * Unregister a previously registered dataflow instance by process id. This
	 * method should never be called directly from your code and is only to
	 * support the pause / resume notification system.
	 */
	public void unregisterDataflow(ProcessIdentifier dataflowProcessId);

}
