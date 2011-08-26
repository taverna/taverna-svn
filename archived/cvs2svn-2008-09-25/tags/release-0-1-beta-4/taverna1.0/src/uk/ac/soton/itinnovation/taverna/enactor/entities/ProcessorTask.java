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
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-06-12 16:09:23 $
//                              $Revision: 1.16 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.TimePoint;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceSelectionCriteria;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.DataParseException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;

// Utility Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// JDOM Imports
import org.jdom.Element;
import org.jdom.JDOMException;

import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTask;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.lang.StringBuffer;



public abstract class ProcessorTask extends TavernaTask{
    
    protected static final String PROVENANCE_NAMESPACE = "http://www.it-innovation.soton.ac.uk/taverna/workflow/enactor/provenance";
    protected Processor proc = null;
    protected LogLevel logLevel = null;	
    private Logger logger = Logger.getLogger(ProcessorTask.class);
		private String userID;
		private String userCtx;
    
    
    /**
     * Default Constructor
     * @param id
     */
    public ProcessorTask(String id,Processor p,LogLevel l,String userID, String userCtx) {
        super(id);
				proc = p;
				this.logLevel = new LogLevel(l.getLevel());
				this.userID = userID;
				this.userCtx = userCtx;
    }
    
		/**
		 * Retrieve the user identifier for the parent workflow
		 */
		 protected String getUserID() {
			 return userID;
		 }

		/**
		 * Retrieve the user context for the parent workflow
		 */
		 protected String getUserNamespaceContext() {
			 return userCtx;
		 }

    /**
     * Undertakes any special cancel processing required by Processor tasks
     */
    public void cancelConcreteTask() {
	//no additional processing is undertaken, any running invocations are allowed to
        //complete normally, any scheduled tasks are cancelled normally in core framework
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
    
    /**
     * Retrieve provenance information for this task, concrete tasks should
     * overide this method and provide this information as an XML JDOM element
     */
    public org.jdom.Element getProvenance() {
	return new Element("processorExecution");
    }
    
    
    /**
     * Wrapper method to enable pre and post processing for actual service invocations
     */
    public uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage doTask() {
	try {
	    
		uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage result;
	    startTime =  new TimePoint();
		//do any pre-processing here
	    
	    // Check all the inputs, find any cardinality mismatches.
	    Map inputInfoMap = new HashMap();
	    GraphNode[] inputs = getParents();
	    for (int i = 0; i < inputs.length; i++) {
		if (inputs[i] instanceof PortTask) {
		    PortTask pt = (PortTask)inputs[i];
		    Part p = pt.getData();
				String name = p.getName();
				Object value = p.getTypedValue();
				PartInfo pi = new PartInfo(p);
				inputInfoMap.put(name, pi);
			}
	    }
	    
	    // Get the output PortTask objects
	    Map outputPortTaskMap = new HashMap();
	    GraphNode[] outputs = getChildren();
	    for (int i = 0; i < outputs.length; i++) {
				if (outputs[i] instanceof PortTask) {
						PortTask pt = (PortTask)outputs[i];
						outputPortTaskMap.put(pt.getScuflPort().getName(),pt); 
				}
	    }

	    InputSet is = new InputSet();
	    // This is the interesting bit, we need to build a list of 
	    // map objects where each map contains the set of named
	    // inputs for a single invocation of the processor. We then
	    // iterate over this list, configuring each instance of the
	    // processor invocation, collecting the results into a similar
	    // output map list. After all the invocations have completed
	    // the ouput map list is used to populate the output port tasks
	    // with the (potentially higher dimension) overall outputs.
	    
	    //... TODO - ADD PART ARRAYS TO INPUT SET HERE
	    // Completely naive implementation that just puts the parts in as they
	    // are without attempting to do any iteration at all
	    for (Iterator i = inputInfoMap.keySet().iterator(); i.hasNext(); ) {
				PartInfo pi = (PartInfo)inputInfoMap.get(i.next());
				int dim = pi.getDimension();
				if(dim==0)
					is.addSinglePart(pi.getPart());
				else if (dim>0){
					//at present only support single dimension String Arrays
					//if this is a String Array and the associated input is a String 
					//(distinguished by port syntactic type)
					//then want to iterate
					
					Part part = pi.getPart();
					Port port = proc.locatePort(part.getName());
					if(part.getType().equals("string[]") && port.getSyntacticType().equals("string")) {
						String[] strings = (String[]) part.getTypedValue();
						Part[] prts = new Part[strings.length];
						for(int x=0;x<strings.length;x++) {
							Part prt = new Part(-1,part.getName(),"string",strings[x]);
							prts[x] = prt;
							}
							is.addOrthogonalArray(prts);
					}
					else {
						//type is actually supposed to be a string array so supply it directly
						is.addSinglePart(pi.getPart());
					}
				}
				else 
					return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED,"Unsupported array type needed");
	    }
	    
	    List inputList = is.getCurrentState();
	    List outputList = new ArrayList();
	    
	    // Iterate over all the maps in the input list
	    
	    for (int i = 0; i < inputList.size(); i++) {
				Map inputMap = (Map)inputList.get(i);
				// Invoke with the appropriate input map
				// Put result parts in the output map.
				Map outputMap = execute(inputMap);
				outputList.add(outputMap);
	    }
			if(inputList.size()==0) {
				outputList.add(execute(new HashMap()));		//for the exceptional case of no input parameters
			}
	    // Check whether there was any iteration
	    if (outputList.size() == 1) {
		Map outputMap = (Map)outputList.get(0);
		for (Iterator i = outputMap.keySet().iterator(); i.hasNext(); ) {
		    Part p = (Part)outputMap.get(i.next());
		    // TODO - put parts in the appropriate port tasks for output
		    PortTask pt = (PortTask)outputPortTaskMap.get(p.getName());
		    if(pt!=null)
					pt.setData(p);
		}
	    }
	    else {
		int outputSizes = outputList.size();
		// Iterate over the different part names
		
		Map firstOutputRow = (Map)outputList.get(0);
		for (Iterator i = firstOutputRow.keySet().iterator(); i.hasNext(); ) {
		    Part exemplarPart = (Part)firstOutputRow.get(i.next());
		    String partName = exemplarPart.getName();
		    // Add a '[]' to the part type
		    String partType = exemplarPart.getType()+"[]";
		    // Assemble the actual data array by iterating over all
		    // the rows
		    Object[] partData = new Object[outputSizes];
		    for (int j = 0; j < outputSizes; j++) {
			// Populate the partData array from the data 
			// in each output part.
					try {
							Map row = (Map)outputList.get(j);
							Part thePart = (Part)row.get(partName);
							Object data = thePart.getTypedValue();
							partData[j] = data;
						}
						catch (Exception e) {
							// TODO - Return a fault code
							logger.error(e);
							return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED,"Unable to obtain part data");
							}
					}
					// Now have an array populated with the appropriate data.
					// TODO - Create the Part object and put it into the appropriate output port task
					Part thePart = new Part(-1, partName, partType, partData);
					PortTask pt = (PortTask)outputPortTaskMap.get(thePart.getName());
					if (pt!=null)	{
						pt.setData(thePart);
					}
				}
	    }
			endTime = new TimePoint();
			return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.COMPLETE,"Task completed successfully");
	    

