package net.sf.taverna.t2.provenance;

import org.jdom.Element;

public class IterationProvenanceItem implements ProvenanceItem {
	private int [] iteration;
	private InputDataProvenanceItem inputDataItem;
	private OutputDataProvenanceItem outputDataItem;
	private ErrorProvenanceItem errorItem;
	
	

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



	public Element getAsXML() {
		Element result = new Element("iteration");
		result.setAttribute("id", iterationToString());
		if (inputDataItem!=null) result.addContent(inputDataItem.getAsXML());
		if (outputDataItem!=null) result.addContent(outputDataItem.getAsXML());
		if (errorItem!=null) result.addContent(errorItem.getAsXML());
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

}
