/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.implementation;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;

// Utility Imports
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * An implementation of the EnactorProxy class that represents
 * an in memory instance of the FreeFluo enactor.
 * @author Tom Oinn
 */
public class FreefluoEnactorProxy implements EnactorProxy {

    /**
     * Initialize the proxy settings for the enactor should
     * these be required.
     */
    static {
	try {
	    ResourceBundle rb = ResourceBundle.getBundle("mygrid");
	    Properties sysProps = System.getProperties();
	    Enumeration keys = rb.getKeys();
	    while (keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		String value = (String) rb.getString(key);
		sysProps.put(key, value);
	    }
	}
	catch (Exception e) {
	    // No resource bundle specified, probably an error
	    // but we'll fail silently here.
	}
    }
    
    private static String DEFAULT_USER = "DEFAULT_USER";
    private static String DEFAULT_USER_CONTEXT = "DEFAULT_USER_CONTEXT";
    private static String BROKER_CLASS = "uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker";

    /**
     * Create an enactor instance if one does not already exist
     * and submit the supplied workflow to it. This then returns
     * a TavernaFlowReceipt object, now retrofitted to implement
     * the WorkflowInstance interface.
     */
    public WorkflowInstance submitWorkflow(ScuflModel workflow, Map inputs) 
	throws WorkflowSubmissionException {
	try {
	    FlowBroker broker = FlowBrokerFactory.createFlowBroker(BROKER_CLASS);
	    TavernaFlowReceipt theReceipt = (TavernaFlowReceipt)((TavernaFlowBroker)broker).submitFlow(workflow,
												       inputs,
												       DEFAULT_USER,
												       DEFAULT_USER_CONTEXT);
	    return (WorkflowInstance)theReceipt;
	}
	catch (Exception e) {
	    WorkflowSubmissionException wse = new WorkflowSubmissionException("Error during submission of workflow to in memory freefluo enactor");
	    wse.initCause(e);
	    throw wse;
	}
    }

}
