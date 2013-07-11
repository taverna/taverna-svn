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

import net.sf.taverna.t2.component.profile.ComponentProfile;
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
public class ProfileChooserPanel extends JPanel implements Observer<RegistryChoiceMessage>, Observable<ProfileChoiceMessage> {

	/**
	 *
	 */
	private static final long serialVersionUID = 2175274929391537032L;

	private static Logger logger = Logger.getLogger(ProfileChooserPanel.class);
	
	private List<Observer<ProfileChoiceMessage>> observers = new ArrayList<Observer<ProfileChoiceMessage>>();



	@SuppressWarnings("rawtypes")
	private JComboBox profileBox = new JComboBox();

	private SortedMap<String, ComponentProfile> profileMap = new TreeMap<String, ComponentProfile>();

	private ComponentRegistry registry;

	@SuppressWarnings("unchecked")
	public ProfileChooserPanel() {
		super();
		profileBox.setPrototypeDisplayValue(Utils.LONG_STRING);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Profile:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		this.add(profileBox, gbc);
		profileBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {

					setProfile(profileMap.get(profileBox.getSelectedItem()));
				}
			}});

		profileBox.setEditable(false);
	}

	@Override
	public void notify(Observable<RegistryChoiceMessage> sender,
			RegistryChoiceMessage message) throws Exception {
		try {
			this.registry = message.getChosenRegistry();
			this.updateProfileModel();
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateProfileModel() {
		profileMap.clear();
		profileBox.removeAllItems();
		profileBox.setToolTipText(null);
		profileBox.addItem("Reading profiles");
		profileBox.setEnabled(false);
		(new ProfileUpdater()).execute();
	}

	private void setProfile(ComponentProfile componentProfile) {
		if (componentProfile != null) {
			profileBox.setToolTipText(componentProfile.getDescription());
		} else {
			profileBox.setToolTipText(null);
		}
		ComponentProfile chosenProfile = getChosenProfile();
		ProfileChoiceMessage message = new ProfileChoiceMessage(chosenProfile);
		for (Observer<ProfileChoiceMessage> o : getObservers()) {
			try {
				o.notify(ProfileChooserPanel.this, message);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public ComponentProfile getChosenProfile() {
		if (profileBox.getSelectedIndex() >= 0) {
			Object selectedItem = profileBox.getSelectedItem();
			return profileMap.get(selectedItem);
		} else {
			return null;
		}
	}

	private class ProfileUpdater extends SwingWorker<String, Object> {

		@Override
		protected String doInBackground() throws Exception {
			List<ComponentProfile> componentProfiles;
			if (registry != null) {
			try {
				componentProfiles = registry.getComponentProfiles();
			} catch (ComponentRegistryException e) {
				logger.error(e);
				return null;
			} catch (NullPointerException e) {
				logger.error(e);
				return null;
			}
			for (ComponentProfile p : componentProfiles) {
				try {
					String name = p.getName();
					profileMap.put(name, p);
				} catch (NullPointerException e) {
					logger.error(e);

				}
			}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
	    protected void done() {
			profileBox.removeAllItems();
			for (String name : profileMap.keySet()) {
				profileBox.addItem(name);
			}
			if (!profileMap.isEmpty()) {
				String firstKey = profileMap.firstKey();
				profileBox.setSelectedItem(firstKey);
				setProfile(profileMap.get(firstKey));
				profileBox.setEnabled(true);
			} else {
				profileBox.addItem("No profiles available");
				profileBox.setEnabled(false);
			}
		}

	}

	@Override
	public void addObserver(Observer<ProfileChoiceMessage> observer) {
		observers.add(observer);
		ComponentProfile chosenProfile = getChosenProfile();
			ProfileChoiceMessage message = new ProfileChoiceMessage(chosenProfile);
			try {
				observer.notify(this, message);
			} catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public void removeObserver(Observer<ProfileChoiceMessage> observer) {
		observers.remove(observer);
		}

	@Override
	public List<Observer<ProfileChoiceMessage>> getObservers() {
		return observers;
	}

}
