package net.sf.taverna.t2.testing;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.springframework.context.ApplicationContext;

/**
 * A helper class to support tests for the {@link WorkflowModelTranslator}
 * 
 * @author Stuart Owen
 * 
 */
public class InvocationTestHelper extends DataflowTranslationHelper {

	protected InvocationContext context;
	private ReferenceService referenceService;
	private ProvenanceConnector provenanceConnector;
	
	@SuppressWarnings("unchecked")
	@Before
	public void makeDataManager() {
		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
		"inMemoryIntegrationTestsContext.xml");
		referenceService = (ReferenceService) appContext.getBean("t2reference.service.referenceService");
		provenanceConnector = (ProvenanceConnector) appContext.getBean("provenanceConnector");
		
		context =  new InvocationContext() {


			public ReferenceService getReferenceService() {
				return referenceService;
			}

			public <T> List<? extends T> getEntities(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			public ProvenanceConnector getProvenanceConnector() {
				return provenanceConnector;
			}
		};
	}


	protected Map<String, DummyEventHandler> addDummyEventHandlersToOutputs(
			Dataflow dataflow) throws EditException {
		Edits edits = new EditsImpl();
		Map<String, DummyEventHandler> eventHandlers = new HashMap<String, DummyEventHandler>();
		for (DataflowOutputPort outputPort : dataflow.getOutputPorts()) {
			DummyEventHandler testOutputEventHandler = new DummyEventHandler(
					context.getReferenceService());
			eventHandlers.put(outputPort.getName(), testOutputEventHandler);
			Datalink link = edits.createDatalink(outputPort,
					testOutputEventHandler);
			edits.getConnectDatalinkEdit(link).doEdit();
		}
		return eventHandlers;
	}

	protected DataflowValidationReport validateDataflow(Dataflow dataflow) {
		DataflowValidationReport report = dataflow.checkValidity();
		for (TokenProcessingEntity unsatisfiedEntity : report.getUnsatisfiedEntities()) {
			System.out.println(unsatisfiedEntity.getLocalName());
		}
		for (TokenProcessingEntity failedEntity : report.getFailedEntities()) {
			System.out.println(failedEntity.getLocalName());
		}
		for (DataflowOutputPort unresolvedOutput : report
				.getUnresolvedOutputs()) {
			System.out.println(unresolvedOutput.getName());
		}
		return report;
	}

	/**
	 * 
	 * Uses a default max time of 1 minute.
	 * 
	 * @param eventHandlers
	 * @throws InterruptedException
	 * @throws DataflowTimeoutException
	 */
	protected void waitForCompletion(
			Map<String, DummyEventHandler> eventHandlers)
			throws InterruptedException, DataflowTimeoutException {
		waitForCompletion(eventHandlers, 60);

	}

	/**
	 * 
	 * Uses a default max time of 30 seconds
	 * 
	 * @param listener
	 * @throws InterruptedException
	 * @throws DataflowTimeoutException
	 */
	protected void waitForCompletion(CaptureResultsListener listener) throws InterruptedException, DataflowTimeoutException {
		waitForCompletion(listener, 30);
	}
	
	protected void waitForCompletion(CaptureResultsListener listener,int maxtimeSeconds) throws InterruptedException, DataflowTimeoutException{
		float time=0;
		int maxTime = maxtimeSeconds*1000;
		int interval=100;
		while (!listener.isFinished()) {
			Thread.sleep(interval);
			time+=interval;
			if (time>maxTime) {
				throw new DataflowTimeoutException("The max time of "
						+ maxtimeSeconds
						+ "s was exceed waiting for the results");
			}
		}
	}
	protected void waitForCompletion(
			Map<String, DummyEventHandler> eventHandlers, int maxtimeSeconds)
			throws InterruptedException, DataflowTimeoutException {
		int time = 0;
		boolean finished = false;
		while (!finished) {
			finished = true;
			for (DummyEventHandler testEventHandler : eventHandlers.values()) {
				if (testEventHandler.getResult() == null) {
					finished = false;
					Thread.sleep(1000);
					time += 1000;
					break;
				}
				if (time > (maxtimeSeconds * 1000))
					throw new DataflowTimeoutException("The max time of "
							+ maxtimeSeconds
							+ "s was exceed waiting for the results");
			}
		}
	}
	
	protected Dataflow loadDataflow(String resourceName) throws Exception {
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		InputStream inStream = InvocationTestHelper.class.getResourceAsStream("/t2flow/" + resourceName);
		if (inStream==null) throw new IOException("Unable to find resource for t2 dataflow:"+resourceName);
		SAXBuilder builder = new SAXBuilder();
		Element el= builder.build(inStream).detachRootElement();
		return deserializer.deserializeDataflow(el);
	}
}
