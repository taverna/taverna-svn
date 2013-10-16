/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.rshell.views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.taverna.t2.activities.rshell.RshellPortTypes;
import net.sf.taverna.t2.activities.rshell.RshellPortTypes.DataTypes;
import net.sf.taverna.t2.lang.ui.EditorKeySetUtil;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workbench.ui.credentialmanager.CredentialManagerUI;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityPortConfiguration;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ListConfigurationComponent;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.MultiPageActivityConfigurationPanel;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ScriptConfigurationComponent;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ValidatingTextField;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ValidatingTextGroup;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Component for configuring an Rshell activity.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class RshellConfigurationPanel extends MultiPageActivityConfigurationPanel {

	private ScriptConfigurationComponent scriptConfigurationComponent;
	private ValidatingTextGroup inputTextGroup, outputTextGroup;
	private CredentialManagerUI credManagerUI;
	private CredentialManager credentialManager;
	private JTextField hostnameField;
	private JTextField portField;
	private JCheckBox keepSessionAliveCheckBox;

	public RshellConfigurationPanel(Activity activity, CredentialManager credentialManager) {
		super(activity);
		this.credentialManager = credentialManager;
		initialise();
	}

	@Override
	protected void initialise() {
		json = getConfiguration().getJson().deepCopy();
		List<ActivityPortConfiguration> inputPorts = getInputPorts();
		inputPorts.clear();
		for (InputActivityPort activityPort : getActivity().getInputPorts()) {
			inputPorts.add(new RshellActivityPortConfiguration(activityPort, getInputDataType(activityPort.getName())));
		}
		List<ActivityPortConfiguration> outputPorts = getOutputPorts();
		outputPorts.clear();
		for (OutputActivityPort activityPort : getActivity().getOutputPorts()) {
			outputPorts.add(new RshellActivityPortConfiguration(activityPort, getOutputDataType(activityPort.getName())));
		}
		removeAllPages();
		addPage("Script", createScriptEditPanel());
		addPage("Input ports", createInputPanel());
		addPage("Output ports", createOutputPanel());
		addPage("Connection", createSettingsPanel());
	}

	@Override
	public boolean checkValues() {
		return true;
	}

	@Override
	public void noteConfiguration() {
		setProperty("script", scriptConfigurationComponent.getScript());
		ObjectNode json = getJson();

		ObjectNode connection = json.objectNode();
		connection.put("hostname", hostnameField.getText());
		try {
			connection.put("port", Integer.parseInt(portField.getText()));
		} catch (NumberFormatException e) {
			connection.put("port", json.get("connection").get("port").asInt());
		}
		connection.put("keepSessionAlive", keepSessionAliveCheckBox.isSelected());
		json.put("connection", connection);

		List<ActivityPortConfiguration> inputPorts = getInputPorts();
		if (inputPorts.isEmpty()) {
			json.remove("inputTypes");
		} else {
			ArrayNode DataTypes = json.arrayNode();
			for (ActivityPortConfiguration activityPortConfiguration : inputPorts) {
				RshellActivityPortConfiguration portConfiguration = (RshellActivityPortConfiguration) activityPortConfiguration;
				ObjectNode semanticType = json.objectNode();
				DataTypes.add(semanticType);
				semanticType.put("port", portConfiguration.getName());
				semanticType.put("dataType", portConfiguration.getDataType().name());
			}
			json.put("inputTypes", DataTypes);
		}

		List<ActivityPortConfiguration> outputPorts = getOutputPorts();
		if (outputPorts.isEmpty()) {
			json.remove("outputTypes");
		} else {
			ArrayNode DataTypes = json.arrayNode();
			for (ActivityPortConfiguration activityPortConfiguration : outputPorts) {
				RshellActivityPortConfiguration portConfiguration = (RshellActivityPortConfiguration) activityPortConfiguration;
				ObjectNode semanticType = json.objectNode();
				DataTypes.add(semanticType);
				semanticType.put("port", portConfiguration.getName());
				semanticType.put("dataType", portConfiguration.getDataType().name());
			}
			json.put("outputTypes", DataTypes);
		}
	}

	private Component createScriptEditPanel() {
		Set<String> keywords = EditorKeySetUtil.loadKeySet(getClass().getResourceAsStream("keys.txt"));
		Set<String> ports = new HashSet<>();
		for (InputActivityPort ip : getActivity().getInputPorts()) {
			ports.add(ip.getName());
		}
		for (OutputActivityPort op : getActivity().getOutputPorts()) {
			ports.add(op.getName());
		}
		scriptConfigurationComponent = new ScriptConfigurationComponent(getProperty("script"), keywords, ports, "Rshell", ".r");
		return scriptConfigurationComponent;
	}

	private Component createInputPanel() {
		inputTextGroup = new ValidatingTextGroup();
		ListConfigurationComponent<ActivityPortConfiguration> inputPanel = new ListConfigurationComponent<ActivityPortConfiguration>(
				"Input Port", getInputPorts()) {
			@Override
			protected Component createItemComponent(ActivityPortConfiguration port) {
				return new PortComponent(port, inputTextGroup);
			}

			@Override
			protected ActivityPortConfiguration createDefaultItem() {
				return new RshellActivityPortConfiguration("in", DataTypes.STRING);
			}
		};
		return inputPanel;
	}

	private Component createOutputPanel() {
		outputTextGroup = new ValidatingTextGroup();
		ListConfigurationComponent<ActivityPortConfiguration> outputPanel = new ListConfigurationComponent<ActivityPortConfiguration>(
				"Output Port", getOutputPorts()) {
			@Override
			protected Component createItemComponent(ActivityPortConfiguration port) {
				return new PortComponent(port, outputTextGroup);
			}

			@Override
			protected ActivityPortConfiguration createDefaultItem() {
				return new RshellActivityPortConfiguration("out", DataTypes.STRING);
			}
		};
		return outputPanel;
	}

	private Component createSettingsPanel() {
		JPanel settingsPanel = new JPanel(new GridBagLayout());

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.weightx = 0.0;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = 0;
		labelConstraints.fill = GridBagConstraints.NONE;
		labelConstraints.anchor = GridBagConstraints.LINE_START;

		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.weightx = 1.0;
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 0;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.weightx = 1.0;
		buttonConstraints.gridx = 1;
		buttonConstraints.gridy = 2;
		buttonConstraints.fill = GridBagConstraints.NONE;
		buttonConstraints.anchor = GridBagConstraints.WEST;

		Dimension dimension = new Dimension(0, 0);

		hostnameField = new JTextField();
		JLabel hostnameLabel = new JLabel("Hostname");
		hostnameField.setSize(dimension);
		hostnameLabel.setSize(dimension);
		hostnameLabel.setLabelFor(hostnameField);
		JsonNode connectionSettings = getJson().get("connection");

		hostnameField.setText(connectionSettings.get("hostname").textValue());

		portField = new JTextField();
		JLabel portLabel = new JLabel("Port");
		portField.setSize(dimension);
		portLabel.setSize(dimension);
		portLabel.setLabelFor(portField);
		portField.setText(connectionSettings.get("port").asText());

		// "Set username and password" button
		ActionListener usernamePasswordListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (credManagerUI == null) {
					credManagerUI = new CredentialManagerUI(credentialManager);
				}
				credManagerUI.newPasswordForService(URI.create("rserve://"+hostnameField.getText()+":"+portField.getText())); // this is used as a key for the service in Credential Manager
			}
		};
		JButton setHttpUsernamePasswordButton = new JButton("Set username and password");
		setHttpUsernamePasswordButton.setSize(dimension);
		setHttpUsernamePasswordButton.addActionListener(usernamePasswordListener);

		keepSessionAliveCheckBox = new JCheckBox("Keep Session Alive");
		keepSessionAliveCheckBox.setSelected(connectionSettings
					.get("keepSessionAlive").booleanValue());

		settingsPanel.add(hostnameLabel, labelConstraints);
		labelConstraints.gridy++;
		settingsPanel.add(hostnameField, fieldConstraints);
		fieldConstraints.gridy++;

		settingsPanel.add(portLabel, labelConstraints);
		labelConstraints.gridy++;
		settingsPanel.add(portField, fieldConstraints);
		fieldConstraints.gridy++;

		settingsPanel.add(setHttpUsernamePasswordButton, buttonConstraints);
		buttonConstraints.gridy++;

		fieldConstraints.gridy++;
		settingsPanel.add(keepSessionAliveCheckBox, fieldConstraints);
		fieldConstraints.gridy++;

		return settingsPanel;
	}

	private DataTypes getInputDataType(String name) {
		for (JsonNode jsonNode : getJson().get("inputTypes")) {
			if (jsonNode.get("port").textValue().equals(name)) {
				return DataTypes.valueOf(jsonNode.get("dataType").textValue());
			}
		}
		return null;
	}

	private DataTypes getOutputDataType(String name) {
		for (JsonNode jsonNode : getJson().get("outputTypes")) {
			if (jsonNode.get("port").textValue().equals(name)) {
				return DataTypes.valueOf(jsonNode.get("dataType").textValue());
			}
		}
		return null;
	}

	class PortComponent extends JPanel {

		private ValidatingTextField nameField;
		private JComboBox<DataTypes> dataTypeselector;
		private final ValidatingTextGroup validatingTextGroup;

		public PortComponent(final ActivityPortConfiguration portConfiguration,
				ValidatingTextGroup validatingTextGroup) {
			final RshellActivityPortConfiguration rshellPortConfiguration = (RshellActivityPortConfiguration) portConfiguration;
			this.validatingTextGroup = validatingTextGroup;

			nameField = new ValidatingTextField(portConfiguration.getName());
			validatingTextGroup.addValidTextComponent(nameField);
			nameField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					portConfiguration.setName(nameField.getText());
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					portConfiguration.setName(nameField.getText());
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					portConfiguration.setName(nameField.getText());
				}
			});

			dataTypeselector = new JComboBox<DataTypes>(RshellPortTypes.getInputTypes());
			dataTypeselector.setSelectedItem(rshellPortConfiguration.getDataType());
			dataTypeselector.setRenderer(new PortTypesListCellRenderer());
			dataTypeselector.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					rshellPortConfiguration.setDataType((DataTypes) dataTypeselector.getSelectedItem());
				}
			});

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			add(new JLabel("Port name"), c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			add(nameField, c);
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0;
			add(new JLabel("Type"), c);
			add(dataTypeselector, c);

		}

		public void removeNotify() {
			validatingTextGroup.removeTextComponent(nameField);
		}

	}

}
