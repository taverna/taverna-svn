package net.sf.taverna.t2.testing;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Port;

public class DummyEventHandler extends AbstractAnnotatedThing<Port> implements
		EventHandlingInputPort {

	protected int eventCount = 0;
	public AbstractDataManager dataManager;
	private Object result;

	public DummyEventHandler(AbstractDataManager dataManager) {
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
