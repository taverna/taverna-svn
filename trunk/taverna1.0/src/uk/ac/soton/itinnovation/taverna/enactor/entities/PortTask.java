
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
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005-01-18 11:14:07 $
//                              $Revision: 1.32 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import org.embl.ebi.escience.scufl.enactor.event.*;
import org.embl.ebi.escience.scufl.enactor.implementation.*;
import org.embl.ebi.escience.scufl.enactor.*;

import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;
import uk.ac.soton.itinnovation.freefluo.core.flow.*; 
import uk.ac.soton.itinnovation.freefluo.main.Engine;

// Utility Imports
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import java.lang.System;
import java.util.Collection;
import java.util.*;



/**
 * GraphNode that represents a port on a Processor.
 */
public class PortTask extends AbstractTask {
    
    private static Logger logger = Logger.getLogger(PortTask.class);
    public static int IN = 0;
    public static int OUT = 1;
    // The WorkflowInstance object which can access this
    // workflow instance
    private WorkflowInstance workflowInstance = null;
    private Port thePort; 
    private DataThing theDataThing = null;
    
    public PortTask(String id, Flow flow, Port port) {
	super(id, flow);
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
	if (dataAvailable()) {
	    return;
	}
	//System.out.println("Pushing data into port task "+getScuflPort().getProcessor().getName()+"."+getScuflPort().getName());
	// Check whether the new data is a lower dimension than
	// the type of this port task
	String portSyntaxType = thePort.getSyntacticType();
	String dataSyntaxType = newDataThing.getSyntacticType();
	// If, for example, we have a 'text/plain' being put into a l('text/plain') then
	// we can reasonably create a new container with just the single element in
	//if (portSyntaxType != dataSyntaxType) {
	String portSetType = portSyntaxType.split("\\'")[0];
	String dataSetType = dataSyntaxType.split("\\'")[0];
	////System.out.println("Set types are "+portSetType+" and "+dataSetType);
	// Get the number of 'l(' elements
	int portDimension = (portSetType.length())/2;
	int dataDimension = (dataSetType.length())/2;
	int encapsulationDifference = portDimension - dataDimension;
	////System.out.println("Think this is a difference of "+encapsulationDifference+" ("+portDimension+"-"+dataDimension+")");
	if (encapsulationDifference > 0) {
	    String[] lsidWrapArray = new String[encapsulationDifference];
	    Object theDataObject = newDataThing.getDataObject();
	    while (encapsulationDifference > 0) {
		encapsulationDifference--;
		// While the dimensionality has not been reconciled create
		// a new List container and put the current object in it.
		//System.out.println("Wrapping data...");
		List newList = new ArrayList();
		newList.add(theDataObject);
		theDataObject = newList;
	    }
	    this.theDataThing = new DataThing(theDataObject);
	    this.theDataThing.copyMetadataFrom(newDataThing);	
	    // Fully populate the dataThing with LSID values if it doesn't already have them
	    this.theDataThing.fillLSIDValues();
	    // Emit an event corresponding to the wrapping operation.
	    String originalLSID = newDataThing.getLSID(newDataThing.getDataObject());
	    List l = (List)this.theDataThing.getDataObject();
	    for (int i = 0; i < lsidWrapArray.length; i++) {
		lsidWrapArray[i] = this.theDataThing.getLSID(l);
		try {
		    l = (List)l.get(0);
		}
		catch (ClassCastException cce) {
		    break;
		}
	    }
	    WorkflowEventDispatcher.DISPATCHER.fireCollectionConstructed(new CollectionConstructionEvent(workflowInstance,
													 lsidWrapArray,
													 originalLSID));
	}
	else {
	    this.theDataThing = newDataThing;	
	    // Fully populate the dataThing with LSID values if it doesn't already have them
	    this.theDataThing.fillLSIDValues();
	}
	// Copy any MIME types available from the markup object
	// on the Scufl port into the MIME container in the
	// DataThing. This will only happen on input ports as 
	// these are the only workflow entities that can support
	// the additional annotation at the moment.
	if (getScuflPort() instanceof OutputPort) {
	    PortTask childPortTask = null;
	    Collection g = getChildren();
	    for (Iterator i = g.iterator(); i.hasNext();) {
		Task task = (Task) i.next();
                if (task instanceof PortTask) {
		    childPortTask = (PortTask) task;
		    break;
		}
	    }
	    if (childPortTask!=null) {
		Port targetMetadataPort = childPortTask.getScuflPort();
		SemanticMarkup portMarkup = targetMetadataPort.getMetadata();
		String[] portMIMETypes = portMarkup.getMIMETypes();
		for (int i = 0; i < portMIMETypes.length; i++) {
		    ////System.out.println("Adding mime type "+portMIMETypes[i]+" to "+((Object)theDataThing).toString());
		    this.theDataThing.getMetadata().addMIMEType(portMIMETypes[i]);
		}
		// Copy any semantic markup into the markup object as well
		this.theDataThing.getMetadata().setSemanticType(portMarkup.getSemanticType());
	    }
	    // Now copy all MIME types available from this port
	    SemanticMarkup portMarkup = getScuflPort().getMetadata();
	    String[] portMIMETypes = portMarkup.getMIMETypes();
	    for (int i = 0; i < portMIMETypes.length; i++) {
		////System.out.println("Adding mime type "+portMIMETypes[i]+" to "+((Object)theDataThing).toString());
		this.theDataThing.getMetadata().addMIMEType(portMIMETypes[i]);
	    }
	}
	// Fully populate the dataThing with LSID values if it doesn't already have them
	this.theDataThing.fillLSIDValues();
	// If this is a workflow source then store the data thing.
	if (getScuflPort().isSource()) {
	    if (ProcessorTask.STORE != null) {
		try {
		    ProcessorTask.STORE.storeDataThing(theDataThing,true);
		}
		catch (Exception ex) {
		    //
		}
	    }
	}
	if (getScuflPort() instanceof InputPort) {
	    // This is an input
	    Collection parents = getParents();
	    Set removeMe = new HashSet();
	    if (parents.size() > 1) {
		for (Iterator i = parents.iterator(); i.hasNext(); ) {
		    PortTask port = (PortTask)i.next();
		    if (port.dataAvailable() == false) {
			removeMe.add(port);
		    }
		}
	    }
	    for (Iterator i = removeMe.iterator(); i.hasNext();) {
		getParents().remove(i.next());
	    }
	}
	/**
	 // Kick all parent tasks into the completed state (um, is this the right way to do this?)
	 for (Iterator i = getParents().iterator(); i.hasNext(); ) {
	 Object o = i.next();
	 if (o instanceof PortTask) {
	 ((PortTask)o).cancel();
	 }
	 }
	*/
    }
    
