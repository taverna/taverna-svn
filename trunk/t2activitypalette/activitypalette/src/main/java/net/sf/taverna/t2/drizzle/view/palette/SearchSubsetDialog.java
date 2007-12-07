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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetModel;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.ObjectAndFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyPatternFilter;
import net.sf.taverna.t2.drizzle.util.TrueFilter;
import net.sf.taverna.t2.drizzle.view.subset.ActivitySubsetPanel;

/**
 * @author alanrw
 * 
 */
public final class SearchSubsetDialog extends JDialog {

	public SearchSubsetDialog(final ActivitySubsetPanel subsetPanel, final PropertiedObjectSet<ProcessorFactoryAdapter> registry) {
		this.setLayout(new GridLayout(0, 1));

		JPanel keyPanel = new JPanel();
		keyPanel.add(new JLabel("Property key"));
		Set<PropertyKey> keys = new TreeSet<PropertyKey>(subsetPanel.getSubsetModel().getPropertyKeyProfile());
		final JComboBox keysCombo = new JComboBox(keys.toArray(new PropertyKey[0]));
		keyPanel.add(keysCombo);
		
		this.add(keyPanel);

		JPanel patternPanel = new JPanel();
		patternPanel.add(new JLabel("Text Pattern"));
		final JTextField patternField = new JTextField();
		patternField.setColumns(10);
		patternPanel.add(patternField);

		this.add(patternPanel);

		JPanel buttonPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				SearchSubsetDialog.this.dispose();
			}

		});
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				PropertyKey selectedKey = (PropertyKey) keysCombo.getSelectedItem();
				
				String pattern = patternField.getText();
				if ((pattern == null) || (pattern.length() == 0)) {
					JOptionPane.showMessageDialog(SearchSubsetDialog.this,
							"pattern must be specified"); //$NON-NLS-1$
					return;
				}
				ActivityRegistrySubsetModel searchResults =
					ActivityPaletteModel.getSearchResultsSubsetModel();
				HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>> filters =
					new HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>>();
				filters.add(subsetPanel.getSubsetModel().getFilter());
				filters.add(new PropertyPatternFilter<ProcessorFactoryAdapter>(selectedKey, pattern, registry));
				PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter =
					new ObjectAndFilter<ProcessorFactoryAdapter>(filters);

				searchResults.addOredFilter(additionalFilter);
				SearchSubsetDialog.this.dispose();
			}

		});
		buttonPanel.add(cancelButton);
		buttonPanel.add(searchButton);
		this.add(buttonPanel);

		this.pack();
	}
}
