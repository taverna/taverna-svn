/**
 *
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.PrefixPanel;
import net.sf.taverna.t2.component.ui.panel.ProfileChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.panel.SearchChoicePanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceDesc;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentSearchAction extends AbstractAction {

	private static final String WFDESC_PREFIX = "wfdesc";

	private static final long serialVersionUID = -7780471499146286881L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ComponentSearchAction.class);

	private static final String SEARCH_FOR_COMPONENTS = "Search for components...";

	private JPanel overallPanel;
	private GridBagConstraints gbc;
	
	public ComponentSearchAction() {
		super (SEARCH_FOR_COMPONENTS, ComponentServiceIcon.getIcon());
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {

		overallPanel = new JPanel();
		overallPanel.setLayout(new GridBagLayout());

		gbc = new GridBagConstraints();

		RegistryChooserPanel registryPanel = new RegistryChooserPanel();

		gbc.insets.left = 5;
		gbc.insets.right = 5;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.gridy++;
		overallPanel.add(registryPanel, gbc);

		ProfileChooserPanel profilePanel = new ProfileChooserPanel();
		registryPanel.addObserver(profilePanel);
		gbc.gridx = 0;
		gbc.gridy++;
		overallPanel.add(profilePanel, gbc);
		
		PrefixPanel prefixPanel = new PrefixPanel();
		profilePanel.addObserver(prefixPanel);
		gbc.gridx = 0;
		gbc.gridy++;
		overallPanel.add(prefixPanel, gbc);
		
		JTextArea queryPane = new JTextArea(20, 80);
		gbc.gridx = 0;
		gbc.weighty = 1;
		gbc.gridy++;
		overallPanel.add(new JScrollPane(queryPane), gbc);
			
		int answer = JOptionPane.showConfirmDialog(null, overallPanel, "Search for components", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
			ComponentRegistry chosenRegistry = registryPanel.getChosenRegistry();
			if (chosenRegistry == null) {
				JOptionPane.showMessageDialog(null, "Unable to determine registry", "Component Registry Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ComponentProfile chosenProfile = profilePanel.getChosenProfile();
			if (chosenProfile == null) {
				JOptionPane.showMessageDialog(null, "Unable to determine profile", "Component Profile Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String prefixString = "";
			for (Entry<String, String> entry : prefixPanel.getPrefixMap().entrySet()) {
				if (!entry.getKey().equals(WFDESC_PREFIX)) {
					prefixString += constructPrefixString(entry);
				}
			}
					showSearchResultsChoice(chosenRegistry, prefixString, queryPane.getText());
		}
	}

	private void showSearchResultsChoice(ComponentRegistry chosenRegistry, String prefixString, String queryString
			) {
		
		SearchChoicePanel searchChoicePanel = new SearchChoicePanel(chosenRegistry, prefixString, queryString);
		int answer = JOptionPane.showOptionDialog(null, 
		        searchChoicePanel, 
		        "Matching components", 
		        JOptionPane.OK_CANCEL_OPTION, 
		        JOptionPane.QUESTION_MESSAGE, 
		        null, 
		        new String[]{"Add to workflow", "Cancel"}, // this is the array
		        "Cancel");
		if (answer == JOptionPane.OK_OPTION) {
			ComponentVersionIdentification ident = searchChoicePanel.getVersionIdentification();
			if (ident != null) {
			ComponentServiceDesc newDesc = new ComponentServiceDesc (ident);
			WorkflowView.importServiceDescription(newDesc, false);
			}
		}
	}


	private static String constructPrefixString(Entry<String, String> entry) {
		String key = entry.getKey();
		String value = entry.getValue();
		return String.format("PREFIX %s:<%s>\n", key, value);

	}

}
