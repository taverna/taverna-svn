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
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.sf.taverna.t2.activities.rshell.RshellConnectionSettings;
import net.sf.taverna.t2.lang.ui.EditorKeySetUtil;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ListConfigurationComponent;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.MultiPageActivityConfigurationPanel;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ScriptConfigurationComponent;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ValidatingTextField;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ValidatingTextGroup;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.property.PropertyLiteral;
import uk.org.taverna.scufl2.api.property.PropertyResource;
import uk.org.taverna.scufl2.api.property.UnexpectedPropertyException;

/**
 * Component for configuring an Rshell activity.
 *
 * @author David Withers
 */
public class RshellConfigurationPanel extends MultiPageActivityConfigurationPanel {

	private static final Logger logger = Logger.getLogger(RshellConfigurationPanel.class);
	private static final URI ACTIVITY_TYPE = URI.create("http://ns.taverna.org.uk/2010/activity/rshell");

	private ServiceDescription serviceDescription;

	private ScriptConfigurationComponent scriptConfigurationComponent;
	private List<PropertyResource> inputPortDefinitions, outputPortDefinitions;
	private ValidatingTextGroup inputTextGroup, outputTextGroup;

	public RshellConfigurationPanel(Activity activity, ServiceDescription serviceDescription) {
		super(activity);
		this.serviceDescription = serviceDescription;
		initialise();
	}

	@Override
	protected void initialise() {
		inputPortDefinitions = getPortDefinitions(activity.getInputPorts());
		outputPortDefinitions = getPortDefinitions(activity.getOutputPorts());
		removeAllPages();
		addPage("Script", createScriptEditPanel());
		addPage("Input ports", createInputPanel());
		addPage("Output ports", createOutputPanel());
		addPage("Connection", createSettingsPanel());
	}

	@Override
	public boolean checkValues() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void noteConfiguration() {
		setPropertyResource(serviceDescription.getActivityConfiguration().getPropertyResource());
		setProperty("script", scriptConfigurationComponent.getScript());
		for (PropertyResource propertyResource : inputPortDefinitions) {
			propertyResource.addProperty(Scufl2Tools.PORT_DEFINITION.resolve("#inputPortDefinition"), propertyResource);
		}
		for (PropertyResource propertyResource : outputPortDefinitions) {
			propertyResource.addProperty(Scufl2Tools.PORT_DEFINITION.resolve("#outputPortDefinition"), propertyResource);
		}
	}

	private Component createScriptEditPanel() {
		Set<String> keywords = EditorKeySetUtil.loadKeySet(getClass().getResourceAsStream("keys.txt"));
		scriptConfigurationComponent = new ScriptConfigurationComponent(getProperty("script"), keywords, "Rshell", ".r");
		return scriptConfigurationComponent;
	}

	private Component createInputPanel() {
		inputTextGroup = new ValidatingTextGroup();
		ListConfigurationComponent<PropertyResource> inputPanel = new ListConfigurationComponent<PropertyResource>("Input Port", inputPortDefinitions) {
			@Override
			protected Component createItemComponent(PropertyResource portDefinition) {
				return new PortComponent(portDefinition, inputTextGroup);
			}

			@Override
			protected PropertyResource createDefaultItem() {
				PropertyResource portDefinition = new PropertyResource();
				portDefinition.setTypeURI(Scufl2Tools.PORT_DEFINITION.resolve("#InputPortDefinition"));
				portDefinition.addProperty(Scufl2Tools.PORT_DEFINITION.resolve("#name"), new PropertyLiteral("in"));
				portDefinition.addProperty(Scufl2Tools.PORT_DEFINITION.resolve("#depth"), new PropertyLiteral(0));
				return portDefinition;
			}
		};
		return inputPanel;
	}

