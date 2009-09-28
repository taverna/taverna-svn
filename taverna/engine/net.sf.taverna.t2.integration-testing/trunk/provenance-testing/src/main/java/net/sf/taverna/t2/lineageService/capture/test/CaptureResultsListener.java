package net.sf.taverna.t2.lineageService.capture.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceAnalysis;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class CaptureResultsListener implements ResultListener {

	private static Logger logger = Logger.getLogger(CaptureResultsListener.class);

	private int outputCount;
	private Map<String,Object> resultMap = new HashMap<String,Object>();
	private final InvocationContext context;

	public CaptureResultsListener(Dataflow dataflow, InvocationContext context) {

		this.context = context;
		outputCount=dataflow.getOutputPorts().size();
		
		logger.info("initial output count = "+outputCount);
	}
	
	
	public void resultTokenProduced(WorkflowDataToken dataToken,
			String portname) {
		if (dataToken.getIndex().length==0) {
			T2Reference reference = dataToken.getData();
			logger.info("Output reference = " + reference);

//			ReferenceService referenceService = context.getReferenceService();
//
//			try {
//				ReferenceSetService referenceSetService = referenceService.getReferenceSetService();
//				ReferenceSet referenceSet = referenceSetService.getReferenceSet(reference);
//				Set<ExternalReferenceSPI> externalReferences = referenceSet.getExternalReferences();
//				System.out.println("data nature: "+externalReferences.iterator().next().getDataNature());
//				
////				referenceService.renderIdentifier(reference, Object.class, context);
//
//				//referenceService.resolveIdentifier(reference, null, context);
//			} catch(Exception e) {
//				System.out.println(e.getMessage());
//			}

//			ReferenceSetService referenceSetService = referenceService.getReferenceSetService();
//			ReferenceSet referenceSet = referenceSetService.getReferenceSet(reference);
//			Set<ExternalReferenceSPI> externalReferences = referenceSet.getExternalReferences();
//			System.out.println(externalReferences.iterator().next().getDataNature());
//
	//		System.out.println("data: "+context.getReferenceService().renderIdentifier(reference, String.class, context));

			// FIXME data resolution is broken -- not sure why.
//			resultMap.put(portname, context.getReferenceService().renderIdentifier(reference, Object.class,context));

			outputCount--;
			logger.debug("current output count = "+outputCount);
		}
	}

	public boolean isFinished() {
		return outputCount == 0;
	}

	public Object getResult(String outputName) {
		return resultMap.get(outputName);
	}

}
