/**
 * 
 */
package net.sf.taverna.t2.annotation.beans;

/**
 * @author alanrw
 *
 */
public abstract class OntologyTerm {
	
	private Ontology containingOntology;
	
	private String concept;

	/**
	 * @return the concept
	 */
	public synchronized final String getConcept() {
		return concept;
	}

	/**
	 * @param concept the concept to set
	 */
	public synchronized final void setConcept(final String concept) {
		this.concept = concept;
	}

	/**
	 * @return the containingOntology
	 */
	public synchronized final Ontology getContainingOntology() {
		return containingOntology;
	}

	/**
	 * @param containingOntology the containingOntology to set
	 */
	public synchronized final void setContainingOntology(final Ontology containingOntology) {
		this.containingOntology = containingOntology;
	}

	/**
	 * 
	 */
	public OntologyTerm() {
		// TODO Auto-generated constructor stub
	}

}
