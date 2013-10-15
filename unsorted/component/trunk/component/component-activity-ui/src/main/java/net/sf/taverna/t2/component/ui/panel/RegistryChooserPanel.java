/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import static java.awt.event.ItemEvent.SELECTED;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class RegistryChooserPanel extends JPanel implements
		Observable<RegistryChoiceMessage> {
	private static final long serialVersionUID = 8390860727800654604L;
	private static final Logger logger = Logger
			.getLogger(RegistryChooserPanel.class);

	private List<Observer<RegistryChoiceMessage>> observers = new ArrayList<Observer<RegistryChoiceMessage>>();

	private final JComboBox registryBox;

	Map<String, String> toolTipMap = new HashMap<String, String>();

	private ComponentPreference pref = ComponentPreference.getInstance();

	final SortedMap<String, Registry> registryMap;

	public RegistryChooserPanel() {
		super();
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		registryMap = pref.getRegistryMap();
		registryBox = new JComboBox(registryMap.keySet().toArray());
		registryBox.setPrototypeDisplayValue(Utils.LONG_STRING);

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
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED)
					dealWithSelection();
			}
		});

		String firstKey = registryMap.firstKey();
		registryBox.setSelectedItem(firstKey);
		dealWithSelection();
	}

	private void updateToolTipText() {
		String key = (String) registryBox.getSelectedItem();
		Registry registry = registryMap.get(key);
		registryBox.setToolTipText(registry.getRegistryBase().toString());
	}

	private void dealWithSelection() {
		updateToolTipText();
		Registry chosenRegistry = getChosenRegistry();
		RegistryChoiceMessage message = new RegistryChoiceMessage(
				chosenRegistry);
		for (Observer<RegistryChoiceMessage> o : getObservers())
			try {
				o.notify(RegistryChooserPanel.this, message);
			} catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public void addObserver(Observer<RegistryChoiceMessage> observer) {
		observers.add(observer);
		Registry chosenRegistry = getChosenRegistry();
		RegistryChoiceMessage message = new RegistryChoiceMessage(
				chosenRegistry);
		try {
			observer.notify(this, message);
		} catch (Exception e) {
			logger.error(e);
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

	public Registry getChosenRegistry() {
		if (registryBox.getSelectedIndex() < 0)
			return null;
		return registryMap.get(registryBox.getSelectedItem());
	}
}
