package net.sf.taverna.t2.activities.gridsam;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import net.sf.taverna.t2.activities.gridsam.GridsamActivity;
import net.sf.taverna.t2.activities.ogsadai.OgsaDaiActivity;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.AbstractAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.DiagnosticEventHandler;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.apache.commons.io.IOUtils;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class OgsaDaiToGridsamTest {

	public InMemoryDataManager dataManager;	
	
	@Before
	public void makeDataManager() {
		dataManager = new InMemoryDataManager(
				"namespace", Collections.EMPTY_SET);
		ContextManager.baseManager = dataManager;
	}
	
	private ProcessorImpl processor;
	
	public Processor createGridsamProcessor() throws EditException, JDOMException,
			IOException, ActivityConfigurationException,
			MalformedIdentifierException {

		// Create a processor from the simple echo activity
		GridsamActivity activity = new GridsamActivity();
		activity.configure(null);
		return Tools.buildFromActivity(activity);
	}
	
	public Processor createOgsaDaiProcessor() throws EditException, JDOMException,
	IOException, ActivityConfigurationException,
			MalformedIdentifierException {

		// Create a processor from the simple echo activity
		OgsaDaiActivity activity = new OgsaDaiActivity();
		activity.configure(null);
		return Tools.buildFromActivity(activity);
	}


	@Test
	public void testSingleDataTokenBehaviour()
			throws MalformedIdentifierException, EditException, JDOMException,
			IOException, ActivityConfigurationException, InterruptedException {
		Edits edits = new EditsImpl();
		Processor gridSam = createGridsamProcessor();
		Processor ogsaDai = createOgsaDaiProcessor();
		
		
		Datalink link=edits.createDatalink(ogsaDai.getOutputPorts().get(0), gridSam.getInputPorts().get(0));
		edits.getConnectDatalinkEdit(link).doEdit();
		
		
		// Set up data manager
		ContextManager.baseManager = dataManager;
		WorkflowDataToken token = new WorkflowDataToken("outerProcess1",
				new int[0], Literal.buildLiteral("select * from littleblackbook where id<10"));
		
		TestEventHandler testOutputEventHandler = new TestEventHandler(dataManager);
		link = edits.createDatalink(gridSam.getOutputPorts().get(0), testOutputEventHandler);
		edits.getConnectDatalinkEdit(link).doEdit();
		
		
		ogsaDai.getInputPorts().get(0).receiveEvent(token);
		Thread.sleep(10000);
		System.out.println(testOutputEventHandler.getEventCount());
		System.out.println(testOutputEventHandler.getResult());
		if (testOutputEventHandler.getResult() instanceof InputStream) {
			System.out.println(IOUtils.toString((InputStream)testOutputEventHandler.getResult()));
		}
	}
}

class TestEventHandler extends AbstractAnnotatedThing implements EventHandlingInputPort {

	protected int eventCount = 0;
	public InMemoryDataManager dataManager;	
	private Object result;
	

	public TestEventHandler(InMemoryDataManager dataManager) {
		super();
		this.dataManager = dataManager;
	}

	public void receiveEvent(Event e) {
		eventCount++;
		if (e instanceof WorkflowDataToken) {
			WorkflowDataToken token = (WorkflowDataToken)e;
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
		}
		System.out.println(e.toString());
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
