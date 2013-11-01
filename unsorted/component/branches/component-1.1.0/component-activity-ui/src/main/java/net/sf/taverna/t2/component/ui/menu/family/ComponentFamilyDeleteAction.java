/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.family;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.WEST;
import static java.lang.String.format;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static net.sf.taverna.t2.component.ui.util.Utils.removeComponentServiceProvider;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.ui.panel.FamilyChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentFamilyDeleteAction extends AbstractAction {
	private static final String CONFIRM_MSG = "Are you sure you want to delete %s";
	private static final String CONFIRM_TITLE = "Delete Component Family Confirmation";
	private static final String DELETE_FAMILY_LABEL = "Delete family...";
	private static final String ERROR_TITLE = "Component Family Deletion Error";
	private static final String FAILED_MSG = "Unable to delete %s: %s";
	private static final String FAMILY_FAIL_TITLE = "Component Family Problem";
	private static final String OPEN_MSG = "Components in the family are open";
	private static final String PICK_FAMILY_TITLE = "Delete Component Family";
	private static final String REGISTRY_FAIL_TITLE = "Component Registry Problem";
	private static final String WHAT_FAMILY_MSG = "Unable to determine family";
	private static final String WHAT_REGISTRY_MSG = "Unable to determine registry";
	private static final FileManager fm = FileManager.getInstance();
	private static final Logger logger = getLogger(ComponentFamilyDeleteAction.class);
	private static final long serialVersionUID = -4976161883778371344L;

	public ComponentFamilyDeleteAction() {
		super(DELETE_FAMILY_LABEL, getIcon());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		RegistryChooserPanel registryPanel = new RegistryChooserPanel();

		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		overallPanel.add(registryPanel, gbc);

		FamilyChooserPanel familyPanel = new FamilyChooserPanel();
		registryPanel.addObserver(familyPanel);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = 1;
		overallPanel.add(familyPanel, gbc);

		int answer = showConfirmDialog(null, overallPanel, PICK_FAMILY_TITLE,
				OK_CANCEL_OPTION);
		if (answer == OK_OPTION)
			doDelete(registryPanel.getChosenRegistry(),
					familyPanel.getChosenFamily());
	}

	private void doDelete(Registry chosenRegistry, Family chosenFamily) {
		if (chosenRegistry == null) {
			showMessageDialog(null, WHAT_REGISTRY_MSG, REGISTRY_FAIL_TITLE,
					ERROR_MESSAGE);
			return;
		} else if (chosenFamily == null) {
			showMessageDialog(null, WHAT_FAMILY_MSG, FAMILY_FAIL_TITLE,
					ERROR_MESSAGE);
			return;
		} else if (familyIsInUse(chosenRegistry, chosenFamily)) {
			showMessageDialog(null, OPEN_MSG, FAMILY_FAIL_TITLE, ERROR_MESSAGE);
			return;
		}

		if (showConfirmDialog(null,
				format(CONFIRM_MSG, chosenFamily.getName()), CONFIRM_TITLE,
				YES_NO_OPTION) != YES_OPTION)
			return;

		try {
			chosenRegistry.removeComponentFamily(chosenFamily);
			ComponentServiceProviderConfig config = new ComponentServiceProviderConfig();
			config.setFamilyName(chosenFamily.getName());
			config.setRegistryBase(chosenRegistry.getRegistryBase());
			removeComponentServiceProvider(config);
		} catch (RegistryException e) {
			logger.error(e);
			showMessageDialog(null,
					format(FAILED_MSG, chosenFamily.getName(), e.getMessage()),
					ERROR_TITLE, ERROR_MESSAGE);
		} catch (ConfigurationException e) {
			logger.error(e);
		}
	}

	private static boolean familyIsInUse(Registry chosenRegistry,
			Family chosenFamily) {
		for (Dataflow d : fm.getOpenDataflows()) {
			Object dataflowSource = fm.getDataflowSource(d);
			if (dataflowSource instanceof Version.ID) {
				Version.ID ident = (Version.ID) dataflowSource;
				if (ident.getRegistryBase().toString()
						.equals(chosenRegistry.getRegistryBase().toString())
						&& ident.getFamilyName().equals(chosenFamily.getName()))
					return true;
			}
		}
		return false;
	}

}
