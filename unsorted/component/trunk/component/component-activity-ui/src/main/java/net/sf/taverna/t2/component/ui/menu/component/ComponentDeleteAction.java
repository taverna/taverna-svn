/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.ComponentChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentDeleteAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2992743162132614936L;

	private static Logger logger = Logger.getLogger(ComponentDeleteAction.class);
		
	private static final String DELETE_COMPONENT = "Delete component...";
	
	private static FileManager fm = FileManager.getInstance();
	
	public ComponentDeleteAction() {
		super (DELETE_COMPONENT, ComponentServiceIcon.getIcon());
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		ComponentChooserPanel panel = new ComponentChooserPanel();
		int answer = JOptionPane.showConfirmDialog(null, panel, "Component choice", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
			Component chosenComponent = panel.getChosenComponent();
			ComponentRegistry chosenRegistry = panel.getChosenRegistry();
			ComponentFamily chosenFamily = panel.getChosenFamily();
			if (chosenComponent == null) {
				JOptionPane.showMessageDialog(null, "Unable to determine component", "Component Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (componentIsInUse(chosenRegistry, chosenFamily, chosenComponent)) {
				JOptionPane.showMessageDialog(null, "The component is open", "Component Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + chosenComponent.getName(), "Delete Component Confirmation", JOptionPane.YES_NO_OPTION);
			if (confirmation == JOptionPane.YES_OPTION) {
				try {
					chosenFamily.removeComponent(chosenComponent);
					ComponentServiceProviderConfig config = new ComponentServiceProviderConfig();
					config.setFamilyName(chosenFamily.getName());
					config.setRegistryBase(chosenRegistry.getRegistryBase());
					Utils.refreshComponentServiceProvider(config);
				} catch (ComponentRegistryException e) {
					JOptionPane.showMessageDialog(null, "Unable to delete " + chosenComponent.getName() + "\n" + e.getMessage(), "Component Deletion Error", JOptionPane.ERROR_MESSAGE);
					logger.error(e);
				} catch (ConfigurationException e) {
					logger.error(e);
				}
			}
		}
	}


	private static boolean componentIsInUse(ComponentRegistry chosenRegistry,
			ComponentFamily chosenFamily, Component chosenComponent) {
		for (Dataflow d : fm.getOpenDataflows()) {
			Object dataflowSource = fm.getDataflowSource(d);
			if (dataflowSource instanceof ComponentVersionIdentification) {
				ComponentVersionIdentification ident = (ComponentVersionIdentification) dataflowSource;
				if (ident.getRegistryBase().equals(chosenRegistry.getRegistryBase()) &&
						ident.getFamilyName().equals(chosenFamily.getName()) &&
						ident.getComponentName().equals(chosenComponent.getName())) {
					return true;
				}
			}
		}
		return false;
	}

}
