package net.sf.taverna.t2.workflowmodel.serialization;

import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class DummyActivity extends AbstractActivity<Integer> {

	private Integer bean=null;
	
	@Override
	public void configure(Integer conf) throws ActivityConfigurationException {
		bean=conf;
	}

	@Override
	public Integer getConfiguration() {
		return bean;
	}

}
