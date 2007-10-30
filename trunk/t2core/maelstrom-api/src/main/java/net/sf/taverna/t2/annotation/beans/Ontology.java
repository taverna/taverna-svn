/**
 * 
 */
package net.sf.taverna.t2.annotation.beans;

import java.net.URI;

/**
 * @author alanrw
 *
 */
public abstract class Ontology {
	
	private URI sourceURI;

	/**
	 * 
	 */
	public Ontology() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the sourceURI
	 */
	public synchronized final URI getSourceURI() {
		return sourceURI;
	}

	/**
	 * @param sourceURI the sourceURI to set
	 */
	public synchronized final void setSourceURI(final URI sourceURI) {
		this.sourceURI = sourceURI;
	}

}
