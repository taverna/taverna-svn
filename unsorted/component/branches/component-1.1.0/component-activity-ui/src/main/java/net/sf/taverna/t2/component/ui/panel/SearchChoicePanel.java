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

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
@SuppressWarnings("serial")
public class SearchChoicePanel extends JPanel {
	private static final Logger logger = getLogger(SearchChoicePanel.class);
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
		componentBox.setPrototypeDisplayValue(LONG_STRING);
		familyBox = new JComboBox(SEARCHING_ARRAY);
		familyBox.setPrototypeDisplayValue(LONG_STRING);
		versionBox = new JComboBox(SEARCHING_ARRAY);
		versionBox.setPrototypeDisplayValue(LONG_STRING);

		GridBagConstraints gbc = new GridBagConstraints();

		JLabel registryLabel = new JLabel("Component registry:");

		gbc.insets.left = 5;
		gbc.insets.right = 5;
		gbc.gridx = 0;
		gbc.anchor = WEST;
		gbc.fill = BOTH;
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

		new Searcher().execute();
	}

	private class Searcher extends SwingWorker<Set<Version.ID>, Object> {
		@Override
		protected Set<Version.ID> doInBackground() throws Exception {
			return registry.searchForComponents(prefixes, queryText);
		}

		@Override
		protected void done() {
			clearAll();
			try {
				Set<Version.ID> matches = get();
				if (matches.isEmpty())
					setAll(NO_MATCHES);
				else
					searchCompletedSuccessfully(matches);
			} catch (InterruptedException e) {
				logger.error("search was interrupted", e);
				setAll(SEARCH_FAILED);
			} catch (ExecutionException e) {
				logger.error("problem in execution", e.getCause());
				setAll(SEARCH_FAILED);
			}
		}
	}

	private void clearAll() {
		familyBox.removeAllItems();
		componentBox.removeAllItems();
		versionBox.removeAllItems();
	}

	private void setAll(String text) {
		registryURLLabel.setText(text);
		familyBox.addItem(text);
		componentBox.addItem(text);
		versionBox.addItem(text);
	}

	private String[] calculateMatchingFamilyNames(
			Set<Version.ID> matchingComponents) {
		TreeSet<String> result = new TreeSet<String>();
		for (Version.ID v : matchingComponents)
			result.add(v.getFamilyName());
		return result.toArray(new String[0]);
	}

	private void updateComponentBox(Set<Version.ID> matchingComponents,
			JComboBox componentBox, String selectedItem) {
		componentBox.removeAllItems();
		String[] matchingComponentNames = calculateMatchingComponentNames(
				matchingComponents, selectedItem);
		for (String componentName : matchingComponentNames)
			componentBox.addItem(componentName);
		componentBox.setSelectedIndex(0);
	}

	private String[] calculateMatchingComponentNames(
			Set<Version.ID> matchingComponents, String familyName) {
		TreeSet<String> result = new TreeSet<String>();
		for (Version.ID v : matchingComponents)
			if (v.getFamilyName().equals(familyName))
				result.add(v.getComponentName());
		return result.toArray(new String[0]);
	}

	private void updateVersionBox(Set<Version.ID> matchingComponents,
			JComboBox versionBox, String componentName, String familyName) {
		versionBox.removeAllItems();
		for (Integer v : calculateMatchingVersionNumbers(matchingComponents,
				componentName, familyName))
			versionBox.addItem(v);
		versionBox.setSelectedIndex(0);
	}

	private Integer[] calculateMatchingVersionNumbers(
			Set<Version.ID> matchingComponents, String componentName,
			String familyName) {
		TreeSet<Integer> result = new TreeSet<Integer>();
		for (Version.ID v : matchingComponents)
			if (v.getFamilyName().equals(familyName)
					&& v.getComponentName().equals(componentName))
				result.add(v.getComponentVersion());
		return result.toArray(new Integer[0]);
	}

	public Version.ID getVersionIdentification() {
		String registryString = registryURLLabel.getText();
		if (RESERVED_WORDS.contains(registryString))
			return null;

		return new ComponentVersionIdentification(registry.getRegistryBase(),
				(String) familyBox.getSelectedItem(),
				(String) componentBox.getSelectedItem(),
				(Integer) versionBox.getSelectedItem());
	}

	private void searchCompletedSuccessfully(final Set<Version.ID> matches) {
		Version.ID one = (Version.ID) matches.toArray()[0];
		registryURLLabel.setText(ComponentPreference.getInstance()
				.getRegistryName(one.getRegistryBase()));
		String[] componentFamilyNames = calculateMatchingFamilyNames(matches);
		for (String familyName : componentFamilyNames)
			familyBox.addItem(familyName);
		familyBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == SELECTED)
					updateComponentBox(matches, componentBox,
							(String) familyBox.getSelectedItem());
			}
		});
		componentBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == SELECTED)
					updateVersionBox(matches, versionBox,
							(String) componentBox.getSelectedItem(),
							(String) familyBox.getSelectedItem());
			}
		});
		familyBox.setSelectedIndex(0);
		updateComponentBox(matches, componentBox,
				(String) familyBox.getSelectedItem());
		updateVersionBox(matches, versionBox,
				(String) componentBox.getSelectedItem(),
				(String) familyBox.getSelectedItem());
	}
}
