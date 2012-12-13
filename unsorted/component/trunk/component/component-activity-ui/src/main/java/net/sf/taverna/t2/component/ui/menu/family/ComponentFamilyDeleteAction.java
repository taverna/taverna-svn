/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.family;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.ui.panel.FamilyChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentFamilyDeleteAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4976161883778371344L;



	private static Logger logger = Logger.getLogger(ComponentFamilyDeleteAction.class);
	

	
	private static final String DELETE_FAMILY = "Delete family...";
	
	public ComponentFamilyDeleteAction() {
		super (DELETE_FAMILY, ComponentServiceIcon.getIcon());
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		RegistryChooserPanel registryPanel = new RegistryChooserPanel();
		
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		overallPanel.add(registryPanel, gbc);
		
		FamilyChooserPanel familyPanel = new FamilyChooserPanel();
		registryPanel.addObserver(familyPanel);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = 1;
		overallPanel.add(familyPanel, gbc);
				
		int answer = JOptionPane.showConfirmDialog(null, overallPanel, "Delete Component Family", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
			ComponentRegistry chosenRegistry = registryPanel.getChosenRegistry();
			if (chosenRegistry == null) {
				JOptionPane.showMessageDialog(null, "Unable to determine registry", "Component Registry Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ComponentFamily chosenFamily = familyPanel.getChosenFamily();
			if (chosenFamily == null) {
				JOptionPane.showMessageDialog(null, "Unable to determine family", "Component Family Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + chosenFamily.getName(), "Delete Component Family Confirmation", JOptionPane.YES_NO_OPTION);
			if (confirmation == JOptionPane.YES_OPTION) {
				try {
					chosenRegistry.removeComponentFamily(chosenFamily);
				} catch (ComponentRegistryException e) {
					JOptionPane.showMessageDialog(null, "Unable to delete " + chosenFamily.getName(), "Component Family Deletion Error", JOptionPane.ERROR_MESSAGE);
					logger.error(e);
				}
			}
		}

	}

}
