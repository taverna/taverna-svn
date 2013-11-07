package net.sf.taverna.t2.component.ui.config;

import static java.awt.event.ItemEvent.SELECTED;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateComponent;
import static net.sf.taverna.t2.component.ui.util.Utils.SHORT_STRING;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.SortedMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.ComponentListCellRenderer;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ComponentConfigurationPanel
		extends
		ActivityConfigurationPanel<ComponentActivity, ComponentActivityConfigurationBean> {
	private static Logger logger = getLogger(ComponentConfigurationPanel.class);

	private ComponentActivity activity;
	private ComponentActivityConfigurationBean configBean;

	private final JComboBox componentVersionChoice = new JComboBox();

	public ComponentConfigurationPanel(ComponentActivity activity) {
		this.activity = activity;
		componentVersionChoice.setPrototypeDisplayValue(SHORT_STRING);
		configBean = activity.getConfiguration();
		initGui();
	}

	protected void initGui() {
		removeAll();
		setLayout(new GridLayout(0, 2));

		componentVersionChoice.setRenderer(new ComponentListCellRenderer());
		componentVersionChoice.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED)
					updateToolTipText();
			}
		});
		updateComponentVersionChoice();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 2;
		this.add(new JLabel("Component version:"), gbc);
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
		Integer version = ((Version) componentVersionChoice.getSelectedItem())
				.getVersionNumber();
		return !version.equals(configBean.getComponentVersion());
	}

	/**
	 * Prepare a new configuration bean from the UI, to be returned with
	 * getConfiguration()
	 */
	@Override
	public void noteConfiguration() {
		ComponentVersionIdentification newIdent = new ComponentVersionIdentification(
				configBean);
		newIdent.setComponentVersion(((Version) componentVersionChoice
				.getSelectedItem()).getVersionNumber());
		configBean = new ComponentActivityConfigurationBean(newIdent);
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
		Component component;
		componentVersionChoice.removeAllItems();
		componentVersionChoice.setToolTipText(null);
		try {
			component = calculateComponent(configBean);
		} catch (RegistryException e) {
			logger.error("failed to get component", e);
			return;
		}
		SortedMap<Integer, Version> componentVersionMap = component
				.getComponentVersionMap();
		for (Version v : componentVersionMap.values()) {
			componentVersionChoice.addItem(v);
		}
		componentVersionChoice.setSelectedItem(componentVersionMap
				.get(configBean.getComponentVersion()));
		updateToolTipText();
	}

	private void updateToolTipText() {
		Version selectedVersion = (Version) componentVersionChoice
				.getSelectedItem();
		componentVersionChoice.setToolTipText(selectedVersion.getDescription());
	}
}