	private Component createOutputPanel() {
		outputTextGroup = new ValidatingTextGroup();
		ListConfigurationComponent<PropertyResource> inputPanel = new ListConfigurationComponent<PropertyResource>("Output Port", outputPortDefinitions) {
			@Override
			protected Component createItemComponent(PropertyResource portDefinition) {
				return new PortComponent(portDefinition, outputTextGroup);
			}

			@Override
			protected PropertyResource createDefaultItem() {
				PropertyResource portDefinition = new PropertyResource();
				portDefinition.setTypeURI(Scufl2Tools.PORT_DEFINITION.resolve("#OutputPortDefinition"));
				portDefinition.addProperty(Scufl2Tools.PORT_DEFINITION.resolve("#name"), new PropertyLiteral("out"));
				portDefinition.addProperty(Scufl2Tools.PORT_DEFINITION.resolve("#depth"), new PropertyLiteral(0));
				return portDefinition;
			}
		};
		return inputPanel;
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

		JTextField hostnameField = new JTextField();
		JLabel hostnameLabel = new JLabel("Hostname");
		hostnameField.setSize(dimension);
		hostnameLabel.setSize(dimension);
		hostnameLabel.setLabelFor(hostnameField);
		RshellConnectionSettings connectionSettings = configuration
				.getConnectionSettings();

		hostnameField.setText(connectionSettings.getHost());

		JTextField portField = new JTextField();
		JLabel portLabel = new JLabel("Port");
		portField.setSize(dimension);
		portLabel.setSize(dimension);
		portLabel.setLabelFor(portField);
		portField.setText(Integer.toString(connectionSettings.getPort()));

		// "Set username and password" button
		ActionListener usernamePasswordListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (credManagerUI != null)
					credManagerUI.newPasswordForService(URI.create("rserve://"+hostnameField.getText()+":"+portField.getText())); // this is used as a key for the service in Credential Manager
			}
		};
		JButton setHttpUsernamePasswordButton = new JButton("Set username and password");
		setHttpUsernamePasswordButton.setSize(dimension);
		setHttpUsernamePasswordButton.addActionListener(usernamePasswordListener);

		JCheckBox keepSessionAliveCheckBox = new JCheckBox("Keep Session Alive");
		keepSessionAliveCheckBox.setSelected(connectionSettings
					.isKeepSessionAlive());

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

	class PortComponent extends JPanel {

		private ValidatingTextField nameField;
		private JComboBox comboBox;
		private PropertyLiteral nameProperty;
		private PropertyLiteral dataTypeProperty;
		private final ValidatingTextGroup validatingTextGroup;

		public PortComponent(PropertyResource portDefinition, ValidatingTextGroup validatingTextGroup) {
			this.validatingTextGroup = validatingTextGroup;
			try {
				SortedSet<PropertyLiteral> properties = portDefinition.getPropertiesAsLiterals(Scufl2Tools.PORT_DEFINITION.resolve("#name"));
				for (PropertyLiteral propertyLiteral : properties) {
					nameProperty = propertyLiteral;
					break;
				}
			} catch (UnexpectedPropertyException e) {
				logger.warn(e);
			}
			try {
				SortedSet<PropertyLiteral> properties = portDefinition.getPropertiesAsLiterals(Scufl2Tools.PORT_DEFINITION.resolve("#dataType"));
				for (PropertyLiteral propertyLiteral : properties) {
					dataTypeProperty = propertyLiteral;
					break;
				}
			} catch (UnexpectedPropertyException e) {
				logger.warn(e);
			}

			nameField = new ValidatingTextField(nameProperty.getLiteralValue());
			validatingTextGroup.addValidTextComponent(nameField);
			nameField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					nameProperty.setLiteralValue(nameField.getText());
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					nameProperty.setLiteralValue(nameField.getText());
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					nameProperty.setLiteralValue(nameField.getText());
				}
			});

			comboBox = new JComboBox();
			comboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dataTypeProperty.setLiteralValue(comboBox.getSelectedItem());
				}
			})

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			add(new JLabel("Name"), c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			add(nameField, c);
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0;
			add(new JLabel("Type"), c);
			add(comboBox, c);

		}

		public void removeNotify() {
			validatingTextGroup.removeTextComponent(nameField);
		}

	}
}
