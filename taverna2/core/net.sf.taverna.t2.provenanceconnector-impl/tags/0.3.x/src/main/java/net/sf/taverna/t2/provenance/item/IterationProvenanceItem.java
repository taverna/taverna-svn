package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.Element;

public class IterationProvenanceItem implements ProvenanceItem {
	private int [] iteration;
	private InputDataProvenanceItem inputDataItem;
	private OutputDataProvenanceItem outputDataItem;
	private ErrorProvenanceItem errorItem;
	private String processId;
	private String parentId;
	private String identifier;
	
	

	public IterationProvenanceItem(int[] iteration) {
		super();
		this.iteration = iteration;
	}



	public void setInputDataItem(InputDataProvenanceItem inputDataItem) {
		this.inputDataItem = inputDataItem;
	}



	public void setOutputDataItem(OutputDataProvenanceItem outputDataItem) {
		this.outputDataItem = outputDataItem;
	}



	public void setErrorItem(ErrorProvenanceItem errorItem) {
		this.errorItem = errorItem;
	}



	public Element getAsXML(ReferenceService referenceService) {
		Element result = new Element("iteration");
		result.setAttribute("id", iterationToString());
		result.setAttribute("identifier", this.identifier);
		result.setAttribute("processID", this.processId);
		result.setAttribute("parent", this.parentId);
		if (inputDataItem!=null) result.addContent(inputDataItem.getAsXML(referenceService));
		if (outputDataItem!=null) result.addContent(outputDataItem.getAsXML(referenceService));
		if (errorItem!=null) result.addContent(errorItem.getAsXML(referenceService));
		return result;
	}
	
	private String iterationToString() {
		String result = "[";
		for (int i=0;i<iteration.length;i++) {
			result+=iteration[i];
			if (i<(iteration.length-1)) result+=",";
		}
		result+="]";
		return result;
	}



	public String getAsString() {
		return null;
	}



	public int[] getIteration() {
		return iteration;
	}



	public InputDataProvenanceItem getInputDataItem() {
		return inputDataItem;
	}



	public OutputDataProvenanceItem getOutputDataItem() {
		return outputDataItem;
	}



	public String getEventType() {
		return SharedVocabulary.ITERATION_EVENT_TYPE;
	}



	public ErrorProvenanceItem getErrorItem() {
		return errorItem;
	}



	public void setIteration(int[] iteration) {
		this.iteration = iteration;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}



	public String getParentId() {
		// TODO Auto-generated method stub
		return parentId;
	}



	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		// TODO Auto-generated method stub
		
	}



	public void setParentId(String parentId) {
		this.parentId = parentId;
		// TODO Auto-generated method stub
		
	}



	public String getProcessId() {
		// TODO Auto-generated method stub
		return processId;
	}
	
	

}
