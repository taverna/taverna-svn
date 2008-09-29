/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.view.XScuflView;

import uk.ac.soton.itinnovation.freefluo.conf.ConfigurationDescription;
import uk.ac.soton.itinnovation.freefluo.conf.EngineConfiguration;
import uk.ac.soton.itinnovation.freefluo.conf.EngineConfigurationImpl;
import uk.ac.soton.itinnovation.freefluo.lang.BadlyFormedDocumentException;
import uk.ac.soton.itinnovation.freefluo.lang.ParsingException;
import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.ac.soton.itinnovation.freefluo.main.EngineImpl;
import uk.ac.soton.itinnovation.freefluo.main.EngineStub;
import uk.ac.soton.itinnovation.freefluo.main.InvalidFlowContextException;
import uk.ac.soton.itinnovation.freefluo.main.UnknownWorkflowInstanceException;

/**
 * An implementation of the EnactorProxy class that uses the Freefluo workflow
 * enactor. For now, either a local or remote freefluo enactor is used depending
 * on whether the property mygrid.enactor.soap.endpoint is set in
 * mygrid.properties. If it isn't set, a local in-memory enactor is used; if it
 * is, the enactor at the provided soap endpoint is used.
 * 
 * @author Tom Oinn
 */
public class FreefluoEnactorProxy implements EnactorProxy {

	private static Logger logger = Logger.getLogger(FreefluoEnactorProxy.class);
	
	private Engine engine = null;

	private static EnactorProxy staticInstance = null;

	private EngineConfiguration config = null;

	/**
	 * Singleton pattern to retrieve a FreefluoEnactorProxy
	 * 
	 * @return
	 */
	public static EnactorProxy getInstance() {
		if (staticInstance == null) {
			staticInstance = new FreefluoEnactorProxy();
		}
		return staticInstance;
	}

	/** 
	 * Protected constructor, use singleton pattern with getInstance() instead
	 * 
	 * @see getInstance()
	 *
	 */
	protected FreefluoEnactorProxy() {
		// See whether we need to create a local engine or a proxy
		// to talk to one over SOAP
		String enactorEndpoint = System
				.getProperty("mygrid.enactor.soap.endpoint");

		// temp use of system properties for auth credential for
		// remote freefluo web service.
		String username = System.getProperty("mygrid.enactor.username");
		String password = System.getProperty("mygrid.enactor.password");
		EngineConfiguration engineConfig = getEngineConfiguration();
		if (enactorEndpoint != null) {
				EngineStub stub;
				try {
					stub = new EngineStub(engineConfig, new URL(
							enactorEndpoint));
				} catch (MalformedURLException e) {
					logger.error("Invalid enactor endpoint URL: " + enactorEndpoint, e);
					throw new IllegalStateException("Invalid enactor endpoint: " + enactorEndpoint);
				}
				this.engine = stub;
				if (username != null) {
					stub.setUsername(username);
				}
				if (password != null) {
					stub.setPassword(password);
				}
		} else {
			this.engine = new EngineImpl(engineConfig);
		}
	}

	public WorkflowInstance compileWorkflow(ScuflModel workflow, Map input,
			UserContext user) throws WorkflowSubmissionException {
		WorkflowInstance workflowInstance = compileWorkflow(workflow, user);
		workflowInstance.setInputs(input);
		return workflowInstance;
	}

	public WorkflowInstance compileWorkflow(ScuflModel workflow,
			UserContext user) throws WorkflowSubmissionException {
		String strWorkflow = XScuflView.getXMLText(workflow);
		String workflowInstanceId;
		try {
			workflowInstanceId = engine.compile(strWorkflow);
		} catch (BadlyFormedDocumentException e) {
			throw new WorkflowSubmissionException("Badly formed workflow document", e);
		} catch (ParsingException e) {
			throw new WorkflowSubmissionException("Could not parse workflow", e);
		}
		if (user != null) {
			try {
				engine.setFlowContext(workflowInstanceId, user.toFlowContext());
			} catch (UnknownWorkflowInstanceException e) {
				throw new WorkflowSubmissionException("Unknown workflow instance " + workflowInstanceId, e);
			} catch (InvalidFlowContextException e) {
				throw new WorkflowSubmissionException("Invalid flow context " + user.toFlowContext(), e);
			}
		}
		WorkflowInstance workflowInstance = WorkflowInstanceImpl.getInstance(
				engine, workflow, workflowInstanceId);
		return workflowInstance;
	}

	private EngineConfiguration getEngineConfiguration() {
		if (config != null) {
			return config;
		}

		ConfigurationDescription configDescription = new ConfigurationDescription(
				"taverna",
				"uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaScuflModelParser",
				"uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaDataHandler");
		try {
			config = new EngineConfigurationImpl(configDescription, getClass()
					.getClassLoader());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
		return config;
	}
}
