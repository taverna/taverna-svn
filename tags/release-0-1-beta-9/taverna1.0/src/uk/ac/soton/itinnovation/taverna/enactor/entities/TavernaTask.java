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
//                              $Date: 2004-01-27 12:57:53 $
//                              $Revision: 1.11 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.taverna.enactor.entities;

import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Task;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.TimePoint;

import java.lang.String;



public abstract class TavernaTask extends Task {

	protected TimePoint startTime = null;
	protected TimePoint endTime = null;
    
	/**
     * Default Constructor
     * @param id
     */
    public TavernaTask(String id) {
        super(id);
    }

	/**
	 * Retrieve the execution start time
	 */
	public TimePoint getStartTime() {
		return startTime;
	}

	/**
	 * Retrieve the execution end time
	 */
	public TimePoint getEndTime() {
		return endTime;
	}

    /**
     * Undertakes any special cancel processing required by Taverna tasks
     */
    public void cancelConcreteTask() {//no additional processing is undertaken, any running invocations are allowed to
        //complete normally, any scheduled tasks are cancelled normally in core framework
    }  
	
	public abstract uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage doTask();
}