/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.view.*;
import org.embl.ebi.escience.scufl.enactor.implementation.*;
import java.util.Map;
import java.util.Iterator;

public class WorkflowCreationEvent extends WorkflowInstanceEvent {

    private Map inputs;
    private String defn;
    private ScuflModel model;

    public WorkflowCreationEvent(WorkflowInstance workflow,
				 Map inputs,
				 String definitionLSID) {
	super(workflow);
	this.defn = definitionLSID;
	this.inputs = inputs;
	if (workflow instanceof WorkflowInstanceImpl) {
	    this.model = ((WorkflowInstanceImpl)workflow).getWorkflowModel();
	}
    }

    /**
     * Returns a map of name->DataThing objects where the names
     * are the names of workflow inputs
     */
    public Map getInputs() {
	return this.inputs;
    }

    /**
     * Returns the LSID of the workflow definition used to create
     * this workflow instance
     */
    public String getDefinitionLSID() {
	return this.defn;
    }

    /**
     * Return a reference to the ScuflModel object used to create
     * this workflow
     */
    public ScuflModel getModel() {
	return this.model;
    }

    /**
     * Return the XML form of the workflow definition, or 
     * return null if the workflow model has not been initialised
     * for some reason
     */
    public String getModelXML() {
	if (this.model != null) {
	    XScuflView xsv = new XScuflView(this.model);
	    String xml = xsv.getXMLText();
	    this.model.removeListener(xsv);
	    return xml;
	}
	else {
	    return null;
	}
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
	sb.append("Created from workflow definition "+this.model.getDescription().getLSID()+"\n");
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
