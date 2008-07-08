package net.sf.taverna.t2.testing;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class CaptureResultsListener implements ResultListener {

	private int outputCount;
	private Map<String,Object> resultMap = new HashMap<String,Object>();
	private ReferenceService referenceService;
	
	public CaptureResultsListener(Dataflow dataflow, ReferenceService referenceService) {
		this.referenceService = referenceService;
		outputCount=dataflow.getOutputPorts().size();
	}
	public void resultTokenProduced(WorkflowDataToken dataToken,
			String portname) {
		if (dataToken.getIndex().length==0) {
			try {
				resultMap.put(portname, referenceService.renderIdentifier(dataToken.getData(), Object.class, null));
			} catch (RetrievalException e) {
				e.printStackTrace();
			} 
			outputCount--;
		}
	}
	
	public boolean isFinished() {
		return outputCount==0;
	}
	
	public Object getResult(String outputName) {
		return resultMap.get(outputName);
	}

}
