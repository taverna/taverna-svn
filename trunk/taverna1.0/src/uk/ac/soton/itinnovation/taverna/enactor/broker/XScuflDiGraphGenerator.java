////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Darren Marvin
//      Created Date        :   2003/04/09
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-05-23 12:36:00 $
//                              $Revision: 1.14 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTaskFactory;

// Utility Imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.broker.XScuflInvalidException;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * Digraph generator for XScufl. It generates
 * a Digraph that reflects the dependencies between
 * activities in a workflow defined by a ScuflModel.
 */
public class XScuflDiGraphGenerator {

    private static Logger logger = Logger.getLogger(XScuflDiGraphGenerator.class);

    /**
     * Single static build method that takes in
     * an ScuflModel and builds a DiGraph
     * for it.
     *
     * @param flowID gives the assigned identifier for this flow
	 * @param model that contains all the information related to the XScufl definition.
	 * @param input to the workflow
	 * @param user identifier
     * @return Workflow Enactor DiGraph
     */
    public static DiGraph build(String flowID, ScuflModel model, Input input, String userID) throws XScuflInvalidException, java.net.MalformedURLException {
        DiGraph graph = new DiGraph(model.toString());
		try{
						
			List tasks = new ArrayList();
			List flowExtInPorts = new ArrayList();
			List flowExtOutPorts = new ArrayList();
			List inPorts = new ArrayList();
			List outPorts = new ArrayList();
			List processorTasks = new ArrayList();
			Port[] sourcePorts = model.getWorkflowSourcePorts();
			for(int i=0;i<sourcePorts.length;i++) {
			    PortTask pT = getPortTask(flowID,outPorts,sourcePorts[i]);
			    flowExtInPorts.add(pT);
			    addToListIfNotThere(tasks,pT);
			}
			Port[] sinkPorts = model.getWorkflowSinkPorts();
			for(int i=0;i<sinkPorts.length;i++) {
			    PortTask pT = getPortTask(flowID,inPorts,sinkPorts[i]);
			    flowExtOutPorts.add(pT);
			    addToListIfNotThere(tasks,pT);
			}
			//create tasks for each processor
			
			Processor[] processors = model.getProcessors();
			for(int i=0;i<processors.length;i++){
			    Processor theProcessor = processors[i];
			    String id = flowID + ":Processor:" + theProcessor.getName();
				
				LogLevel logLevel = new LogLevel(theProcessor.getLogLevel());
			    
			    // Create the actual task to do the work of this processor.
			    ProcessorTask serviceTask = TavernaTaskFactory.getConcreteTavernaTask(id,theProcessor,logLevel);
			    addToListIfNotThere(tasks,serviceTask);
			    processorTasks.add(serviceTask);
			    
			    // for each task generate the necessary input and output port nodes
			    // djm
			    //
			    // Specifically, _only_ the necessary ones? - it seems to only create
			    // port tasks for ports in the processor that are bound or external, 
			    // which makes sense I guess. tmo@ebi.ac.uk, 27th april 2003
				// this is initially the situation, it is unclear how to deal with unbound ports - the situation where
				// processors may only need part of the inputs before kicking off. This requires further investigation
				// and will in all likelihood lead to changes in the algorithm adopted here, but for now this is enough.
				// I think I will actually double the number of 
			}

			//dataconstraints associate nodes based on linking ports between processors
			//each dataconstraint is a single node it has both input and output identifiers that have significance
			//for a particular pair of linked services
			DataConstraint[] dConstraints = model.getDataConstraints();
			for(int i=0;i<dConstraints.length;i++) {
				//link up processors and external ports for this data constraint
				PortTask source = getPortTask(flowID,outPorts,dConstraints[i].getSource());
				PortTask sink = getPortTask(flowID,inPorts,dConstraints[i].getSink());
				//tie them up
				source.addChild(sink);
				sink.addParent(source);
				//some will already be distinguished as external input and output ports
				addToListIfNotThere(tasks,source);
				addToListIfNotThere(tasks,sink);

				//find the source or sink ProcessorTask
				Iterator iterator = processorTasks.iterator();
				while(iterator.hasNext()) {
					ProcessorTask pT = (ProcessorTask) iterator.next();
					String procName = pT.getProcessor().getName();
					if(procName.equals(source.getScuflPort().getProcessor().getName())) {
					    pT.addChild(source);
						source.addParent(pT);
					}
					if(procName.equals(sink.getScuflPort().getProcessor().getName())) {
					    pT.addParent(sink);
					    sink.addChild(pT);
					}
				}				
				
			}

			//for the taverna enactor concurrency constraints allow control flow declaration between processors
			
			ConcurrencyConstraint[]conConstraints =  model.getConcurrencyConstraints();
			for(int i=0;i<conConstraints.length;i++) {
			    //validate that the state transition is supported
			    if(!validConcurrencyConstraint(conConstraints[i]))
				throw new XScuflInvalidException("The concurrency constraint '" + conConstraints[i].getName() + "' uses a state transition that is not supported by the enactor");
			    //want to get the source processor
			    Processor controller = conConstraints[i].getControllingProcessor();
			    Processor controlled = conConstraints[i].getTargetProcessor();
			    ProcessorTask controllerTask = null;
			    ProcessorTask controlledTask = null;
			    Iterator itor = processorTasks.iterator();
			    while(itor.hasNext()) {
				ProcessorTask p = (ProcessorTask) itor.next();
				if(p.getProcessor().getName().equals(controller.getName()))
				    controllerTask = p;
				else if(p.getProcessor().getName().equals(controlled.getName()))
				    controlledTask = p;
			    }
			    if(controllerTask == null || controlledTask == null) {
				throw new XScuflInvalidException("Could not match processors within concurrency constraint '" + conConstraints[i].getName() + "' with actual processors in the ScuflModel");
			    }
			    //otherwise tie the two together as dependent
			    controllerTask.addChild(controlledTask);
			    controlledTask.addParent(controllerTask);
			}
			
			
			
			//set input and output nodes
			List partList = input.getPartList();
			Iterator iterator = flowExtInPorts.iterator();
			while(iterator.hasNext()) {
				PortTask pT = (PortTask) iterator.next();
				graph.addInputNode(pT);
				//load the data
				Iterator it = partList.iterator();
				while(it.hasNext()) {
				    Part part = (Part) it.next();
				    String partName = part.getName();
				    if(partName.equals(pT.getScuflPort().getName()))
				       pT.setData(new Part(-1,pT.getScuflPort().getName(),part.getType(),part.getValue()));
				}		
								
			}
			
			//processors can have no inputs and / or no outputs
			//and therefore are inputs or outputs to the graph
			iterator = processorTasks.iterator();
			while(iterator.hasNext()) {
				ProcessorTask p = (ProcessorTask) iterator.next();
				if(p.getNumberOfParents()==0)
					//it has no inputs and therefore is an input node for the graph
					graph.addInputNode(p);
				if(p.getNumberOfChildren()==0)
					//it has no outputs and therefore is an output node for the graph
					graph.addOutputNode(p);

			}
			
			iterator = flowExtOutPorts.iterator();
			while(iterator.hasNext()) {
				PortTask pT = (PortTask) iterator.next();
				graph.addOutputNode(pT);
				
			}

			iterator = tasks.iterator();
			GraphNode[] gnodes = new GraphNode[tasks.size()];

			int count = 0;
			while (iterator.hasNext()) {
				GraphNode gn = (GraphNode) iterator.next();

				gnodes[count++] = gn;
			}
			graph.setNodeList(gnodes);

			
			if (!checkRouteToAllNodes(processorTasks, graph.getInputNodes()))
				throw new XScuflInvalidException("Not all the processor nodes are accessible from input nodes");		
		}		
		catch(Exception ex) {
			logger.error(ex);
			throw new XScuflInvalidException("Unrecognised error occured whilst generating workflow, please consult the logs");
		}
		return graph;
		
    }

