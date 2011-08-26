/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.implementation;

import org.embl.ebi.escience.scufl.ScuflModel;

import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;

import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.ac.soton.itinnovation.freefluo.main.EngineStub;
import uk.ac.soton.itinnovation.freefluo.main.EngineImpl;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener;

// Utility Imports
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import java.lang.Exception;
import java.lang.String;
import java.lang.System;

import java.net.URL;


/**
 * An implementation of the EnactorProxy class that uses
 * the Freefluo workflow enactor.  For now, either a local
 * or remote freefluo enactor is used depending on whether
 * the property mygrid.enactor.soap.endpoint is set in
 * mygrid.properties.  If it isn't set, a local in-memory
 * enactor is used; if it is, the enactor at the provided
 * soap endpoint is used.  
 *
 * @author Tom Oinn
 */
public class FreefluoEnactorProxy implements EnactorProxy {

    public WorkflowInstance compileWorkflow(ScuflModel workflow, Map input) 
	          throws WorkflowSubmissionException {
	      WorkflowInstance workflowInstance = compileWorkflow(workflow);
        workflowInstance.setInputs(input);
        return workflowInstance;
    }

    public WorkflowInstance compileWorkflow(ScuflModel workflow) throws WorkflowSubmissionException {
        try {
            Engine engine = null;

            // would be nice to have a definition class somewhere with property definitions in
            String enactorEndpoint = System.getProperty("mygrid.enactor.soap.endpoint");
            if(enactorEndpoint != null) {
                engine = new EngineStub(new URL(enactorEndpoint));
            }
            else {
                // Use an in-process freefluo engine
	              engine = new EngineImpl();
            }

            String workflowInstanceId = engine.compile(workflow);
            WorkflowInstance workflowInstance = new WorkflowInstanceImpl(engine, workflowInstanceId);   
            return workflowInstance;
	      }
	      catch (Exception e) {
	          WorkflowSubmissionException wse = new WorkflowSubmissionException("Error during submission of workflow to in memory freefluo enactor");
	          wse.initCause(e);
	          throw wse;
	      }
    }

    /**
     * Dummy context handler, doesn't do anything at the moment
     */
    public void setUserContext(UserContext theContext) {
	      //
    }

    /**
     * Return default user context
     */
    public UserContext getUserContext() {
	      return new UserContext() {
            public String getUser() {
                return "Unknown user";
            }
            public String getProject() {
                return "Unknown project";
            }
            public String getOrganisation() {
                return "Unknown organisation";
            }
            public String getExperiment() {
                return "Unknown experiment";
            }
            public String[] getUserNameAndPassword(String resourceExpression) {
                return new String[]{"",""};
            }
	      };
    }	   
}