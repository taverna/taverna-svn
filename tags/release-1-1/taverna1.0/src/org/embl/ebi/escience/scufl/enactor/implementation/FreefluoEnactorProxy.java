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
import uk.ac.soton.itinnovation.freefluo.conf.*;

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

    private Engine engine = null;
    private static EnactorProxy staticInstance = null;
    private EngineConfiguration config = null;
    
    public static EnactorProxy getInstance() {
	if (staticInstance == null) {
	    staticInstance = new FreefluoEnactorProxy();
	}
	return staticInstance;
    }

    public FreefluoEnactorProxy() {
	// See whether we need to create a local engine or a proxy
	// to talk to one over SOAP
	String enactorEndpoint = System.getProperty("mygrid.enactor.soap.endpoint");

        // temp use of system properties for auth credential for 
        // remote freefluo web service.
        String username = System.getProperty("mygrid.enactor.username");
        String password = System.getProperty("mygrid.enactor.password");
        EngineConfiguration engineConfig = getEngineConfiguration();
	if (enactorEndpoint != null){
	    try {
                EngineStub stub = new EngineStub(engineConfig, new URL(enactorEndpoint));     
		this.engine = stub;
                if(username != null) {
                    stub.setUsername(username);
                }
                if(password != null) {
                    stub.setPassword(password);
                }
	    }
	    catch (Exception ex) {
		// Problem with remote enactor creation
		ex.printStackTrace();
	    }
	}
	else {
            this.engine = new EngineImpl(engineConfig);
	}
    }

    public WorkflowInstance compileWorkflow(ScuflModel workflow, Map input, UserContext user) 
	          throws WorkflowSubmissionException {
	WorkflowInstance workflowInstance = compileWorkflow(workflow, user);
        workflowInstance.setInputs(input);
        return workflowInstance;
    }

    public WorkflowInstance compileWorkflow(ScuflModel workflow, UserContext user) throws WorkflowSubmissionException {
	try {
            XScuflView scuflView = new XScuflView(workflow);
            String strWorkflow = scuflView.getXMLText();
	    String workflowInstanceId = engine.compile(strWorkflow);

            if(user != null) {
              engine.setFlowContext(workflowInstanceId, user.toFlowContext());
            }

            WorkflowInstance workflowInstance = new WorkflowInstanceImpl(engine, workflow, workflowInstanceId);
	    String definitionLSID = workflow.getDescription().getLSID();
	    String instanceLSID = workflowInstance.getID();
	    WorkflowInstanceImpl.instanceToDefinitionLSID.put(instanceLSID, definitionLSID);
            return workflowInstance;
	}
	catch (Exception e) {
	    WorkflowSubmissionException wse = new WorkflowSubmissionException("Error during submission of workflow to in memory freefluo enactor");
	    wse.initCause(e);
	    e.printStackTrace();
            wse.initCause(e);
	    throw wse;
	}
    }

    private EngineConfiguration getEngineConfiguration() {
        if(config != null) {
          return config;
        }
        
        ConfigurationDescription configDescription = new ConfigurationDescription("taverna", 
                "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaScuflModelParser", 
                "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaDataHandler"); 
        try {
          config = new EngineConfigurationImpl(configDescription, getClass().getClassLoader());
        }
        catch(Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e.getMessage(), e);
        }
        return config;
    }
}
