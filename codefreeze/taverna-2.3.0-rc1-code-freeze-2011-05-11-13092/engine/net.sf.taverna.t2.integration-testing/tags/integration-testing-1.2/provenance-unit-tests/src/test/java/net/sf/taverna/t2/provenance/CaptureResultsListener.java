package net.sf.taverna.t2.provenance;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class CaptureResultsListener implements ResultListener {

	private static Logger logger = Logger
			.getLogger(CaptureResultsListener.class);

	private int outputCount;
	private Map<String, Object> resultMap = new HashMap<String, Object>();
	private final InvocationContext context;

	public CaptureResultsListener(Dataflow dataflow, InvocationContext context) {

		this.context = context;
		outputCount = dataflow.getOutputPorts().size();
		logger.info("initial output count = " + outputCount);
	}

	public void resultTokenProduced(WorkflowDataToken dataToken, String portname) {
		if (dataToken.getIndex().length == 0) {
			T2Reference reference = dataToken.getData();
			logger.info("Output " + portname +": " + reference);
			resultMap.put(portname, context.getReferenceService()
					.renderIdentifier(reference, Object.class, context));
			outputCount--;
		}
	}

	public boolean isFinished() {
		return outputCount == 0;
	}

	public Map<String, Object> getResults() {
		return resultMap;
	}
	
	public Object getResult(String outputName) {
		if (! resultMap.containsKey(outputName)) {
			throw new NullPointerException("Unknown output: " + outputName);
		}
		return resultMap.get(outputName);
	}

}
