
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
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-06-09 07:28:23 $
//                              $Revision: 1.8 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceSelectionCriteria;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTask;
import java.lang.Exception;
import java.lang.String;



/**
 * GraphNode that represents a port on a Processor.
 */
public class PortTask extends TavernaTask{
	
	private static Logger logger = Logger.getLogger(PortTask.class);

	public static int IN = 0;
	public static int OUT = 1;

	private org.embl.ebi.escience.scufl.Port port; 
	private int type;
	private Part dataPacket = null;
		
	public PortTask(String id, org.embl.ebi.escience.scufl.Port port) {
		super(id);
		this.port = port;
		if(port instanceof org.embl.ebi.escience.scufl.InputPort)
			type = 0;
		else 
			type = 1;
	}

	/**
	 * Obtains the type of this Port, either input (0) or output (1).
	 * @return type
	 */
	public int type() {
		return type;
	}

	/**
	 * Obtains the original XScufl definition Port 
	 * @return XScufl definition port
	 */
	public org.embl.ebi.escience.scufl.Port getScuflPort() {
		return port;
	}

	/**
	 * Checks to see if data is available on this port
	 * @return true if available
	 */
	public boolean dataAvailable() {
		if(dataPacket==null) {
			return false;
		}
		else
			return true;
	}

	/**
	 * Blocking method that waits for data to arrive
	 * and then supplies it
	 * @return holder for data
	 */
	
	public synchronized Part getData() {
		return dataPacket;
	}

	/**
	 * Sets a reference to the actual data holder
	 * @param data holder for data
	 */
	public synchronized void setData(Part p) {
		dataPacket = p;				
	}

	/**
     * Forces class to clean itself up a bit.
     */
    public void cleanUpConcreteTask() {
		port = null;
		dataPacket = null;
	}

	/**
     * Retrieves the selection criteria associated with this task. This criteria
     * determines the service provider for a particular service.
     */
    public ServiceSelectionCriteria getServiceSelectionCriteria() {
        return null;
    }

	public TaskStateMessage doTask() {
		TaskStateMessage msg = null;
		try{
			
			//don't do anything, later versions may have persistence writing to do
			//set data on child porttasks too
		    if(dataPacket==null) {
					msg = new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED,"No data for port " + port.getName() + ",please check its links");
		    }	else {
				GraphNode[] chds = getChildren();
				for(int i=0;i<chds.length;i++) {
					if(chds[i] instanceof PortTask) {
						PortTask pT = (PortTask) chds[i];
						String portName = pT.getScuflPort().getName();
						//want the part for this portname 
						Part part = new Part(-1,portName,dataPacket.getType(),dataPacket.getTypedValue());
						pT.setData(part);
					}
				}
				msg = new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.COMPLETE, "Task finished successfully");
			}
			
		}
		catch(Exception ex) {
		    logger.error(ex);
		    msg = new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, ex.getMessage());
		}
		return msg;
	}
}
