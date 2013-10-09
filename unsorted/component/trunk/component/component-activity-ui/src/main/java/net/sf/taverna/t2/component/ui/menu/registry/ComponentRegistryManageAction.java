/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.registry;

import static net.sf.taverna.t2.workbench.ui.impl.configuration.ui.T2ConfigurationFrame.showConfiguration;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.ui.preference.ComponentPreferenceUIFactory;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;

/**
 * @author alanrw
 * 
 */
public class ComponentRegistryManageAction extends AbstractAction {
	private static final long serialVersionUID = 8993945811345164194L;
	private static final String MANAGE_REGISTRY = "Manage registries...";

	public ComponentRegistryManageAction() {
		super(MANAGE_REGISTRY, ComponentServiceIcon.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		showConfiguration(ComponentPreferenceUIFactory.DISPLAY_NAME);
	}

}
