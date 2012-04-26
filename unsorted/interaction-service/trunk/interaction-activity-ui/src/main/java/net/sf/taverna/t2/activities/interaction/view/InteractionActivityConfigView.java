/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.view;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
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
public class InteractionActivityConfigView extends
		ActivityConfigurationPanel<InteractionActivity, InteractionActivityConfigurationBean> {
	
	private InteractionActivity activity;
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
	
	public InteractionActivityConfigView(InteractionActivity activity) {
		this.activity = activity;
		configuration = (InteractionActivityConfigurationBean) cloneBean(activity.getConfiguration());
		setLayout(new GridBagLayout());
		initialise();
	}

	private void initialise() {
		CSH
        .setHelpIDString(
                        this,
                        "net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.InteractionConfigView");
configuration = activity.getConfiguration();
inputViewList = new ArrayList<InteractionInputViewer>();
outputViewList = new ArrayList<InteractionOutputViewer>();
setBorder(javax.swing.BorderFactory.createTitledBorder(null, null,
        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        new java.awt.Font("Lucida Grande", 1, 12)));
final InteractionActivityConfigurationBean configBean = activity
        .getConfiguration();



tabbedPane = new JTabbedPane();
tabbedPane.addTab("Interaction source", getSourcePanel());
tabbedPane.addTab("Input ports", new JScrollPane(getInputPanel()));
tabbedPane.addTab("Output ports", new JScrollPane(getOutputPanel()));

GridBagConstraints outerConstraint = new GridBagConstraints();
outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
outerConstraint.gridx = 0;
outerConstraint.gridy = 0;

outerConstraint.fill = GridBagConstraints.BOTH;
outerConstraint.weighty = 0.1;
outerConstraint.weightx = 0.1;
add(tabbedPane, outerConstraint);

setPreferredSize(new Dimension(600,500));

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

        outputGridy = 1;
        outputConstraint.gridx = 0;
        for (ActivityOutputPortDefinitionBean outputBean : configuration
                        .getOutputPortDefinitions()) {
                // FIXME refactor this into a method
                outputConstraint.gridy = outputGridy;
                final InteractionOutputViewer beanshellOutputViewer = new InteractionOutputViewer(
                                outputBean, true);
                outputViewList.add(beanshellOutputViewer);
                outputConstraint.gridx = 0;
                outputConstraint.weightx = 0.1;
                final JTextField nameField = beanshellOutputViewer.getNameField();
                nameField.getDocument().addDocumentListener(new DocumentListener(){
                        public void changedUpdate(DocumentEvent e) {
                    //Plain text components don't fire these events.                                    
                        }
                        public void insertUpdate(DocumentEvent e) {
                                outputsChanged = true;
                        }
                        public void removeUpdate(DocumentEvent e) {             
                                outputsChanged = true;
                        }
                });
                outputEditPanel.add(nameField, outputConstraint);
                outputConstraint.weightx = 0;
                outputConstraint.gridx = 1;
                final JSpinner depthSpinner = beanshellOutputViewer
                                .getDepthSpinner();
                depthSpinner.addChangeListener(new ChangeListener(){

                        public void stateChanged(ChangeEvent e) {
                                outputsChanged = true;
                        }
                });
                outputEditPanel.add(depthSpinner, outputConstraint);
                outputConstraint.gridx = 2;

                final JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(new AbstractAction() {

                        public void actionPerformed(ActionEvent e) {

                                outputsChanged = true;

                                outputViewList.remove(beanshellOutputViewer);
                                outputEditPanel.remove(nameField);
                                outputEditPanel.remove(depthSpinner);
                                // outputEditPanel.remove(granularDepthSpinner);
                                // outputEditPanel.remove(addMimeButton);
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
                                
                                outputsChanged = true;

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
                                final InteractionOutputViewer beanshellOutputViewer = new InteractionOutputViewer(
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

                                final JButton removeButton = new JButton("Remove");
                                removeButton.addActionListener(new AbstractAction() {

                                        public void actionPerformed(ActionEvent e) {
                                                outputViewList.remove(beanshellOutputViewer);
                                                outputEditPanel.remove(nameField);
                                                outputEditPanel.remove(depthSpinner);
                                                // outputEditPanel.remove(granularDepthSpinner);
                                                outputEditPanel.remove(removeButton);
                                                // outputEditPanel.remove(addMimeButton);
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

        return outerOutputPanel;
	}

	private JPanel getInputPanel() {
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
                final InteractionInputViewer beanshellInputViewer = new InteractionInputViewer(
                                inputBean, true);
                inputViewList.add(beanshellInputViewer);
                inputConstraint.gridx = 0;
                final JTextField nameField = beanshellInputViewer.getNameField();
                nameField.getDocument().addDocumentListener(new DocumentListener(){

						public void changedUpdate(DocumentEvent e) {
                    //Plain text components don't fire these events.                                    
                        }
                        public void insertUpdate(DocumentEvent e) {
                                inputsChanged = true;
                        }
                        public void removeUpdate(DocumentEvent e) {             
                                inputsChanged = true;
                        }
                });
                inputConstraint.weightx = 0.1;
                inputEditPanel.add(nameField, inputConstraint);
                inputConstraint.weightx = 0.0;
                inputConstraint.gridx = 1;
                final JSpinner depthSpinner = beanshellInputViewer
                                .getDepthSpinner();
                depthSpinner.addChangeListener(new ChangeListener(){

                        public void stateChanged(ChangeEvent e) {
                                inputsChanged = true;
                        }
                });
                inputEditPanel.add(depthSpinner, inputConstraint);
                inputConstraint.gridx = 2;
                final JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(new AbstractAction() {

						public void actionPerformed(ActionEvent e) {
                                
                                inputsChanged = true;

                                inputViewList.remove(beanshellInputViewer);
                                inputEditPanel.remove(nameField);
                                inputEditPanel.remove(depthSpinner);
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

                        inputsChanged = true;

                        ActivityInputPortDefinitionBean bean = new ActivityInputPortDefinitionBean();
                        bean.setAllowsLiteralValues(true);
                        bean.setDepth(0);
                        List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = new ArrayList<Class<? extends ExternalReferenceSPI>>();
                        // handledReferenceSchemes.add(FileReference.class);
                        bean.setHandledReferenceSchemes(handledReferenceSchemes);
                        List<String> mimeTypes = new ArrayList<String>();
                        mimeTypes.add("text/plain");
                        bean.setMimeTypes(mimeTypes);

                        String name2 = "in" + newInputPortNumber ;
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
                        final InteractionInputViewer beanshellInputViewer = new InteractionInputViewer(
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
                        final JButton removeButton = new JButton("Remove");
                        removeButton.addActionListener(new AbstractAction() {

                                public void actionPerformed(ActionEvent e) {
                                        inputViewList.remove(beanshellInputViewer);
                                        inputEditPanel.remove(nameField);
                                        inputEditPanel.remove(depthSpinner);
                                        inputEditPanel.remove(removeButton);
                                        inputEditPanel.revalidate();
                                        inputEditPanel.repaint();
                                        outerInputPanel.revalidate();
                                        outerInputPanel.repaint();
                                }

                        });
                        inputEditPanel.add(removeButton, inputConstraint);
                        inputEditPanel.revalidate();
                        inputEditPanel.repaint();

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

	private JPanel getSourcePanel() {
		JPanel sourcePanel = new JPanel();
		sourcePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JLabel sourceHtmlLabel = new JLabel("HTML:");
		sourcePanel.add(sourceHtmlLabel, gbc);
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.gridy = 1;
		sourceTextField.setText(configuration.getPresentationOrigin());
		sourceTextField.setColumns(40);
		sourcePanel.add(sourceTextField);
		
		JPanel filler = new JPanel();
        gbc.weightx = 0.1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy++;
        sourcePanel.add(filler, gbc);
		return sourcePanel;
	}

	@Override
	public boolean checkValues() {
		return true;
	}

	@Override
	public InteractionActivityConfigurationBean getConfiguration() {
		return configuration;
	}

	@Override
	public boolean isConfigurationChanged() {
		return (inputsChanged ||
				outputsChanged ||
				!sourceTextField.getText().equals(configuration.getPresentationOrigin()));
	}

	@Override
	public void noteConfiguration() {
		configuration = makeConfiguration();
        inputsChanged = false;
        outputsChanged = false;
	}

	private InteractionActivityConfigurationBean makeConfiguration() {
		// Set the new configuration
        List<ActivityInputPortDefinitionBean> inputBeanList = new ArrayList<ActivityInputPortDefinitionBean>();
        for (InteractionInputViewer inputView : inputViewList) {
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
                                .setDepth((Integer) inputView.getDepthSpinner()
                                                .getValue());
                activityInputPortDefinitionBean.setName(inputView
                                .getNameField().getText());
                inputBeanList.add(activityInputPortDefinitionBean);
        }

        List<ActivityOutputPortDefinitionBean> outputBeanList = new ArrayList<ActivityOutputPortDefinitionBean>();
        for (InteractionOutputViewer outputView : outputViewList) {
                ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
                activityOutputPortDefinitionBean
                                .setDepth((Integer) outputView.getDepthSpinner()
                                                .getValue());
                
//              activityOutputPortDefinitionBean
//                              .setGranularDepth((Integer) outputView
//                                              .getGranularDepthSpinner().getValue());
                
                // NOTE: Granular depth must match output depth because we return
                // the full lists right away
                activityOutputPortDefinitionBean
                                .setGranularDepth(activityOutputPortDefinitionBean
                                                .getDepth());
                
                
                activityOutputPortDefinitionBean.setName(outputView
                                .getNameField().getText());
                activityOutputPortDefinitionBean.setMimeTypes(new ArrayList<String>());

                outputBeanList.add(activityOutputPortDefinitionBean);
        }
        
        InteractionActivityConfigurationBean newConfiguration =
                (InteractionActivityConfigurationBean) cloneBean (configuration);
        newConfiguration
                        .setInputPortDefinitions(inputBeanList);
        newConfiguration
                        .setOutputPortDefinitions(outputBeanList);
        newConfiguration.setPresentationOrigin(sourceTextField.getText());
        return newConfiguration;

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

}
