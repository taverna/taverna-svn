/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static java.lang.String.format;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
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
	private static final long serialVersionUID = -2992743162132614936L;
	private static final Logger logger = Logger
			.getLogger(ComponentDeleteAction.class);
	private static final String DELETE_COMPONENT = "Delete component...";
	private static final FileManager fm = FileManager.getInstance();

	public ComponentDeleteAction() {
		super(DELETE_COMPONENT, ComponentServiceIcon.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		ComponentChooserPanel panel = new ComponentChooserPanel();
		int answer = showConfirmDialog(null, panel, "Component choice",
				OK_CANCEL_OPTION);
		if (answer == OK_OPTION) {
			doDelete(panel.getChosenComponent(), panel.getChosenRegistry(),
					panel.getChosenFamily());
		}
	}

	private void doDelete(Component chosenComponent, Registry chosenRegistry,
			Family chosenFamily) {
		if (chosenComponent == null) {
			showMessageDialog(null, "Unable to determine component",
					"Component Problem", ERROR_MESSAGE);
			return;
		}
		if (componentIsInUse(chosenRegistry, chosenFamily, chosenComponent)) {
			showMessageDialog(null, "The component is open",
					"Component Problem", ERROR_MESSAGE);
			return;
		}
		int confirmation = showConfirmDialog(null,
				"Are you sure you want to delete " + chosenComponent.getName(),
				"Delete Component Confirmation", YES_NO_OPTION);
		try {
			if (confirmation == YES_OPTION) {
				chosenFamily.removeComponent(chosenComponent);
				ComponentServiceProviderConfig config = new ComponentServiceProviderConfig();
				config.setFamilyName(chosenFamily.getName());
				config.setRegistryBase(chosenRegistry.getRegistryBase());
				Utils.refreshComponentServiceProvider(config);
			}
		} catch (RegistryException e) {
			showMessageDialog(
					null,
					format("Unable to delete %s\n%s",
							chosenComponent.getName(), e.getMessage()),
					"Component Deletion Error", ERROR_MESSAGE);
			logger.error(e);
		} catch (ConfigurationException e) {
			logger.error(e);
		}
	}

	private static boolean componentIsInUse(Registry chosenRegistry,
			Family chosenFamily, Component chosenComponent) {
		for (Dataflow d : fm.getOpenDataflows()) {
			Object dataflowSource = fm.getDataflowSource(d);
			if (dataflowSource instanceof Version.ID) {
				Version.ID ident = (Version.ID) dataflowSource;
				if (ident.getRegistryBase().equals(
						chosenRegistry.getRegistryBase())
						&& ident.getFamilyName().equals(chosenFamily.getName())
						&& ident.getComponentName().equals(
								chosenComponent.getName())) {
					return true;
				}
			}
		}
		return false;
	}

}
