package net.sf.taverna.t2.workflowmodel.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
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
		addInput("input",0, true, new ArrayList<Class<? extends ExternalReferenceSPI>>(), String.class);
		addOutput("output",0,0);
		this.config = conf;
	}

	@Override
	public void executeAsynch(Map<String, T2Reference> data,
			AsynchronousActivityCallback callback) {
		T2Reference inputID = data.get("input");
		Map<String, T2Reference> outputMap = new HashMap<String, T2Reference>();
		outputMap.put("output", inputID);
		callback.receiveResult(outputMap, new int[0]);
	}

	@Override
	public EchoConfig getConfiguration() {
		return config;
	}

	public HealthReport checkActivityHealth() {
		return new HealthReport("AsynchEchoActivity",
				"Everything is hunky dorey", Status.OK);
	}

}
