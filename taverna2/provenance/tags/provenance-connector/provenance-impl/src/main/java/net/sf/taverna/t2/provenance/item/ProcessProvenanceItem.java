package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabularly.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;

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
	

	public Element getAsXML(ReferenceService referenceService) {
		Element result = new Element("process");
		result.setAttribute("facadeID",getFacadeID());
		result.setAttribute("dataflowID",getDataflowID());
		if (processorProvenanceItem!=null) {
			result.addContent(processorProvenanceItem.getAsXML(referenceService));
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
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.PROCESS_EVENT_TYPE;
	}

	public String getOwningProcess() {
		return owningProcess;
	}

	public void setOwningProcess(String owningProcess) {
		this.owningProcess = owningProcess;
	}

}
