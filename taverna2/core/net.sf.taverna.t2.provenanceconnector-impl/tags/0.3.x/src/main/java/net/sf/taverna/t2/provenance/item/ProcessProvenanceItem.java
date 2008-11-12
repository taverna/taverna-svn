package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.Element;

public class ProcessProvenanceItem implements ProvenanceItem {
	private String owningProcess;
	private ProcessorProvenanceItem processorProvenanceItem;
	private String parentId;
	private String identifier;
	
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
		result.setAttribute("identifier", this.identifier);
		result.setAttribute("processID", this.owningProcess);
		result.setAttribute("parent", this.parentId);
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
	
	public String getIdentifier() {
		return identifier;
	}

	public void setProcessId(String processId) {
		this.owningProcess = processId;
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
		return owningProcess;
	}

}
