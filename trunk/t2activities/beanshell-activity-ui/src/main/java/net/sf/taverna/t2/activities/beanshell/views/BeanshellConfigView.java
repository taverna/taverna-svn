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
package net.sf.taverna.t2.activities.beanshell.views;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.help.CSH;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
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
 * Provides the configurable view for a {@link BeanshellActivity} through it's
 * {@link BeanshellActivityConfigurationBean}. Has 3 main tabs - Script, Ports &
 * Dependencies. The {@link #inputViewList} contains the
 * {@link BeanshellInputViewer}s describing the input ports and
 * {@link #outputViewList} has the {@link BeanshellOutputViewer}s
 * 
 * @author Ian Dunlop
 * 
 */
public class BeanshellConfigView extends JPanel {
	/** The beanshell script */
	private JEditTextArea scriptText;
	/** A List of views over the input ports */
	private List<BeanshellInputViewer> inputViewList;
	/** A List of views over the output ports */
	private List<BeanshellOutputViewer> outputViewList;
	/** The activity which this view describes */
	private BeanshellActivity activity;
	/** the configuration bean used to configure the activity */
	private BeanshellActivityConfigurationBean configuration;
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

	// private JPanel mimes;

	/**
	 * Stores the {@link BeanshellActivity}, gets its
	 * {@link BeanshellActivityConfigurationBean}, sets the layout and calls
	 * {@link #initialise()} to get the view going
	 * 
	 * @param activity
	 *            the {@link BeanshellActivity} that the view is over
	 */
	public BeanshellConfigView(BeanshellActivity activity) {
		this.activity = activity;
		configuration = activity.getConfiguration();
		setLayout(new GridBagLayout());
		initialise();
	}

	public BeanshellActivityConfigurationBean getConfiguration() {
		return configuration;
	}

	public boolean isConfigurationChanged() {
		return configChanged;
	}

	/**
	 * Adds a {@link JButton} which handles the reconfiguring of the
	 * {@link BeanshellActivity} through the altered
	 * {@link BeanshellActivityConfigurationBean}. Sets up the initial tabs -
	 * Script (also sets the initial value), Ports & Dependencies and their
	 * initial values through {@link #setDependencies()},
	 * {@link #setPortPanel()}
	 */
	private void initialise() {
		CSH
				.setHelpIDString(
						this,
						"net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.BeanshellConfigView");
		setSize(500, 500);
		AbstractAction okAction = getOKAction();
		button = new JButton(okAction);
		button.setText("OK");
		button.setToolTipText("Click to configure with the new values");
		inputViewList = new ArrayList<BeanshellInputViewer>();
		outputViewList = new ArrayList<BeanshellOutputViewer>();
		setBorder(javax.swing.BorderFactory.createTitledBorder(null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		setSize(500, 500);
		final BeanshellActivityConfigurationBean configBean = activity
				.getConfiguration();

		JPanel scriptEditPanel = new JPanel(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Script", scriptEditPanel);
		tabbedPane.addTab("Ports", setPortPanel());

		tabbedPane.addTab("Dependencies", setDependencies());

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
//				configBean.setScript(scriptText.getText());
			}
		});
		scriptEditPanel.add(scriptText, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

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

	/**
	 * Gets the Group, Artifacts and Version from the
	 * {@link BeanshellActivityConfigurationBean#getDependencies()}. These are
	 * in the format group:artifact:version so are parsed into the correct
	 * display format.
	 * 
	 * @return a panel with a non-editable tabular view of the dependencies
	 */
	private JPanel setDependencies() {
		JPanel dependencies = new JPanel(new GridBagLayout());
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		JLabel label = new JLabel("Group");
		label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		dependencies.add(label, outerConstraint);
		outerConstraint.gridx = 1;
		JLabel label2 = new JLabel("Aritifact");
		label2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		dependencies.add(label2, outerConstraint);
		outerConstraint.gridx = 2;
		JLabel label3 = new JLabel("Version");
		label3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		dependencies.add(label3, outerConstraint);

		int gridy = 1;
		for (String dependency : configuration.getDependencies()) {
			outerConstraint.gridy = gridy;
			String[] split = dependency.split(":");
			outerConstraint.gridx = 0;
			JLabel label4 = new JLabel(split[0]);
			label4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
			dependencies.add(label4, outerConstraint);
			outerConstraint.gridx = 1;
			JLabel label5 = new JLabel(split[1]);
			label5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
			dependencies.add(label5, outerConstraint);
			outerConstraint.gridx = 2;
			JLabel label6 = new JLabel(split[02]);
			label6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
			dependencies.add(label6, outerConstraint);
			gridy++;
		}
		JPanel filler = new JPanel();
		outerConstraint.gridy = gridy;
		outerConstraint.weighty = 0.1;
		dependencies.add(filler, outerConstraint);
		return dependencies;
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
	 * {@link BeanshellActivityConfigurationBean} and creates a
	 * {@link BeanshellInputViewer} for each one. Displays the name and a
	 * {@link JSpinner} to change the depth for each one and a {@link JButton}
	 * to remove it. Currently the individual components from a
	 * {@link BeanshellInputViewer} are added rather than the
	 * {@link BeanshellInputViewer} itself
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
		inputEditPanel.add(new JLabel("Depth"), inputConstraint);

		inputGridy = 1;
		inputConstraint.gridx = 0;
		for (ActivityInputPortDefinitionBean inputBean : configuration
				.getInputPortDefinitions()) {
			// FIXME refactor this into a method
			inputConstraint.gridy = inputGridy;
			final BeanshellInputViewer beanshellInputViewer = new BeanshellInputViewer(
					inputBean, true);
			inputViewList.add(beanshellInputViewer);
			inputConstraint.gridx = 0;
			final JTextField nameField = beanshellInputViewer.getNameField();
			inputConstraint.weightx = 0.1;
			inputEditPanel.add(nameField, inputConstraint);
			inputConstraint.weightx = 0.0;
			inputConstraint.gridx = 1;
			final JSpinner depthSpinner = beanshellInputViewer
					.getDepthSpinner();
			inputEditPanel.add(depthSpinner, inputConstraint);
			inputConstraint.gridx = 2;
			final JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					inputViewList.remove(beanshellInputViewer);
					inputEditPanel.remove(nameField);
					inputEditPanel.remove(depthSpinner);
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
				final BeanshellInputViewer beanshellInputViewer = new BeanshellInputViewer(
						bean, true);
				inputViewList.add(beanshellInputViewer);
				inputConstraint.weightx = 0.1;
				inputConstraint.gridx = 0;
				final JTextField nameField = beanshellInputViewer
						.getNameField();
				inputEditPanel.add(nameField, inputConstraint);
				inputConstraint.weightx = 0;
				inputConstraint.gridx = 1;
				final JSpinner depthSpinner = beanshellInputViewer
						.getDepthSpinner();
				inputEditPanel.add(depthSpinner, inputConstraint);
				inputConstraint.gridx = 2;
				final JButton removeButton = new JButton("remove");
				removeButton.addActionListener(new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
						inputViewList.remove(beanshellInputViewer);
						inputEditPanel.remove(nameField);
						inputEditPanel.remove(depthSpinner);
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
	 * {@link BeanshellActivityConfigurationBean} and creates a
	 * {@link BeanshellOutputViewer} for each one. Displays the name and a
	 * {@link JSpinner} to change the depth and granular depth for each one and
	 * a {@link JButton} to remove it. Currently the individual components from
	 * a {@link BeanshellOutputViewer} are added rather than the
	 * {@link BeanshellOutputViewer} itself
	 * 
	 * @return the panel containing the view of the output ports
	 */
	private JPanel setOutputPanel() {
		// mimes = new JPanel();
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
		outputEditPanel.add(new JLabel("Depth"), outputConstraint);
		// outputConstraint.gridx = 2;
		// outputEditPanel.add(new JLabel("GranularDepth"), outputConstraint);

		outputGridy = 1;
		outputConstraint.gridx = 0;
		for (ActivityOutputPortDefinitionBean outputBean : configuration
				.getOutputPortDefinitions()) {
			// FIXME refactor this into a method
			outputConstraint.gridy = outputGridy;
			final BeanshellOutputViewer beanshellOutputViewer = new BeanshellOutputViewer(
					outputBean, true);
			outputViewList.add(beanshellOutputViewer);
			outputConstraint.gridx = 0;
			outputConstraint.weightx = 0.1;
			final JTextField nameField = beanshellOutputViewer.getNameField();
			outputEditPanel.add(nameField, outputConstraint);
			outputConstraint.weightx = 0;
			outputConstraint.gridx = 1;
			final JSpinner depthSpinner = beanshellOutputViewer
					.getDepthSpinner();
			outputEditPanel.add(depthSpinner, outputConstraint);
			outputConstraint.gridx = 2;
			// final JSpinner granularDepthSpinner = beanshellOutputViewer
			// .getGranularDepthSpinner();
			// outputEditPanel.add(granularDepthSpinner, outputConstraint);
			// outputConstraint.gridx = 3;
			// final JButton addMimeButton = beanshellOutputViewer
			// .getAddMimeButton();
			// addMimeButton.addActionListener(new AbstractAction() {
			//
			// public void actionPerformed(ActionEvent e) {
			// mimes.add(beanshellOutputViewer.getMimeTypeConfig());
			// mimes.setVisible(true);
			// }
			//
			// });

			// final JFrame mimeFrame = new JFrame();
			// outputEditPanel.add(addMimeButton, outputConstraint);
			// beanshellOutputViewer.getMimeTypeConfig().addNewMimeListener(
			// new AbstractAction() {
			//
			// public void actionPerformed(ActionEvent e) {
			// // beanshellOutputViewer.getMimeFrame().setVisible(
			// // false);
			// // beanshellOutputViewer.getMimeTypeConfig().setVisible(false);
			// // mimeFrame.setVisible(false);
			// mimes.removeAll();
			// mimes.setVisible(false);
			//
			// }
			//
			// });
			// mimeFrame.add(beanshellOutputViewer
			// .getMimeTypeConfig());

			outputConstraint.gridx = 4;
			final JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					outputViewList.remove(beanshellOutputViewer);
					outputEditPanel.remove(nameField);
					outputEditPanel.remove(depthSpinner);
					// outputEditPanel.remove(granularDepthSpinner);
					// outputEditPanel.remove(addMimeButton);
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
		// outerPanelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
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
					final BeanshellOutputViewer beanshellOutputViewer = new BeanshellOutputViewer(
							bean, true);
					outputViewList.add(beanshellOutputViewer);
					outputConstraint.gridy = outputGridy;
					outputConstraint.gridx = 0;
					final JTextField nameField = beanshellOutputViewer
							.getNameField();
					outputConstraint.weightx = 0.1;
					outputEditPanel.add(nameField, outputConstraint);
					outputConstraint.gridx = 1;
					outputConstraint.weightx = 0;
					final JSpinner depthSpinner = beanshellOutputViewer
							.getDepthSpinner();
					outputEditPanel.add(depthSpinner, outputConstraint);
					outputConstraint.gridx = 2;
					// final JSpinner granularDepthSpinner =
					// beanshellOutputViewer
					// .getGranularDepthSpinner();
					// outputEditPanel.add(granularDepthSpinner,
					// outputConstraint);
					// outputConstraint.gridx = 3;
					// final JButton addMimeButton = beanshellOutputViewer
					// .getAddMimeButton();
					// addMimeButton.addActionListener(new AbstractAction() {
					//
					// public void actionPerformed(ActionEvent e) {
					// mimes
					// .add(beanshellOutputViewer
					// .getMimeTypeConfig());
					// mimes.setVisible(true);
					// }
					//
					// });
					// outputEditPanel.add(addMimeButton, outputConstraint);
					// beanshellOutputViewer.getMimeTypeConfig()
					// .addNewMimeListener(new AbstractAction() {
					// // hide the mime frame, annotations added when
					// // the actual OK button is clicked
					// public void actionPerformed(ActionEvent e) {
					// // beanshellOutputViewer.getMimeFrame()
					// // .setVisible(false);
					// mimes.removeAll();
					// mimes.setVisible(false);
					// //
					// beanshellOutputViewer.getMimeTypeConfig().setVisible(false
					// );
					// // mimeFrame.setVisible(false);
					//
					// }
					//
					// });
					// mimeFrame.add(beanshellOutputViewer
					// .getMimeTypeConfig());
					outputConstraint.gridx = 4;
					final JButton removeButton = new JButton("remove");
					removeButton.addActionListener(new AbstractAction() {

						public void actionPerformed(ActionEvent e) {
							outputViewList.remove(beanshellOutputViewer);
							outputEditPanel.remove(nameField);
							outputEditPanel.remove(depthSpinner);
							// outputEditPanel.remove(granularDepthSpinner);
							outputEditPanel.remove(removeButton);
							// outputEditPanel.remove(addMimeButton);
							outputEditPanel.revalidate();
						}

					});
					outputEditPanel.add(removeButton, outputConstraint);
					outputEditPanel.revalidate();
					newOutputPortNumber++;

					outputGridy++;
				} catch (Exception e1) {
					// throw it, log it??
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
		// outerOutputPanel.add(mimes, outerPanelConstraint);
		// mimes.setBorder(BorderFactory.createTitledBorder("Add Mime Types"));

		return outerOutputPanel;
	}

	public void setButtonClickedListener(ActionListener listener) {
		buttonClicked = listener;
	}

	/**
	 * Calls
	 * {@link BeanshellActivity#configure(BeanshellActivityConfigurationBean)}
	 * using a {@link BeanshellActivityConfigurationBean} set with the new
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
				for (BeanshellInputViewer inputView : inputViewList) {
					ActivityInputPortDefinitionBean activityInputPortDefinitionBean = new ActivityInputPortDefinitionBean();
					activityInputPortDefinitionBean
							.setHandledReferenceSchemes(inputView.getBean()
									.getHandledReferenceSchemes());
					activityInputPortDefinitionBean.setMimeTypes(inputView
							.getBean().getMimeTypes());
					activityInputPortDefinitionBean
							.setTranslatedElementType(inputView.getBean()
									.getTranslatedElementType());
					activityInputPortDefinitionBean
							.setAllowsLiteralValues((Boolean) inputView
									.getLiteralSelector().getSelectedItem());
					activityInputPortDefinitionBean
							.setDepth((Integer) inputView.getDepthSpinner()
									.getValue());
					activityInputPortDefinitionBean.setName(inputView
							.getNameField().getText());
					inputBeanList.add(activityInputPortDefinitionBean);
				}

				List<ActivityOutputPortDefinitionBean> outputBeanList = new ArrayList<ActivityOutputPortDefinitionBean>();
				for (BeanshellOutputViewer outputView : outputViewList) {
					ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
					activityOutputPortDefinitionBean
							.setDepth((Integer) outputView.getDepthSpinner()
									.getValue());
					// activityOutputPortDefinitionBean
					// .setGranularDepth((Integer) outputView
					// .getGranularDepthSpinner().getValue());
					activityOutputPortDefinitionBean.setName(outputView
							.getNameField().getText());
					activityOutputPortDefinitionBean.setMimeTypes(outputView
							.getMimeTypeConfig().getMimeTypeList());
					// outputView.getMimeTypeConfig().getMimeTypeList();

					// Edits edits = EditsRegistry.getEdits();

					// FIXME add all the mime types as an annotation

					outputBeanList.add(activityOutputPortDefinitionBean);
				}
				BeanshellActivityConfigurationBean beanshellActivityConfigurationBean = new BeanshellActivityConfigurationBean();
				beanshellActivityConfigurationBean.setScript(scriptText
						.getText());
				beanshellActivityConfigurationBean
						.setInputPortDefinitions(inputBeanList);
				beanshellActivityConfigurationBean
						.setOutputPortDefinitions(outputBeanList);

				configuration = beanshellActivityConfigurationBean;
				configChanged = true;
				setVisible(false);
				buttonClicked.actionPerformed(e);
			}

		};
	}

	/**
	 * Check the proposed port name against the set of input ports that the activity
	 * has
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
	 * Check the proposed port name against the set of output ports that the activity
	 * has
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

}
