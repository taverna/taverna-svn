/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.palette;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.drizzle.decoder.CommonKey;
import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;
import net.sf.taverna.t2.drizzle.model.ActivitySubsetModel;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.ObjectAndFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyPatternFilter;
import net.sf.taverna.t2.drizzle.view.subset.ActivitySubsetPanel;

/**
 * @author alanrw
 * 
 */
public final class SearchSubsetDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6090097608584963674L;

	/**
	 * @param panel
	 * @param subsetPanel
	 * @param propertiedProcessorFactoryAdapterSet
	 */
	public SearchSubsetDialog(
			final ActivityPalettePanel panel,
			final ActivitySubsetPanel subsetPanel,
			final PropertiedObjectSet<ProcessorFactoryAdapter> propertiedProcessorFactoryAdapterSet) {
		this.setLayout(new GridLayout(0, 1));

		JPanel keyPanel = new JPanel();
		keyPanel.add(new JLabel("Property key")); //$NON-NLS-1$
		Set<PropertyKey> keys = new TreeSet<PropertyKey>(subsetPanel
				.getSubsetModel().getPropertyKeyProfile());
		final JComboBox keysCombo = new JComboBox(keys
				.toArray(new PropertyKey[0]));
		keysCombo.setSelectedItem(CommonKey.NameKey);
		keyPanel.add(keysCombo);

		this.add(keyPanel);

		JPanel patternPanel = new JPanel();
		patternPanel.add(new JLabel("Text Pattern")); //$NON-NLS-1$
		final JTextField patternField = new JTextField();
		patternField.setColumns(10);
		patternPanel.add(patternField);

		this.add(patternPanel);

		final JCheckBox clearResults = new JCheckBox("clear results");
		clearResults.setSelected(true);
		this.add(clearResults);

		final JCheckBox searchAllActivities = new JCheckBox("search all activities");
		searchAllActivities.setSelected(true);
		this.add(searchAllActivities);

		JPanel buttonPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel"); //$NON-NLS-1$
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				SearchSubsetDialog.this.dispose();
			}

		});
		JButton searchButton = new JButton("Search"); //$NON-NLS-1$
		
		ActionListener searchListener = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				PropertyKey selectedKey = (PropertyKey) keysCombo
						.getSelectedItem();

				String pattern = patternField.getText();
				if ((pattern == null) || (pattern.length() == 0)) {
					JOptionPane.showMessageDialog(SearchSubsetDialog.this,
							"pattern must be specified"); //$NON-NLS-1$
					return;
				}
				ActivitySubsetModel searchResults = ActivityPaletteModel
						.getSearchResultsSubsetModel();
				PropertiedObjectFilter<ProcessorFactoryAdapter> searchFilter = new PropertyPatternFilter<ProcessorFactoryAdapter>(
						selectedKey, pattern,
						propertiedProcessorFactoryAdapterSet);
				if (searchAllActivities.isSelected()) {
					if (clearResults.isSelected()) {
						searchResults.setFilter(searchFilter);
					} else {
						searchResults.addOredFilter(searchFilter);
					}
				} else {
				HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>> filters = new HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>>();
				filters.add(subsetPanel.getSubsetModel().getFilter());
				filters.add(searchFilter);
				PropertiedObjectFilter<ProcessorFactoryAdapter> newFilter = new ObjectAndFilter<ProcessorFactoryAdapter>(
						filters);
				if (clearResults.isSelected()) {
					searchResults.setFilter(newFilter);
				} else {

					searchResults.addOredFilter(newFilter);
				}
				}
				panel.showTabWithModel(searchResults);
				SearchSubsetDialog.this.dispose();
			}

		};
		searchButton.addActionListener(searchListener);
		patternField.addActionListener(searchListener);
		buttonPanel.add(cancelButton);
		buttonPanel.add(searchButton);
		this.add(buttonPanel);

		this.pack();
	}
}
