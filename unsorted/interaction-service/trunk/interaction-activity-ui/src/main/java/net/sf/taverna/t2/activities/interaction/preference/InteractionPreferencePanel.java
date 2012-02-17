/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.preference;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.helper.Helper;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class InteractionPreferencePanel extends JPanel {
	
	private static Logger logger = Logger.getLogger(InteractionPreferencePanel.class);
	
	private static InteractionPreference pref = InteractionPreference.getInstance();
	
	private JTextField portField;
	private JTextField presentationDirectoryField;
	private JTextField hostnameField;
	
	/**
	 * The size of the field for the JTextFields.
	 */
	private static int TEXTFIELD_SIZE = 25;

	public InteractionPreferencePanel() {
		super();
		initComponents();
		setFields();
	}
	
	private void initComponents() {
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		
		// Title describing what kind of settings we are configuring here
        JTextArea descriptionText = new JTextArea("Interaction preference");
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
        this.add(descriptionText, gbc);
        
		portField = new JTextField(TEXTFIELD_SIZE);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel("Jetty port"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(portField, gbc);

		presentationDirectoryField = new JTextField(TEXTFIELD_SIZE);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel("Presentation directory"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(presentationDirectoryField, gbc);

		hostnameField = new JTextField(TEXTFIELD_SIZE);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel("Hostname"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(hostnameField, gbc);
        
		// Add buttons panel
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        this.add(createButtonPanel(), gbc);
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
				setFields();
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
				setFields();
			}
		});
		panel.add(applyButton);

		return panel;
	}

	protected void applySettings() {
		pref.setPort(portField.getText());
		pref.setPresentationDirectory(presentationDirectoryField.getText());
		pref.setHostname(hostnameField.getText());
		pref.store();
	}

	protected void setFields() {
		portField.setText(pref.getPort());
		presentationDirectoryField.setText(pref.getPresentationDirectory());
		hostnameField.setText(pref.getHostname());
	}
	
	
}
