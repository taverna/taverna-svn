/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.help.CSH;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.activities.rshell.RShellPortSymanticTypeBean;
import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.rshell.RshellConnectionSettings;
import net.sf.taverna.t2.activities.rshell.RshellPortTypes.SymanticTypes;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.TextAreaDefaults;
import org.syntax.jedit.tokenmarker.JavaTokenMarker;

/**
 * Provides the configurable view for a {@link RshellActivity} through it's
 * {@link RshellActivityConfigurationBean}. Has 4 main tabs - Script, Ports &
 * Connection Settings. The {@link #inputViewList} contains the
 * {@link RshellInputViewer}s describing the input ports and
 * {@link #outputViewList} has the {@link RhellOutputViewer}s
 * 
 * Loosely based on the original Taverna 1.x RShell Config although mainly
 * copies the Taverna 2 Beanshell code
 * 
 * @author Ian Dunlop
 * @author Ingo Wassink
 * 
 */
public class RshellActivityConfigView extends JPanel {
	/** False for R1.5 and below, true for R1.6 and above */
	private boolean rVersion;
	/** The beanshell script */
	private JEditTextArea scriptText;
	/** A List of views over the input ports */
	private List<RshellInputViewer> inputViewList;
	/** A List of views over the output ports */
	private List<RshellOutputViewer> outputViewList;
	/** The activity which this view describes */
	private RshellActivity activity;
	/** the configuration bean used to configure the activity */
	private RshellActivityConfigurationBean configuration;
	/**
	 * Holds the state of the OK button in case a parent view wants to know
	 * whether the configuration is finished
	 */
	private ActionListener buttonClicked;
	/** Remembers where the next input should be placed in the view */
	private int inputGridy;
	/**
	 * An incremental name of newInputPort + this number is used to name new
	 * ports
	 */
	private int newInputPortNumber = 0;
	/**
	 * An incremental name of newOutputPort + this number is used to name new
	 * ports
	 */
	private int newOutputPortNumber = 0;
	/** Remembers where the next output should be placed in the view */
	private int outputGridy;
	/** Parent panel for the outputs */
	private JPanel outerOutputPanel;
	/** parent panel for the inputs */
	private JPanel outerInputPanel;
	private JButton button;

	private boolean configChanged = false;
	private JTextField hostnameField;
	private JTextField portField;
	private JTextField usernameField;
	private JTextField passwordField;
	private JCheckBox keepSessionAliveCheckBox;
	private JPanel settingsPanel;
	private File currentDirectory;

	// private JPanel mimes;

	/**
	 * Stores the {@link RshellActivity}, gets its
	 * {@link RshellActivityConfigurationBean}, sets the layout and calls
	 * {@link #initialise()} to get the view going
	 * 
	 * @param activity
	 *            the {@link RshellActivity} that the view is over
	 */
	public RshellActivityConfigView(RshellActivity activity) {
		this.activity = activity;
		configuration = activity.getConfiguration();
		setLayout(new GridBagLayout());
		initialise();
	}

	public RshellActivityConfigurationBean getConfiguration() {
		return configuration;
	}

	public boolean isConfigurationChanged() {
		return configChanged;
	}

