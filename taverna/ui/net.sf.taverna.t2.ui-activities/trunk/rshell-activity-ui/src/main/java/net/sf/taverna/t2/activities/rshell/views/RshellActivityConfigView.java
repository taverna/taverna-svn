/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester   
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import net.sf.taverna.t2.activities.rshell.RShellPortSymanticTypeBean;
import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.rshell.RshellConnectionSettings;
import net.sf.taverna.t2.activities.rshell.RshellPortTypes.SemanticTypes;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.lang.ui.FileTools;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.apache.log4j.Logger;

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
public class RshellActivityConfigView extends ActivityConfigurationPanel<RshellActivity, RshellActivityConfigurationBean> {
	
	private static final String EXTENSION = ".r";

	private static Logger logger = Logger.getLogger(RshellActivityConfigView.class);

	/** False for R1.5 and below, true for R1.6 and above */
	private boolean rVersion;
	/** The beanshell script */
	private JEditorPane scriptTextArea;
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
	private JTextField hostnameField;
	private JTextField portField;
	private JTextField usernameField;
	private JTextField passwordField;
	private JCheckBox keepSessionAliveCheckBox;
	private JPanel settingsPanel;
	private JTabbedPane tabbedPane;
	private JTabbedPane ports;

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
		initialise();
	}

	public RshellActivityConfigurationBean getConfiguration() {
		return configuration;
	}
	
	public boolean isConfigurationChanged() {
		String configurationString = convertBeanToString(activity.getConfiguration());
		return (!convertBeanToString(calculateConfiguration()).equals(configurationString));
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
		configuration = activity.getConfiguration();
		setLayout(new GridBagLayout());
		inputViewList = new ArrayList<RshellInputViewer>();
		outputViewList = new ArrayList<RshellOutputViewer>();
//		setBorder(javax.swing.BorderFactory.createTitledBorder(null, null,
//				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
//				javax.swing.border.TitledBorder.DEFAULT_POSITION,
//				new java.awt.Font("Lucida Grande", 1, 12)));
		setSize(500, 500);
		final RshellActivityConfigurationBean configBean = activity
				.getConfiguration();

		JPanel scriptEditPanel = new JPanel(new BorderLayout());
//		scriptEditPanel.setBorder(BorderFactory.createTitledBorder(
//				BorderFactory.createEtchedBorder(), "R Script"));



		scriptTextArea = new JTextPane();

		scriptTextArea.setDocument(new RshellDocument());
		scriptTextArea.setText(configBean.getScript());
		scriptTextArea.setCaretPosition(0);
		scriptTextArea.setPreferredSize(new Dimension(0, 0));
		scriptTextArea.setEditorKit( new NoWrapEditorKit() );

		for (ActivityInputPortDefinitionBean ip : configuration.getInputPortDefinitions()) {
			String name = ip.getName();
			((RshellDocument) scriptTextArea.getDocument()).addPort(name);
		}
		for (ActivityOutputPortDefinitionBean op : configuration.getOutputPortDefinitions()) {
			String name = op.getName();
			((RshellDocument) scriptTextArea.getDocument()).addPort(name);
		}

		JScrollPane scrollPane = new JScrollPane( scriptTextArea );
		scrollPane.setPreferredSize( new Dimension( 200, 100 ) );
		
		scriptEditPanel.add(scrollPane, BorderLayout.CENTER);

		JButton loadRScriptButton = new JButton("Load script");
		loadRScriptButton.setToolTipText("Load an R script from a file");
		loadRScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newScript = FileTools.readStringFromFile(RshellActivityConfigView.this);
				if (newScript != null) {
					scriptTextArea.setText(newScript);
				}
			}
		});

		JButton saveRScriptButton = new JButton("Save script");
		saveRScriptButton.setToolTipText("Save the R script to a file");
		saveRScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTools.saveStringToFile(RshellActivityConfigView.this, "Save R script", ".r", scriptTextArea.getText());
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

/*		final JCheckBox rVersionCheck = new JCheckBox("R1.6+");
		rVersionCheck.setEnabled(false);
		rVersionCheck.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// FIXME do something when this is selected - ie, the ports view
				// will change in some way, as will the connection used
				rVersion = rVersionCheck.isSelected();
			}

		});*/

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
//		buttonPanel.add(rVersionCheck);
		buttonPanel.add(loadRScriptButton);
		buttonPanel.add(saveRScriptButton);
		buttonPanel.add(clearScriptButton);
		
