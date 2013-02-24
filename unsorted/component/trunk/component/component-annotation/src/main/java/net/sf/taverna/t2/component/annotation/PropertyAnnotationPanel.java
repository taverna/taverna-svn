/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import javax.swing.JPanel;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author alson
 *
 */
public abstract class PropertyAnnotationPanel extends JPanel {
	
	abstract RDFNode getNewTargetNode();

}
