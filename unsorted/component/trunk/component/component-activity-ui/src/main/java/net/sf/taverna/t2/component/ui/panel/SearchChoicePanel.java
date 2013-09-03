/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.menu.component.ComponentSearchAction;
import net.sf.taverna.t2.component.ui.util.Utils;

/**
 * @author alanrw
 *
 */
public class SearchChoicePanel extends JPanel {
	


	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SearchChoicePanel.class);

	private static final String SEARCHING = "Searching...";	
	private static final String[] SEARCHING_ARRAY = new String[] {SEARCHING};
	private static final String NO_MATCHES = "No matches";
	private static final String SEARCH_FAILED = "Search failed";
	
	private static final List RESERVED_WORDS = Arrays.asList(new String[]{SEARCHING, NO_MATCHES, SEARCH_FAILED});

	private ComponentRegistry registry;
	private String prefixes;
	private String queryText;
	private JLabel registryURLLabel;
	private JComboBox familyBox;
	private JComboBox componentBox;
	private JComboBox versionBox;

	public SearchChoicePanel(ComponentRegistry registry, String prefixes, String queryText) {
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
	
	private class Searcher extends SwingWorker<Set<ComponentVersionIdentification>, Object> {


		@Override
		protected Set<ComponentVersionIdentification> doInBackground()
				throws Exception {
			return registry.searchForComponents(prefixes, queryText);
		}
		
		@SuppressWarnings("unchecked")
		@Override
	    protected void done() {
			familyBox.removeAllItems();
			componentBox.removeAllItems();
			versionBox.removeAllItems();
			try {
				final Set<ComponentVersionIdentification> matches = this.get();
				if (matches.isEmpty()) {
					registryURLLabel.setText(NO_MATCHES);
					familyBox.addItem(NO_MATCHES);
					componentBox.addItem(NO_MATCHES);
					versionBox.addItem(NO_MATCHES);
				} else {
					ComponentVersionIdentification one = (ComponentVersionIdentification) matches.toArray()[0];
					registryURLLabel.setText(ComponentPreference.getInstance().getRegistryName(one.getRegistryBase()));
					String[] componentFamilyNames = calculateMatchingFamilyNames(matches);
					for (String familyName : componentFamilyNames) {
						familyBox.addItem(familyName);
					}
					familyBox.addItemListener(new ItemListener() {
						
										@Override
										public void itemStateChanged(ItemEvent e) {
											if (e.getStateChange() == ItemEvent.SELECTED) {
										          updateComponentBox (matches, componentBox, (String) familyBox.getSelectedItem());
										       }
										}});
					componentBox.addItemListener(new ItemListener() {
						
									@Override
									public void itemStateChanged(ItemEvent e) {
										if (e.getStateChange() == ItemEvent.SELECTED) {
									          updateVersionBox (matches, versionBox, (String) componentBox.getSelectedItem(), (String) familyBox.getSelectedItem());
									       }
									}});
					familyBox.setSelectedIndex(0);
					updateComponentBox(matches, componentBox, (String) familyBox.getSelectedItem());
			          updateVersionBox (matches, versionBox, (String) componentBox.getSelectedItem(), (String) familyBox.getSelectedItem());
				}
			} catch (InterruptedException  e) {
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
			Set<ComponentVersionIdentification> matchingComponents) {
		TreeSet<String> result = new TreeSet<String>();
		for (ComponentVersionIdentification v : matchingComponents) {
			result.add(v.getFamilyName());
		}
		return result.toArray(new String[0]);
	}

	private void updateComponentBox(Set<ComponentVersionIdentification>  matchingComponents, JComboBox componentBox, String selectedItem ) {
		componentBox.removeAllItems();
		String[] matchingComponentNames = calculateMatchingComponentNames(matchingComponents, selectedItem);
		for (String componentName : matchingComponentNames) {
			componentBox.addItem(componentName);
		}
		componentBox.setSelectedIndex(0);
	}
	private String[] calculateMatchingComponentNames(
			Set<ComponentVersionIdentification> matchingComponents,
			String familyName) {
		TreeSet<String> result = new TreeSet<String>();
		for (ComponentVersionIdentification v : matchingComponents) {
			if (v.getFamilyName().equals(familyName)) {
			result.add(v.getComponentName());
			}
		}
		return result.toArray(new String[0]);
	}

	private void updateVersionBox(
			Set<ComponentVersionIdentification> matchingComponents,
			JComboBox versionBox, String componentName, String familyName) {
		versionBox.removeAllItems();
		Integer[] matchingVersionNumbers = calculateMatchingVersionNumbers(matchingComponents, componentName, familyName);
		for (Integer v : matchingVersionNumbers) {
			versionBox.addItem(v);
		}
		versionBox.setSelectedIndex(0);
	}




	private Integer[] calculateMatchingVersionNumbers(
			Set<ComponentVersionIdentification> matchingComponents,
			String componentName, String familyName) {
		TreeSet<Integer> result = new TreeSet<Integer>();
		for (ComponentVersionIdentification v : matchingComponents) {
			if (v.getFamilyName().equals(familyName) && v.getComponentName().equals(componentName)) {
			result.add(v.getComponentVersion());
			}
		}
		return result.toArray(new Integer[0]);
	}

	public ComponentVersionIdentification getVersionIdentification() {
		String registryString = registryURLLabel.getText();
		if (RESERVED_WORDS.contains(registryString)) {
			return null;
		}
			return new ComponentVersionIdentification (registry.getRegistryBase(), (String) familyBox.getSelectedItem(), (String) componentBox.getSelectedItem(), (Integer) versionBox.getSelectedItem());

	}



}