	/**
	 * Adds a {@link JButton} which handles the reconfiguring of the
	 * {@link RshellActivity} through the altered
	 * {@link RshellActivityConfigurationBean}. Sets up the initial tabs -
	 * Script (also sets the initial value), Ports & Dependencies and their
	 * initial values through {@link #setDependencies()},
	 * {@link #setPortPanel()}
	 */
	private void initialise() {
		CSH
				.setHelpIDString(
						this,
						"net.sf.taverna.t2.activities.rshell.views.RshellActivityConfigView");
		setSize(500, 500);
		AbstractAction okAction = getOKAction();
		button = new JButton(okAction);
		button.setText("OK");
		button.setToolTipText("Click to configure with the new values");
		inputViewList = new ArrayList<RshellInputViewer>();
		outputViewList = new ArrayList<RshellOutputViewer>();
		setBorder(javax.swing.BorderFactory.createTitledBorder(null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		setSize(500, 500);
		final RshellActivityConfigurationBean configBean = activity
				.getConfiguration();

		JPanel scriptEditPanel = new JPanel(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Script", scriptEditPanel);
		tabbedPane.addTab("Ports", setPortPanel());
		createSettingsPanel();
		tabbedPane.addTab("Connection Settings", settingsPanel);

		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;

		outerConstraint.fill = GridBagConstraints.BOTH;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		add(tabbedPane, outerConstraint);

		scriptText = new JEditTextArea(new TextAreaDefaults());
		scriptText.setText(configBean.getScript());
		scriptText.setTokenMarker(new JavaTokenMarker());
		scriptText.setCaretPosition(0);
		scriptText.setPreferredSize(new Dimension(0, 0));
		scriptText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				// configBean.setScript(scriptText.getText());
			}
		});
		scriptEditPanel.add(scriptText, BorderLayout.CENTER);

		// JPanel scriptButtonsPanel = new JPanel();
		// BoxLayout boxLayout= new
		// BoxLayout(scriptButtonsPanel,BoxLayout.X_AXIS);
		// scriptButtonsPanel.setLayout(boxLayout);
		JButton loadRScriptButton = new JButton("Load script");
		loadRScriptButton.setToolTipText("Load an R script from a file");
		loadRScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readScriptFile();
			}
		});

		JButton clearScriptButton = new JButton("Clear script");
		clearScriptButton
				.setToolTipText("Clear current script from the edit area");
		clearScriptButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clearScript();
			}

		});

		final JCheckBox rVersionCheck = new JCheckBox("R1.6+");
		rVersionCheck.setEnabled(false);
		rVersionCheck.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// FIXME do something when this is selected - ie, the ports view
				// will change in some way, as will the connection used
				rVersion = rVersionCheck.isSelected();
			}

		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(rVersionCheck);
		buttonPanel.add(loadRScriptButton);
		buttonPanel.add(clearScriptButton);

		buttonPanel.add(button);
		JButton cancelButton = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				configChanged = false;
				buttonClicked.actionPerformed(e);
			}
		});

		outerConstraint.gridx = 0;
		outerConstraint.gridy = 1;
		outerConstraint.fill = GridBagConstraints.NONE;
		outerConstraint.anchor = GridBagConstraints.LINE_END;
		outerConstraint.gridy = 2;
		outerConstraint.weighty = 0;
		cancelButton.setText("Cancel");
		buttonPanel.add(cancelButton);
		add(buttonPanel, outerConstraint);
	}

	private void createSettingsPanel() {
		settingsPanel = new JPanel();
		BoxLayout mgr = new BoxLayout(settingsPanel, BoxLayout.Y_AXIS);
		settingsPanel.setLayout(mgr);

		Dimension dimension = new Dimension(0, 0);

		hostnameField = new JTextField();
		JLabel hostnameLabel = new JLabel("Hostname");
		hostnameField.setSize(dimension);
		hostnameLabel.setSize(dimension);
		hostnameLabel.setLabelFor(hostnameField);
		RshellConnectionSettings connectionSettings = configuration
				.getConnectionSettings();
		if (connectionSettings != null) {
			hostnameField.setText(connectionSettings.getHost());
		} else {
			hostnameField.setText(RshellConnectionSettings.DEFAULT_HOST);
		}

		portField = new JTextField();
		JLabel portLabel = new JLabel("Port");
		portField.setSize(dimension);
		portLabel.setSize(dimension);
		portLabel.setLabelFor(portField);
		if (connectionSettings != null) {

			portField.setText(Integer.toString(connectionSettings.getPort()));
		} else {
			portField.setText(Integer.toString(RshellConnectionSettings.DEFAULT_PORT));
		}

		usernameField = new JTextField();
		JLabel usernameLabel = new JLabel("Username");
		usernameField.setSize(dimension);
		usernameLabel.setSize(dimension);
		usernameLabel.setLabelFor(usernameField);
		if (connectionSettings != null) {

			usernameField.setText(connectionSettings.getUsername());
		}

		passwordField = new JTextField();
		JLabel passwordLabel = new JLabel("Password");
		passwordField.setSize(dimension);
		passwordLabel.setSize(dimension);
		passwordLabel.setLabelFor(passwordField);
		if (connectionSettings != null) {

			passwordField.setText(connectionSettings.getPassword());
		}

		keepSessionAliveCheckBox = new JCheckBox("Keep Session Alive");
		if (connectionSettings != null) {

			keepSessionAliveCheckBox.setSelected(connectionSettings
					.isKeepSessionAlive());
		}

		JPanel hostPanel = new JPanel();
		BoxLayout layout = new BoxLayout(hostPanel, BoxLayout.X_AXIS);
		hostPanel.setLayout(layout);

		hostPanel.add(hostnameLabel);
		hostPanel.add(hostnameField);

		JPanel portPanel = new JPanel();
		BoxLayout layout1 = new BoxLayout(portPanel, BoxLayout.X_AXIS);
		portPanel.setLayout(layout1);

		portPanel.add(portLabel);
		portPanel.add(portField);

		JPanel userPanel = new JPanel();
		BoxLayout layout2 = new BoxLayout(userPanel, BoxLayout.X_AXIS);
		userPanel.setLayout(layout2);

		userPanel.add(usernameLabel);
		userPanel.add(usernameField);

		JPanel passwordPanel = new JPanel();
		BoxLayout layout3 = new BoxLayout(passwordPanel, BoxLayout.X_AXIS);
		passwordPanel.setLayout(layout3);

		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordField);

		JPanel keepAlivePanel = new JPanel();
		BoxLayout layout4 = new BoxLayout(keepAlivePanel, BoxLayout.X_AXIS);
		keepAlivePanel.setLayout(layout4);

		keepAlivePanel.add(keepSessionAliveCheckBox);

		settingsPanel.add(hostPanel);
		settingsPanel.add(portPanel);
		settingsPanel.add(userPanel);
		settingsPanel.add(passwordPanel);
		settingsPanel.add(keepAlivePanel);

	}

	/**
	 * Creates a {@link JTabbedPane} with the Output and Input ports
	 * 
	 * @return a {@link JTabbedPane} with the ports
	 */
	private JTabbedPane setPortPanel() {
		JTabbedPane ports = new JTabbedPane();

		JPanel portEditPanel = new JPanel(new GridLayout(0, 2));

		GridBagConstraints panelConstraint = new GridBagConstraints();
		panelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		panelConstraint.gridx = 0;
		panelConstraint.gridy = 0;
		panelConstraint.weightx = 0.1;
		panelConstraint.weighty = 0.1;
		panelConstraint.fill = GridBagConstraints.BOTH;

		JScrollPane inputScroller = new JScrollPane(setInputPanel());
		portEditPanel.add(inputScroller, panelConstraint);

		panelConstraint.gridy = 1;
		ports.add("Inputs Ports", inputScroller);
		JScrollPane outputScroller = new JScrollPane(setOutputPanel());
		portEditPanel.add(outputScroller, panelConstraint);
		ports.add("Output Ports", outputScroller);

		return ports;
	}

	/**
	 * Loops through the {@link ActivityInputPortDefinitionBean} in the
	 * {@link RshellActivityConfigurationBean} and creates a
	 * {@link RshellInputViewer} for each one. Displays the name and a
	 * {@link JSpinner} to change the {@link SymanticTypes} for each one and a {@link JButton}
	 * to remove it. Currently the individual components from a
	 * {@link RshellInputViewer} are added rather than the
	 * {@link RshellInputViewer} itself
	 * 
	 * @return panel containing the view over the input ports
	 */
	private JPanel setInputPanel() {
		final JPanel inputEditPanel = new JPanel(new GridBagLayout());
		inputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Inputs"));

		final GridBagConstraints inputConstraint = new GridBagConstraints();
		inputConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		inputConstraint.gridx = 0;
		inputConstraint.gridy = 0;
		inputConstraint.weightx = 0.1;
		inputConstraint.fill = GridBagConstraints.BOTH;

		inputEditPanel.add(new JLabel("Name"), inputConstraint);
		inputConstraint.gridx = 1;
		inputEditPanel.add(new JLabel("Semantic Type"), inputConstraint);

		inputGridy = 1;
		inputConstraint.gridx = 0;
		for (ActivityInputPortDefinitionBean inputBean : configuration
				.getInputPortDefinitions()) {
			// FIXME refactor this into a method
			inputConstraint.gridy = inputGridy;
			final RshellInputViewer rshellInputViewer = new RshellInputViewer(
					inputBean, true, configuration.getInputSymanticTypes());
			inputViewList.add(rshellInputViewer);
			inputConstraint.gridx = 0;
			final JTextField nameField = rshellInputViewer.getNameField();
			inputConstraint.weightx = 0.1;
			inputEditPanel.add(nameField, inputConstraint);
			inputConstraint.weightx = 0.0;
			inputConstraint.gridx = 1;
			// final JSpinner depthSpinner = rshellInputViewer
			// .getDepthSpinner();
			final JComboBox semanticSelector = rshellInputViewer
					.getSemanticSelector();
			inputEditPanel.add(semanticSelector, inputConstraint);
			inputConstraint.gridx = 2;
			final JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					inputViewList.remove(rshellInputViewer);
					inputEditPanel.remove(nameField);
					inputEditPanel.remove(semanticSelector);
					inputEditPanel.remove(removeButton);
					inputEditPanel.revalidate();
					outerInputPanel.revalidate();
				}

			});
			inputEditPanel.add(removeButton, inputConstraint);
			inputGridy++;
		}
		outerInputPanel = new JPanel();
		outerInputPanel.setLayout(new GridBagLayout());
		GridBagConstraints outerPanelConstraint = new GridBagConstraints();
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0.1;
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerInputPanel.add(new JScrollPane(inputEditPanel),
				outerPanelConstraint);
		outerPanelConstraint.weighty = 0;
		JButton addInputPortButton = new JButton(new AbstractAction() {
			// FIXME refactor this into a method
			public void actionPerformed(ActionEvent e) {
				ActivityInputPortDefinitionBean bean = new ActivityInputPortDefinitionBean();
				bean.setAllowsLiteralValues(true);
				bean.setDepth(0);
				List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = new ArrayList<Class<? extends ExternalReferenceSPI>>();
				// handledReferenceSchemes.add(FileReference.class);
				bean.setHandledReferenceSchemes(handledReferenceSchemes);
				List<String> mimeTypes = new ArrayList<String>();
				mimeTypes.add("text/plain");
				bean.setMimeTypes(mimeTypes);

				String name2 = "in" + newInputPortNumber;
				boolean nameExists = true;
				while (nameExists == true) {
					nameExists = inputPortNameExists(name2, activity
							.getInputPorts());
					if (nameExists) {
						newInputPortNumber++;
						name2 = "in" + newInputPortNumber;
					}
				}

				bean.setName(name2);
				newInputPortNumber++;
				bean.setTranslatedElementType(String.class);
				inputConstraint.gridy = inputGridy;
				final RshellInputViewer rshellInputViewer = new RshellInputViewer(
						bean, true, configuration.getInputSymanticTypes());
				inputViewList.add(rshellInputViewer);
				inputConstraint.weightx = 0.1;
				inputConstraint.gridx = 0;
				final JTextField nameField = rshellInputViewer.getNameField();
				inputEditPanel.add(nameField, inputConstraint);
				inputConstraint.weightx = 0;
				inputConstraint.gridx = 1;
				final JComboBox semanticSelector = rshellInputViewer
						.getSemanticSelector();
				inputEditPanel.add(semanticSelector, inputConstraint);
				inputConstraint.gridx = 2;
				final JButton removeButton = new JButton("remove");
				removeButton.addActionListener(new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
						inputViewList.remove(rshellInputViewer);
						inputEditPanel.remove(nameField);
						inputEditPanel.remove(semanticSelector);
						inputEditPanel.remove(removeButton);
						inputEditPanel.revalidate();
						outerInputPanel.revalidate();
					}

				});
				inputEditPanel.add(removeButton, inputConstraint);
				inputEditPanel.revalidate();

				inputGridy++;
			}

		});
		addInputPortButton.setText("Add Port");
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		JPanel filler = new JPanel();
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;

		buttonPanel.add(filler, outerPanelConstraint);

		outerPanelConstraint.weightx = 0;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 1;
		outerPanelConstraint.gridy = 0;

		buttonPanel.add(addInputPortButton, outerPanelConstraint);

		outerPanelConstraint.weightx = 0;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 1;
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerInputPanel.add(buttonPanel, outerPanelConstraint);

		return outerInputPanel;
	}

	/**
	 * Loops through the {@link ActivityInputPortDefinitionBean} in the
	 * {@link RshellActivityConfigurationBean} and creates a
	 * {@link RshellOutputViewer} for each one. Displays the name and a
	 * {@link JSpinner} to change the {@link SymanticTypes} for each one and
	 * a {@link JButton} to remove it. Currently the individual components from
	 * a {@link RshellOutputViewer} are added rather than the
	 * {@link RshellOutputViewer} itself
	 * 
	 * @return the panel containing the view of the output ports
	 */
	private JPanel setOutputPanel() {
		final JPanel outputEditPanel = new JPanel(new GridBagLayout());
		outputEditPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Outputs"));

		final GridBagConstraints outputConstraint = new GridBagConstraints();
		outputConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outputConstraint.gridx = 0;
		outputConstraint.gridy = 0;
		outputConstraint.weightx = 0.1;
		outputConstraint.weighty = 0.1;
		outputConstraint.fill = GridBagConstraints.BOTH;
		outputConstraint.weighty = 0;
		outputEditPanel.add(new JLabel("Name"), outputConstraint);
		outputConstraint.gridx = 1;
		outputEditPanel.add(new JLabel("Semantic Type"), outputConstraint);

		outputGridy = 1;
		outputConstraint.gridx = 0;
		for (ActivityOutputPortDefinitionBean outputBean : configuration
				.getOutputPortDefinitions()) {
			// FIXME refactor this into a method
			outputConstraint.gridy = outputGridy;
			final RshellOutputViewer rshellOutputViewer = new RshellOutputViewer(
					outputBean, true, configuration.getOutputSymanticTypes());
			outputViewList.add(rshellOutputViewer);
			outputConstraint.gridx = 0;
			outputConstraint.weightx = 0.1;
			final JTextField nameField = rshellOutputViewer.getNameField();
			outputEditPanel.add(nameField, outputConstraint);
			outputConstraint.weightx = 0;
			outputConstraint.gridx = 1;
			final JSpinner depthSpinner = rshellOutputViewer.getDepthSpinner();
			final JComboBox semanticTypeSelector = rshellOutputViewer
					.getSemanticTypeSelector();
			outputEditPanel.add(semanticTypeSelector, outputConstraint);
			outputConstraint.gridx = 2;
			outputConstraint.gridx = 4;
			final JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					outputViewList.remove(rshellOutputViewer);
					outputEditPanel.remove(nameField);
					outputEditPanel.remove(semanticTypeSelector);
					outputEditPanel.remove(removeButton);
					outputEditPanel.revalidate();
					outerOutputPanel.revalidate();
				}

			});
			outputEditPanel.add(removeButton, outputConstraint);
			outputGridy++;
		}
		outerOutputPanel = new JPanel();
		outerOutputPanel.setLayout(new GridBagLayout());
		GridBagConstraints outerPanelConstraint = new GridBagConstraints();
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0.1;
		outerOutputPanel.add(new JScrollPane(outputEditPanel),
				outerPanelConstraint);
		outerPanelConstraint.weighty = 0;
		JButton addOutputPortButton = new JButton(new AbstractAction() {
			// FIXME refactor this into a method
			public void actionPerformed(ActionEvent e) {
				try {
					ActivityOutputPortDefinitionBean bean = new ActivityOutputPortDefinitionBean();
					bean.setDepth(0);
					bean.setGranularDepth(0);
					List<String> mimeTypes = new ArrayList<String>();
					mimeTypes.add("text/plain");
					bean.setMimeTypes(mimeTypes);
					String name2 = "out" + newOutputPortNumber;
					boolean nameExists = true;
					while (nameExists == true) {
						nameExists = outputPortNameExists(name2, activity
								.getOutputPorts());
						if (nameExists) {
							newOutputPortNumber++;
							name2 = "out" + newOutputPortNumber;
						}
					}
					bean.setName(name2);
					final RshellOutputViewer rshellOutputViewer = new RshellOutputViewer(
							bean, true, configuration.getOutputSymanticTypes());
					outputViewList.add(rshellOutputViewer);
					outputConstraint.gridy = outputGridy;
					outputConstraint.gridx = 0;
					final JTextField nameField = rshellOutputViewer
							.getNameField();
					outputConstraint.weightx = 0.1;
					outputEditPanel.add(nameField, outputConstraint);
					outputConstraint.gridx = 1;
					outputConstraint.weightx = 0;
					final JComboBox semanticTypeSelector = rshellOutputViewer
							.getSemanticTypeSelector();
					outputEditPanel.add(semanticTypeSelector, outputConstraint);
					outputConstraint.gridx = 2;
					outputConstraint.gridx = 4;
					final JButton removeButton = new JButton("remove");
					removeButton.addActionListener(new AbstractAction() {

						public void actionPerformed(ActionEvent e) {
							outputViewList.remove(rshellOutputViewer);
							outputEditPanel.remove(nameField);
							outputEditPanel.remove(semanticTypeSelector);
							outputEditPanel.remove(removeButton);
							outputEditPanel.revalidate();
						}

					});
					outputEditPanel.add(removeButton, outputConstraint);
					outputEditPanel.revalidate();
					newOutputPortNumber++;

					outputGridy++;
				} catch (Exception e1) {
					// TODO throw it, log it??
				}
			}

		});
		addOutputPortButton.setText("Add Port");
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		JPanel filler = new JPanel();
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;

		buttonPanel.add(filler, outerPanelConstraint);

		outerPanelConstraint.weightx = 0;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 1;
		outerPanelConstraint.gridy = 0;

		buttonPanel.add(addOutputPortButton, outerPanelConstraint);

		outerPanelConstraint.weightx = 0;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 1;
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerOutputPanel.add(buttonPanel, outerPanelConstraint);
		outerPanelConstraint.gridx = 1;
		outerPanelConstraint.gridy = 0;

		return outerOutputPanel;
	}

	public void setButtonClickedListener(ActionListener listener) {
		buttonClicked = listener;
	}

	/**
	 * Calls
	 * {@link RshellActivity#configure(RshellActivityConfigurationBean)}
	 * using a {@link RshellActivityConfigurationBean} set with the new
	 * values in the view. After setting the values it uses the
	 * {@link #buttonClicked} {@link ActionListener} to tell any listeners that
	 * the new values have been set (primarily used to tell any parent
	 * components to remove the frames containing this panel)
	 * 
	 * @return the action which occurs when the OK button is clicked
	 */
	private AbstractAction getOKAction() {
		return new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				List<ActivityInputPortDefinitionBean> inputBeanList = new ArrayList<ActivityInputPortDefinitionBean>();
				RshellActivityConfigurationBean rshellActivityConfigurationBean = new RshellActivityConfigurationBean();
				List<RShellPortSymanticTypeBean> inputSemanticTypes = new ArrayList<RShellPortSymanticTypeBean>();
				List<RShellPortSymanticTypeBean> outputSemanticTypes = new ArrayList<RShellPortSymanticTypeBean>();
				for (RshellInputViewer inputView : inputViewList) {
					ActivityInputPortDefinitionBean activityInputPortDefinitionBean = new ActivityInputPortDefinitionBean();
					activityInputPortDefinitionBean.setName(inputView
							.getNameField().getText());
					inputBeanList.add(activityInputPortDefinitionBean);
					RShellPortSymanticTypeBean bean = new RShellPortSymanticTypeBean();
					bean.setName(inputView.getNameField().getText());
					bean.setSymanticType((SymanticTypes) inputView
							.getSemanticSelector().getSelectedItem());
					inputSemanticTypes.add(bean);
				}

				List<ActivityOutputPortDefinitionBean> outputBeanList = new ArrayList<ActivityOutputPortDefinitionBean>();
				for (RshellOutputViewer outputView : outputViewList) {
					ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
					activityOutputPortDefinitionBean.setName(outputView
							.getNameField().getText());
					activityOutputPortDefinitionBean.setMimeTypes(new ArrayList<String>());
					outputBeanList.add(activityOutputPortDefinitionBean);
					RShellPortSymanticTypeBean bean = new RShellPortSymanticTypeBean();
					bean.setSymanticType((SymanticTypes) outputView
							.getSemanticTypeSelector().getSelectedItem());
					bean.setName(outputView.getNameField().getText());
					outputSemanticTypes.add(bean);
				}
				rshellActivityConfigurationBean
						.setInputSymanticTypes(inputSemanticTypes);
				rshellActivityConfigurationBean
						.setOutputSymanticTypes(outputSemanticTypes);
				rshellActivityConfigurationBean.setScript(scriptText.getText());
				rshellActivityConfigurationBean
						.setInputPortDefinitions(inputBeanList);
				rshellActivityConfigurationBean
						.setOutputPortDefinitions(outputBeanList);
				RshellConnectionSettings connectionSettings = new RshellConnectionSettings();

				connectionSettings.setUsername(usernameField.getText());
				connectionSettings.setHost(hostnameField.getText());
				connectionSettings.setPassword(passwordField.getText());
				connectionSettings.setKeepSessionAlive(keepSessionAliveCheckBox
						.isSelected());
				connectionSettings.setPort(portField.getText());
				connectionSettings.setNewRVersion(rVersion);
				rshellActivityConfigurationBean
						.setConnectionSettings(connectionSettings);

				configuration = rshellActivityConfigurationBean;
				configChanged = true;
				setVisible(false);
				buttonClicked.actionPerformed(e);
			}

		};
	}

	/**
	 * Check the proposed port name against the set of input ports that the
	 * activity has
	 * 
	 * @param name
	 * @param set
	 * @return
	 */
	private boolean inputPortNameExists(String name, Set<ActivityInputPort> set) {
		for (Port port : set) {
			if (name.equals(port.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the proposed port name against the set of output ports that the
	 * activity has
	 * 
	 * @param name
	 * @param set
	 * @return
	 */
	private boolean outputPortNameExists(String name, Set<OutputPort> set) {
		for (Port port : set) {
			if (name.equals(port.getName())) {
				return true;
			}
		}
		return false;
	}

	private void readScriptFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".r");
			}

			@Override
			public String getDescription() {
				return ".r (R files)";
			}

		});
		fileChooser.setAcceptAllFileFilterUsed(true);
		if (currentDirectory != null) {
			fileChooser.setCurrentDirectory(currentDirectory);
		}
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			currentDirectory = fileChooser.getCurrentDirectory();
			File selectedFile = fileChooser.getSelectedFile();

			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						selectedFile));

				String line;
				StringBuffer buffer = new StringBuffer();
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
					buffer.append("\n");
				}
				reader.close();

				scriptText.setText(buffer.toString());

			} catch (FileNotFoundException ffe) {
				JOptionPane.showMessageDialog(this, "File '"
						+ selectedFile.getName() + "' not found",
						"File not found", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Can not read file '"
						+ selectedFile.getName() + "'", "Can not read file",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	/**
	 * Method for clearing the script
	 * 
	 */
	private void clearScript() {
		if (JOptionPane.showConfirmDialog(this,
				"Do you really want to clear the script?",
				"Clearing the script", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			scriptText.setText("");
		}

	}

}
