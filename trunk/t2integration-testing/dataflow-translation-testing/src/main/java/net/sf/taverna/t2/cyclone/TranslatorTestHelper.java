package net.sf.taverna.t2.cyclone;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
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

/**
 * A helper class to support tests for the {@link WorkflowModelTranslator}
 * @author Stuart Owen
 *
 */
public class TranslatorTestHelper {
	
	protected InMemoryDataManager dataManager;
	
	protected void setUpRavenRepository() throws IOException {
		File tmpDir = File.createTempFile("taverna", "raven");
		tmpDir.delete();
		tmpDir.mkdir();
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		TavernaSPIRegistry.setRepository(tempRepository);
	}

	protected ScuflModel loadScufl(String resourceName)
			throws UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			IOException {
		ScuflModel model = new ScuflModel();
		InputStream inStream = TranslatorTestHelper.class.getResourceAsStream("/"+resourceName);
		XScuflParser.populate(inStream,model,null);
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
		waitForCompletion(eventHandlers,60);
		
	}
	
	protected void waitForCompletion(
			Map<String, DummyEventHandler> eventHandlers, int maxtimeSeconds)
			throws InterruptedException, DataflowTimeoutException {
		int time=0;
		boolean finished = false;
		while (!finished) {
			finished = true;
			for (DummyEventHandler testEventHandler : eventHandlers.values()) {
				if (testEventHandler.getResult() == null) {
					finished = false;
					Thread.sleep(1000);
					time+=1000;
					break;
				}
				if (time>(maxtimeSeconds*1000)) throw new DataflowTimeoutException("The max time of "+maxtimeSeconds+"s was exceed waiting for the results");
			}
		}
	}
}
