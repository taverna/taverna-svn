/**
 * 
 */
package net.sf.taverna.t2.activities.sadi.utils;

import com.hp.hpl.jena.ontology.OntResource;

import ca.wilkinsonlab.sadi.utils.OwlUtils;

/**
 * Utility methods to format OWL property and class names.
 * Halfway through the second time I decided to change how properties and
 * classes were displayed in the various dialogs, I decided I'd rather
 * change them all in one place...
 * @author Luke McCarthy
 */
public class LabelUtils
{
	/**
	 * Returns a human-readable label for the specified OntResource.
	 * @param r the OntResource (usually an OntProperty or OntClass)
	 * @return a human-readable label for the specified OntResource
	 */
	public static String getLabel(OntResource r)
	{
		return getLabel(r, false);
	}
	
	/**
	 * Returns a human-readable label for the specified OntResource
	 * that may include the resource's URI if it exists.
	 * @param r the OntResource (usually an OntProperty or OntClass)
	 * @param withURI if true, includ the resource's URI
	 * @return a human-readable label for the specified OntResource
	 */
	public static String getLabel(OntResource r, boolean withURI)
	{
		String label = OwlUtils.getLabel(r);
		if (withURI && r.isURIResource())
			return String.format("%s (%s)", label, r.getURI());
		else
			return label;
	}
}
