/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class RegistryChooserPanel extends JPanel implements Observable<RegistryChoiceMessage> {

	/**
	 *
	 */
	private static final long serialVersionUID = 8390860727800654604L;

	private static Logger logger = Logger.getLogger(RegistryChooserPanel.class);

	private List<Observer<RegistryChoiceMessage>> observers = new ArrayList<Observer<RegistryChoiceMessage>>();

	private JComboBox registryBox = new JComboBox();

	private ComponentPreference pref = ComponentPreference.getInstance();

	public RegistryChooserPanel() {
		super();
		registryBox.setPrototypeDisplayValue(Utils.LONG_STRING);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		String longestKey = "";
		final SortedMap<String, ComponentRegistry> registryMap = pref.getRegistryMap();
		for (String registryName : registryMap.keySet()) {
			registryBox.addItem(registryName);
			if (registryName.length() > longestKey.length()) {
				longestKey = registryName;
			}
		}
		registryBox.setPrototypeDisplayValue(longestKey);

		registryBox.setEditable(false);

		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		this.add(new JLabel("Component registry:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		this.add(registryBox, gbc);

		registryBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					dealWithSelection();
				}
			}
		});

		registryBox.setSelectedItem(registryMap.firstKey());
		dealWithSelection();
	}

	private void dealWithSelection() {
		ComponentRegistry chosenRegistry = getChosenRegistry();

		RegistryChoiceMessage message = new RegistryChoiceMessage(chosenRegistry);
		for (Observer<RegistryChoiceMessage> o : getObservers()) {
			try {
				o.notify(RegistryChooserPanel.this, message);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	@Override
	public void addObserver(Observer<RegistryChoiceMessage> observer) {
		observers.add(observer);
		ComponentRegistry chosenRegistry = getChosenRegistry();
		if (chosenRegistry != null) {
			RegistryChoiceMessage message = new RegistryChoiceMessage(chosenRegistry);
			try {
				observer.notify(this, message);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	@Override
	public List<Observer<RegistryChoiceMessage>> getObservers() {
		return observers;
	}

	@Override
	public void removeObserver(Observer<RegistryChoiceMessage> observer) {
		observers.remove(observer);
	}

	public ComponentRegistry getChosenRegistry() {
		if (registryBox.getSelectedIndex() >= 0) {
			String name = (String) registryBox.getSelectedItem();
			ComponentRegistry chosenRegistry = pref.getRegistryMap().get(name);
			return chosenRegistry;
		}
		return null;
	}

}
