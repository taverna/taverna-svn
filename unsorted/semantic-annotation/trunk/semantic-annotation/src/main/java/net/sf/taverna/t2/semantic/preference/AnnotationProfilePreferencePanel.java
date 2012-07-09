/**
 * 
 */
package net.sf.taverna.t2.semantic.preference;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.semantic.configuration.DefaultAnnotationProfileConfiguration;
import net.sf.taverna.t2.workbench.helper.Helper;

/**
 * @author alanrw
 *
 */
public class AnnotationProfilePreferencePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5566237518531613382L;
	private JTextField defaultProfileField;
	/**
	 * The size of the field for the JTextFields.
	 */
	private static int TEXTFIELD_SIZE = 25;


	public AnnotationProfilePreferencePanel() {
		super();
		initComponents(DefaultAnnotationProfileConfiguration.getINSTANCE());
	}


	private void initComponents(DefaultAnnotationProfileConfiguration config) {
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		// Title describing what kind of settings we are configuring here
        JTextArea descriptionText = new JTextArea("Default annotation profile");
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

		
		defaultProfileField = new JTextField(TEXTFIELD_SIZE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel("Default annotation profile"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(defaultProfileField, gbc);

		// Add buttons panel
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        this.add(createButtonPanel(), gbc);

		setFields(config);
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
				setFields(DefaultAnnotationProfileConfiguration.getINSTANCE());
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
				setFields(DefaultAnnotationProfileConfiguration.getINSTANCE());
			}
		});
		panel.add(applyButton);

		return panel;
	}
	
	protected void applySettings() {
//		DefaultAnnotationProfileConfiguration conf = DefaultAnnotationProfileConfiguration.getINSTANCE();
		if (validateFields()) {
			saveSettings();
		}
	}


	private boolean validateFields() {
		return validateDefaultProfileField();
	}


	private boolean validateDefaultProfileField() {
			try {
				new URL(defaultProfileField.getText());
			} catch (MalformedURLException e) {
				return false;
			}
			return true;
	}


	private void saveSettings() {
		DefaultAnnotationProfileConfiguration conf = DefaultAnnotationProfileConfiguration.getINSTANCE();
		conf.setProperty(DefaultAnnotationProfileConfiguration.DEFAULT_PROFILE,
				defaultProfileField.getText());
	}


	/**
	 * Set the shown field values to those currently in use 
	 * (i.e. last saved configuration).
	 */
	private void setFields(DefaultAnnotationProfileConfiguration configurable) {
		populateFields(configurable);
	}


	private void populateFields(
			DefaultAnnotationProfileConfiguration configurable) {
		defaultProfileField.setText(configurable.getProperty(DefaultAnnotationProfileConfiguration.DEFAULT_PROFILE));
		
	}

}
