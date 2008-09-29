package net.sf.taverna.service.executeremotely;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.RESTException;

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

	private static Logger logger =
		Logger.getLogger(RemoteWorkflowInstance.class);

	private JobREST job;

	private UILogger uiLog;

	public RemoteWorkflowInstance(JobREST job) {
		this(job, new UILogger.DummyUILogger());
	}

	public RemoteWorkflowInstance(JobREST job, UILogger uiLog) {
		this.job = job;
		this.uiLog = uiLog;
	}

	public boolean changeOutputPortTaskData(String processorId,
		String OutputPortName, Object newData) {
		// TODO Auto-generated method stub
		return false;
	}

	public void destroy() {
	}

	public String getID() {
		return job.getURI();
	}

	public Map<String, DataThing>[] getIntermediateResultsForProcessor(
		String processorName) throws UnknownProcessorException {
		logger.warn("Unimplemented: getIntermediateResultsForProcessor()");
		return null;
	}

	public Map<String, DataThing> getOutput() {
		String outputDoc;
		try {
			outputDoc = job.getOutputs().getBaclava();
		} catch (RESTException e) {
			logger.warn("Could not get output document for " + job, e);
			uiLog.log("Could not get output document for " + job);
			delay();
			throw new IllegalStateException("Could not get output document", e);
		} catch (IOException e) {
			logger.warn("Could not read output document for " + job, e);
			uiLog.log("Could not read output document for " + job);
			delay();
			throw new IllegalStateException("Could not read output document", e);
		}
		try {
			return parseDataDoc(outputDoc);
		} catch (JDOMException e) {
			logger.error("Could not parse output document for " + job, e);
			uiLog.log("Could not parse output document for " + job);
			delay();
			throw new IllegalStateException("Could not parse output document",
				e);
		}
	}

	public String getProgressReportXMLString() {
		String report;
		try {
			report = job.getReport();
		} catch (RESTException e) {
			logger.warn("Could not get progress report for " + job, e);
			uiLog.log("Could not get progress report for " + job);
			delay();
			throw new IllegalStateException("Could not get progress report", e);
		}
		if (report.length() == 0) {
			delay();
			uiLog.log("Empty progress report for " + job);
			throw new IllegalStateException("Empty progress report");
		}
		return report;
	}

	public String getStatus() {
		try {
			return job.getStatus().toString();
		} catch (RESTException e) {
			logger.warn("Could not get status for " + job, e);
			uiLog.log("Could not get status for " + job);
			delay();
			throw new IllegalStateException("Could not get status", e);
		}

	}

	public UserContext getUserContext() {
		logger.warn("Unimplemented: getUserContext()");
		return null;
	}

	public ScuflModel getWorkflowModel() {
		String scufl;
		scufl = job.getWorkflow().getScufl();
		ScuflModel model = new ScuflModel();
		try {
			XScuflParser.populate(scufl, model, null);
		} catch (ScuflException ex) {
			
			logger.error("Could not load workflow:\n" + scufl, ex);
			uiLog.log("Could not load workflow for " + job);
			delay();
			throw new IllegalStateException("Could not load workflow", ex);
		}
		return model;
	}

	public void run() throws InvalidInputException {
		throw new IllegalStateException("Can't run twice");
	}

	public void setInputs(Map inputMap) {
		throw new IllegalStateException("Can't change inputs");
	}

	public String getProvenanceXMLString() {
		logger.warn("Unimplemented: getProvenanceXMLString()");
		return null;
	}

	public void cancelExecution() {
		logger.warn("Unimplemented: cancelExecution()");
	}

	public String getErrorMessage() {
		logger.warn("Unimplemented: getErrorMessage()");
		return null;
	}

	public boolean isDataNonVolatile(String arg0) {
		logger.warn("Unimplemented: isDataNonVolatile()");
		return false;
	}

	public boolean isPaused() {
		logger.warn("Unimplemented: isPaused()");
		return false;
	}

	public void pause(String processorId) {
		logger.warn("Unimplemented: pause()");
	}

	public void pauseExecution() {
		logger.warn("Unimplemented: pauseExecution()");
	}

	public void resume(String processorId) {
		logger.warn("Unimplemented: resume()");
	}

	public void resumeExecution() {
		logger.warn("Unimplemented: resumeExecution()");
	}

	/**
	 * Avoid hammering web service if something went wrong, by sleeping for 250
	 * milliseconds. This method could be called whenever a fault occured.
	 */
	private static void delay() {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}
	}

	private static Map<String, DataThing> parseDataDoc(String xml)
		throws JDOMException {
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

}
