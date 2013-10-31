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
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static net.sf.taverna.t2.component.ui.util.Utils.refreshComponentServiceProvider;
import static org.apache.log4j.Logger.getLogger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.ui.panel.ComponentChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentDeleteAction extends AbstractAction {
	private static final String COMPONENT_PROBLEM_TITLE = "Component Problem";
	private static final String CONFIRM_MSG = "Are you sure you want to delete %s?";
	private static final String CONFIRM_TITLE = "Delete Component Confirmation";
	private static final String DELETE_COMPONENT_LABEL = "Delete component...";
	private static final String DELETE_FAILED_TITLE = "Component Deletion Error";
	private static final String FAILED_MSG = "Unable to delete %s\n%s";
	private static final String OPEN_COMPONENT_MSG = "The component is open";
	private static final String TITLE = "Component choice";
	private static final String WHAT_COMPONENT_MSG = "Unable to determine component";
	private static final long serialVersionUID = -2992743162132614936L;
	private static final Logger logger = getLogger(ComponentDeleteAction.class);
	private static final FileManager fm = FileManager.getInstance();

	public ComponentDeleteAction() {
		super(DELETE_COMPONENT_LABEL, getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		ComponentChooserPanel panel = new ComponentChooserPanel();
		int answer = showConfirmDialog(null, panel, TITLE, OK_CANCEL_OPTION);
		if (answer == OK_OPTION)
			doDelete(panel.getChosenComponent(), panel.getChosenRegistry(),
					panel.getChosenFamily());
	}

	private void doDelete(Component chosenComponent, Registry chosenRegistry,
			Family chosenFamily) {
		if (chosenComponent == null) {
			showMessageDialog(null, WHAT_COMPONENT_MSG,
					COMPONENT_PROBLEM_TITLE, ERROR_MESSAGE);
			return;
		}
		if (componentIsInUse(chosenRegistry, chosenFamily, chosenComponent)) {
			showMessageDialog(null, OPEN_COMPONENT_MSG,
					COMPONENT_PROBLEM_TITLE, ERROR_MESSAGE);
			return;
		}
		int confirmation = showConfirmDialog(null,
				format(CONFIRM_MSG, chosenComponent.getName()), CONFIRM_TITLE,
				YES_NO_OPTION);
		try {
			if (confirmation == YES_OPTION) {
				chosenFamily.removeComponent(chosenComponent);
				ComponentServiceProviderConfig config = new ComponentServiceProviderConfig();
				config.setFamilyName(chosenFamily.getName());
				config.setRegistryBase(chosenRegistry.getRegistryBase());
				refreshComponentServiceProvider(config);
			}
		} catch (RegistryException e) {
			showMessageDialog(
					null,
					format(FAILED_MSG, chosenComponent.getName(),
							e.getMessage()), DELETE_FAILED_TITLE, ERROR_MESSAGE);
			logger.error(e);
		} catch (ConfigurationException e) {
			logger.error(e);
		}
	}

	private static boolean componentIsInUse(Registry chosenRegistry,
			Family chosenFamily, Component chosenComponent) {
		for (Dataflow d : fm.getOpenDataflows()) {
			Object dataflowSource = fm.getDataflowSource(d);
			if (dataflowSource instanceof Version.ID
					&& ((Version.ID) dataflowSource)
							.mostlyEqualTo(chosenComponent))
				return true;
		}
		return false;
	}

}
