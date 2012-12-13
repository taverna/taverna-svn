/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentVersion;
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
	
	private DefaultComboBoxModel componentVersionModel = new DefaultComboBoxModel();
	
	private ComponentChooserPanel componentChooserPanel = new ComponentChooserPanel();

	public ComponentVersionChooserPanel() {
		super();
		this.setLayout(new GridBagLayout());
		
		componentVersionChoice.setModel(componentVersionModel);
		componentVersionChoice.setRenderer(new ComponentListCellRenderer());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 5, 0, 5);
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
		this.add(new JLabel("Component version:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentVersionChoice, gbc);
	}

	private void updateComponentVersionModel() {
		Component chosenComponent = componentChooserPanel.getChosenComponent();
		componentVersionModel.removeAllElements();
		ComponentVersion lastVersion = null;
		try {
			if (chosenComponent != null) {
			for (ComponentVersion version : chosenComponent.getComponentVersionMap().values()) {
				componentVersionModel.addElement(version);
				lastVersion = version;
			}
			}
			componentVersionChoice.setEnabled(componentVersionModel.getSize() > 0);
		} catch (NullPointerException e) {
			logger.error("Unable to read component versions", e);
		}
		if (lastVersion != null) {
			componentVersionChoice.setSelectedItem(lastVersion);
		}
	}

	public ComponentVersion getChosenComponentVersion() {
		if (componentVersionChoice.getSelectedIndex() >= 0) {
			return (ComponentVersion) componentVersionChoice.getSelectedItem();
		}
		return null;
	}

	@Override
	public void notify(Observable<ComponentChoiceMessage> sender,
			ComponentChoiceMessage message) throws Exception {
		updateComponentVersionModel();
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
