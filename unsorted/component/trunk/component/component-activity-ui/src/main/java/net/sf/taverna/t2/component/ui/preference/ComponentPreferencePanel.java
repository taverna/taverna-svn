package net.sf.taverna.t2.component.ui.preference;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateRegistry;
import static net.sf.taverna.t2.workbench.helper.Helper.showHelp;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;

import org.apache.log4j.Logger;

public class ComponentPreferencePanel extends JPanel {
	private static final String VALIDATION_MESSAGE = "Set the registry name";
	private static final String EXCEPTION_MESSAGE = "Unable to access registry at ";
	private static final String EXCEPTION_TITLE = "Component registry problem";
	private static final String INVALID_NAME = "Invalid registry name";
	private static final String DUPLICATE = "Duplicate registry name";
	private static final long serialVersionUID = 1310173658718093383L;

	private final Logger logger = Logger
			.getLogger(ComponentPreferencePanel.class);

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
		JTextArea descriptionText = new JTextArea(
				"Component registry management");
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
		gbc.insets = new Insets(10, 0, 0, 0);

		registryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		registryTable.setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		registryTable.setSelectionMode(SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(registryTable);
		// registryTable.setFillsViewportHeight(true);

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

		JButton removeButton = new DeselectingButton(new AbstractAction(
				"Remove registry") {
			private static final long serialVersionUID = 1506913889704140541L;

			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = registryTable.getSelectedRow();
				if (selectedRow != -1) {
					tableModel.removeRow(selectedRow);
				}
			}
		});
		panel.add(removeButton);

		JButton addLocalButton = new DeselectingButton(new AbstractAction(
				"Add local registry") {
			private static final long serialVersionUID = 855395154244911211L;

			public void actionPerformed(ActionEvent arg0) {
				LocalRegistryPanel inputPanel = new LocalRegistryPanel();

				ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
						"Add Local Component Registry", inputPanel);
				vuid.addTextComponentValidation(
						inputPanel.getRegistryNameField(), VALIDATION_MESSAGE,
						tableModel.getRegistryMap().keySet(), DUPLICATE,
						"[\\p{L}\\p{Digit}_.]+", INVALID_NAME);
				vuid.setSize(new Dimension(400, 250));
				if (vuid.show(ComponentPreferencePanel.this)) {
					String location = inputPanel.getLocationField().getText();
					File newDir = new File(location);
					try {
						tableModel.insertRegistry(inputPanel
								.getRegistryNameField().getText(),
								getLocalRegistry(newDir));
					} catch (MalformedURLException e) {
						showMessageDialog(null, EXCEPTION_MESSAGE + location,
								EXCEPTION_TITLE, ERROR_MESSAGE);
						logger.error(e);
					} catch (RegistryException e) {
						showMessageDialog(null, EXCEPTION_MESSAGE + location,
								EXCEPTION_TITLE, ERROR_MESSAGE);
						logger.error(e);
					}
				}
			}
		});
		panel.add(addLocalButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		JButton addRemoteButton = new DeselectingButton(new AbstractAction(
				"Add remote registry") {
			private static final long serialVersionUID = 7790264169310979116L;

			public void actionPerformed(ActionEvent arg0) {
				RemoteRegistryPanel inputPanel = new RemoteRegistryPanel();

				ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
						"Add Remote Component Registry", inputPanel);
				vuid.addTextComponentValidation(
						inputPanel.getRegistryNameField(), VALIDATION_MESSAGE,
						tableModel.getRegistryMap().keySet(), DUPLICATE,
						"[\\p{L}\\p{Digit}_.]+", INVALID_NAME);
				vuid.addTextComponentValidation(inputPanel.getLocationField(),
						"Set the URL of the profile", null, "",
						Utils.URL_PATTERN, "Invalid URL");
				vuid.setSize(new Dimension(400, 250));
				if (vuid.show(ComponentPreferencePanel.this)) {
					String location = inputPanel.getLocationField().getText();
					try {
						tableModel.insertRegistry(inputPanel
								.getRegistryNameField().getText(),
								getRemoteRegistry(location));
					} catch (MalformedURLException e) {
						showMessageDialog(null, EXCEPTION_MESSAGE + location,
								EXCEPTION_TITLE, ERROR_MESSAGE);
						logger.error(e);
					} catch (RegistryException e) {
						showMessageDialog(null, EXCEPTION_MESSAGE + location,
								EXCEPTION_TITLE, ERROR_MESSAGE);
						logger.error(e);
					}
				}
			}
		});
		panel.add(addRemoteButton);

		return panel;
	}

	Registry getLocalRegistry(File location) throws RegistryException,
			MalformedURLException {
		return calculateRegistry(location.toURI().toURL());
	}

	Registry getRemoteRegistry(String location) throws MalformedURLException,
			RegistryException {
		URL url = new URL(location);
		if (url.getProtocol() == null || url.getProtocol().equals("file"))
			throw new MalformedURLException(
					"may not use relative or local URLs for locating registry");
		return calculateRegistry(url);
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
				showHelp(panel);
			}
		});
		panel.add(helpButton);

		/**
		 * The resetButton changes the property values shown to those
		 * corresponding to the configuration currently applied.
		 */
		JButton resetButton = new DeselectingButton(
				new AbstractAction("Reset") {
					public void actionPerformed(ActionEvent arg0) {
						setFields();
					}
				});
		panel.add(resetButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		JButton applyButton = new DeselectingButton(
				new AbstractAction("Apply") {
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
