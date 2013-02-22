/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

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
public class ProfileChooserPanel extends JPanel implements Observer<RegistryChoiceMessage> {

	/**
	 *
	 */
	private static final long serialVersionUID = 2175274929391537032L;

	private static Logger logger = Logger.getLogger(ProfileChooserPanel.class);

	private JComboBox profileBox = new JComboBox();

	private JTextArea profileDescription = new JTextArea(10, 60);

	private SortedMap<String, ComponentProfile> profileMap = new TreeMap<String, ComponentProfile>();

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
		profileDescription.setBorder(new TitledBorder("Profile description"));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weighty = 1;
		this.add(profileDescription, gbc);
		profileDescription.setEditable(false);
	}

	@Override
	public void notify(Observable<RegistryChoiceMessage> sender,
			RegistryChoiceMessage message) throws Exception {
		this.setRegistry(message.getChosenRegistry());
	}

	private void setRegistry(ComponentRegistry chosenRegistry) {
		profileMap.clear();
		profileBox.removeAllItems();
		profileDescription.setText("");
		List<ComponentProfile> componentProfiles;
		try {
			componentProfiles = chosenRegistry.getComponentProfiles();
		} catch (ComponentRegistryException e) {
			logger.error(e);
			return;
		} catch (NullPointerException e) {
			logger.error(e);
			return;
		}
			for (ComponentProfile p : componentProfiles) {
				try {
					String name = p.getName();
					profileMap.put(name, p);
				}
			  catch (NullPointerException e) {
				logger.error(e);

			}
			}
			for (String name : profileMap.keySet()) {
				profileBox.addItem(name);
			}
			if (!profileMap.isEmpty()) {
				String firstKey = profileMap.firstKey();
				profileBox.setSelectedItem(firstKey);
				setProfile(profileMap.get(firstKey));
			}
	}

	private void setProfile(ComponentProfile componentProfile) {
		profileDescription.setText(componentProfile.getDescription());
	}

	public ComponentProfile getChosenProfile() {
		if (profileBox.getSelectedIndex() >= 0) {
			Object selectedItem = profileBox.getSelectedItem();
			return profileMap.get(selectedItem);
		} else {
			return null;
		}
	}

}
