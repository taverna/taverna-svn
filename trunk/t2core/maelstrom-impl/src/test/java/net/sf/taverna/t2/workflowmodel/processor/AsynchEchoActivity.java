package net.sf.taverna.t2.workflowmodel.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.HealthReportImpl;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

public class AsynchEchoActivity extends
		AbstractAsynchronousActivity<EchoConfig> implements
		AsynchronousActivity<EchoConfig> {

	private EchoConfig config;

	@Override
	public void configure(EchoConfig conf) throws ActivityConfigurationException {
		addInput("input",0, new ArrayList<String>());
		addOutput("output",0,0, new ArrayList<String>());
		this.config = conf;
	}

	@Override
	public void executeAsynch(Map<String, EntityIdentifier> data,
			AsynchronousActivityCallback callback) {
		EntityIdentifier inputID = data.get("input");
		Map<String, EntityIdentifier> outputMap = new HashMap<String, EntityIdentifier>();
		outputMap.put("output", inputID);
		callback.receiveResult(outputMap, new int[0]);
	}

	@Override
	public EchoConfig getConfiguration() {
		return config;
	}

	public HealthReport checkActivityHealth() {
		return new HealthReportImpl("AsynchEchoActivity","Everything is hunky dorey",Status.OK);
	}
	
	

}
