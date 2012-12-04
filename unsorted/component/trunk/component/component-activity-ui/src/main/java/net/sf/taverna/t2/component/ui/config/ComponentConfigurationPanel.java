package net.sf.taverna.t2.component.ui.config;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.ui.view.ComponentListCellRenderer;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.apache.log4j.Logger;


@SuppressWarnings("serial")
public class ComponentConfigurationPanel
		extends
		ActivityConfigurationPanel<ComponentActivity, 
        ComponentActivityConfigurationBean> {
	
	private static Logger logger = Logger.getLogger(ComponentConfigurationPanel.class);

	private ComponentActivity activity;
	private ComponentActivityConfigurationBean configBean;
	
	private final JComboBox componentVersionChoice = new JComboBox();
	
	private DefaultComboBoxModel componentVersionModel = new DefaultComboBoxModel();

	public ComponentConfigurationPanel(ComponentActivity activity) {
		this.activity = activity;
		configBean = activity.getConfiguration();
		initGui();
	}

	protected void initGui() {
		removeAll();
		setLayout(new GridLayout(0, 2));

		componentVersionChoice.setModel(componentVersionModel);
		componentVersionChoice.setRenderer(new ComponentListCellRenderer());
		updateComponentVersionChoice();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 2;
		this.add(new JLabel("Component version"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentVersionChoice, gbc);

		// Populate fields from activity configuration bean
		refreshConfiguration();
	}

	/**
	 * Check that user values in UI are valid
	 */
	@Override
	public boolean checkValues() {
		return true;
	}

	/**
	 * Return configuration bean generated from user interface last time
	 * noteConfiguration() was called.
	 */
	@Override
	public ComponentActivityConfigurationBean getConfiguration() {
		// Should already have been made by noteConfiguration()
		return configBean;
	}

	/**
	 * Check if the user has changed the configuration from the original
	 */
	@Override
	public boolean isConfigurationChanged() {
		Integer version = (Integer) componentVersionChoice.getSelectedItem();
		return (!version.equals(configBean.getComponentVersion()));
	}

	/**
	 * Prepare a new configuration bean from the UI, to be returned with
	 * getConfiguration()
	 */
	@Override
	public void noteConfiguration() {
		ComponentActivityConfigurationBean newConfig = new ComponentActivityConfigurationBean(configBean.getRegistryBase(), configBean.getFamilyName(), configBean.getComponentName(), (Integer) componentVersionChoice.getSelectedItem());
		configBean = newConfig;
	}

	/**
	 * Update GUI from a changed configuration bean (perhaps by undo/redo).
	 * 
	 */
	@Override
	public void refreshConfiguration() {
		configBean = activity.getConfiguration();
		
		updateComponentVersionChoice();
	}

	private void updateComponentVersionChoice() {
		componentVersionModel.removeAllElements();
		Component component;
		try {
			component = configBean.calculateComponent();
		} catch (ComponentRegistryException e) {
			logger.error(e);
			return;
		}
		for (Integer i : component.getComponentVersionMap().keySet()) {
			componentVersionModel.addElement(i);
		}
		componentVersionChoice.setSelectedItem(configBean.getComponentVersion());
	}
	
	
}
