
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
//      Created Date        :   2003/4/9
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-11-17 18:00:11 $
//                              $Revision: 1.20 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceSelectionCriteria;

// Utility Imports
import java.util.ArrayList;
import java.util.List;




/**
 * GraphNode that represents a port on a Processor.
 */
public class PortTask extends TavernaTask {
    
    private static Logger logger = Logger.getLogger(PortTask.class);
    public static int IN = 0;
    public static int OUT = 1;
    
    private Port thePort; 
    private DataThing theDataThing = null;
    
    public PortTask(String id, Port port) {
	super(id);
	this.thePort = port;
    }
    
    /**
     * Obtains the type of this Port, either input (0) or output (1).
     * @return type
     */
    public int type() {
	if (this.thePort instanceof InputPort) {
	    return PortTask.IN;
	}
	else {
	    return PortTask.OUT;
	}
    }
    
    /**
     * Obtains the original XScufl definition Port 
     * @return XScufl definition port
     */
    public Port getScuflPort() {
	return this.thePort;
    }
    
    /**
     * Checks to see if data is available on this port
     * @return true if available
     */
    public boolean dataAvailable() {
	return (theDataThing!=null);
    }
    
    /**
     * Blocking method that waits for data to arrive
     * and then supplies it
     * @return holder for data
     */
    public synchronized DataThing getData() {
	return this.theDataThing;
    }
    
    /**
     * Sets a reference to the actual data holder
     * @param newDataThing holder for data
     */
    public synchronized void setData(DataThing newDataThing) {
	// Check whether the new data is a lower dimension than
	// the type of this port task
	String portSyntaxType = thePort.getSyntacticType();
	String dataSyntaxType = newDataThing.getSyntacticType();
	// If, for example, we have a 'text/plain' being put into a l('text/plain') then
	// we can reasonably create a new container with just the single element in
	//if (portSyntaxType != dataSyntaxType) {
	String portSetType = portSyntaxType.split("\\'")[0];
	String dataSetType = dataSyntaxType.split("\\'")[0];
	//System.out.println("Set types are "+portSetType+" and "+dataSetType);
	// Get the number of 'l(' elements
	int portDimension = (portSetType.length())/2;
	int dataDimension = (dataSetType.length())/2;
	int encapsulationDifference = portDimension - dataDimension;
	//System.out.println("Think this is a difference of "+encapsulationDifference+" ("+portDimension+"-"+dataDimension+")");
	if (encapsulationDifference > 0) {  
	    Object theDataObject = newDataThing.getDataObject();
	    while (encapsulationDifference > 0) {
		encapsulationDifference--;
		// While the dimensionality has not been reconciled create
		// a new List container and put the current object in it.
		System.out.println("Wrapping data...");
		List newList = new ArrayList();
		newList.add(theDataObject);
		theDataObject = newList;
	    }
	    this.theDataThing = new DataThing(theDataObject);
	}
	else {
	    this.theDataThing = newDataThing;
	}
	// Copy any MIME types available from the markup object
	// on the Scufl port into the MIME container in the
	// DataThing
	SemanticMarkup portMarkup = getScuflPort().getMetadata();
	String[] portMIMETypes = portMarkup.getMIMETypes();
	for (int i = 0; i < portMIMETypes.length; i++) {
	    System.out.println("Adding mime type "+portMIMETypes[i]+" to "+((Object)theDataThing).toString());
	    this.theDataThing.getMetadata().addMIMEType(portMIMETypes[i]);
	}
	// Copy any semantic markup into the markup object as well
	this.theDataThing.getMetadata().setSemanticType(portMarkup.getSemanticType());
    }
    
    /**
     * Forces class to clean itself up a bit.
     */
    public void cleanUpConcreteTask() {
	thePort = null;
	theDataThing = null;
    }
    
    /**
     * Retrieves the selection criteria associated with this task. This criteria
     * determines the service provider for a particular service.
     */
    public ServiceSelectionCriteria getServiceSelectionCriteria() {
        return null;
    }

    /**
     * Transfer data from this port task into any child port tasks that are present, this
     * is effectively pushing information from the output port to the input port; other
     * processor implementations pull from the input port and push to the output port so
     * this is the link inbetween two processors.
     */
    public TaskStateMessage doTask() {
	TaskStateMessage msg = null;
	try{
	    //don't do anything, later versions may have persistence writing to do
	    //set data on child porttasks too
	    if(theDataThing == null) {
		msg = new TaskStateMessage(getParentFlow().getID(), 
					   getID(), 
					   TaskStateMessage.FAILED,
					   "No data for port " + thePort.getName() + ",please check its links");
	    }	
	    else {
		GraphNode[] chds = getChildren();
		for(int i=0;i<chds.length;i++) {
		    if(chds[i] instanceof PortTask) {
			PortTask childPortTask = (PortTask)chds[i];
			childPortTask.setData(this.theDataThing);
		    }
		}
		msg = new TaskStateMessage(getParentFlow().getID(), 
					   getID(), 
					   TaskStateMessage.COMPLETE, 
					   "Task finished successfully");
	    }
	}
	catch(Exception ex) {
	    logger.error(ex);
	    msg = new TaskStateMessage(getParentFlow().getID(), 
				       getID(), 
				       TaskStateMessage.FAILED, 
				       ex.getMessage());
	}
	return msg;
    }

}
