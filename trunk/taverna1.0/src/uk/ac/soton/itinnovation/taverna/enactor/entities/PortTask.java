
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
//                              $Date: 2003-09-30 17:11:18 $
//                              $Revision: 1.14 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceSelectionCriteria;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.scufl.*;





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
	this.theDataThing = newDataThing;				
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
