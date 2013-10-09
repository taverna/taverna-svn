/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.util.Utils;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
@SuppressWarnings("serial")
public class SearchChoicePanel extends JPanel {

	private Logger logger = Logger.getLogger(SearchChoicePanel.class);

	private static final String SEARCHING = "Searching...";
	private static final String[] SEARCHING_ARRAY = new String[] { SEARCHING };
	private static final String NO_MATCHES = "No matches";
	private static final String SEARCH_FAILED = "Search failed";

	private static final List<String> RESERVED_WORDS = Arrays
			.asList(new String[] { SEARCHING, NO_MATCHES, SEARCH_FAILED });

	private Registry registry;
	private String prefixes;
	private String queryText;
	private JLabel registryURLLabel;
	private JComboBox familyBox;
	private JComboBox componentBox;
	private JComboBox versionBox;

	public SearchChoicePanel(Registry registry, String prefixes,
			String queryText) {
		super();
		this.registry = registry;
		this.prefixes = prefixes;
		this.queryText = queryText;
		this.setLayout(new GridBagLayout());

		componentBox = new JComboBox(SEARCHING_ARRAY);
		componentBox.setPrototypeDisplayValue(Utils.LONG_STRING);
		familyBox = new JComboBox(SEARCHING_ARRAY);
		familyBox.setPrototypeDisplayValue(Utils.LONG_STRING);
		versionBox = new JComboBox(SEARCHING_ARRAY);
		versionBox.setPrototypeDisplayValue(Utils.LONG_STRING);

		GridBagConstraints gbc = new GridBagConstraints();

		JLabel registryLabel = new JLabel("Component registry:");

		gbc.insets.left = 5;
		gbc.insets.right = 5;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.gridy++;
		this.add(registryLabel, gbc);
		gbc.gridx = 1;
		registryURLLabel = new JLabel(SEARCHING);
		this.add(registryURLLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		this.add(new JLabel("Component family:"), gbc);
		gbc.gridx = 1;

		this.add(familyBox, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		this.add(new JLabel("Component:"), gbc);
		gbc.gridx = 1;
		this.add(componentBox, gbc);

		gbc.gridx = 0;
		gbc.gridy++;

		this.add(new JLabel("Component version:"), gbc);
		gbc.gridx = 1;
		this.add(versionBox, gbc);

		(new Searcher()).execute();

	}

	private class Searcher extends SwingWorker<Set<Version.ID>, Object> {

		@Override
		protected Set<Version.ID> doInBackground() throws Exception {
			return registry.searchForComponents(prefixes, queryText);
		}

		@Override
		protected void done() {
			familyBox.removeAllItems();
			componentBox.removeAllItems();
			versionBox.removeAllItems();
			try {
				final Set<Version.ID> matches = this.get();
				if (matches.isEmpty()) {
					registryURLLabel.setText(NO_MATCHES);
					familyBox.addItem(NO_MATCHES);
					componentBox.addItem(NO_MATCHES);
					versionBox.addItem(NO_MATCHES);
				} else {
					Version.ID one = (Version.ID) matches.toArray()[0];
					registryURLLabel.setText(ComponentPreference.getInstance()
							.getRegistryName(one.getRegistryBase()));
					String[] componentFamilyNames = calculateMatchingFamilyNames(matches);
					for (String familyName : componentFamilyNames) {
						familyBox.addItem(familyName);
					}
					familyBox.addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {
								updateComponentBox(matches, componentBox,
										(String) familyBox.getSelectedItem());
							}
						}
					});
					componentBox.addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {
								updateVersionBox(
										matches,
										versionBox,
										(String) componentBox.getSelectedItem(),
										(String) familyBox.getSelectedItem());
							}
						}
					});
					familyBox.setSelectedIndex(0);
					updateComponentBox(matches, componentBox,
							(String) familyBox.getSelectedItem());
					updateVersionBox(matches, versionBox,
							(String) componentBox.getSelectedItem(),
							(String) familyBox.getSelectedItem());
				}
			} catch (InterruptedException e) {
				logger.error(e);
				registryURLLabel.setText(SEARCH_FAILED);
				familyBox.addItem(SEARCH_FAILED);
				componentBox.addItem(SEARCH_FAILED);
				versionBox.addItem(SEARCH_FAILED);

			} catch (ExecutionException e) {
				logger.error(e);
				registryURLLabel.setText(SEARCH_FAILED);
				familyBox.addItem(SEARCH_FAILED);
				componentBox.addItem(SEARCH_FAILED);
				versionBox.addItem(SEARCH_FAILED);
			}
		}
	}

	private String[] calculateMatchingFamilyNames(
			Set<Version.ID> matchingComponents) {
		TreeSet<String> result = new TreeSet<String>();
		for (Version.ID v : matchingComponents) {
			result.add(v.getFamilyName());
		}
		return result.toArray(new String[0]);
	}

	private void updateComponentBox(Set<Version.ID> matchingComponents,
			JComboBox componentBox, String selectedItem) {
		componentBox.removeAllItems();
		String[] matchingComponentNames = calculateMatchingComponentNames(
				matchingComponents, selectedItem);
		for (String componentName : matchingComponentNames) {
			componentBox.addItem(componentName);
		}
		componentBox.setSelectedIndex(0);
	}

	private String[] calculateMatchingComponentNames(
			Set<Version.ID> matchingComponents, String familyName) {
		TreeSet<String> result = new TreeSet<String>();
		for (Version.ID v : matchingComponents) {
			if (v.getFamilyName().equals(familyName)) {
				result.add(v.getComponentName());
			}
		}
		return result.toArray(new String[0]);
	}

	private void updateVersionBox(Set<Version.ID> matchingComponents,
			JComboBox versionBox, String componentName, String familyName) {
		versionBox.removeAllItems();
		Integer[] matchingVersionNumbers = calculateMatchingVersionNumbers(
				matchingComponents, componentName, familyName);
		for (Integer v : matchingVersionNumbers) {
			versionBox.addItem(v);
		}
		versionBox.setSelectedIndex(0);
	}

	private Integer[] calculateMatchingVersionNumbers(
			Set<Version.ID> matchingComponents, String componentName,
			String familyName) {
		TreeSet<Integer> result = new TreeSet<Integer>();
		for (Version.ID v : matchingComponents) {
			if (v.getFamilyName().equals(familyName)
					&& v.getComponentName().equals(componentName)) {
				result.add(v.getComponentVersion());
			}
		}
		return result.toArray(new Integer[0]);
	}

	public Version.ID getVersionIdentification() {
		String registryString = registryURLLabel.getText();
		if (RESERVED_WORDS.contains(registryString)) {
			return null;
		}
		return new ComponentVersionIdentification(registry.getRegistryBase(),
				(String) familyBox.getSelectedItem(),
				(String) componentBox.getSelectedItem(),
				(Integer) versionBox.getSelectedItem());
	}

}
