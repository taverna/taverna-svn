package net.sf.taverna.t2.provenance;

import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

import org.jdom.Element;

public abstract class DataProvenanceItem implements ProvenanceItem {
	
	private Map<String, EntityIdentifier> dataMap;
	protected abstract boolean isInput();
	public DataProvenanceItem(Map<String, EntityIdentifier> dataMap) {
		super();
		this.dataMap = dataMap;
	}

	public Element getAsXML(DataFacade dataFacade) {
		String name = isInput() ? "inputdata" : "outputdata";
		Element result = new Element(name);
		for (String port : dataMap.keySet()) {
			Element portElement = new Element("port");
			portElement.setAttribute("name",port);
			result.addContent(portElement);
			//dataFacade.resolveToString(dataMap.get(port));
		}
		return result;
	}

}
