/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.profile;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.ui.panel.LicenseChooserPanel;
import net.sf.taverna.t2.component.ui.panel.ProfileChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.panel.SharingPolicyChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;

/**
 * @author alanrw
 *
 */
public class ComponentProfileCopyAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6332253931049645259L;
	private static final String COPY_PROFILE = "Copy profile...";
	
	public ComponentProfileCopyAction() {
		super (COPY_PROFILE, ComponentServiceIcon.getIcon());
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		RegistryChooserPanel sourceRegistryPanel = new RegistryChooserPanel();
		sourceRegistryPanel.setBorder(new TitledBorder("Source registry"));
		
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		overallPanel.add(sourceRegistryPanel, gbc);
		
		ProfileChooserPanel profilePanel = new ProfileChooserPanel();
		profilePanel.setBorder(new TitledBorder("Source profile"));

		sourceRegistryPanel.addObserver(profilePanel);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = 1;
		overallPanel.add(profilePanel, gbc);
		
		RegistryChooserPanel targetRegistryPanel = new RegistryChooserPanel();
		targetRegistryPanel.setBorder(new TitledBorder("Target registry"));
		gbc.gridy = 2;
		overallPanel.add(targetRegistryPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridy++;
		SharingPolicyChooserPanel permissionPanel = new SharingPolicyChooserPanel();
		targetRegistryPanel.addObserver(permissionPanel);
		overallPanel.add(permissionPanel, gbc);

		gbc.gridy++;
		LicenseChooserPanel licensePanel = new LicenseChooserPanel();
		targetRegistryPanel.addObserver(licensePanel);
		overallPanel.add(licensePanel, gbc);

		int answer = JOptionPane.showConfirmDialog(null, overallPanel, "Copy Component Profile", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
				try {
					ComponentRegistry sourceRegistry = sourceRegistryPanel.getChosenRegistry();
					if (sourceRegistry == null) {
					JOptionPane.showMessageDialog(null, "Unable to determine source registry", "Component Registry Problem", JOptionPane.ERROR_MESSAGE);
					return;
					}
					ComponentRegistry targetRegistry = targetRegistryPanel.getChosenRegistry();
					if (targetRegistry == null) {
					JOptionPane.showMessageDialog(null, "Unable to determine target registry", "Component Registry Problem", JOptionPane.ERROR_MESSAGE);
					return;
					}
					if (sourceRegistry.equals(targetRegistry)) {
						JOptionPane.showMessageDialog(null, "Cannot copy to the same registry", "Copy Problem", JOptionPane.ERROR_MESSAGE);
						return;						
					}
					ComponentProfile sourceProfile = profilePanel.getChosenProfile();
					if (sourceProfile == null) {
						JOptionPane.showMessageDialog(null, "Unable to determine source profile", "Component Profile Problem", JOptionPane.ERROR_MESSAGE);
						return;					
					}
					for (ComponentProfile p : targetRegistry.getComponentProfiles()) {
						if (p.getName().equals(sourceProfile.getName())) {
							JOptionPane.showMessageDialog(null, "Target registry already contains a profile named " + sourceProfile.getName(), "Copy Problem", JOptionPane.ERROR_MESSAGE);
							return;
						}
						String sourceId = sourceProfile.getId();
						if (sourceId == null) {
							JOptionPane.showMessageDialog(null, "Source profile \"" + sourceProfile.getName() + "\" has no id ", "Copy Problem", JOptionPane.ERROR_MESSAGE);
							return;
						}
						String id = p.getId();
						if (sourceId.equals(id)) {
							JOptionPane.showMessageDialog(null, "Target registry already contains a profile with id " + sourceId, "Copy Problem", JOptionPane.ERROR_MESSAGE);
							return;							
						}
					}
					targetRegistry.addComponentProfile(sourceProfile,
							licensePanel.getChosenLicense(), permissionPanel.getChosenPermission());
				} catch (ComponentRegistryException e) {
					JOptionPane.showMessageDialog(null, "Unable to save profile", "Registry Exception", JOptionPane.ERROR_MESSAGE);
				}
		}

	}

}
