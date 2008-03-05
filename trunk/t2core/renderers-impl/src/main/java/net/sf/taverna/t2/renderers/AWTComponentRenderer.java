/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sf.taverna.t2.renderers;


import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Class that renders objects which have a Java Component
 * subclass as their user object
 * @author Tom Oinn
 * @author Ian Dunlop
 */
public class AWTComponentRenderer implements Renderer{
    

	public AWTComponentRenderer() {
    }

	public boolean canHandle(String mimeType) {
		return true;
	}

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) throws RendererException {
		JPanel itemPanel = new JPanel(new BorderLayout());
		Component c = null;
		try {
			c = (Component)dataFacade.resolve(entityIdentifier, Component.class);
		} catch (RetrievalException e) {
			throw new RendererException(
					"Could not resolve " + entityIdentifier, e);
		} catch (NotFoundException e) {
			throw new RendererException("Data Manager Could not find "
					+ entityIdentifier, e);
		}
		itemPanel.add(c, BorderLayout.CENTER);
		return itemPanel;
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType) throws RendererException{
		Object resolve = null;
		try {
			resolve = facade.resolve(entityIdentifier, Component.class);
		} catch (RetrievalException e) {
			throw new RendererException(
					"Could not resolve " + entityIdentifier, e);
		} catch (NotFoundException e) {
			throw new RendererException("Data Manager Could not find "
					+ entityIdentifier, e);
		}
		if (resolve instanceof Component) {
			return true;
		}

		return false;
	}

	public String getType() {
		return "AWT Component";
	}


}
