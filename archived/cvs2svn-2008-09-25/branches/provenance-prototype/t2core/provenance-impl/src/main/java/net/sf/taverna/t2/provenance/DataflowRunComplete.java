package net.sf.taverna.t2.provenance;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class DataflowRunComplete implements ProvenanceItem {

	public String getAsString() {
		return "<DataflowRunComplete/>";
	}

	public Element getAsXML(DataFacade dataFacade) {
		SAXBuilder saxBuilder=new SAXBuilder("org.apache.xerces.parsers.SAXParser");
		Reader stringReader=new StringReader(getAsString());
		org.jdom.Document document = null;
		try {
			document = saxBuilder.build(stringReader);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return document.getRootElement();
	}

	public String getEventType() {
		// TODO Auto-generated method stub
		return SharedVocabulary.END_WORKFLOW_EVENT_TYPE;
	}

}
