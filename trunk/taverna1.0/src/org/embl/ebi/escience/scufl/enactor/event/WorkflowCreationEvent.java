/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.baclava.DataThing;
import java.util.Map;
import java.util.Iterator;

public class WorkflowCreationEvent extends WorkflowInstanceEvent {

    private Map inputs;

    public WorkflowCreationEvent(WorkflowInstance workflow,
				 Map inputs) {
	super(workflow);
	this.inputs = inputs;
    }

    /**
     * Returns a map of name->DataThing objects where the names
     * are the names of workflow inputs
     */
    public Map getInputs() {
	return this.inputs;
    }

    /**
     * Override toString()
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Workflow '"+this.workflowInstance.getID()+"' created with "+this.inputs.size()+" input"+(this.inputs.size()!=1?"s":"")+"\n");
	for (Iterator i = this.inputs.keySet().iterator(); i.hasNext();) {
	    String inputName = (String)i.next();
	    DataThing inputValue = (DataThing)inputs.get(inputName);
	    String inputLSID = inputValue.getLSID(inputValue.getDataObject());
	    sb.append("  '"+inputName+"'->"+inputLSID+"\n");
	}
	UserContext workflowContext = this.workflowInstance.getUserContext();
	if (workflowContext == null) {
	    sb.append("No user context supplied\n");
	}
	else {
	    sb.append("Workflow context :\n");
	    sb.append("  'Person'->"+workflowContext.getPersonLSID()+"\n");
	    sb.append("  'ExperimentDesign'->"+workflowContext.getExperimentDesignLSID()+"\n");
	    sb.append("  'Organization'->"+workflowContext.getOrganizationLSID()+"\n");
	}
	return sb.toString();
    }
    
}
