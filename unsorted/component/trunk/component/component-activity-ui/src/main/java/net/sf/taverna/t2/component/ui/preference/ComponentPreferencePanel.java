package net.sf.taverna.t2.component.ui.preference;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.component.ui.util.Utils;

import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.helper.Helper;

public class ComponentPreferencePanel extends JPanel {
	
	private final Logger logger = Logger.getLogger(ComponentPreferencePanel.class);
	
	private RegistryTableModel tableModel = new RegistryTableModel();

	private JTable registryTable = new JTable(tableModel);
	
	public ComponentPreferencePanel() {
		super();
		initialize();
	}
	
	private void initialize() {

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		
		// Title describing what kind of settings we are configuring here
        JTextArea descriptionText = new JTextArea("Component registry management");
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setEditable(false);
        descriptionText.setFocusable(false);
        descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(descriptionText, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(10,0,0,0);
        
        registryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        registryTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        registryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(registryTable);
 //       registryTable.setFillsViewportHeight(true);
        
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        this.add(scrollPane, gbc);
        
		// Add buttons panel
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        this.add(createRegistryButtonPanel(), gbc);

        
		// Add buttons panel
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        this.add(createButtonPanel(), gbc);

		setFields();
       

	}
	
	private Component createRegistryButtonPanel() {
		final JPanel panel = new JPanel();

		JButton removeButton = new DeselectingButton(new AbstractAction("Remove registry") {
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = registryTable.getSelectedRow();
				if (selectedRow != -1) {
					tableModel.removeRow(selectedRow);
				}
			}
		});
		panel.add(removeButton);

		JButton addLocalButton = new DeselectingButton(new AbstractAction("Add local registry") {
			public void actionPerformed(ActionEvent arg0) {
				
				LocalRegistryPanel inputPanel = new LocalRegistryPanel();
				
				ValidatingUserInputDialog vuid = new ValidatingUserInputDialog("Add Local Component Registry", inputPanel);
				vuid.addTextComponentValidation(inputPanel.getRegistryNameField(), "Set the registry name", tableModel.getRegistryMap().keySet(), "Duplicate registry name", "[\\p{L}\\p{Digit}_.]+", "Invalid registry name");
				vuid.setSize(new Dimension(400, 250));
				if (vuid.show(ComponentPreferencePanel.this)) {
					File newDir = new File(inputPanel.getLocationField().getText());
					ComponentRegistry newRegistry = LocalComponentRegistry.getComponentRegistry(newDir);
					tableModel.insertRegistry(inputPanel.getRegistryNameField().getText(), newRegistry);
				}
			}
		});
		panel.add(addLocalButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		JButton addRemoteButton = new DeselectingButton(new AbstractAction("Add remote registry") {
			public void actionPerformed(ActionEvent arg0) {
				RemoteRegistryPanel inputPanel = new RemoteRegistryPanel();
				
				ValidatingUserInputDialog vuid = new ValidatingUserInputDialog("Add Remote Component Registry", inputPanel);
				vuid.addTextComponentValidation(inputPanel.getRegistryNameField(), "Set the registry name", tableModel.getRegistryMap().keySet(), "Duplicate registry name", "[\\p{L}\\p{Digit}_.]+", "Invalid registry name");
				vuid.addTextComponentValidation(inputPanel.getLocationField(), "Set the URL of the profile", null, "", Utils.URL_PATTERN, "Invalid URL");
				vuid.setSize(new Dimension(400, 250));
				if (vuid.show(ComponentPreferencePanel.this)) {
					ComponentRegistry newRegistry;
					try {
						newRegistry = MyExperimentComponentRegistry.getComponentRegistry(new URL(inputPanel.getLocationField().getText()));
						tableModel.insertRegistry(inputPanel.getRegistryNameField().getText(), newRegistry);
					} catch (MalformedURLException e) {
						logger.error(e);
					}
				}
			}
		});
		panel.add(addRemoteButton);

		return panel;
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
		JButton helpButton = new DeselectingButton(new AbstractAction("Help") {
			public void actionPerformed(ActionEvent arg0) {
				Helper.showHelp(panel);
			}
		});
		panel.add(helpButton);

		/**
		 * The resetButton changes the property values shown to those
		 * corresponding to the configuration currently applied.
		 */
		JButton resetButton = new DeselectingButton(new AbstractAction("Reset") {
			public void actionPerformed(ActionEvent arg0) {
				setFields();
			}
		});
		panel.add(resetButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		JButton applyButton = new DeselectingButton(new AbstractAction("Apply") {
			public void actionPerformed(ActionEvent arg0) {
				applySettings();
				setFields();
			}
		});
		panel.add(applyButton);

		return panel;
	}

	private void applySettings() {
		ComponentPreference pref = ComponentPreference.getInstance();
		pref.setRegistryMap(tableModel.getRegistryMap());
		if (validateFields()) {
			saveSettings();
		}
	}

	private void setFields() {
		ComponentPreference pref = ComponentPreference.getInstance();
		tableModel.setRegistryMap(pref.getRegistryMap());
	}

	private boolean validateFields() {
		return true;
	}

	private void saveSettings() {
		ComponentPreference pref = ComponentPreference.getInstance();
		pref.store();
	}


}
