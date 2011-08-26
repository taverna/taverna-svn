package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;

import org.jdom.Element;

public interface ProvenanceItem {

	public Element getAsXML(DataFacade dataFacade);
	
	public String getAsString();
	
	public String getEventType();

}