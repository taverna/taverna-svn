/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentVersionChooserPanel extends JPanel implements Observer<ComponentChoiceMessage>{

	/**
	 *
	 */
	private static final long serialVersionUID = 5125907010496468219L;

	private static Logger logger = Logger.getLogger(ComponentVersionChooserPanel.class);

	private final JComboBox componentVersionChoice = new JComboBox();

	private SortedMap<Integer, ComponentVersion> componentVersionMap =
		new TreeMap<Integer, ComponentVersion>();

	private ComponentChooserPanel componentChooserPanel = new ComponentChooserPanel();

	public ComponentVersionChooserPanel() {
		super();
		this.setLayout(new GridBagLayout());

		componentVersionChoice.setRenderer(new ComponentListCellRenderer());
		componentVersionChoice.setPrototypeDisplayValue(Utils.SHORT_STRING);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		this.add(componentChooserPanel, gbc);
		componentChooserPanel.addObserver(this);

		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Component version:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentVersionChoice, gbc);
	}

	private void updateComponentVersionModel() {
		Component chosenComponent = componentChooserPanel.getChosenComponent();
		componentVersionMap.clear();
		componentVersionChoice.removeAllItems();
		ComponentVersion lastVersion = null;
		try {
			if (chosenComponent != null) {
			for (ComponentVersion version : chosenComponent.getComponentVersionMap().values()) {
				componentVersionMap.put(version.getVersionNumber(), version);
				componentVersionChoice.addItem(version);
				lastVersion = version;
			}
			}
			componentVersionChoice.setEnabled(!componentVersionMap.isEmpty());
			if (componentVersionMap.isEmpty()) {
				componentVersionChoice.addItem("No versions available");
			}
		} catch (NullPointerException e) {
			logger.error("Unable to read component versions", e);
		}
		if (lastVersion != null) {
			componentVersionChoice.setSelectedItem(lastVersion);
		}
	}

	public ComponentVersion getChosenComponentVersion() {
		if (!componentVersionMap.isEmpty()) {
			return (ComponentVersion) componentVersionChoice.getSelectedItem();
		}
		return null;
	}

	@Override
	public void notify(Observable<ComponentChoiceMessage> sender,
			ComponentChoiceMessage message) throws Exception {
		try {
		updateComponentVersionModel();
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	public ComponentRegistry getChosenRegistry() {
		return componentChooserPanel.getChosenRegistry();
	}

	public ComponentFamily getChosenFamily() {
		return componentChooserPanel.getChosenFamily();
	}

	public Component getChosenComponent() {
		return componentChooserPanel.getChosenComponent();
	}


}