    /**
     * Forces class to clean itself up a bit.
     */
    protected void handleDestroy() {
	thePort = null;
	theDataThing = null;
    }
    

    /**
     * Transfer data from this port task into any child port tasks that are present, this
     * is effectively pushing information from the output port to the input port; other
     * processor implementations pull from the input port and push to the output port so
     * this is the link inbetween two processors.
     */
    public void handleRun(RunEvent runEvent) {
	try{
	    //don't do anything, later versions may have persistence writing to do
	    //set data on child porttasks too
	    if(theDataThing == null) {
		fail("Task " + getTaskId() + " in flow " + getFlow().getFlowId() + " failed.  " + 
					   "No data for port " + thePort.getName() + ",please check its links");
	    }	
	    else {
		// Get the workflow instance object
		Flow flow = getFlow();
		String flowID = flow.getFlowId();
		Engine e = flow.getEngine();
		this.workflowInstance = (WorkflowInstance)new org.embl.ebi.escience.scufl.enactor.implementation.WorkflowInstanceImpl(e, thePort.getProcessor().getModel(), flowID);
                //System.out.println("Invoking : "+getScuflPort().getProcessor().getName()+"."+getScuflPort().getName());
		for(Iterator i = getChildren().iterator(); i.hasNext();) {
		    Task task = (Task) i.next();
                    if(task instanceof PortTask) {
			PortTask childPortTask = (PortTask) task;
			childPortTask.setData(this.theDataThing);
		    }
		}

                // "Task " + getTaskId() + " in flow " + getFlow().getFlowId() + " completed successfully"
		complete();
	    }
	}
	catch(Exception ex) {
	    logger.error(ex);
	    fail("Task " + getTaskId() + " in flow " + getFlow().getFlowId() + " failed.  " + ex.getMessage());
	}
	//System.out.println("Done : "+getScuflPort().getProcessor().getName()+"."+getScuflPort().getName());
    }

}
