package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.helper.Helper;

public class RapidMinerPluginPreferencesPlugin extends JPanel {
	
	JTextField repositoryLocationTextField;
	
	JTextField flPathToFloraTextField;
	
	JTextField flTempDirField;
	
    private static RapidMinerPluginConfiguration configuration = RapidMinerPluginConfiguration.getInstance();
	
	public RapidMinerPluginPreferencesPlugin() {
	
		initializeGUI();
		
	}
	
	private void initializeGUI() {
		// TODO Auto-generated method stub
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		
		// Title describing what kind of settings we are configuring here
		JTextArea descriptionText = new JTextArea("e-LICO settings for RapidAnalytics & Flora");
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		descriptionText.setEditable(false);
		descriptionText.setFocusable(false);
		descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));
		    
		JLabel rapidAnalyticsTitleLabel = new JLabel("RapidAnalytics");
		
		JLabel raUsernameLabel = new JLabel("username");
		JLabel raPasswordLabel = new JLabel("password");
		JLabel raRepositoryLocation = new JLabel("repository location url");
		
		repositoryLocationTextField = new JTextField();
		
		JLabel floraTitleLabel = new JLabel("Flora");
		
		JLabel floraPathLabel = new JLabel("flora location");
		JLabel floraTempDirLabel = new JLabel("temporary directory");
		
		flPathToFloraTextField = new JTextField();
		flTempDirField = new JTextField();
		
		// rapid analytics
		c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1d;
        c.weighty = 0d;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        // description
        add(descriptionText, c);

        // rapid analytics title
        c.insets = new Insets(10, 0, 10, 0);
        add(rapidAnalyticsTitleLabel, c);
        
        // rapid analytics username label and textfield	[DEPRECIATED]
	        /*
	        c.gridwidth = 1;
	        c.insets = new Insets(0, 20, 0, 0);
	        c.gridx = 0;
	        c.weightx = 0d;
	        add(raUsernameLabel, c);
	        c.insets = new Insets(0, 5, 0, 30);
	        c.gridx = 1;
	        add(usernameTextField, c);
	        
	        // password textfield
	        c.insets = new Insets(0, 20, 0, 0);
	        c.gridx = 0;
	        add(raPasswordLabel, c);
	        c.insets = new Insets(0, 5, 0, 30);
	        c.gridx = 1;
	        add(passwordTextField, c);
	        */
        
        // repository location
        c.insets = new Insets(0, 20, 0, 0);
        c.gridx = 0;
        add(raRepositoryLocation, c);
        c.insets = new Insets(0, 5, 0, 30);
        c.gridx = 1;
        add(repositoryLocationTextField, c);
        
        // flora title
        c.gridwidth = 2;
        c.gridx = 0;
        c.insets = new Insets(10, 0, 10, 0);
        add(floraTitleLabel, c);
        
        // flora path label text field
        c.gridwidth = 1;
        c.insets = new Insets(0, 20, 0, 0);
        c.gridx = 0;
        add(floraPathLabel, c);
        c.insets = new Insets(0, 5, 0, 30);
        c.gridx = 1;
        add(flPathToFloraTextField, c);
        
        // flora temp dir label and textfield
        c.insets = new Insets(0, 20, 0, 0);
        c.gridx = 0;
        c.weighty = 1;
        add(floraTempDirLabel, c);
        c.insets = new Insets(0, 5, 0, 30);
        c.gridx = 1;
        add(flTempDirField, c);
        
        // buttons
        c.gridx = 1;
        c.insets = new Insets(0, 10, 10, 10);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weighty = 1d;
        add(createButtonPanel(), c);

        setFields(configuration);
	}

	// button panel
	@SuppressWarnings("serial")
	private JPanel createButtonPanel() {
		final JPanel panel = new JPanel();

		/**
		 * The helpButton shows help about the current component
		 */
		JButton helpButton = new JButton(new AbstractAction("Help") {
			public void actionPerformed(ActionEvent arg0) {
				Helper.showHelp(panel);
			}
		});
		panel.add(helpButton);

		/**
		 * The resetButton changes the property values shown to those
		 * corresponding to the configuration currently applied.
		 */
		JButton resetButton = new JButton(new AbstractAction("Reset") {
			public void actionPerformed(ActionEvent arg0) {
				resetToDefaultValues();
				setFields(RapidMinerPluginConfiguration.getInstance());
			}
		});
		panel.add(resetButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		JButton applyButton = new JButton(new AbstractAction("Apply") {
			public void actionPerformed(ActionEvent arg0) {
				applySettings();
				setFields(RapidMinerPluginConfiguration.getInstance());
			}
		});
		panel.add(applyButton);

		return panel;
	}
	
	public void setFields(RapidMinerPluginConfiguration configuration) {
		
		repositoryLocationTextField.setText(configuration.getProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION));
		
		flPathToFloraTextField.setText(configuration.getProperty(RapidMinerPluginConfiguration.FL_LOCATION));
		
		flTempDirField.setText(configuration.getProperty(RapidMinerPluginConfiguration.FL_TEMPDIR));
		
	}
	
	public void resetToDefaultValues() {
		
		configuration.setProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION,  configuration.getDefaultProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION));

		configuration.setProperty(RapidMinerPluginConfiguration.FL_LOCATION,  configuration.getDefaultProperty(RapidMinerPluginConfiguration.FL_LOCATION));

		configuration.setProperty(RapidMinerPluginConfiguration.FL_TEMPDIR,  configuration.getDefaultProperty(RapidMinerPluginConfiguration.FL_TEMPDIR));		
	}
	
	private void applySettings() {
		
		configuration.setProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION, repositoryLocationTextField.getText());

		configuration.setProperty(RapidMinerPluginConfiguration.FL_LOCATION, flPathToFloraTextField.getText());

		configuration.setProperty(RapidMinerPluginConfiguration.FL_TEMPDIR, flTempDirField.getText());

	}
	
	// [testing] for testing purposes only
	public static void main(String [] args) {
		JDialog dialog = new JDialog();
        dialog.add(new RapidMinerPluginPreferencesPlugin());
        dialog.setModal(true);
        dialog.setSize(500, 400);
        dialog.setVisible(true);
        System.exit(0);
	}

}