	    //return result;
	}
	catch (TaskExecutionException ex) {
			endTime = new TimePoint();
			logger.error(ex);
			return new TaskStateMessage(getParentFlow().getID(),getID(), TaskStateMessage.FAILED,ex.getMessage());
	}
	catch (Exception ex){
	    endTime = new TimePoint();
		logger.error(ex);
	    return new TaskStateMessage(getParentFlow().getID(), 
					getID(), 
					TaskStateMessage.FAILED, 
					"Unrecognised dispatch failure for a task within the workflow.");
	}	
    }
    
    
    /**
     * Method that actually undertakes a service action. Should be implemented by concrete processors.
		 * @return output map containing part name / value pairs.
     */
    protected abstract java.util.Map execute(Map inputMap) throws TaskExecutionException;
}
/**
 * Contains the current inner state of the input model
 */
class InputSet {

    private List currentList;

    public InputSet() {
	this.currentList = new ArrayList();
    }

    public synchronized List getCurrentState() {
	return this.currentList;
    }
    
    /**
     * Add a single item to the table, just adds it to each
     * row in place without having to do the entire orthogonal
     * copy and replace. Is otherwise identical in behaviour
     * to the addOrthogonalArray called with a single element
     * array
     */
    public void addSinglePart(Part newPart) {
	if (currentList.size() == 0) {
	    // Catch the empty list
	    Map inputMap = new HashMap();
	    inputMap.put(newPart.getName(), newPart);
	    currentList.add(inputMap);
	}
	else {
	    for (int i = 0; i < currentList.size(); i++) {
		// Iterate over each row in the current list
		// adding the single part to the map.
		Map inputMap = (Map)currentList.get(i);
		inputMap.put(newPart.getName(), newPart);
	    }
	}
    }

