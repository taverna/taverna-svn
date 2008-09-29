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
//      Created Date        :   2003/04/8
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-04-17 15:21:48 $
//                              $Revision: 1.2 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceSelectionCriteria;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTask;
import java.lang.String;



public abstract class ProcessorTask extends TavernaTask{

    protected Processor proc = null;
	
	/**
     * Default Constructor
     * @param id
     */
    public ProcessorTask(String id,Processor p) {
        super(id);
		proc = p;
    }

    /**
     * Undertakes any special cancel processing required by Processor tasks
     */
    public void cancelConcreteTask() {//no additional processing is undertaken, any running invocations are allowed to
        //complete normally, any scheduled tasks are cancelled normally in core framework
    }  
	
	public uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage doTask() {
		return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, "No idea how to do task");
	}

	/**
	 * Retrieves the XScufl Processor object for this task
	 * @return 
	 */
	public Processor getProcessor() {
		return proc;
	}
	
	public ServiceSelectionCriteria getServiceSelectionCriteria() {
		return null;
	}
}