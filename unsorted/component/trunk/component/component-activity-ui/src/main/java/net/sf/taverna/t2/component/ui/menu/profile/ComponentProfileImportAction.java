/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.profile;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;

/**
 * @author alanrw
 *
 */
public class ComponentProfileImportAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3796754761286943970L;
	private static final String IMPORT_PROFILE = "Import profile...";
	
	private static Logger logger = Logger.getLogger(ComponentProfileImportAction.class);
	
	public ComponentProfileImportAction() {
		super (IMPORT_PROFILE, ComponentServiceIcon.getIcon());
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
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		overallPanel.add(new JLabel("Profile Location:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		final JTextField profileLocation = new JTextField(40);
		overallPanel.add(profileLocation, gbc);
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.gridy++;
		JButton browseButton = new JButton(new AbstractAction("Browse"){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "XML files", "xml");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(null);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			       try {
					profileLocation.setText(chooser.getSelectedFile().toURI().toURL().toString());
				} catch (MalformedURLException e) {
					logger.error(e);
				}
			    }
			}});
		overallPanel.add(browseButton, gbc);
		
		int answer = JOptionPane.showConfirmDialog(null, overallPanel, "Import Component Profile", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
				try {
					ComponentRegistry chosenRegistry = registryPanel.getChosenRegistry();
					if (chosenRegistry == null) {
					JOptionPane.showMessageDialog(null, "Unable to determine registry", "Component Registry Problem", JOptionPane.ERROR_MESSAGE);
					return;
					}
					URL componentProfileURL = new URL(profileLocation.getText());
					ComponentProfile newProfile = new ComponentProfile(componentProfileURL);
					String newName = newProfile.getName();
					boolean alreadyUsed = false;
					for (ComponentProfile p : chosenRegistry.getComponentProfiles()) {
						if (p.getName().equals(newName)) {
							alreadyUsed = true;
							break;
						}
					}
					if (alreadyUsed) {
						JOptionPane.showMessageDialog(null, newName + " is already used", "Duplicate profile name", JOptionPane.ERROR_MESSAGE);
					} else {
						chosenRegistry.addComponentProfile(newProfile);
					}
				} catch (MalformedURLException e) {
					JOptionPane.showMessageDialog(null, profileLocation.getText() + " is not a valid URL", "Invalid URL", JOptionPane.ERROR_MESSAGE);
				} catch (ComponentRegistryException e) {
					JOptionPane.showMessageDialog(null, "Unable to save profile", "Registry Exception", JOptionPane.ERROR_MESSAGE);
				}
		}

	}

}
