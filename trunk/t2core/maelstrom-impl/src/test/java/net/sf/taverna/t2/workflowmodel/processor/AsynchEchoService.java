package net.sf.taverna.t2.workflowmodel.processor;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.service.AbstractAsynchronousService;
import net.sf.taverna.t2.workflowmodel.processor.service.AsynchronousService;
import net.sf.taverna.t2.workflowmodel.processor.service.AsynchronousServiceCallback;
import net.sf.taverna.t2.workflowmodel.processor.service.ServiceConfigurationException;

public class AsynchEchoService extends
		AbstractAsynchronousService<EchoConfig> implements
		AsynchronousService<EchoConfig> {

	private EchoConfig config;
	
	@Override
	public void configure(EchoConfig conf) throws ServiceConfigurationException {
		addInput("input",0);
		addOutput("output",0,0);
		this.config = conf;
	}

	@Override
	public void executeAsynch(Map<String, EntityIdentifier> data,
			AsynchronousServiceCallback callback) {
		EntityIdentifier inputID = data.get("input");
		Map<String, EntityIdentifier> outputMap = new HashMap<String, EntityIdentifier>();
		outputMap.put("output", inputID);
		System.out.println(config.getFoo());
		callback.receiveResult(outputMap, new int[0]);
	}

	@Override
	public EchoConfig getConfiguration() {
		return config;
	}

}
