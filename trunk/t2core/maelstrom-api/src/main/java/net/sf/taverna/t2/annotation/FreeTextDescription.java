package net.sf.taverna.t2.annotation;

/**
 * An unconstrained textual description held as a String
 * 
 * @author Tom Oinn
 * 
 */
public interface FreeTextDescription extends WorkflowAnnotation {

	/**
	 * Returns the descriptive text
	 * 
	 * @return free text description
	 */
	public String getDescription();

}
