/*
 * FetaOntologyTermModel.java
 *
 * Created on March 17, 2005, 2:13 PM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

/**
 * 
 * @author alperp
 */

public class FetaOntologyTermModel {

	private String rdfID;

	private String rdfLabel;

	private String rdfComment;

	private boolean hasExplicitLabel = false;

	/** Creates a new instance of FetaOntologyTermModel */
	public FetaOntologyTermModel(String id, String label) {
		rdfID = id;
		rdfLabel = label;
		hasExplicitLabel = true;
	}

	/** Creates a new instance of FetaOntologyTermModel */
	public FetaOntologyTermModel(String id) {
		rdfID = id;
		String[] resourceIDParts = id.split("#");
		if (resourceIDParts.length == 2) {
			rdfLabel = resourceIDParts[1];
			System.out.println("Setting label -->" + rdfLabel);
			// temporarily ..if it has a label specified it should be set after
			// construction
		} else {

			rdfLabel = id;
		}
		rdfComment = "No documentation is specified for this term.";
	}

	public String toString() {
		return rdfLabel;
	}

	public String getID() {
		return rdfID;

	}

	public String getLabel() {
		return rdfLabel;

	}

	public String getDefinition() {
		return rdfComment;

	}

	public void setLabel(String label) {
		rdfLabel = label;
		hasExplicitLabel = true;

	}

	public void setDefinition(String definition) {
		rdfComment = definition;

	}

	public boolean hasExplicitLabel() {
		return hasExplicitLabel;

	}

}
