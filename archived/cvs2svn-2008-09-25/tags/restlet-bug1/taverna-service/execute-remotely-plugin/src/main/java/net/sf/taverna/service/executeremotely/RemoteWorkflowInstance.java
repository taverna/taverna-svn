package net.sf.taverna.service.executeremotely;

import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Map;

import net.sf.taverna.service.interfaces.client.UnknownJobException;
import net.sf.taverna.service.wsdl.client.Taverna;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

public class RemoteWorkflowInstance implements WorkflowInstance {

	private static Logger logger = Logger.getLogger(RemoteWorkflowInstance.class);
	
	private Taverna service;
	private String job_id;

	public RemoteWorkflowInstance(Taverna service, String id) {
		if (service == null || id == null) {
			throw new NullPointerException("Taverna service or job id is null");
		}
		service = service;
		job_id = id;
	}
	
	public void cancelExecution() {
		// TODO Auto-generated method stub
	}

	public boolean changeOutputPortTaskData(String processorId,
		String OutputPortName, Object newData) {
		// TODO Auto-generated method stub
		return false;
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getID() {
		// Assume UUIDs are globally unique
		return job_id;
	}

	public Map<String, DataThing>[] getIntermediateResultsForProcessor(
		String processorName) throws UnknownProcessorException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, DataThing> getOutput() {
		String outputDoc;
		try {
			outputDoc = service.getResultDocument(job_id);
		} catch (UnknownJobException ex) {
			delay();
			logger.error("Unknown job when retrieving output", ex);
			throw new IllegalStateException("Unknown job id", ex);
		} catch (RemoteException ex) {
			delay();
			logger.warn("Could not retrieve output", ex);
			throw new IllegalStateException("Could not retrieve output", ex);
		}
		try {
			return parseDataDoc(outputDoc);
		} catch (JDOMException e) {
			delay();
			logger.error("Could not parse output document", e);
			throw new IllegalStateException("Could not parse output document", e);
		}
	}

	/**
	 * Avoid hammering web service if something went wrong, by
	 * sleeping for 250 milliseconds. This method could be called
	 * whenever a fault occured.
	 *
	 */
	private static void delay() {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}
	}
	
	private static Map<String, DataThing> parseDataDoc(String xml) throws JDOMException {
		SAXBuilder builder = new SAXBuilder();		
		Document doc;
		try {
			doc = builder.build(new StringReader(xml));
		} catch (IOException e) {
			logger.error("Could not read inputDoc with StringReader", e);
			delay();
			throw new RuntimeException(e);
		}
		return DataThingXMLFactory.parseDataDocument(doc);				
	}
	

	public String getProgressReportXMLString() {
		String progress;
		try {
			progress = service.getProgressReport(job_id);
		} catch (UnknownJobException ex) {
			delay();
			throw new IllegalStateException("Unknown job " + job_id);
		} catch (RemoteException ex) {
			logger.warn("Could not retrieve progress report", ex);
			delay();
			throw new IllegalStateException("Could not retrieve progress report", ex);

		}
		return progress;
	}

	public String getProvenanceXMLString() {
		return null;
	}

	public String getStatus() {
		try {
			String status = service.jobStatus(job_id);
			if (status.equals("UNKNOWN")) {
				logger.error("Unknown job " + job_id);
				delay();
				throw new IllegalStateException("Unknown job " + job_id);
			}
			return status;
		} catch (RemoteException ex) {
			logger.warn("Could not check job status", ex);
			delay();
			throw new IllegalStateException("Could not check job status", ex);
		}
	}

	public UserContext getUserContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public ScuflModel getWorkflowModel() {
		String scufl;
		try {
			scufl = service.getWorkflow(job_id);
		} catch (UnknownJobException ex) {
			logger.error("Unknown job when retrieving workflow model", ex);
			delay();
			throw new IllegalStateException("Unknown job id", ex);
		} catch (RemoteException ex) {
			logger.warn("Could not get workflow", ex);
			delay();
			throw new IllegalStateException("Could not get workflow", ex);

		}
		ScuflModel model = new ScuflModel();
		try {
			XScuflParser.populate(scufl, model, null);
		} catch (ScuflException ex) {
			logger.error("Could not load workflow:\n" + scufl, ex);
			delay();
			throw new IllegalStateException("Could not load workflow", ex);
		}
		return model;
	}

	public boolean isDataNonVolatile(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPaused() {
		return false;
	}

	public void pause(String processorId) {
		
	}

	public void pauseExecution() {
		// TODO Auto-generated method stub

	}

	public void resume(String processorId) {
		// TODO Auto-generated method stub

	}

	public void resumeExecution() {
		// TODO Auto-generated method stub

	}

	public void run() throws InvalidInputException {
		throw new IllegalStateException("Can't run twice");
	}

	public void setInputs(Map inputMap) {
		throw new IllegalStateException("Can't change inputs");
	}

}
