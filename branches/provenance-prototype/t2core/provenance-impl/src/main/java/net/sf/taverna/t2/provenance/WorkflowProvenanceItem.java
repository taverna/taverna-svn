package net.sf.taverna.t2.provenance;

import java.io.IOException;
import java.io.StringReader;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.util.tools.DataflowSerialiser;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class WorkflowProvenanceItem implements ProvenanceItem {

	private Dataflow dataflow;

	public WorkflowProvenanceItem(Dataflow dataflow) {
		super();
		this.dataflow = dataflow;
	}

	public Element getAsXML(DataFacade dataFacade) {
		try {
			Document build = new SAXBuilder().build((new StringReader(
					getAsString())));
			return build.getRootElement();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getAsString() {
		DataflowSerialiser dataflowSerialiser = new DataflowSerialiser(
				this.dataflow);
		String serialiseWorkflow = dataflowSerialiser.serialiseWorkflow();
		return serialiseWorkflow;
	}

}
