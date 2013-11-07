/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import javax.swing.JComponent;
import javax.swing.JLabel;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author alanrw
 * 
 */
public class FallbackPropertyPanelFactory extends PropertyPanelFactorySPI {
	@Override
	public JComponent getInputComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		return new JLabel("Unable to handle "
				+ semanticAnnotationProfile.getPredicateString());
	}

	@Override
	public RDFNode getNewTargetNode(Statement originalStatement,
			JComponent component) {
		return null;
	}

	@Override
	public int getRatingForSemanticAnnotation(
			SemanticAnnotationProfile semanticAnnotationProfile) {
		return 0;
	}

	@Override
	public JComponent getDisplayComponent(
			SemanticAnnotationProfile semanticAnnotationProfile,
			Statement statement) {
		return getDefaultDisplayComponent(semanticAnnotationProfile, statement);
	}
}