//		outerConstraint.gridx = 0;
//		outerConstraint.gridy = 1;
//		outerConstraint.fill = GridBagConstraints.NONE;
//		outerConstraint.anchor = GridBagConstraints.LINE_END;
//		outerConstraint.gridy = 2;
//		outerConstraint.weighty = 0;
		scriptEditPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Script", scriptEditPanel);
		tabbedPane.addTab("Input ports", new JScrollPane(setInputPanel()));
		tabbedPane.addTab("Output ports", new JScrollPane(setOutputPanel()));
		createSettingsPanel();
		tabbedPane.addTab("Connection Settings", settingsPanel);
		tabbedPane.addTab("Information", createInfoPanel());

		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;

		outerConstraint.fill = GridBagConstraints.BOTH;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		add(tabbedPane, outerConstraint);
		setPreferredSize(new Dimension(500, 500));
		validate();
	}

	private void createSettingsPanel() {
		
		settingsPanel = new JPanel(new GridBagLayout());

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

		Dimension dimension = new Dimension(0, 0);

		hostnameField = new JTextField();
		JLabel hostnameLabel = new JLabel("Hostname");
		hostnameField.setSize(dimension);
		hostnameLabel.setSize(dimension);
		hostnameLabel.setLabelFor(hostnameField);
		RshellConnectionSettings connectionSettings = configuration
				.getConnectionSettings();

		hostnameField.setText(connectionSettings.getHost());
		
		portField = new JTextField();
		JLabel portLabel = new JLabel("Port");
		portField.setSize(dimension);
		portLabel.setSize(dimension);
		portLabel.setLabelFor(portField);
		portField.setText(Integer.toString(connectionSettings.getPort()));

		usernameField = new JTextField();
		JLabel usernameLabel = new JLabel("Username");
		usernameField.setSize(dimension);
		usernameLabel.setSize(dimension);
		usernameLabel.setLabelFor(usernameField);
		usernameField.setText(connectionSettings.getUsername());

		passwordField = new JTextField();
		JLabel passwordLabel = new JLabel("Password");
		passwordField.setSize(dimension);
		passwordLabel.setSize(dimension);
		passwordLabel.setLabelFor(passwordField);
		passwordField.setText(connectionSettings.getPassword());

		keepSessionAliveCheckBox = new JCheckBox("Keep Session Alive");
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
		
		settingsPanel.add(usernameLabel, labelConstraints);
		labelConstraints.gridy++;
		settingsPanel.add(usernameField, fieldConstraints);
		fieldConstraints.gridy++;
		
		settingsPanel.add(passwordLabel, labelConstraints);
		labelConstraints.gridy++;
		settingsPanel.add(passwordField, fieldConstraints);
		fieldConstraints.gridy++;
		settingsPanel.add(keepSessionAliveCheckBox, fieldConstraints);
		fieldConstraints.gridy++;
		
	}

	/**
	 * Creates a {@link JTabbedPane} with the Output and Input ports
	 * 
	 * @return a {@link JTabbedPane} with the ports
	 */
	private JTabbedPane setPortPanel() {
		ports = new JTabbedPane();

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
	 * {@link JSpinner} to change the {@link SemanticTypes} for each one and a {@link JButton}
	 * to remove it. Currently the individual components from a
	 * {@link RshellInputViewer} are added rather than the
	 * {@link RshellInputViewer} itself
	 * 
	 * @return panel containing the view over the input ports
	 */
	private JPanel setInputPanel() {
		final JPanel inputEditPanel = new JPanel(new GridBagLayout());
//		inputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
//				.createEtchedBorder(), "Inputs"));

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
			final JButton removeButton = new JButton("Remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					inputViewList.remove(rshellInputViewer);
					inputEditPanel.remove(nameField);
					inputEditPanel.remove(semanticSelector);
					inputEditPanel.remove(removeButton);
					inputEditPanel.revalidate();
					inputEditPanel.repaint();
					outerInputPanel.revalidate();
					outerInputPanel.repaint();
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
				final JButton removeButton = new JButton("Remove");
				removeButton.addActionListener(new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
						inputViewList.remove(rshellInputViewer);
						inputEditPanel.remove(nameField);
						inputEditPanel.remove(semanticSelector);
						inputEditPanel.remove(removeButton);
						inputEditPanel.revalidate();
						inputEditPanel.repaint();
						outerInputPanel.revalidate();
						outerInputPanel.repaint();
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
	 * {@link JSpinner} to change the {@link SemanticTypes} for each one and
	 * a {@link JButton} to remove it. Currently the individual components from
	 * a {@link RshellOutputViewer} are added rather than the
	 * {@link RshellOutputViewer} itself
	 * 
	 * @return the panel containing the view of the output ports
	 */
	private JPanel setOutputPanel() {
		final JPanel outputEditPanel = new JPanel(new GridBagLayout());
//		outputEditPanel.setBorder(BorderFactory.createTitledBorder(
//				BorderFactory.createEtchedBorder(), "Outputs"));

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
			final JButton removeButton = new JButton("Remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					outputViewList.remove(rshellOutputViewer);
					outputEditPanel.remove(nameField);
					outputEditPanel.remove(semanticTypeSelector);
					outputEditPanel.remove(removeButton);
					outputEditPanel.revalidate();
					outputEditPanel.repaint();
					outerOutputPanel.revalidate();
					outerOutputPanel.repaint();
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
					final JButton removeButton = new JButton("Remove");
					removeButton.addActionListener(new AbstractAction() {

						public void actionPerformed(ActionEvent e) {
							outputViewList.remove(rshellOutputViewer);
							outputEditPanel.remove(nameField);
							outputEditPanel.remove(semanticTypeSelector);
							outputEditPanel.remove(removeButton);
							outputEditPanel.revalidate();
							outputEditPanel.repaint();
							outerOutputPanel.revalidate();
							outerOutputPanel.repaint();
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

	public boolean saveScript() {
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle("Save R Script");

		fileChooser.resetChoosableFileFilters();
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		fileChooser.setFileFilter(new ExtensionFileFilter(new String[] { EXTENSION }));

		fileChooser.setCurrentDirectory(new File(curDir));

		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			int returnVal = fileChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put("currentDir", fileChooser.getCurrentDirectory()
						.toString());
				File file = fileChooser.getSelectedFile();
				String extension = EXTENSION;
				if (!file.getName().toLowerCase().endsWith(extension)) {
					String newName = file.getName() + extension;
					file = new File(file.getParentFile(), newName);
				}

				// TODO: Open in separate thread to avoid hanging UI
				try {
					if (file.exists()) {
						logger.info("File already exists: " + file);
						String msg = "Are you sure you want to overwrite existing file "
								+ file + "?";
						int ret = JOptionPane.showConfirmDialog(
								this, msg, "File already exists",
								JOptionPane.YES_NO_CANCEL_OPTION);
						if (ret == JOptionPane.YES_OPTION) {
							
						} else if (ret == JOptionPane.NO_OPTION) {
							tryAgain = true;
							continue;
						} else {
							logger.info("Aborted overwrite of " + file);
							return false;
						}
					}
					BufferedWriter out = new BufferedWriter(new FileWriter(file));
			        out.write(scriptTextArea.getText());
			        out.close();
					logger.info("Saved script by overwriting " + file);
					return true;
				} catch (IOException ex) {
					logger.warn("Could not save script to " + file, ex);
					JOptionPane.showMessageDialog(this,
							"Could not save script to " + file + ": \n\n"
									+ ex.getMessage(), "Warning",
							JOptionPane.WARNING_MESSAGE);
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Method for clearing the script
	 * 
	 */
	private void clearScript() {
		if (JOptionPane.showConfirmDialog(this,
				"Do you really want to clear the script?",
				"Clearing the script", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			scriptTextArea.setText("");
		}

	}
	
	public RshellActivityConfigurationBean calculateConfiguration() {
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
			SemanticTypes selectedItem = (SemanticTypes) inputView
					.getSemanticSelector().getSelectedItem();
//			activityInputPortDefinitionBean.setDepth(selectedItem.getDepth());
			bean.setSymanticType(selectedItem);
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
			SemanticTypes selectedItem = (SemanticTypes) outputView
					.getSemanticTypeSelector().getSelectedItem();
//			activityOutputPortDefinitionBean.setDepth(selectedItem.getDepth());
			bean.setSymanticType(selectedItem);
			bean.setName(outputView.getNameField().getText());
			outputSemanticTypes.add(bean);
		}
		rshellActivityConfigurationBean
				.setInputSymanticTypes(inputSemanticTypes);
		rshellActivityConfigurationBean
				.setOutputSymanticTypes(outputSemanticTypes);
		rshellActivityConfigurationBean.setScript(scriptTextArea.getText());
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
		rshellActivityConfigurationBean
				.setConnectionSettings(connectionSettings);

		return rshellActivityConfigurationBean;
	}

	@Override
	public void noteConfiguration() {
		if (isConfigurationChanged()) {
			configuration = calculateConfiguration();
		}
	}

	@Override
	public void refreshConfiguration() {
		int visibleTab = -1;
		if (tabbedPane != null) {
			visibleTab = tabbedPane.getSelectedIndex();
		}
		this.removeAll();
		initialise();
		if (visibleTab != -1) {
			tabbedPane.setSelectedIndex(visibleTab);
		}
	}

	@Override
	public boolean checkValues() {
		boolean result = true;
		String text = "";
		Set<String> inputPortNames = new HashSet<String>();
		for (RshellInputViewer v : inputViewList) {
			String name = v.getNameField().getText();
			if (inputPortNames.contains(name)) {
				text += "Two input ports have the name " + name + "\n";
				result = false;
			} else {
				inputPortNames.add(name);
			}
		}
		Set<String> outputPortNames = new HashSet<String>();
		for (RshellOutputViewer v : outputViewList) {
			String name = v.getNameField().getText();
			if (inputPortNames.contains(name)) {
				text += "An input and an output port are named " + name + "\n";
				result = false;
			}
			if (outputPortNames.contains(name)) {
				text += "Two output ports have the name " + name + "\n";
				result = false;
			} else {
				outputPortNames.add(name);
			}
		}
		if (!result) {
			JOptionPane.showMessageDialog(this, text, "Port name problem", JOptionPane.ERROR_MESSAGE);
		}
		return result;
	}
	
	private JPanel createInfoPanel() {
		/** **************************************************************** */
		/*
		 * Info panel
		 * /******************************************************************
		 */
		JPanel infoPanel = new JPanel(new BorderLayout());
		tabbedPane.addTab("Info", infoPanel);

		JPanel infoContentPanel = new JPanel(new GridBagLayout());
//		infoContentPanel.setBorder(BorderFactory.createTitledBorder(
//				BorderFactory.createEtchedBorder(), "Info"));
		infoPanel.add(infoContentPanel, BorderLayout.NORTH);

		GridBagConstraints infoConstraints = new GridBagConstraints();
		infoConstraints.weightx = 0.0;
		infoConstraints.gridx = 0;
		infoConstraints.gridy = 0;
		infoConstraints.fill = GridBagConstraints.NONE;

		infoContentPanel.add(new JLabel("Rshell for Taverna 2.1, 2009"), infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("Ingo Wassink"), infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("Human Media Interaction"),
				infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("University of Twente"),
				infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("BioRange"), infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel
				.add(
						new JLabel(
								"<html><a href='http://www.ewi.utwente.nl/~biorange'>www.ewi.utwente.nl/~biorange</a></html>"),
						infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("and"), infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("Alan R Williams"), infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("University of Manchester, UK"),
				infoConstraints);
		return infoPanel;
	}

	/**
	 * 
	 * The following classes are copied from http://forums.sun.com/thread.jspa?threadID=622683
	 *
	 */
	private class NoWrapEditorKit extends StyledEditorKit
	{
		public ViewFactory getViewFactory()
		{
				return new StyledViewFactory();
		} 
	}
	 
		static class StyledViewFactory implements ViewFactory
		{
			public View create(Element elem)
			{
				String kind = elem.getName();
	 
				if (kind != null)
				{
					if (kind.equals(AbstractDocument.ContentElementName))
					{
						return new LabelView(elem);
					}
					else if (kind.equals(AbstractDocument.ParagraphElementName))
					{
						return new ParagraphView(elem);
					}
					else if (kind.equals(AbstractDocument.SectionElementName))
					{
						return new NoWrapBoxView(elem, View.Y_AXIS);
					}
					else if (kind.equals(StyleConstants.ComponentElementName))
					{
						return new ComponentView(elem);
					}
					else if (kind.equals(StyleConstants.IconElementName))
					{
						return new IconView(elem);
					}
				}
	 
		 		return new LabelView(elem);
			}
		}

		static class NoWrapBoxView extends BoxView {
	        public NoWrapBoxView(Element elem, int axis) {
	            super(elem, axis);
	        }
	 
	        public void layout(int width, int height) {
	            super.layout(32768, height);
	        }
	        public float getMinimumSpan(int axis) {
	            return super.getPreferredSpan(axis);
	        }
	    }

}
