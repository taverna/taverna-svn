/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.renderers;


import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * Class that renders objects which have a Java Component
 * subclass as their user object
 * @author Tom Oinn
 */
public class AWTComponentRenderer extends AbstractRenderer.ByJavaClass {
    
    public AWTComponentRenderer() {
	super("Java AWT Component");
    }
    
    public boolean isTerminal() {
	return true;
    }
    
    public boolean canHandle(RendererRegistry renderers,
			     Object userObject,
			     Class dataClass) {
	Class componentClass = Component.class;
	return componentClass.isAssignableFrom(dataClass);
    }
    
    public JComponent getComponent(RendererRegistry renderers,
				   DataThing dataThing)
	throws RendererException {
	JPanel itemPanel = new JPanel(new BorderLayout());
	Component c = (Component)dataThing.getDataObject();
	itemPanel.add(c, BorderLayout.CENTER);
	return itemPanel;
    }


}
