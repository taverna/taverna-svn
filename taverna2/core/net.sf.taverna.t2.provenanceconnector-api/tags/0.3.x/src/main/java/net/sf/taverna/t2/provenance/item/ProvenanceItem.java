package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.Element;

public interface ProvenanceItem {

	public Element getAsXML(ReferenceService referenceService);

	public String getAsString();

	public String getEventType();

	public String getIdentifier();

	/**
	 * A unique id for this event. Any children would use this as their parentId
	 * 
	 * @param identifier
	 */
	public void setIdentifier(String identifier);

	public void setProcessId(String processId);
	
	public String getProcessId();

	/**
	 * The owner of this provenance Item
	 * 
	 * @param parentId
	 */
	public void setParentId(String parentId);

	public String getParentId();

}