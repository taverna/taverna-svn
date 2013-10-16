/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComponentChooserPanel extends JPanel implements
		Observable<ComponentChoiceMessage>, Observer {
	private static final long serialVersionUID = -4459660016225074302L;

	private static Logger logger = Logger
			.getLogger(ComponentChooserPanel.class);

	private List<Observer<ComponentChoiceMessage>> observers = new ArrayList<Observer<ComponentChoiceMessage>>();

	private final JComboBox componentChoice = new JComboBox();

	private SortedMap<String, Component> componentMap = new TreeMap<String, Component>();

	private RegistryAndFamilyChooserPanel registryAndFamilyChooserPanel = new RegistryAndFamilyChooserPanel();

	public ComponentChooserPanel() {
		super();
		this.setLayout(new GridBagLayout());

		componentChoice.setPrototypeDisplayValue(Utils.LONG_STRING);

		updateComponentModel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		this.add(registryAndFamilyChooserPanel, gbc);

		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		this.add(new JLabel("Component name:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentChoice, gbc);
		registryAndFamilyChooserPanel.addObserver(this);

		componentChoice.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					updateToolTipText();
					notifyObservers();
				}
			}
		});
	}

	protected void updateToolTipText() {
		Component chosenComponent = componentMap.get(componentChoice
				.getSelectedItem());
		if (chosenComponent == null)
			componentChoice.setToolTipText(null);
		else
			componentChoice.setToolTipText(chosenComponent.getDescription());
	}

	private void notifyObservers() {
		ComponentChoiceMessage message = new ComponentChoiceMessage(
				registryAndFamilyChooserPanel.getChosenFamily(),
				getChosenComponent());
		for (Observer<ComponentChoiceMessage> o : getObservers())
			try {
				o.notify(ComponentChooserPanel.this, message);
			} catch (Exception e) {
				logger.error(e);
			}
	}

	private void updateComponentModel() {
		componentMap.clear();
		componentChoice.removeAllItems();
		componentChoice.setToolTipText(null);
		notifyObservers();
		componentChoice.addItem("Reading components");
		componentChoice.setEnabled(false);
		new ComponentUpdater().execute();
	}

	public Component getChosenComponent() {
		if (componentMap.isEmpty())
			return null;
		return componentMap.get(componentChoice.getSelectedItem());
	}

	@Override
	public void notify(Observable sender, Object message) throws Exception {
		try {
			if (message instanceof FamilyChoiceMessage)
				updateComponentModel();
			else if (message instanceof ProfileChoiceMessage)
				registryAndFamilyChooserPanel.notify(null,
						(ProfileChoiceMessage) message);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public void addObserver(Observer<ComponentChoiceMessage> observer) {
		observers.add(observer);
		Component chosenComponent = getChosenComponent();
		ComponentChoiceMessage message = new ComponentChoiceMessage(
				registryAndFamilyChooserPanel.getChosenFamily(),
				chosenComponent);
		try {
			observer.notify(this, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public List<Observer<ComponentChoiceMessage>> getObservers() {
		return observers;
	}

	@Override
	public void removeObserver(Observer<ComponentChoiceMessage> observer) {
		observers.remove(observer);
	}

	public Registry getChosenRegistry() {
		return registryAndFamilyChooserPanel.getChosenRegistry();
	}

	public Family getChosenFamily() {
		return registryAndFamilyChooserPanel.getChosenFamily();
	}

	private class ComponentUpdater extends SwingWorker<String, Object> {
		@Override
		protected String doInBackground() throws Exception {
			Family chosenFamily = registryAndFamilyChooserPanel
					.getChosenFamily();
			if (chosenFamily != null)
				for (Component component : chosenFamily.getComponents())
					componentMap.put(component.getName(), component);

			return null;
		}

		@Override
		protected void done() {
			componentChoice.removeAllItems();
			for (String componentName : componentMap.keySet())
				componentChoice.addItem(componentName);
			if (!componentMap.isEmpty()) {
				componentChoice.setSelectedItem(componentMap.firstKey());
				updateToolTipText();
			} else
				componentChoice.addItem("No components available");

			notifyObservers();
			componentChoice.setEnabled(!componentMap.isEmpty());
		}

	}

}
