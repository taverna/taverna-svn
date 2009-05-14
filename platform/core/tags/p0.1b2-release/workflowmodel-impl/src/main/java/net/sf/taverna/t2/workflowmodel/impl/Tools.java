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
package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * 
 * Contains static methods concerned with legacy Processor construction and XML
 * handling for the various configurable types such as Activity and
 * DispatchLayer.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 * 
 */
public class Tools {

	/**
	 * Iterates over all the processors in the dataflow, returning the first
	 * processor found to contain the given activity.
	 * 
	 * @param dataflow
	 * @param activity
	 * @return the processor to which the activity is attached, or null if it
	 *         cannot be found
	 */
	public Processor findProcessorForActivity(Dataflow dataflow,
			Activity<?> activity) {
		for (Processor p : dataflow.getProcessors()) {
			for (Activity<?> a : p.getActivityList()) {
				if (a == activity)
					return p;
			}
		}
		return null;
	}

	/**
	 * Construct a new {@link Processor} with a single {@link Activity} and
	 * overall processor inputs and outputs mapped to the activity inputs and
	 * outputs. This is intended to be equivalent to the processor creation in
	 * Taverna1 where the concepts of Processor and Activity were somewhat
	 * confused; it also inserts retry, parallelise and failover layers
	 * configured as a Taverna1 process would be.
	 * <p>
	 * Modifies the given activity object, adding the mappings for input and
	 * output port names (these will all be fooport->fooport but they're still
	 * needed)
	 * 
	 * @param activity
	 *            the {@link Activity} to use to build the new processor around
	 * @return An initialised {@link ProcessorImpl}
	 */
	public static ProcessorImpl buildFromActivity(Activity<?> activity,
			PluginManager manager) throws EditException {
		EditsImpl edits = new EditsImpl(manager);
		ProcessorImpl processor = (ProcessorImpl) edits.createProcessor("");
		new DefaultDispatchStackEdit(processor, manager).doEdit();
		// Add the Activity to the processor
		processor.activityList.add(activity);
		// Create processor inputs and outputs corresponding to activity inputs
		// and outputs and set the mappings in the Activity object.
		activity.getInputPortMapping().clear();
		activity.getOutputPortMapping().clear();
		for (InputPort ip : activity.getInputPorts()) {
			ProcessorInputPort pip = edits.createProcessorInputPort(processor,
					ip.getName(), ip.getDepth());
			new AddProcessorInputPortEdit(processor, pip).doEdit();
			activity.getInputPortMapping().put(ip.getName(), ip.getName());
		}
		for (OutputPort op : activity.getOutputPorts()) {
			ProcessorOutputPort pop = edits.createProcessorOutputPort(
					processor, op.getName(), op.getDepth(), op
							.getGranularDepth());
			new AddProcessorOutputPortEdit(processor, pop).doEdit();
			activity.getOutputPortMapping().put(op.getName(), op.getName());
		}

		return processor;
	}

	/**
	 * Returns a unique processor name for the supplied Dataflow, based upon the
	 * preferred name. A numeric prefix is added to the preferred name, and
	 * incremented until it is unique.
	 * 
	 * @param preferredName -
	 *            the preferred name for the Processor
	 * @param dataflow -
	 *            the dataflow for which the Processor name needs to be unique
	 * @return
	 */
	public static String uniqueProcessorName(String preferredName,
			Dataflow dataflow) {
		String uniqueName = preferredName;
		boolean found = true;
		int prefix = 0;
		while (found) {
			found = false;
			for (Processor p : dataflow.getProcessors()) {
				if (p.getLocalName().equals(uniqueName)) {
					uniqueName = preferredName + String.valueOf(prefix);
					prefix++;
					found = true;
					break;
				}
			}
		}
		return uniqueName;
	}

}
