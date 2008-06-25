package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.Map;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

public class DummyDataflowActivity extends AbstractAsynchronousActivity<Dataflow> {
	Dataflow df;
	@Override
	public void configure(Dataflow conf) throws ActivityConfigurationException {
		this.df=conf;
		
	}

	@Override
	public void executeAsynch(Map<String, T2Reference> data,
			AsynchronousActivityCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Dataflow getConfiguration() {
		return df;
	}

}
