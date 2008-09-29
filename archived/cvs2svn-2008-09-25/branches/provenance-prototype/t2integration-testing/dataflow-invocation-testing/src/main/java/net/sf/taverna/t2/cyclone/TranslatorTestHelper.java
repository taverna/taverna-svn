package net.sf.taverna.t2.cyclone;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.InMemoryProvenanceConnector;
import net.sf.taverna.t2.provenance.ProvenanceConnector;
import net.sf.taverna.t2.provenance.WebServiceProvenanceConnector;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.junit.Before;

/**
 * A helper class to support tests for the {@link WorkflowModelTranslator}
 * 
 * @author Stuart Owen
 * 
 */
public class TranslatorTestHelper {

	protected AbstractDataManager dataManager;
	protected DataFacade dataFacade;
	protected InvocationContext context;
	protected ProvenanceConnector provenanceConnector;
	
	@SuppressWarnings("unchecked")
	@Before
	public void makeDataManager() {
		dataManager = new InMemoryDataManager("namespace",
				Collections.EMPTY_SET);
		provenanceConnector = new WebServiceProvenanceConnector();
		//provenanceConnector = new InMemoryProvenanceConnector();
		dataFacade=new DataFacade(dataManager);
		context =  new InvocationContext() {
			public DataManager getDataManager() {
				return dataManager;
			}

			public ProvenanceConnector getProvenanceManager() {
				// TODO Auto-generated method stub
				return provenanceConnector;
			}
		};
	}

	protected void setUpRavenRepository() throws IOException {
		File tmpDir = File.createTempFile("taverna", "raven");
		tmpDir.delete();
		tmpDir.mkdir();
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		TavernaSPIRegistry.setRepository(tempRepository);
	}

	protected Dataflow translateScuflFile(String filename) throws IOException,
			UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			WorkflowTranslationException {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl(filename);
		Dataflow dataflow = WorkflowModelTranslator.doTranslation(model);
		return dataflow;
	}

	protected ScuflModel loadScufl(String resourceName)
			throws UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			IOException {
		ScuflModel model = new ScuflModel();
		InputStream inStream = TranslatorTestHelper.class
				.getResourceAsStream("/" + resourceName);
		XScuflParser.populate(inStream, model, null);
		return model;
	}

	protected Map<String, DummyEventHandler> addDummyEventHandlersToOutputs(
			Dataflow dataflow) throws EditException {
		Edits edits = new EditsImpl();
		Map<String, DummyEventHandler> eventHandlers = new HashMap<String, DummyEventHandler>();
		for (DataflowOutputPort outputPort : dataflow.getOutputPorts()) {
			DummyEventHandler testOutputEventHandler = new DummyEventHandler(
					dataManager);
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
}
