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
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-04-18 20:41:42 $
//                              $Revision: 1.4 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;


import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;
import java.util.StringTokenizer;

import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTaskFactory;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.SoaplabProcessor;
import org.embl.ebi.escience.scufl.Port; 
import org.embl.ebi.escience.scufl.DataConstraint;


import org.apache.log4j.Logger;


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

			//create tasks for each processor
			Processor[] processors = model.getProcessors();
			ArrayList soapLabProcessors = new ArrayList();
			for(int i=0;i<processors.length;i++){
				//at moment only support SoapLabProcessors
				if(!(processors[i] instanceof SoaplabProcessor))
					throw new XScuflInvalidException("Processor '" + processors[i].getName() + "' is not a Soaplab processor. Only support Soaplab processors at present.");
				//actual custom task selected depends on the wsdl identifier
				//note cannot support Java API version, although is this simply a custom task?
				SoaplabProcessor sProcessor = (SoaplabProcessor) processors[i];
				soapLabProcessors.add(sProcessor);
				//Note following will make a network connection.
				URL wsdlURI = sProcessor.getEndpoint();
				String serviceID = wsdlURI.toExternalForm();
				String id = flowID + ":Processor:" + sProcessor.getName();
				ProcessorTask serviceTask = TavernaTaskFactory.getConcreteTavernaTask(id,serviceID,sProcessor);
				addToListIfNotThere(tasks,serviceTask);
				processorTasks.add(serviceTask);
				//for each task generate the necessary input and output port nodes				
			}

			//dataconstraints associate nodes based on linking ports between processors
			//each dataconstraint is a single node it has both input and output identifiers that have significance
			//for a particular pair of linked services
			DataConstraint[] dConstraints = model.getDataConstraints();
			//ArrayList dataNodes = new ArrayList(dConstraints.length);
			for(int i=0;i<dConstraints.length;i++) {
				//DataNode dNode = new DataNode(dConstraints[i]);
				//addToListIfNotThere(dataNodes,dNode);
				//link up processors and external ports for this data constraint
				PortTask source = getPortTask(flowID,outPorts,dConstraints[i].getSource());
				PortTask sink = getPortTask(flowID,inPorts,dConstraints[i].getSink());
				//tie them up
				source.addChild(sink);
				sink.addParent(source);
				//some will already be distinguished as external input and output ports
				addToListIfNotThere(tasks,source);
				addToListIfNotThere(tasks,sink);

				//find the source ProcessorTask
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
			
			//get external ports 
			Port[] externalPorts = model.getExternalPorts();
			if(externalPorts.length == 0)
				throw new XScuflInvalidException("There are no external ports defined for the XScufl workflow");
			for(int i=0;i<externalPorts.length;i++) {
				if(externalPorts[i] instanceof InputPort) {
					PortTask source = getPortTask(flowID,flowExtInPorts,externalPorts[i]);
					addToListIfNotThere(tasks,source);
					//find processortasks that match
					Iterator iterator = processorTasks.iterator();
					while(iterator.hasNext()) {
						ProcessorTask pT = (ProcessorTask) iterator.next();
						String procName = pT.getProcessor().getName();
						if(procName.equals(source.getScuflPort().getProcessor().getName())) {
							pT.addParent(source);
							source.addChild(pT);
						}						
					}
				}
				if(externalPorts[i] instanceof OutputPort) {
					PortTask sink = getPortTask(flowID,flowExtOutPorts,externalPorts[i]);
					addToListIfNotThere(tasks,sink);
					Iterator iterator = processorTasks.iterator();
					while(iterator.hasNext()) {
						ProcessorTask pT = (ProcessorTask) iterator.next();
						String procName = pT.getProcessor().getName();
						if(procName.equals(sink.getScuflPort().getProcessor().getName())) {
							pT.addChild(sink);
							sink.addParent(pT);
						}						
					}
				}
			}

			//set input and output nodes
			Iterator iterator = flowExtInPorts.iterator();
			while(iterator.hasNext()) {
				PortTask pT = (PortTask) iterator.next();
				graph.addInputNode(pT);
				//load the data
				Port port = pT.getScuflPort();
				String portName = port.getName();
				String parentProc = port.getProcessor().getName();
				List partList = input.getPartList();
				Iterator it = partList.iterator();
				while(it.hasNext()) {
					Part part = (Part) it.next();
					String partName = part.getName();
					StringTokenizer tokenizer = new StringTokenizer(partName,":");
					String processorID = tokenizer.nextToken();
					String prtName = tokenizer.nextToken();
					if(processorID.equals(parentProc)&&prtName.equals(portName)) {						
						pT.setData(new Part(-1,prtName,part.getType(),part.getValue()));
					}
				}
				
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
			//fix this!!!
		}
		return graph;
		
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
            else
                throw new XScuflInvalidException("The workflow is incomplete since some processors are not accessible when navigating from the start of the workflow, please check links");
        }			
    }
	
    private static boolean checkDescendents(GraphNode gnode, String id) {
        try {
				
            GraphNode[] children = gnode.getChildren();

            for (int i = 0; i < children.length; i++) {
					
                if (children[i].getID().equals(id)) {
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

