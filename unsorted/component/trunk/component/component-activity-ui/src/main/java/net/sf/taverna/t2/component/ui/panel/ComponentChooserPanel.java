/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("unchecked")
public class ComponentChooserPanel extends JPanel implements Observable<ComponentChoiceMessage>, Observer{

	/**
	 *
	 */
	private static final long serialVersionUID = -4459660016225074302L;

	private static Logger logger = Logger.getLogger(ComponentChooserPanel.class);

	private List<Observer<ComponentChoiceMessage>> observers = new ArrayList<Observer<ComponentChoiceMessage>>();



	private final JComboBox componentChoice = new JComboBox();

	private DefaultComboBoxModel componentModel = new DefaultComboBoxModel();

	private RegistryAndFamilyChooserPanel registryAndFamilyChooserPanel = new RegistryAndFamilyChooserPanel();

	public ComponentChooserPanel() {
		super();
		this.setLayout(new GridBagLayout());

		componentChoice.setModel(componentModel);
		componentChoice.setRenderer(new ComponentListCellRenderer());

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
					notifyObservers();
				}

			}});
	}

	private void notifyObservers() {
		ComponentChoiceMessage message = new ComponentChoiceMessage(registryAndFamilyChooserPanel.getChosenFamily(), getChosenComponent());
		for (Observer<ComponentChoiceMessage> o : getObservers()) {
			try {
				o.notify(ComponentChooserPanel.this, message);
			} catch (Exception e) {
				logger.error(e);
			}
		}

	}

	private void updateComponentModel() {
		ComponentFamily chosenFamily = registryAndFamilyChooserPanel.getChosenFamily();
		componentModel.removeAllElements();
		try {
			if (chosenFamily != null) {
				for (Component component : chosenFamily.getComponents()) {
					componentModel.addElement(component);
				}
			}
			if (componentModel.getSize() > 0) {
				componentChoice.setSelectedIndex(0);
			} else {
				notifyObservers();
			}
			componentChoice.setEnabled(componentModel.getSize() != 0);
		} catch (ComponentRegistryException e) {
			logger.error("Unable to read components", e);
		} catch (NullPointerException e) {
			logger.error("Unable to read components", e);
		}
	}

	public Component getChosenComponent() {
		if (componentChoice.getSelectedIndex() >= 0) {
			return (Component) componentChoice.getSelectedItem();
		}
		return null;
	}

	@Override
	public void notify(Observable sender,
			Object message) throws Exception {
		if (message instanceof FamilyChoiceMessage) {
			updateComponentModel();
		} else if (message instanceof ProfileChoiceMessage) {
			registryAndFamilyChooserPanel.notify(null, (ProfileChoiceMessage) message);
		}
	}

	@Override
	public void addObserver(Observer<ComponentChoiceMessage> observer) {
		observers.add(observer);
		Component chosenComponent = getChosenComponent();
		if (chosenComponent != null) {
			ComponentChoiceMessage message = new ComponentChoiceMessage(registryAndFamilyChooserPanel.getChosenFamily(), chosenComponent);
			try {
				observer.notify(this, message);
			} catch (Exception e) {
				logger.error(e);
			}
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

	public ComponentRegistry getChosenRegistry() {
		return registryAndFamilyChooserPanel.getChosenRegistry();
	}

	public ComponentFamily getChosenFamily() {
		return registryAndFamilyChooserPanel.getChosenFamily();
	}

}
