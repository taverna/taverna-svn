/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.preference.ComponentPreferenceUIFactory;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.workbench.ui.impl.configuration.ui.T2ConfigurationFrame;

/**
 * @author alanrw
 *
 */
public class ComponentRegistryManageAction extends AbstractAction {
	
	private static final String MANAGE_REGISTRY = "Manage registries...";
	
	public ComponentRegistryManageAction() {
		super (MANAGE_REGISTRY, ComponentServiceIcon.getIcon());
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		T2ConfigurationFrame.showConfiguration(ComponentPreferenceUIFactory.DISPLAY_NAME);
	}

}
