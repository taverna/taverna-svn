package net.sf.taverna.t2.provenance.item;


import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabularly.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerRegistry;

import org.jdom.Element;

public class WorkflowProvenanceItem implements ProvenanceItem {

	private Dataflow dataflow;

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
			XMLSerializerRegistry instance = XMLSerializerRegistry.getInstance();
			XMLSerializer serializer = instance.getSerializer();
			return serializer.serializeDataflow(this.dataflow);
		} catch (SerializationException e) {
			System.out.println("serialize problem " + e.toString());
		}
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.WORKFLOW_EVENT_TYPE;
	}

}
