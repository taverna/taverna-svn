/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.family;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.ui.panel.FamilyChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
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
public class ComponentFamilyDeleteAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4976161883778371344L;

	private static Logger logger = Logger
			.getLogger(ComponentFamilyDeleteAction.class);

	private static final String DELETE_FAMILY = "Delete family...";

	private static FileManager fm = FileManager.getInstance();

	public ComponentFamilyDeleteAction() {
		super(DELETE_FAMILY, ComponentServiceIcon.getIcon());
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
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		overallPanel.add(registryPanel, gbc);

		FamilyChooserPanel familyPanel = new FamilyChooserPanel();
		registryPanel.addObserver(familyPanel);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = 1;
		overallPanel.add(familyPanel, gbc);

		int answer = showConfirmDialog(null, overallPanel,
				"Delete Component Family", OK_CANCEL_OPTION);
		if (answer == OK_OPTION) {
			doDelete(registryPanel.getChosenRegistry(),
					familyPanel.getChosenFamily());
		}
	}

	private void doDelete(Registry chosenRegistry, Family chosenFamily) {
		if (chosenRegistry == null) {
			showMessageDialog(null, "Unable to determine registry",
					"Component Registry Problem", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (chosenFamily == null) {
			showMessageDialog(null, "Unable to determine family",
					"Component Family Problem", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (familyIsInUse(chosenRegistry, chosenFamily)) {
			showMessageDialog(null, "Components in the family are open",
					"Component Family Problem", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int confirmation = showConfirmDialog(null,
				"Are you sure you want to delete " + chosenFamily.getName(),
				"Delete Component Family Confirmation", YES_NO_OPTION);
		try {
			if (confirmation == YES_OPTION) {
				chosenRegistry.removeComponentFamily(chosenFamily);
				ComponentServiceProviderConfig config = new ComponentServiceProviderConfig();
				config.setFamilyName(chosenFamily.getName());
				config.setRegistryBase(chosenRegistry.getRegistryBase());
				Utils.removeComponentServiceProvider(config);
			}
		} catch (RegistryException e) {
			showMessageDialog(null,
					"Unable to delete " + chosenFamily.getName(),
					"Component Family Deletion Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error(e);
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
				if (ident.getRegistryBase().equals(
						chosenRegistry.getRegistryBase())
						&& ident.getFamilyName().equals(chosenFamily.getName())) {
					return true;
				}
			}
		}
		return false;
	}

}
