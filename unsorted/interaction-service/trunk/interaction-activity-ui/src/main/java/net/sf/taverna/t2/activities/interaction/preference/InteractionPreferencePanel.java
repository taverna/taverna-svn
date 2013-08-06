/**
 *
 */
package net.sf.taverna.t2.activities.interaction.preference;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workbench.helper.Helper;
import net.sf.taverna.t2.workbench.ui.credentialmanager.password.GetPasswordDialog;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class InteractionPreferencePanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 8570351700324719204L;

	private static Logger logger = Logger
			.getLogger(InteractionPreferencePanel.class);

	private static InteractionPreference pref = InteractionPreference
			.getInstance();

	private String hostCache = pref.getHost();
	private String feedPathCache = pref.getFeedPath();
	private String webDavPathCache = pref.getWebDavPath();

	private JCheckBox useJettyField;
	private JTextField portField;
	private JTextField hostField;
	private JTextField feedPathField;
	private JTextField webDavPathField;
	private JCheckBox useUsernameField;
	// private JCheckBox useHttpsField;

	/**
	 * The size of the field for the JTextFields.
	 */
	private static int TEXTFIELD_SIZE = 25;

	public InteractionPreferencePanel() {
		super();
		this.initComponents();
		this.setFields();
	}

	private void initComponents() {
		final JPanel everything = new JPanel();
		everything.setLayout(new GridBagLayout());

		final GridBagConstraints gbc = new GridBagConstraints();

		// Title describing what kind of settings we are configuring here
		final JTextArea descriptionText = new JTextArea(
				"Interaction preference");
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		descriptionText.setEditable(false);
		descriptionText.setFocusable(false);
		descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		everything.add(descriptionText, gbc);

		this.useJettyField = new JCheckBox("Use internal Jetty");
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		everything.add(this.useJettyField, gbc);
		this.useJettyField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				InteractionPreferencePanel.this.updateSelectability();
			}
		});

		this.useUsernameField = new JCheckBox(
				"Secure with usename and password");
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		everything.add(this.useUsernameField, gbc);

		/*
		 * useHttpsField = new JCheckBox("Secure communication with HTTPS");
		 * gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc.fill =
		 * GridBagConstraints.NONE; gbc.insets = new Insets(10,0,0,0);
		 * everything.add(useHttpsField, gbc);
		 */
		this.hostField = new JTextField(TEXTFIELD_SIZE);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		everything.add(new JLabel("Host"), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		everything.add(this.hostField, gbc);

		this.portField = new JTextField(TEXTFIELD_SIZE);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		everything.add(new JLabel("Port"), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		everything.add(this.portField, gbc);

		this.feedPathField = new JTextField(TEXTFIELD_SIZE);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		everything.add(new JLabel("Feed Path"), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		everything.add(this.feedPathField, gbc);

		this.webDavPathField = new JTextField(TEXTFIELD_SIZE);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		everything.add(new JLabel("WebDav Path"), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		everything.add(this.webDavPathField, gbc);

		// Add buttons panel
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(10, 0, 0, 0);
		everything.add(this.createButtonPanel(), gbc);

		this.setLayout(new BorderLayout());
		this.add(everything, BorderLayout.NORTH);
	}

	/**
	 * Create the panel to contain the buttons
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	private JPanel createButtonPanel() {
		final JPanel panel = new JPanel();

		/**
		 * The helpButton shows help about the current component
		 */
		final JButton helpButton = new JButton(new AbstractAction("Help") {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Helper.showHelp(panel);
			}
		});
		panel.add(helpButton);

		/**
		 * The resetButton changes the property values shown to those
		 * corresponding to the configuration currently applied.
		 */
		final JButton resetButton = new JButton(new AbstractAction("Reset") {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				InteractionPreferencePanel.this.setFields();
			}
		});
		panel.add(resetButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		final JButton applyButton = new JButton(new AbstractAction("Apply") {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				InteractionPreferencePanel.this.applySettings();
				InteractionPreferencePanel.this.setFields();
			}
		});
		panel.add(applyButton);

		return panel;
	}

	@SuppressWarnings("deprecation")
	protected void applySettings() {
		pref.setUseJetty(this.useJettyField.isSelected());
		pref.setHost(this.hostField.getText());
		this.hostCache = this.hostField.getText();
		pref.setFeedPath(this.feedPathField.getText());
		this.feedPathCache = this.feedPathField.getText();
		pref.setWebDavPath(this.webDavPathField.getText());
		this.webDavPathCache = this.webDavPathField.getText();
		if (pref.getUseJetty()) {
			if (this.useUsernameField.isSelected()
					&& (!pref.getUseUsername() || !pref.getPort().equals(
							this.portField.getText()))) {
				try {
					final URI serviceURI = InteractionJetty
							.createServiceURI(this.portField.getText());

					final CredentialManager credMan = CredentialManager
							.getInstance();
					if (!credMan.hasUsernamePasswordForService(serviceURI)) {
						final GetPasswordDialog getPasswordDialog = new GetPasswordDialog(
								"Please enter the username and password\nto secure the interaction",
								true);
						getPasswordDialog.setLocationRelativeTo(this);
						getPasswordDialog.setVisible(true);

						final String username = getPasswordDialog.getUsername();
						final String password = getPasswordDialog.getPassword();
						credMan.saveUsernameAndPasswordForService(username,
								password, serviceURI.toString());
					}
				} catch (final URISyntaxException e) {
					logger.error(e);
				} catch (final CMException e) {
					logger.error(e);
				}
			}
		}
		pref.setPort(this.portField.getText());
		pref.setUseUsername(this.useUsernameField.isSelected());
		// pref.setUseHttps(useHttpsField.isSelected());
		pref.store();
		JOptionPane.showMessageDialog(this,
				"The changes will not take effect until Taverna is restarted",
				"Interaction Preference", JOptionPane.WARNING_MESSAGE);
	}

	protected void setFields() {
		this.useJettyField.setSelected(pref.getUseJetty());
		this.portField.setText(pref.getPort());
		this.hostField.setText(pref.getHost());
		this.feedPathField.setText(pref.getFeedPath());
		this.webDavPathField.setText(pref.getWebDavPath());
		this.useUsernameField.setSelected(pref.getUseUsername());
		// useHttpsField.setSelected(pref.getUseHttps());
		this.updateSelectability();
	}

	private void updateSelectability() {
		if (this.useJettyField.isSelected()) {
			this.hostCache = this.hostField.getText();
			this.hostField.setText(pref.getDefaultHost());
			this.hostField.setEnabled(false);

			this.feedPathCache = this.feedPathField.getText();
			this.feedPathField.setText(pref.getDefaultFeedPath());
			this.feedPathField.setEnabled(false);

			this.webDavPathCache = this.webDavPathField.getText();
			this.webDavPathField.setText(pref.getDefaultWebDavPath());
			this.webDavPathField.setEnabled(false);

			this.useUsernameField.setEnabled(true);

			// useHttpsField.setEnabled(true);

		} else {
			this.hostField.setText(this.hostCache);
			this.hostField.setEnabled(true);

			this.feedPathField.setText(this.feedPathCache);
			this.feedPathField.setEnabled(true);

			this.webDavPathField.setText(this.webDavPathCache);
			this.webDavPathField.setEnabled(true);

			this.useUsernameField.setEnabled(false);
			// useHttpsField.setEnabled(false);
		}
	}

}
