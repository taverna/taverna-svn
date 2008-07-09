package net.sf.taverna.t2.provenance;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.util.tools.DataflowSerialiser;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class WorkflowProvenanceItem implements ProvenanceItem {

	private Dataflow dataflow;
	private DataflowSerialiser serialiser;

	public WorkflowProvenanceItem(Dataflow dataflow) {
		this.dataflow = dataflow;
		serialiser = new DataflowSerialiser(dataflow);
	}

	public String getAsString() {
		String serialiseWorkflow = serialiser.serialiseWorkflow();
		return serialiseWorkflow;
	}

	public Element getAsXML(DataFacade dataFacade) {
		SAXBuilder saxBuilder=new SAXBuilder("org.apache.xerces.parsers.SAXParser");
		Reader stringReader=new StringReader(serialiser.serialiseWorkflow());
		org.jdom.Document document = null;
		try {
			document = saxBuilder.build(stringReader);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document.getRootElement();
	}

	public String getEventType() {
		return SharedVocabulary.WORKFLOW_EVENT_TYPE;
	}

}
