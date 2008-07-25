package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
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

	public WorkflowProvenanceItem(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public String getAsString() {
		return null;
	}

	public Element getAsXML(ReferenceService referenceService) {
		try {
			return XMLSerializerRegistry.getInstance().getSerializer().serializeDataflow(this.dataflow);
		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.WORKFLOW_EVENT_TYPE;
	}

}
