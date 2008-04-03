/*
 * FetaOntology.java
 *
 * Created on January 26, 2005, 2:39 PM
 */

package uk.ac.man.cs.img.fetaEngine.commons;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 
 * @author penpecip
 */
public class FetaOntology {
	private URL ontoURL;

	private URL serviceOntoURL;

	/** Creates a new instance of FetaOntology */
	public FetaOntology() {

	}

	public FetaOntology(URL ontologyURL) {
		ontoURL = ontologyURL;
	}

	public InputStream getAnnotationOntology() {
		if (ontoURL == null) {
			return this.getClass().getResourceAsStream(
					"/mygrid-services-lite.rdfs");
		} else {
			try {
				return ontoURL.openStream();
			} catch (IOException ex) {
				return this.getClass().getResourceAsStream(
						"/mygrid-services-lite.rdfs");
			}
		}
	}

	public InputStream getServiceOntology() {
		// TO DO change this to read from URL
		return this.getClass().getResourceAsStream("/service.rdfs");
	}

	public void setAnnotationOntologyURL(URL url) {
		this.ontoURL = url;
	}

	public void setServiceOntologyURL(URL url) {
		this.serviceOntoURL = url;
	}

}
