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
//                              $Date: 2003-04-12 13:18:09 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;


import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;

import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTask;
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
		   
			//get the external input and output ports and assign to InPort and OutPort objects
			Port[] externalPorts = model.getExternalPorts();
			for(int i=0;i<externalPorts.length;i++) {
				if(externalPorts[i] instanceof InputPort) {
					//create ExtInPortTask for it
					PortTask eInPort = new PortTask(externalPorts[i]);
					inPorts.add(eInPort);
					flowExtInPorts.add(eInPort);
					//set the input data

					//make graph input node
					graph.addInputNode(eInPort);				
				}
				else {
					//create ExtOutPortTask for it
					PortTask eOutPort = new PortTask(externalPorts[i]);
					outPorts.add(eOutPort);
					flowExtOutPorts.add(eOutPort);
					
					//make graph output node
					graph.addOutputNode(eOutPort);
				}
			}

			//get the processors and create task for each
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
				TavernaTask serviceTask = TavernaTaskFactory.getConcreteTavernaTask(serviceID,sProcessor.getName());
				tasks.add(serviceTask);
				processorTasks.add(serviceTask);
				//for each task generate the necessary input and output port nodes
				Port[] inputPorts = sProcessor.getInputPorts();
				for(int j=0;j<inputPorts.length;j++) {
					PortTask inPort = new PortTask(inputPorts[j]);
					//add the inPort as a parent to the task
					serviceTask.addParent(inPort);
					//add inPort to list of input Ports
					addToListIfNotThere(inPorts,inPort);
				}
				Port[] outputPorts = sProcessor.getOutputPorts();
				for(int j=0;j<outputPorts.length;j++) {
					PortTask outPort = new PortTask(outputPorts[j]);
					//add the outPort as a child to the task
					serviceTask.addChild(outPort);
					//add outPort to list of output Ports
					addToListIfNotThere(outPorts,outPort);
				}			
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
				PortTask source = getPortTask(outPorts,dConstraints[i].getSource());
				PortTask sink = getPortTask(inPorts,dConstraints[i].getSink());
				//tie them up
				source.addChild(sink);
				sink.addParent(source);
				//some will already be distinguished as external input and output ports
			}		
			
			//ready now to set nodeList for the DiGraph
			Iterator iterator = tasks.iterator();
			GraphNode[] gnodes = new GraphNode[tasks.size()];

			int count = 0;
			while (iterator.hasNext()) {
				GraphNode gn = (GraphNode) iterator.next();

				gnodes[count++] = gn;
			}
			graph.setNodeList(gnodes);

			/*
			if (!checkRouteToAllNodes(processorTasks, graph.getInputNodes()))
				throw new XScuflInvalidException("Some processors are not connected fully within the workflow, please check links");
			*/
			
		}
		catch(Exception ex) {
			//fix this!!!
		}
		return graph;
    }

	private static PortTask getPortTask(List list,Port p) throws XScuflInvalidException{
		Iterator i = list.iterator();
		while(i.hasNext()) {
			PortTask pT = (PortTask) i.next();
			if(pT.getScuflPort().getProcessor().getName().equals(p.getProcessor().getName()) && pT.getScuflPort().getName().equals(p.getName()))
				return pT;
		}
		throw new XScuflInvalidException("Failure in graph configuration has occured");
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
	/*
    private static boolean checkRouteToAllNodes(List processors, GraphNode[] inNodes) throws XScuflInvalidException {
        boolean all = true;

        try {
            Iterator iterator = processors.iterator();

            while (iterator.hasNext()) {
                TavernaTask activity = (TavernaTask) iterator.next();
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
                    throw new XScuflInvalidException("The workflow is invalid since the processor " + activity.getProcessorName() + " is not accessible from the start of the workflow, please check links");
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
	*/
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
