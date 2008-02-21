/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sf.taverna.t2.renderers;


import java.awt.Component;

import javax.swing.JComponent;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Class that renders objects which have a Java Component
 * subclass as their user object
 * @author Tom Oinn
 */
public class AWTComponentRenderer implements Renderer{
    
    private final String name;

	public AWTComponentRenderer(String name) {
		this.name = name;
    }
    
    public boolean isTerminal() {
	return true;
    }
    
    public boolean canHandle(RendererRegistry renderers,
			     Class dataClass) {
	Class componentClass = Component.class;
	return componentClass.isAssignableFrom(dataClass);
    }

	public boolean canHandle(String mimeType) {
		// TODO Auto-generated method stub
		return false;
	}

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) {
//		JPanel itemPanel = new JPanel(new BorderLayout());
//		Component c = (Component)dataThing.getDataObject();
//		itemPanel.add(c, BorderLayout.CENTER);
//		return itemPanel;
//		// TODO Auto-generated method stub
		return null;
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getType() {
		return "AWT Component";
	}


}
