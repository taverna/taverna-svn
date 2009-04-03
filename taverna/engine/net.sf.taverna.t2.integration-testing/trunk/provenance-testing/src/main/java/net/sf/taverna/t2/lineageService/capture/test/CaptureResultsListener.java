package net.sf.taverna.t2.lineageService.capture.test;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class CaptureResultsListener implements ResultListener {

	private int outputCount;
	private Map<String,Object> resultMap = new HashMap<String,Object>();
	private final InvocationContext context;

	public CaptureResultsListener(Dataflow dataflow, InvocationContext context) {

		this.context = context;
		outputCount=dataflow.getOutputPorts().size();
	}
	public void resultTokenProduced(WorkflowDataToken dataToken,
			String portname) {
		if (dataToken.getIndex().length==0) {
			T2Reference reference = dataToken.getData();
			System.out.println("Output reference = " + reference);
			resultMap.put(portname, context.getReferenceService().renderIdentifier(reference, Object.class,context));
			outputCount--;
		}
	}

	public boolean isFinished() {
		return outputCount == 0;
	}

	public Object getResult(String outputName) {
		return resultMap.get(outputName);
	}

}
