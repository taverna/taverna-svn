/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.activities.sadi.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.SADIActivityInputPort;
import net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort;
import net.sf.taverna.t2.activities.sadi.SADIActivityPort;
import net.sf.taverna.t2.activities.sadi.SADIUtils;
import net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription;
import net.sf.taverna.t2.activities.sadi.views.SADIServiceDiscoveryDialog;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * 
 * 
 * @author David Withers
 */
public class SADIFindServicesAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private static final String FIND_SADI_CONSUMER_SERVICES = "Find services that consume ";
	private static final String FIND_SADI_PRODUCER_SERVICES = "Find services that produce ";
	private static final EditManager editManager = EditManager.getInstance();
	private static final Edits edits = editManager.getEdits();

	private final Dataflow dataflow;
	private final SADIActivityPort activityPort;

	/**
	 * Constructs a new SADIFindServicesAction.
	 * 
	 * @param dataflow
	 * 
	 * @param activityPort
	 */
	public SADIFindServicesAction(Dataflow dataflow, SADIActivityPort activityPort) {
		this.dataflow = dataflow;
		this.activityPort = activityPort;
		if (activityPort instanceof SADIActivityOutputPort) {
			putValue(NAME, FIND_SADI_CONSUMER_SERVICES + activityPort.getValuesFromLabel()
					+ "...");
		} else {
			putValue(NAME, FIND_SADI_PRODUCER_SERVICES + activityPort.getValuesFromLabel()
					+ "...");
		}
	}

	public void actionPerformed(ActionEvent e) {
		SADIServiceDiscoveryDialog dialog = new SADIServiceDiscoveryDialog(activityPort);
		if (dialog.show(null)) {
			for (SADIServiceDescription service : dialog.getSelectedServices()) {
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				// create the activity
				SADIActivity activity = new SADIActivity();
				SADIActivityConfigurationBean configuration = service.getActivityConfiguration();
				if (activityPort instanceof SADIActivityOutputPort) {
					// we found this service by its input class, so that will be the only input port...
					configuration.getInputPortMap().put(SADIUtils.getInputClassLabel(service.getServiceInfo()), "");
				}

				try {
					// configure the activity
					activity.configure(configuration);

					String name = service.getName();
					name = Tools.uniqueProcessorName(name, dataflow);

					// create the processor and add the the dataflow
					Processor processor = edits.createProcessor(name);
					editList.add(edits.getDefaultDispatchStackEdit(processor));
					editList.add(edits.getAddActivityEdit(processor, activity));
					editList.add(edits.getAddProcessorEdit(dataflow, processor));

					if (activityPort instanceof SADIActivityOutputPort) {
						SADIActivityOutputPort activityOutputPort = (SADIActivityOutputPort) activityPort;
						// find the inputs that match the output class
						// TODO should match subclasses of the output class too
						Set<SADIActivityInputPort> inputPorts = activity.getInputPortsForClass(activityPort.getValuesFromURI());
						// connect the output port to the input with a matching class
						if (inputPorts.size() > 0) {
							// if there's more than one matching input port connect to one at random
							ActivityInputPort activityInputPort = activity.getInputPorts().iterator().next();
							ProcessorInputPort processorInputPort = edits.createProcessorInputPort(
									processor, activityInputPort.getName(), activityInputPort
									.getDepth());
							editList.add(edits.getAddProcessorInputPortEdit(processor,
									processorInputPort));
							editList.add(edits.getAddActivityInputPortMappingEdit(activity,
									processorInputPort.getName(), activityInputPort.getName()));
							editList.add(Tools.getCreateAndConnectDatalinkEdit(dataflow,
									activityOutputPort, processorInputPort));
						}
					} else {
						SADIActivityInputPort activityInputPort = (SADIActivityInputPort) activityPort;
						// find the outputs that match the input class
						// TODO should match subclasses of the input class too
						Set<SADIActivityOutputPort> outputPorts = activity.getOutputPortsForClass(activityPort.getValuesFromURI());
						// connect the input port to the output with a matching class
						if (outputPorts.size() > 0) {
							// if there's more than one matching output port connect to one at random
							ActivityOutputPort activityOutputPort = outputPorts.iterator().next();
							ProcessorOutputPort processorOutputPort = edits.createProcessorOutputPort(
									processor, activityOutputPort.getName(), activityOutputPort
									.getDepth(), activityOutputPort.getGranularDepth());
							editList.add(edits.getAddProcessorOutputPortEdit(processor,
									processorOutputPort));
							editList.add(edits.getAddActivityOutputPortMappingEdit(activity,
									processorOutputPort.getName(), activityOutputPort.getName()));

							editList.add(Tools.getCreateAndConnectDatalinkEdit(dataflow,
									processorOutputPort, activityInputPort));
						}
						
					}

					try {
						CompoundEdit compoundEdit = new CompoundEdit(editList);
						editManager.doDataflowEdit(dataflow, compoundEdit);
					} catch (EditException ex) {
						ex.printStackTrace();
					}
				} catch (ActivityConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

}
