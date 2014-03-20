/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.profile;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.component.registry.ComponentUtil.makeProfile;
import static org.apache.log4j.Logger.getLogger;

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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.component.ui.panel.LicenseChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.panel.SharingPolicyChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.lang.ui.DeselectingButton;

/**
 * @author alanrw
 * 
 */
public class ComponentProfileImportAction extends AbstractAction {
	private static final long serialVersionUID = -3796754761286943970L;
	private static final Logger log = getLogger(ComponentProfileImportAction.class);
	private static final String IMPORT_PROFILE = "Import profile...";
	private static final JFileChooser chooser = new JFileChooser();

	public ComponentProfileImportAction() {
		super(IMPORT_PROFILE, ComponentServiceIcon.getIcon());
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		RegistryChooserPanel registryPanel = new RegistryChooserPanel();

		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		overallPanel.add(registryPanel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		overallPanel.add(new JLabel("Profile URL or local file path:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		final JTextField profileLocation = new JTextField(30);
		overallPanel.add(profileLocation, gbc);
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.gridy++;
		JButton browseButton = new DeselectingButton(new AbstractAction(
				"Browse") {
			private static final long serialVersionUID = 1574330610799117697L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"XML files", "xml");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == APPROVE_OPTION)
					profileLocation.setText(chooser.getSelectedFile().toURI()
							.toString());
			}
		});
		overallPanel.add(browseButton, gbc);

		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridy++;
		SharingPolicyChooserPanel permissionPanel = new SharingPolicyChooserPanel();
		registryPanel.addObserver(permissionPanel);
		overallPanel.add(permissionPanel, gbc);

		gbc.gridy++;
		LicenseChooserPanel licensePanel = new LicenseChooserPanel();
		registryPanel.addObserver(licensePanel);
		overallPanel.add(licensePanel, gbc);

		int answer = showConfirmDialog(null, overallPanel,
				"Import Component Profile", OK_CANCEL_OPTION);
		if (answer == OK_OPTION)
			doImport(registryPanel.getChosenRegistry(),
					profileLocation.getText(),
					permissionPanel.getChosenPermission(),
					licensePanel.getChosenLicense());
	}

	private void doImport(Registry chosenRegistry, String profileLocation,
			SharingPolicy permission, License license) {
		if (profileLocation == null || profileLocation.equals("")) {
			showMessageDialog(null, "Profile location cannot be blank",
					"Invalid URL", ERROR_MESSAGE);
		} else {
			try {
				if (chosenRegistry == null) {
					showMessageDialog(null, "Unable to determine registry",
							"Component Registry Problem", ERROR_MESSAGE);
					return;
				}
				Profile newProfile = makeProfile(new URL(profileLocation));
				String newName = newProfile.getName();
				for (Profile p : chosenRegistry.getComponentProfiles())
					if (p.getName().equals(newName)) {
						showMessageDialog(null, newName + " is already used",
								"Duplicate profile name", ERROR_MESSAGE);
						return;
					}
				chosenRegistry.addComponentProfile(newProfile, license,
						permission);
			} catch (MalformedURLException e) {
				showMessageDialog(null,
						profileLocation + " is not a valid URL", "Invalid URL",
						ERROR_MESSAGE);
			} catch (RegistryException e) {
				log.error("import profile failed", e);
				showMessageDialog(null,
						"Unable to save profile: " + e.getMessage(),
						"Registry Exception", ERROR_MESSAGE);
			}
		}
	}

}
