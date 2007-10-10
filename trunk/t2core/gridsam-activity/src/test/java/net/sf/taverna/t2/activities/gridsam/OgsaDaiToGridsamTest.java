package net.sf.taverna.t2.activities.gridsam;

import java.io.IOException;
import java.util.Collections;

import net.sf.taverna.t2.activities.ogsadai.OgsaDaiActivity;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.DiagnosticEventHandler;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

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
		
		DiagnosticEventHandler deh = new DiagnosticEventHandler();
		edits.createDatalink(gridSam.getOutputPorts().get(0), deh);
		
		
		ogsaDai.getInputPorts().get(0).receiveEvent(token);
		Thread.sleep(10000);
		System.out.println(deh.getEventCount());
	}
}
