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
//      Created Date        :   2003/04/08
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-04-12 13:18:08 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;


//java imports
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

//third party imports

//local imports
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Flow;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.FlowStates;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowEvent;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowReceipt;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Task;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.taskstate.TaskState;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowCallback;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowMessage;

import org.apache.log4j.Logger;


/**
 * Represents a receipt for a workflow submitted by the client
 */
public class TavernaFlowReceipt extends FlowReceipt {
    
    private Logger logger = Logger.getLogger(getClass());

    /**
     * Constructor for this concrete instance of a flow receipt
     * @ Flow to which this receipt applies.
     */
    public TavernaFlowReceipt(Flow flow) throws WorkflowSubmitException {
        super(flow);        
    }

    /**
     * Process an event associated with a Flow
     *
     * @param FlowEvent describing the event.
     */
    public void processFlowEvent(FlowEvent flowEvent) {        

    }   
}