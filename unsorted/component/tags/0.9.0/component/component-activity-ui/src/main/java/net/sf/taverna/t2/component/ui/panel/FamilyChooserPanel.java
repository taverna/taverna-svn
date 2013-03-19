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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("unchecked")
public class FamilyChooserPanel extends JPanel implements Observer, Observable<FamilyChoiceMessage> {

	/**
	 *
	 */
	private static final long serialVersionUID = -2608831126562927778L;

	private static Logger logger = Logger.getLogger(FamilyChooserPanel.class);

	private List<Observer<FamilyChoiceMessage>> observers = new ArrayList<Observer<FamilyChoiceMessage>>();



	private JComboBox familyBox = new JComboBox();

//	private JTextArea familyDescription = new JTextArea(10,60);

	private SortedMap<String, ComponentFamily> familyMap = new TreeMap<String, ComponentFamily>();

	private ComponentRegistry chosenRegistry = null;

	private ComponentProfile profileFilter = null;

	public FamilyChooserPanel() {
		super();
		familyBox.setPrototypeDisplayValue(Utils.LONG_STRING);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Component family:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		this.add(familyBox, gbc);
		familyBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					updateDescription();
					notifyObservers();
				}
			}});

		familyBox.setEditable(false);

	}

	protected void updateDescription() {
		ComponentFamily chosenFamily = getChosenFamily();
		if (chosenFamily != null) {
			familyBox.setToolTipText(chosenFamily.getDescription());
		} else {
			familyBox.setToolTipText(null);
		}
	}

	@Override
	public void notify(Observable sender,
			Object message) throws Exception {
		try {
			if (message instanceof RegistryChoiceMessage) {
				this.chosenRegistry = ((RegistryChoiceMessage) message)
						.getChosenRegistry();
			} else if (message instanceof ProfileChoiceMessage) {
				this.profileFilter = ((ProfileChoiceMessage) message)
						.getChosenProfile();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			this.updateList();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private void updateList() {
		try {
		familyMap.clear();
		familyBox.removeAllItems();
		familyBox.setToolTipText(null);
		try {
			if (chosenRegistry != null ) {
				for (ComponentFamily f : chosenRegistry.getComponentFamilies()) {
					ComponentProfile componentProfile = f.getComponentProfile();
					if (componentProfile != null) {
					String id = componentProfile.getId();
					if ((profileFilter == null) || id.equals(profileFilter.getId())) {
						familyMap.put(f.getName(), f);
					}
					}
				}
			}
			for (String name : familyMap.keySet()) {
				familyBox.addItem(name);
			}
			if (!familyMap.isEmpty()) {
				String firstKey = familyMap.firstKey();
				familyBox.setSelectedItem(firstKey);
				updateDescription();
			} else {
				familyBox.addItem("No families available");
			}
			notifyObservers();
			familyBox.setEnabled(!familyMap.isEmpty());
		} catch (ComponentRegistryException e) {
			logger.error(e);
		}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	private void notifyObservers() {
		ComponentFamily chosenFamily = getChosenFamily();
		FamilyChoiceMessage message = new FamilyChoiceMessage(chosenFamily);
		for (Observer<FamilyChoiceMessage> o : getObservers()) {
			try {
				o.notify(FamilyChooserPanel.this, message);
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public ComponentFamily getChosenFamily() {
		if (familyBox.getSelectedIndex() >= 0) {
			return familyMap.get(familyBox.getSelectedItem());
		}
		return null;
	}

	@Override
	public void addObserver(Observer<FamilyChoiceMessage> observer) {
		observers.add(observer);
		ComponentFamily chosenFamily = getChosenFamily();
		FamilyChoiceMessage message = new FamilyChoiceMessage(chosenFamily);
		try {
			observer.notify(this, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public List<Observer<FamilyChoiceMessage>> getObservers() {
		return observers;
	}

	@Override
	public void removeObserver(Observer<FamilyChoiceMessage> observer) {
		observers.remove(observer);
	}

}
