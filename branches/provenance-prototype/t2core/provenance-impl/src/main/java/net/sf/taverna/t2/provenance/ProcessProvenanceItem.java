package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;

import org.jdom.Element;

public class ProcessProvenanceItem implements ProvenanceItem {
	private String owningProcess;
	private ProcessorProvenanceItem processorProvenanceItem;
	
	public ProcessProvenanceItem(String owningProcess) {
		super();
		this.owningProcess = owningProcess;
	}

	public String getDataflowID() {
		return owningProcess.split(":")[1];
	}
	
	public String getFacadeID() {
		return owningProcess.split(":")[0];
	}
	

	public Element getAsXML(DataFacade dataFacade) {
		Element result = new Element("process");
		result.setAttribute("facadeID",getFacadeID());
		result.setAttribute("dataflowID",getDataflowID());
		if (processorProvenanceItem!=null) {
			result.addContent(processorProvenanceItem.getAsXML(dataFacade));
		}
		return result;
	}



	public void setProcessorProvenanceItem(
			ProcessorProvenanceItem processorProvenanceItem) {
		this.processorProvenanceItem = processorProvenanceItem;
	}
	
	public ProcessorProvenanceItem getProcessorProvenanceItem() {
		return processorProvenanceItem;
	}

	public String getAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.PROCESS_EVENT_TYPE;
	}

}
