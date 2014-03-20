package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
//import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Port;

public class DummyEventHandler extends AbstractAnnotatedThing<Port> implements
		EventHandlingInputPort {

	protected int eventCount = 0;
	public ReferenceService referenceService;
	private Object result;

	public DummyEventHandler(ReferenceService referenceService) {
		super();
		this.referenceService = referenceService;
	}

	public void receiveEvent(WorkflowDataToken token) {
		eventCount++;
		
		try {
			result = referenceService.renderIdentifier(token.getData(), Object.class, null);
		} catch (Exception e1) {
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
		this.result=null;
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
