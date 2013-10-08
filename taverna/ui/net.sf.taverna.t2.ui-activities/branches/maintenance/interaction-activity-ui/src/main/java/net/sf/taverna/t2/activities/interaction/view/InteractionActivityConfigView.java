/**
 *
 */
package net.sf.taverna.t2.activities.interaction.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.help.CSH;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 * @author alanrw
 * 
 */
public class InteractionActivityConfigView
		extends
		ActivityConfigurationPanel<InteractionActivity, InteractionActivityConfigurationBean> {

	/**
	 *
	 */
	private static final long serialVersionUID = -915222444955259613L;
	private final InteractionActivity activity;
	private InteractionActivityConfigurationBean configuration;
	private ArrayList<InteractionInputViewer> inputViewList;
	private ArrayList<InteractionOutputViewer> outputViewList;
	private JTabbedPane tabbedPane;
	private int inputGridy;
	private int outputGridy;
	private boolean inputsChanged = false;
	private boolean outputsChanged = false;

	private JPanel outerInputPanel;
	protected int newInputPortNumber = 1;
	private JPanel outerOutputPanel;
	protected int newOutputPortNumber = 1;

	JTextField sourceTextField = new JTextField(40);
	JCheckBox progressField = new JCheckBox();

	public InteractionActivityConfigView(final InteractionActivity activity) {
		this.activity = activity;
		this.configuration = (InteractionActivityConfigurationBean) this
				.cloneBean(activity.getConfiguration());
		this.setLayout(new GridBagLayout());
		this.initialise();
	}

	private void initialise() {
		CSH.setHelpIDString(
				this,
				"net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.InteractionConfigView");
		this.configuration = this.activity.getConfiguration();
		this.inputViewList = new ArrayList<InteractionInputViewer>();
		this.outputViewList = new ArrayList<InteractionOutputViewer>();
		this.setBorder(javax.swing.BorderFactory.createTitledBorder(null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		this.activity.getConfiguration();

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.addTab("Interaction source", this.getSourcePanel());
		this.tabbedPane.addTab("Input ports",
				new JScrollPane(this.getInputPanel()));
		this.tabbedPane.addTab("Output ports",
				new JScrollPane(this.getOutputPanel()));

		final GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;

		outerConstraint.fill = GridBagConstraints.BOTH;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		this.add(this.tabbedPane, outerConstraint);

		this.setPreferredSize(new Dimension(600, 500));

		this.validate();

	}

	private JPanel getOutputPanel() {
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

		this.outputGridy = 1;
		outputConstraint.gridx = 0;
		for (final ActivityOutputPortDefinitionBean outputBean : this.configuration
				.getOutputPortDefinitions()) {
			// FIXME refactor this into a method
			outputConstraint.gridy = this.outputGridy;
			final InteractionOutputViewer beanshellOutputViewer = new InteractionOutputViewer(
					outputBean, true);
			this.outputViewList.add(beanshellOutputViewer);
			outputConstraint.gridx = 0;
			outputConstraint.weightx = 0.1;
			final JTextField nameField = beanshellOutputViewer.getNameField();
			nameField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(final DocumentEvent e) {
					// Plain text components don't fire these events.
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					InteractionActivityConfigView.this.outputsChanged = true;
				}

				@Override
				public void removeUpdate(final DocumentEvent e) {
					InteractionActivityConfigView.this.outputsChanged = true;
				}
			});
			outputEditPanel.add(nameField, outputConstraint);
			outputConstraint.weightx = 0;
			outputConstraint.gridx = 1;
			final JSpinner depthSpinner = beanshellOutputViewer
					.getDepthSpinner();
			depthSpinner.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(final ChangeEvent e) {
					InteractionActivityConfigView.this.outputsChanged = true;
				}
			});
			outputEditPanel.add(depthSpinner, outputConstraint);
			outputConstraint.gridx = 2;

			final JButton removeButton = new JButton("Remove");
			removeButton.addActionListener(new AbstractAction() {

				/**
				 * 
				 */
				private static final long serialVersionUID = -8682466544152493330L;

				@Override
				public void actionPerformed(final ActionEvent e) {

					InteractionActivityConfigView.this.outputsChanged = true;

					InteractionActivityConfigView.this.outputViewList
							.remove(beanshellOutputViewer);
					outputEditPanel.remove(nameField);
					outputEditPanel.remove(depthSpinner);
					// outputEditPanel.remove(granularDepthSpinner);
					// outputEditPanel.remove(addMimeButton);
					outputEditPanel.remove(removeButton);
					outputEditPanel.revalidate();
					outputEditPanel.repaint();
					InteractionActivityConfigView.this.outerOutputPanel
							.revalidate();
					InteractionActivityConfigView.this.outerOutputPanel
							.repaint();
				}

			});
			outputEditPanel.add(removeButton, outputConstraint);
			this.outputGridy++;
		}
		this.outerOutputPanel = new JPanel();
		this.outerOutputPanel.setLayout(new GridBagLayout());
		final GridBagConstraints outerPanelConstraint = new GridBagConstraints();
		// outerPanelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0.1;
		this.outerOutputPanel.add(new JScrollPane(outputEditPanel),
				outerPanelConstraint);
		outerPanelConstraint.weighty = 0;
		final JButton addOutputPortButton = new JButton(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6274862208440912573L;

			// FIXME refactor this into a method
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {

					InteractionActivityConfigView.this.outputsChanged = true;

					final ActivityOutputPortDefinitionBean bean = new ActivityOutputPortDefinitionBean();
					bean.setDepth(0);
					bean.setGranularDepth(0);
					final List<String> mimeTypes = new ArrayList<String>();
					mimeTypes.add("text/plain");
					bean.setMimeTypes(mimeTypes);
					String name2 = "out"
							+ InteractionActivityConfigView.this.newOutputPortNumber;
					boolean nameExists = true;
					while (nameExists == true) {
						nameExists = InteractionActivityConfigView.this
								.outputPortNameExists(
										name2,
										InteractionActivityConfigView.this.activity
												.getOutputPorts());
						if (nameExists) {
							InteractionActivityConfigView.this.newOutputPortNumber++;
							name2 = "out"
									+ InteractionActivityConfigView.this.newOutputPortNumber;
						}
					}
					bean.setName(name2);
					final InteractionOutputViewer beanshellOutputViewer = new InteractionOutputViewer(
							bean, true);
					InteractionActivityConfigView.this.outputViewList
							.add(beanshellOutputViewer);
					outputConstraint.gridy = InteractionActivityConfigView.this.outputGridy;
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

					final JButton removeButton = new JButton("Remove");
					removeButton.addActionListener(new AbstractAction() {

						/**
						 * 
						 */
						private static final long serialVersionUID = -1342487127381787354L;

						@Override
						public void actionPerformed(final ActionEvent e) {
							InteractionActivityConfigView.this.outputViewList
									.remove(beanshellOutputViewer);
							outputEditPanel.remove(nameField);
							outputEditPanel.remove(depthSpinner);
							// outputEditPanel.remove(granularDepthSpinner);
							outputEditPanel.remove(removeButton);
							// outputEditPanel.remove(addMimeButton);
							outputEditPanel.revalidate();
							outputEditPanel.repaint();
							InteractionActivityConfigView.this.outerOutputPanel
									.revalidate();
							InteractionActivityConfigView.this.outerOutputPanel
									.repaint();
						}

					});
					outputEditPanel.add(removeButton, outputConstraint);
					outputEditPanel.revalidate();
					InteractionActivityConfigView.this.newOutputPortNumber++;

					InteractionActivityConfigView.this.outputGridy++;
				} catch (final Exception e1) {
					// throw it, log it??
				}
			}

		});
		addOutputPortButton.setText("Add Port");
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		final JPanel filler = new JPanel();
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
		this.outerOutputPanel.add(buttonPanel, outerPanelConstraint);
		outerPanelConstraint.gridx = 1;
		outerPanelConstraint.gridy = 0;

		return this.outerOutputPanel;
	}

	private JPanel getInputPanel() {
		final JPanel inputEditPanel = new JPanel(new GridBagLayout());
		inputEditPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Inputs"));

		final GridBagConstraints inputConstraint = new GridBagConstraints();
		inputConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		inputConstraint.gridx = 0;
		inputConstraint.gridy = 0;
		inputConstraint.weightx = 0.1;
		inputConstraint.fill = GridBagConstraints.BOTH;

		inputEditPanel.add(new JLabel("Name"), inputConstraint);
		inputConstraint.gridx = 1;
		inputEditPanel.add(new JLabel("Depth"), inputConstraint);
		inputConstraint.gridx = 2;
		inputEditPanel.add(new JLabel("Publish"), inputConstraint);

		this.inputGridy = 1;
		inputConstraint.gridx = 0;
		for (final ActivityInputPortDefinitionBean inputBean : this.configuration
				.getInputPortDefinitions()) {
			// FIXME refactor this into a method
			inputConstraint.gridy = this.inputGridy;
			final InteractionInputViewer beanshellInputViewer = new InteractionInputViewer(
					inputBean, true);
			this.inputViewList.add(beanshellInputViewer);
			inputConstraint.gridx = 0;
			final JTextField nameField = beanshellInputViewer.getNameField();
			nameField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(final DocumentEvent e) {
					// Plain text components don't fire these events.
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					InteractionActivityConfigView.this.inputsChanged = true;
				}

				@Override
				public void removeUpdate(final DocumentEvent e) {
					InteractionActivityConfigView.this.inputsChanged = true;
				}
			});
			inputConstraint.weightx = 0.1;
			inputEditPanel.add(nameField, inputConstraint);
			inputConstraint.weightx = 0.0;
			inputConstraint.gridx = 1;
			final JSpinner depthSpinner = beanshellInputViewer
					.getDepthSpinner();
			depthSpinner.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(final ChangeEvent e) {
					InteractionActivityConfigView.this.inputsChanged = true;
				}
			});
			inputEditPanel.add(depthSpinner, inputConstraint);
			inputConstraint.gridx = 2;
			final JCheckBox publishBox = beanshellInputViewer.getPublishField();
			publishBox.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					InteractionActivityConfigView.this.inputsChanged = true;
				}
			});
			inputEditPanel.add(publishBox, inputConstraint);
			inputConstraint.gridx = 3;
			final JButton removeButton = new JButton("Remove");
			removeButton.addActionListener(new AbstractAction() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 2053711722870462633L;

				@Override
				public void actionPerformed(final ActionEvent e) {

					InteractionActivityConfigView.this.inputsChanged = true;

					InteractionActivityConfigView.this.inputViewList
							.remove(beanshellInputViewer);
					inputEditPanel.remove(nameField);
					inputEditPanel.remove(depthSpinner);
					inputEditPanel.remove(publishBox);
					inputEditPanel.remove(removeButton);
					inputEditPanel.revalidate();
					inputEditPanel.repaint();
					InteractionActivityConfigView.this.outerInputPanel
							.revalidate();
					InteractionActivityConfigView.this.outerInputPanel
							.repaint();
				}

			});
			inputEditPanel.add(removeButton, inputConstraint);
			this.inputGridy++;
		}
		this.outerInputPanel = new JPanel();
		this.outerInputPanel.setLayout(new GridBagLayout());
		final GridBagConstraints outerPanelConstraint = new GridBagConstraints();
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0.1;
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		this.outerInputPanel.add(new JScrollPane(inputEditPanel),
				outerPanelConstraint);
		outerPanelConstraint.weighty = 0;
		final JButton addInputPortButton = new JButton(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4632647815901676285L;

			// FIXME refactor this into a method
			@Override
			public void actionPerformed(final ActionEvent e) {

				InteractionActivityConfigView.this.inputsChanged = true;

				final ActivityInputPortDefinitionBean bean = new ActivityInputPortDefinitionBean();
				bean.setAllowsLiteralValues(true);
				bean.setDepth(0);
				final List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = new ArrayList<Class<? extends ExternalReferenceSPI>>();
				// handledReferenceSchemes.add(FileReference.class);
				bean.setHandledReferenceSchemes(handledReferenceSchemes);
				final List<String> mimeTypes = new ArrayList<String>();
				mimeTypes.add("text/plain");
				bean.setMimeTypes(mimeTypes);

				String name2 = "in"
						+ InteractionActivityConfigView.this.newInputPortNumber;
				boolean nameExists = true;
				while (nameExists == true) {
					nameExists = InteractionActivityConfigView.this
							.inputPortNameExists(name2,
									InteractionActivityConfigView.this.activity
											.getInputPorts());
					if (nameExists) {
						InteractionActivityConfigView.this.newInputPortNumber++;
						name2 = "in"
								+ InteractionActivityConfigView.this.newInputPortNumber;
					}
				}

				bean.setName(name2);
				InteractionActivityConfigView.this.newInputPortNumber++;
				bean.setTranslatedElementType(String.class);
				inputConstraint.gridy = InteractionActivityConfigView.this.inputGridy;
				final InteractionInputViewer beanshellInputViewer = new InteractionInputViewer(
						bean, true);
				InteractionActivityConfigView.this.inputViewList
						.add(beanshellInputViewer);
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
				final JCheckBox publishBox = beanshellInputViewer
						.getPublishField();
				inputEditPanel.add(publishBox, inputConstraint);
				inputConstraint.gridx = 3;
				final JButton removeButton = new JButton("Remove");
				removeButton.addActionListener(new AbstractAction() {

					/**
					 * 
					 */
					private static final long serialVersionUID = -23668664002911665L;

					@Override
					public void actionPerformed(final ActionEvent e) {
						InteractionActivityConfigView.this.inputViewList
								.remove(beanshellInputViewer);
						inputEditPanel.remove(nameField);
						inputEditPanel.remove(depthSpinner);
						inputEditPanel.remove(publishBox);
						inputEditPanel.remove(removeButton);
						inputEditPanel.revalidate();
						inputEditPanel.repaint();
						InteractionActivityConfigView.this.outerInputPanel
								.revalidate();
						InteractionActivityConfigView.this.outerInputPanel
								.repaint();
					}

				});
				inputEditPanel.add(removeButton, inputConstraint);
				inputEditPanel.revalidate();
				inputEditPanel.repaint();

				InteractionActivityConfigView.this.inputGridy++;
			}

		});
		addInputPortButton.setText("Add Port");
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		final JPanel filler = new JPanel();
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
		this.outerInputPanel.add(buttonPanel, outerPanelConstraint);

		return this.outerInputPanel;

	}

	/**
	 * Check the proposed port name against the set of input ports that the
	 * activity has
	 * 
	 * @param name
	 * @param set
	 * @return
	 */
	private boolean inputPortNameExists(final String name,
			final Set<ActivityInputPort> set) {
		for (final Port port : set) {
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
	private boolean outputPortNameExists(final String name,
			final Set<OutputPort> set) {
		for (final Port port : set) {
			if (name.equals(port.getName())) {
				return true;
			}
		}
		return false;
	}

	private JPanel getSourcePanel() {
		final JPanel sourcePanel = new JPanel();
		sourcePanel.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		final JLabel sourceHtmlLabel = new JLabel("HTML:");
		sourcePanel.add(sourceHtmlLabel, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.sourceTextField
				.setText(this.configuration.getPresentationOrigin());
		this.sourceTextField.setColumns(40);
		sourcePanel.add(this.sourceTextField, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		final JLabel progressLabel = new JLabel("Progress notification:");
		sourcePanel.add(progressLabel, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.progressField.setSelected(this.configuration
				.isProgressNotification());
		sourcePanel.add(this.progressField, gbc);

		final JPanel filler = new JPanel();
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		sourcePanel.add(filler, gbc);
		return sourcePanel;
	}

	@Override
	public boolean checkValues() {
		if (this.progressField.isSelected() && !this.outputViewList.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"A progress notification cannot have output ports");
			return false;
		}
		return true;
	}

	@Override
	public InteractionActivityConfigurationBean getConfiguration() {
		return this.configuration;
	}

	@Override
	public boolean isConfigurationChanged() {
		return (this.inputsChanged || this.outputsChanged || !this.sourceTextField
				.getText().equals(this.configuration.getPresentationOrigin()));
	}

	@Override
	public void noteConfiguration() {
		this.configuration = this.makeConfiguration();
		this.inputsChanged = false;
		this.outputsChanged = false;
	}

	private InteractionActivityConfigurationBean makeConfiguration() {
		// Set the new configuration
		final List<ActivityInputPortDefinitionBean> inputBeanList = new ArrayList<ActivityInputPortDefinitionBean>();
		for (final InteractionInputViewer inputView : this.inputViewList) {
			final ActivityInputPortDefinitionBean activityInputPortDefinitionBean = new ActivityInputPortDefinitionBean();
			final ActivityInputPortDefinitionBean inputViewBean = inputView
					.getBean();
			activityInputPortDefinitionBean
					.setHandledReferenceSchemes(inputViewBean
							.getHandledReferenceSchemes());
			activityInputPortDefinitionBean.setMimeTypes(inputViewBean
					.getMimeTypes());
			if (inputView.getPublishField().isSelected()) {
				activityInputPortDefinitionBean
						.setTranslatedElementType(byte[].class);
			} else {
				activityInputPortDefinitionBean
						.setTranslatedElementType(String.class);
			}
			activityInputPortDefinitionBean.setDepth((Integer) inputView
					.getDepthSpinner().getValue());
			activityInputPortDefinitionBean.setName(inputView.getNameField()
					.getText());
			inputBeanList.add(activityInputPortDefinitionBean);
		}

		final List<ActivityOutputPortDefinitionBean> outputBeanList = new ArrayList<ActivityOutputPortDefinitionBean>();
		for (final InteractionOutputViewer outputView : this.outputViewList) {
			final ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
			activityOutputPortDefinitionBean.setDepth((Integer) outputView
					.getDepthSpinner().getValue());

			// activityOutputPortDefinitionBean
			// .setGranularDepth((Integer) outputView
			// .getGranularDepthSpinner().getValue());

			// NOTE: Granular depth must match output depth because we return
			// the full lists right away
			activityOutputPortDefinitionBean
					.setGranularDepth(activityOutputPortDefinitionBean
							.getDepth());

			activityOutputPortDefinitionBean.setName(outputView.getNameField()
					.getText());
			activityOutputPortDefinitionBean
					.setMimeTypes(new ArrayList<String>());

			outputBeanList.add(activityOutputPortDefinitionBean);
		}

		final InteractionActivityConfigurationBean newConfiguration = (InteractionActivityConfigurationBean) this
				.cloneBean(this.configuration);
		newConfiguration.setInputPortDefinitions(inputBeanList);
		newConfiguration.setOutputPortDefinitions(outputBeanList);
		String urlText = this.sourceTextField.getText();
		if (!urlText.toLowerCase().matches("^\\w+://.*")) {
			urlText = "http://" + urlText;
		}
		newConfiguration.setPresentationOrigin(urlText);
		newConfiguration.setProgressNotification(this.progressField
				.isSelected());
		return newConfiguration;

	}

	@Override
	public void refreshConfiguration() {
		int visibleTab = -1;
		if (this.tabbedPane != null) {
			visibleTab = this.tabbedPane.getSelectedIndex();
		}
		this.removeAll();
		this.initialise();
		if (visibleTab != -1) {
			this.tabbedPane.setSelectedIndex(visibleTab);
		}
	}

}
