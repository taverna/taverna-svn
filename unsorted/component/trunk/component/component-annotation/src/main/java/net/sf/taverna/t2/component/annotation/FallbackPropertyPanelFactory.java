/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import javax.swing.JComponent;
import javax.swing.JLabel;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author alanrw
 *
 */
public class FallbackPropertyPanelFactory extends PropertyPanelFactorySPI {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.annotation.PropertyPanelFactorySPI#getInputComponent(net.sf.taverna.t2.component.profile.SemanticAnnotationProfile, com.hp.hpl.jena.rdf.model.Statement)
	 */
	@Override
	public JComponent getInputComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		return new JLabel("Unable to handle " + semanticAnnotationProfile.getPredicateString());
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.annotation.PropertyPanelFactorySPI#getNewTargetNode(javax.swing.JComponent)
	 */
	@Override
	public RDFNode getNewTargetNode(JComponent component) {
		return null;
	}

	@Override
	public int getRatingForSemanticAnnotation(
			SemanticAnnotationProfile semanticAnnotationProfile) {
		return 0;
	}


}
