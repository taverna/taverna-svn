package net.sf.taverna.t2.provenance.item;


import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerRegistry;

import org.jdom.Element;

public class WorkflowProvenanceItem implements ProvenanceItem {

	private Dataflow dataflow;
	private String processId;
	private String parentId;
	private String identifier;

	public Dataflow getDataflow() {
		return dataflow;
	}

	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public WorkflowProvenanceItem() {
	}

	public String getAsString() {
		return null;
	}

	public Element getAsXML(ReferenceService referenceService) {
		try {
			if (this.dataflow == null) {
				System.out.println("the dataflow is null!!");
			} else {
				System.out.println("the dataflow is not null");
			}
			XMLSerializerRegistry instance = XMLSerializerRegistry.getInstance();
			XMLSerializer serializer = instance.getSerializer();
//			XMLSerializer serializer = new XMLSerializerImpl();
			System.out.println("attempting to serialise");
			return serializer.serializeDataflow(this.dataflow);
		} catch (SerializationException e) {
			System.out.println("serialize problem " + e.toString());
		}
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.WORKFLOW_EVENT_TYPE;
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
