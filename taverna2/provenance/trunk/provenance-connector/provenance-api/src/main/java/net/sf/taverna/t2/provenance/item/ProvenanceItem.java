package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.Element;

public interface ProvenanceItem {

	public Element getAsXML(ReferenceService referenceService);
	
	public String getAsString();
	
	public String getEventType();

}