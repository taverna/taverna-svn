package net.sf.taverna.t2.testing;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class CaptureResultsListener implements ResultListener {

	private int outputCount;
	private Map<String,Object> resultMap = new HashMap<String,Object>();
	private DataFacade dataFacade;
	
	public CaptureResultsListener(Dataflow dataflow, DataFacade dataFacade) {
		outputCount=dataflow.getOutputPorts().size();
		this.dataFacade=dataFacade;
	}
	public void resultTokenProduced(WorkflowDataToken dataToken,
			String portname) {
		if (dataToken.getIndex().length==0) {
			try {
				resultMap.put(portname, dataFacade.resolve(dataToken.getData()));
			} catch (RetrievalException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
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
