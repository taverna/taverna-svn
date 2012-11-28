/**
 *
 */
package net.sf.taverna.t2.component.ui.file;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentFamilyChooserPanel;
import net.sf.taverna.t2.component.ui.view.ComponentListCellRenderer;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentVersionChooserPanel extends ComponentFamilyChooserPanel {

	private static Logger logger = Logger.getLogger(ComponentVersionChooserPanel.class);

	private final JComboBox componentChoice = new JComboBox();
	
	private DefaultComboBoxModel componentModel = new DefaultComboBoxModel();

	private final JComboBox componentVersionChoice = new JComboBox();
	
	private DefaultComboBoxModel componentVersionModel = new DefaultComboBoxModel();

	public ComponentVersionChooserPanel() {
		super(false);
		
		componentChoice.setModel(componentModel);
		componentChoice.setRenderer(new ComponentListCellRenderer());
		
		componentVersionChoice.setModel(componentVersionModel);
		componentVersionChoice.setRenderer(new ComponentListCellRenderer());
		
		updateComponentModel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 2;
		this.add(new JLabel("Component name"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentChoice, gbc);
		componentChoice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateComponentVersionModel();
			}});
		
		super.addFamilyChoiceListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateComponentModel();
			}});
		
		updateComponentVersionModel();
		
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.gridy = 3;
		this.add(new JLabel("Component version"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentVersionChoice, gbc);
	}

	private void updateComponentModel() {
		ComponentFamily chosenFamily = super.getFamilyChoice();
		componentModel.removeAllElements();
		try {
			for (Component component : chosenFamily.getComponents()) {
				componentModel.addElement(component);
			}
			updateComponentVersionModel();
		} catch (ComponentRegistryException e) {
			logger.error("Unable to read components", e);
		} catch (NullPointerException e) {
			logger.error("Unable to read components", e);
		}
	}

	private void updateComponentVersionModel() {
		Component chosenComponent = getComponentChoice();
		componentVersionModel.removeAllElements();
		ComponentVersion lastVersion = null;
		try {
			for (ComponentVersion version : chosenComponent.getComponentVersionMap().values()) {
				componentVersionModel.addElement(version);
				lastVersion = version;
			}
		} catch (NullPointerException e) {
			logger.error("Unable to read component versions", e);
		}
		if (lastVersion != null) {
			componentVersionChoice.setSelectedItem(lastVersion);
		}
	}

	public Component getComponentChoice() {
		return (Component) componentChoice.getSelectedItem();
	}

	public ComponentVersion getComponentVersionChoice() {
		return (ComponentVersion) componentVersionChoice.getSelectedItem();
	}


}