    private static boolean validConcurrencyConstraint(ConcurrencyConstraint constraint) {
	//Note that actual support of XScufl concurrency constraints may never be complete, Scufl is after all
	//a very general language. At present validation here will reject any state changes that don't follow
	//SCHEDULED->RUNNING->COMPLETED type flow (not FAILED since why bother). In particular at present
	//don't want to support the possibility for cycles, at least until the workflow enactment core supports them natively.
	Processor controller = constraint.getControllingProcessor();
	Processor controlled = constraint.getTargetProcessor();
	//firstly only interested in controller states of COMPLETED
	if(constraint.getControllerStateGuard()!=ConcurrencyConstraint.COMPLETED)
	    return false;
	//only interested in controlled state transitions from SCHEDULED TO RUNNING
	if(constraint.getTargetStateFrom()!=ConcurrencyConstraint.SCHEDULED)
	    return false;
	if(constraint.getTargetStateTo()!=ConcurrencyConstraint.RUNNING)
	    return false;
	//constraint is valid given current enactor support
	return true;
    }

	private static PortTask getPortTask(String flowID,List list,Port p) throws XScuflInvalidException{
		Iterator i = list.iterator();
		while(i.hasNext()) {
			PortTask pT = (PortTask) i.next();
			if(pT.getScuflPort().getProcessor().getName().equals(p.getProcessor().getName()) && pT.getScuflPort().getName().equals(p.getName()))
				return pT;
		}
		//create a new one
		String id = flowID + ":" + ":PortTask:" + p.getProcessor().getName() + ":" + p.getName();
		PortTask newPt = new PortTask(id,p);
		list.add(newPt);
		return newPt;
	}

	private static void addToListIfNotThere(List list, GraphNode gNode) {
        boolean found = false;
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            GraphNode gn = (GraphNode) iterator.next();

            if (gn.getID().equals(gNode)) {
                found = true;
            }
        }
        if (!found)
            list.add(gNode);
    }
	
    private static boolean checkRouteToAllNodes(List processors, GraphNode[] inNodes) throws XScuflInvalidException {
        boolean all = true;

        try {
            Iterator iterator = processors.iterator();

            while (iterator.hasNext()) {
                ProcessorTask activity = (ProcessorTask) iterator.next();
                boolean foundActivity = false;

                for (int i = 0; i < inNodes.length; i++) {
						
                    if (inNodes[i].getID().equals(activity.getID())) {
                        foundActivity = true;
                        break;
                    } else if (checkDescendents(inNodes[i], activity.getID())) {
                        foundActivity = true;
                        break;
                    }
                }
                if (!foundActivity) {
                    throw new XScuflInvalidException("The workflow is invalid since the processor " + activity.getProcessor().getName() + " is not accessible from the start of the workflow, please check links");
                }
            }
            return true;
        } catch (Exception ex) {
            if (ex instanceof XScuflInvalidException)
                throw (XScuflInvalidException) ex;
            else {
				ex.printStackTrace();
                throw new XScuflInvalidException("The workflow is incomplete since some processors are not accessible when navigating from the start of the workflow, please check links");
			}
		}			
    }
	
    private static boolean checkDescendents(GraphNode gnode, String id) {
        try {
				
            GraphNode[] children = gnode.getChildren();

            for (int i = 0; i < children.length; i++) {
					
                if (children[i].getID().equals(id)) {
                    System.out.println("Found match: " + id);
					return true;
                } else if (checkDescendents(children[i], id)) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
	
}

