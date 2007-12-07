package net.sf.taverna.t2.activities.apiconsumer;

import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

public class ApiConsumerActivity extends AbstractAsynchronousActivity<ApiConsumerActivityConfigBean> {

	@Override
	public void configure(ApiConsumerActivityConfigBean bean)
			throws ActivityConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeAsynch(Map<String, EntityIdentifier> data,
			AsynchronousActivityCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ApiConsumerActivityConfigBean getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}
