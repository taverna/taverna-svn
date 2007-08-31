package net.sf.taverna.t2.cloudone.identifier;

/**
 * Enumeration of the three possible types of reference: data documents, error
 * documents and named lists.
 * 
 * @author Tom Oinn
 * 
 */
public enum IDType {

	Data("ddoc"), Error("error"), List("list"), Literal("literal");

	public final String uripart;

	private IDType(String uripart) {
		this.uripart = uripart;
	}
	
}
