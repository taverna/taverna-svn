package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.AbstractAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
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
import org.junit.Before;
import org.junit.Test;

/**
 * @author David Withers
 * @author Stuart Owen
 * 
 */
public class ModelTranslatorTest extends TranslatorTestHelper {

	public InMemoryDataManager dataManager;

	@SuppressWarnings("unchecked")
	@Before
	public void makeDataManager() {
		dataManager = new InMemoryDataManager("namespace",
				Collections.EMPTY_SET);
		ContextManager.baseManager = dataManager;
	}

	private Dataflow translateScuflFile(String filename) throws IOException,
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

	@Test
	public void translateAndValidateTest() throws Exception {
		DataflowImpl dataflow = (DataflowImpl) translateScuflFile("ModifiedBiomartAndEMBOSSAnalysis.xml");
		// DataflowImpl dataflow = (DataflowImpl)
		// translateScuflFile("very_simple_workflow.xml");
		DataflowValidationReport report = dataflow.checkValidity();
		for (Processor unsatisfiedProcessor : report.getUnsatisfiedProcessors()) {
			System.out.println(unsatisfiedProcessor.getLocalName());
		}
		assertTrue(report.getUnsatisfiedProcessors().size() == 0);
		for (Processor failedProcessor : report.getFailedProcessors()) {
			System.out.println(failedProcessor.getLocalName());
		}
		assertTrue(report.getFailedProcessors().size() == 0);
		for (DataflowOutputPort unresolvedOutput : report
				.getUnresolvedOutputs()) {
			System.out.println(unresolvedOutput.getName());
		}
		assertTrue(report.getUnresolvedOutputs().size() == 0);

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

		for (Processor processor : dataflow.getProcessors()) {
			if (processor.getLocalName().equals("hsapiens_gene_ensembl")) {
				System.out.println("fire MakeList");
				processor.fire("test");
				break;
			}
		}

		boolean finished = false;
		while (!finished) {
			finished = true;
			for (DummyEventHandler testEventHandler : eventHandlers.values()) {
				if (testEventHandler.getResult() == null) {
					finished = false;
					Thread.sleep(1000);
					break;
				}
			}
		}
		for (Map.Entry<String, DummyEventHandler> entry : eventHandlers
				.entrySet()) {
			System.out.println("Values for port " + entry.getKey());
			Object result = entry.getValue().getResult();
			if (result instanceof List) {
				for (Object element : (List<?>) result) {
					System.out.println(element);
				}
			} else {
				System.out.println(result);
			}
		}
	}
}

class DummyEventHandler extends AbstractAnnotatedThing implements
		EventHandlingInputPort {

	protected int eventCount = 0;
	public InMemoryDataManager dataManager;
	private Object result;

	public DummyEventHandler(InMemoryDataManager dataManager) {
		super();
		this.dataManager = dataManager;
	}

	public void receiveEvent(WorkflowDataToken token) {
		eventCount++;
		DataFacade dataFacade = new DataFacade(dataManager);
		try {
			result = dataFacade.resolve(token.getData());
		} catch (RetrievalException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(token);
	}

	public Object getResult() {
		return result;
	}

	public int getEventCount() {
		return this.eventCount;
	}

	public void reset() {
		this.eventCount = 0;
	}

	public int getDepth() {
		return 0;
	}

	public String getName() {
		return "Test port";
	}

	public Datalink getIncomingLink() {
		// TODO Auto-generated method stub
		return null;
	}

}