    /**
     * Add a new column to the table, using the orthogonal
     * join method to create new rows from the existing ones.
     */
    public void addOrthogonalArray(Part[] newParts) {
	// Catch the first new inputs to this set
	if (currentList.size() == 0) {
	    for (int i = 0; i < newParts.length; i++) {
		Map inputMap = new HashMap();
		inputMap.put(newParts[i].getName(), newParts[i]);
		currentList.add(inputMap);
	    }
	}
	else {
	    List newList = new ArrayList();
	    // Iterate over each current map in the list
	    for (int i = 0; i < currentList.size(); i++) {
		Map currentInputMap = (Map)currentList.get(i);
		// for each input term copy the existing map
		// into a new one and add the new part to it
		Map newInputMap = new HashMap();
		for (int j = 0; j < newParts.length; j++) {
		    // Copy the part references from the old
		    // map into the new one...
		    for (Iterator k = currentInputMap.keySet().iterator(); k.hasNext(); ) {
			Part p = (Part)(currentInputMap.get(k.next()));
			newInputMap.put(p.getName(), p);
		    }
		    // ...and add the new part as well
		    newInputMap.put(newParts[j].getName(), newParts[j]);
		    newList.add(newInputMap);
		}
	    }
	    // Write the new list to the current list field 
	    this.currentList = newList;
	}
    }
    
    /**
     * Add a new column to the table assuming that the 
     * array length is the same as the list length, just
     * adding a new Part to each map.
     */
    public void addArray(Part[] newParts) {
	if (newParts.length == currentList.size()) {
	    // This is the only condition under which it's possible
	    // to use this addition method
	    for (int i = 0; i < newParts.length; i++) {
		Map inputMap = (Map)currentList.get(i);
		inputMap.put(newParts[i].getName(), newParts[i]);
	    }
	}
	else {
	    // Add the array using the orthogonal join method
	    addOrthogonalArray(newParts);
	}
    }

}


/**
 * Contains and extracts information about a supplied Part object including
 * cardinality and dimensionality, basic type, name etc.
 * tmo@ebi.ac.uk, 5th June 2003
 */
class PartInfo {
    
    Part thePart;
    int dimension;
    Class underlyingType;
    int[] dimensionSizes;

    /**
     * Create a new PartInfo object about the specified Part
     */
    public PartInfo(Part thePart) throws DataParseException, JDOMException {
			List dimensionSizeList = new ArrayList();
			this.thePart = thePart;
			
			// Get the underlying dimensionality of the input type by
			// counting the number of times we can get an array type
			// out of it.
			this.dimension = 0;
			Object value = thePart.getTypedValue();
			while (value.getClass().isArray()) {
					this.dimension++;
					dimensionSizeList.add(new Integer(((Object[])value).length));
					value = ((Object[])value)[0];
			}
			this.underlyingType = value.getClass();
			this.dimensionSizes = new int[dimensionSizeList.size()];
			for (int i = 0; i < dimensionSizeList.size(); i++ ) {
					Integer integer = (Integer)dimensionSizeList.get(i);
					this.dimensionSizes[i] = integer.intValue();
			}
	    
    }
    
    /**
     * Get the part object
     */
    public Part getPart() {
	return this.thePart;
    }
    
    /**
     * Get the dimensionality of the Part object used to create
     * the PartInfo, this will be zero for non array types, 1 
     * for single dimension arrays etc.
     */
    public int getDimension() {
	return this.dimension;
    }

    /**
     * Get the name of the underlying Part object
     */
    public String getDataName() {
	return this.thePart.getName();
    }

    /**
     * Get the value of the underlying Part object
     */
    public Object getDataValue() throws DataParseException, JDOMException {
	return this.thePart.getTypedValue();
    }

    /**
     * Get the Class representing a single object
     * within the (potentially higher dimensional)
     * Part object
     */
    public Class getDataClass() {
	return this.underlyingType;
    }

    /**
     * Return a string representation of the information
     * about this Part object for debug purposes.
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Part info -------------\n");
	sb.append("  name = "+getDataName()+"\n");
	sb.append("  type = "+getDataClass().toString()+"\n");
	sb.append("  dim  = "+getDimension()+"\n");
	if (getDimension() > 0) {
	    sb.append("  lens = ");
	    for (int i = 0; i < getDimension(); i++) {
		sb.append("["+dimensionSizes[i]+"]");
	    }
	    sb.append("\n");
	}
	return sb.toString();
    }
	
}
