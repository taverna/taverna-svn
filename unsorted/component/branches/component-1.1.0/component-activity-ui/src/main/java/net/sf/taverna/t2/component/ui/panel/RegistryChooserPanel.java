/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.event.ItemEvent.SELECTED;
import static net.sf.taverna.t2.component.ui.util.Utils.LONG_STRING;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class RegistryChooserPanel extends JPanel implements
		Observable<RegistryChoiceMessage> {
	private static final String REGISTRY_LABEL = "Component registry:";
	private static final long serialVersionUID = 8390860727800654604L;
	private static final Logger logger = getLogger(RegistryChooserPanel.class);

	private final List<Observer<RegistryChoiceMessage>> observers = new ArrayList<Observer<RegistryChoiceMessage>>();
	private final JComboBox registryBox;
	private final ComponentPreference pref = ComponentPreference.getInstance();
	private final SortedMap<String, Registry> registryMap;

	public RegistryChooserPanel() {
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		registryMap = pref.getRegistryMap();
		registryBox = new JComboBox(registryMap.keySet().toArray());
		registryBox.setPrototypeDisplayValue(LONG_STRING);

		registryBox.setEditable(false);

		gbc.gridx = 0;
		gbc.anchor = WEST;
		this.add(new JLabel(REGISTRY_LABEL), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = BOTH;
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
				o.notify(this, message);
			} catch (Exception e) {
				logger.error("problem handling selection update", e);
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
			logger.error("problem handling addition of observer", e);
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
