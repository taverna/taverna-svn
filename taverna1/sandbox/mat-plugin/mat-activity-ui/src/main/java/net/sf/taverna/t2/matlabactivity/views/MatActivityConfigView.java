package net.sf.taverna.t2.matlabactivity.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import net.sf.taverna.t2.activities.matlab.MatActivity;
import net.sf.taverna.t2.activities.matlab.MatActivityConfigurationBean;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import org.syntax.jedit.JEditTextArea;

/**
 *
 * @author petarj
 */
public class MatActivityConfigView extends JPanel {

    private static final long serialVersionUID = -2496534921297153744L;
    private List<MatActivityInputViewer> inputViewerList;
    private List<MatActivityOutputViewer> outputViewerList;
    private MatActivity activity;
    private MatActivityConfigurationBean configuration;
    private boolean configChanged = false;
    private JButton button;
    private ActionListener buttonClicked;
    private int inputGridy;
    private int outputGridy;
    private JPanel outerInputPanel;
    private JPanel outerOutputPanel;
    private int newInputPortNumber = 0;
    private int newOutputPortNumber = 0;
    private JEditTextArea scriptText;

    public MatActivityConfigView(MatActivity activity) {
        this.activity = activity;
        configuration = activity.getConfiguration();
        initialise();
    }

    public boolean isConfigurationChanged() {
        return configChanged;
    }

    public void setButtonClickedListener(ActionListener listener) {
        buttonClicked = listener;
    }

    public MatActivityConfigurationBean getConfiguration() {
        return configuration;
    }

    private AbstractAction getOKAction() {
        return new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                List<ActivityInputPortDefinitionBean> inputBeans = new ArrayList<ActivityInputPortDefinitionBean>();
                for (MatActivityInputViewer inputView : inputViewerList) {
                    ActivityInputPortDefinitionBean activityInputPortDefinitionBean = new ActivityInputPortDefinitionBean();
                    activityInputPortDefinitionBean.setHandledReferenceSchemes(
                            inputView.getBean().getHandledReferenceSchemes());
                    activityInputPortDefinitionBean.setMimeTypes(inputView.
                            getBean().getMimeTypes());
                    activityInputPortDefinitionBean.setTranslatedElementType(
                            inputView.getBean().getTranslatedElementType());
                    activityInputPortDefinitionBean.setAllowsLiteralValues(
                            (Boolean) inputView.getLiteralSelector().
                            getSelectedItem());
                    activityInputPortDefinitionBean.setDepth((Integer) inputView.
                            getDepthSpinner().getValue());
                    activityInputPortDefinitionBean.setName(inputView.
                            getNameField().getText());
                    inputBeans.add(activityInputPortDefinitionBean);
                }

                List<ActivityOutputPortDefinitionBean> outputBeans = new ArrayList<ActivityOutputPortDefinitionBean>();
                for (MatActivityOutputViewer outputView : outputViewerList) {
                    ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
                    activityOutputPortDefinitionBean.setDepth((Integer) outputView.
                            getDepthSpinner().getValue());
                    activityOutputPortDefinitionBean.setGranularDepth((Integer) outputView.
                            getGranularDepthSpinner().getValue());
                    activityOutputPortDefinitionBean.setName(outputView.
                            getNameField().getText());
                    activityOutputPortDefinitionBean.setMimeTypes(outputView.
                            getMimeTypeConfig().getMimeTypeList());
                    //TODO something about mime types...
                    outputBeans.add(activityOutputPortDefinitionBean);
                }
                MatActivityConfigurationBean matActivityConfigurationBean = new MatActivityConfigurationBean();
                matActivityConfigurationBean.setSctipt(scriptText.getText());
                matActivityConfigurationBean.setInputPortDefinitions(
                        inputBeans);
                matActivityConfigurationBean.setOutputPortDefinitions(
                        outputBeans);
                
                configuration = matActivityConfigurationBean;
                configChanged = true;
                //setVisible(false);
                buttonClicked.actionPerformed(e);
            }
        };
    }

    private JPanel initInputsPanel() {
        final JPanel inputEditPanel = new JPanel(new GridBagLayout());
        inputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.
                createEtchedBorder(), "Inputs"));

        final GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        inputConstraints.gridx = 0;
        inputConstraints.gridy = 0;
        inputConstraints.weightx = 0.1;
        inputConstraints.fill = GridBagConstraints.BOTH;

        inputEditPanel.add(new JLabel("Name"), inputConstraints);
        inputConstraints.gridx = 1;
        inputEditPanel.add(new JLabel("Depth"), inputConstraints);

        inputGridy = 1;
        inputConstraints.gridx = 0;

        for (ActivityInputPortDefinitionBean inputBean : configuration.
                getInputPortDefinitions()) {
            inputConstraints.gridy = inputGridy;
            final MatActivityInputViewer inputViewer = new MatActivityInputViewer(
                    inputBean, true);
            inputViewerList.add(inputViewer);
            inputConstraints.gridx = 0;
            final JTextField nameField = inputViewer.getNameField();
            inputConstraints.weightx = 0.1;
            inputEditPanel.add(nameField, inputConstraints);
            inputConstraints.weightx = 0.0;
            inputConstraints.gridx = 1;
            final JSpinner depthSpinner = inputViewer.getDepthSpinner();
            inputEditPanel.add(depthSpinner, inputConstraints);
            inputConstraints.gridx = 2;
            final JButton removeButton = new JButton("remove");
            removeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    inputViewerList.remove(inputViewer);
                    inputEditPanel.remove(nameField);
                    inputEditPanel.remove(depthSpinner);
                    inputEditPanel.remove(removeButton);
                    outerInputPanel.revalidate();
                }
            });
            inputEditPanel.add(removeButton, inputConstraints);
            inputGridy++;
        }

        outerInputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints outerPanelConstraints = new GridBagConstraints();
        outerPanelConstraints.gridx = 0;
        outerPanelConstraints.gridy = 0;
        outerPanelConstraints.weightx = 0.1;
        outerPanelConstraints.weighty = 0.1;
        outerPanelConstraints.fill = GridBagConstraints.BOTH;
        outerInputPanel.add(new JScrollPane(inputEditPanel),
                outerPanelConstraints);
        outerPanelConstraints.weighty = 0;
        JButton addInputPortButton = new JButton(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                ActivityInputPortDefinitionBean bean = new ActivityInputPortDefinitionBean();
                bean.setAllowsLiteralValues(true);
                bean.setDepth(0);
                List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = new ArrayList<Class<? extends ExternalReferenceSPI>>();

                bean.setHandledReferenceSchemes(handledReferenceSchemes);
                List<String> mimeTypes = new ArrayList<String>();
                mimeTypes.add("text/plain");    //FIXME this is not right!!!
                bean.setMimeTypes(mimeTypes);
                bean.setName("newInputPort" + newInputPortNumber);
                newInputPortNumber++;
                bean.setTranslatedElementType(String.class);
                inputConstraints.gridy = inputGridy;
                final MatActivityInputViewer inputViewer = new MatActivityInputViewer(
                        bean, true);
                inputViewerList.add(inputViewer);
                inputConstraints.weightx = 0.1;
                inputConstraints.gridx = 0;
                final JTextField nameField = inputViewer.getNameField();
                inputEditPanel.add(nameField, inputConstraints);
                inputConstraints.weightx = 0;
                inputConstraints.gridx = 1;
                final JSpinner depthSpinner = inputViewer.getDepthSpinner();
                inputEditPanel.add(depthSpinner, inputConstraints);
                inputConstraints.gridx = 2;
                final JButton removeButton = new JButton("remove");
                removeButton.addActionListener(new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        inputViewerList.remove(inputViewer);
                        inputEditPanel.remove(nameField);
                        inputEditPanel.remove(depthSpinner);
                        inputEditPanel.remove(removeButton);
                        inputEditPanel.revalidate();
                        outerInputPanel.revalidate();
                    }
                });
                inputEditPanel.add(removeButton, inputConstraints);
                inputEditPanel.revalidate();

                inputGridy++;
            }
        });

        addInputPortButton.setText("Add Port");
        JPanel buttonPanel = new JPanel(new GridBagLayout());

        JPanel filler = new JPanel();
        outerPanelConstraints.weightx = 0.1;
        outerPanelConstraints.weighty = 0;
        outerPanelConstraints.gridx = 0;
        outerPanelConstraints.gridy = 0;

        buttonPanel.add(filler, outerPanelConstraints);

        outerPanelConstraints.weightx = 0;
        outerPanelConstraints.weighty = 0;
        outerPanelConstraints.gridx = 1;
        outerPanelConstraints.gridy = 0;

        buttonPanel.add(addInputPortButton, outerPanelConstraints);

        outerPanelConstraints.weightx = 0;
        outerPanelConstraints.weighty = 0;
        outerPanelConstraints.gridx = 0;
        outerPanelConstraints.gridy = 1;
        outerPanelConstraints.fill = GridBagConstraints.BOTH;
        outerInputPanel.add(buttonPanel, outerPanelConstraints);

        return outerInputPanel;
    }

    private JPanel initOutputsPanel() {
        final JPanel outputEditPanel = new JPanel(new GridBagLayout());
        outputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.
                createEtchedBorder(), "Outputs"));

        final GridBagConstraints outputConstraints = new GridBagConstraints();
        outputConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        outputConstraints.gridx = 0;
        outputConstraints.gridy = 0;
        outputConstraints.weightx = 0.1;
        outputConstraints.weighty = 0.1;
        outputConstraints.fill = GridBagConstraints.BOTH;
        outputConstraints.weighty = 0;
        outputEditPanel.add(new JLabel("Name"), outputConstraints);
        outputConstraints.gridx = 1;
        outputEditPanel.add(new JLabel("Depth"), outputConstraints);
        outputConstraints.gridx = 2;
        outputEditPanel.add(new JLabel("GranularDepth"), outputConstraints);

        outputGridy = 1;
        outputConstraints.gridx = 0;
        for (ActivityOutputPortDefinitionBean outputBean : configuration.
                getOutputPortDefinitions()) {
            outputConstraints.gridy = outputGridy;
            final MatActivityOutputViewer outputViewer = new MatActivityOutputViewer(
                    outputBean, true);
            outputViewerList.add(outputViewer);
            outputConstraints.gridx = 0;
            outputConstraints.weightx = 0.1;
            final JTextField nameField = outputViewer.getNameField();
            outputEditPanel.add(nameField, outputConstraints);
            outputConstraints.weightx = 0;
            outputConstraints.gridx = 1;
            final JSpinner depthSpinner = outputViewer.getDepthSpinner();
            outputEditPanel.add(depthSpinner, outputConstraints);
            outputConstraints.gridx = 2;
            final JSpinner granularDepthSpinner = outputViewer.
                    getGranularDepthSpinner();
            outputEditPanel.add(granularDepthSpinner, outputConstraints);
            outputConstraints.gridx = 3;

            // something about MIME...

            //outputConstraints.gridx=4;
            final JButton removeButton = new JButton("remove");
            removeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    outputViewerList.remove(outputViewer);
                    outputEditPanel.remove(nameField);
                    outputEditPanel.remove(depthSpinner);
                    outputEditPanel.remove(granularDepthSpinner);
                    outputEditPanel.remove(removeButton);
                    outputEditPanel.revalidate();
                    outerOutputPanel.revalidate();
                }
            });
            outputEditPanel.add(removeButton, outputConstraints);
            outputGridy++;
        }

        outerOutputPanel = new JPanel();
        outerOutputPanel.setLayout(new GridBagLayout());
        GridBagConstraints outerPanelConstraints = new GridBagConstraints();
        // outerPanelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        outerPanelConstraints.fill = GridBagConstraints.BOTH;
        outerPanelConstraints.gridx = 0;
        outerPanelConstraints.gridy = 0;
        outerPanelConstraints.weightx = 0.1;
        outerPanelConstraints.weighty = 0.1;
        outerOutputPanel.add(new JScrollPane(outputEditPanel),
                outerPanelConstraints);
        outerPanelConstraints.weighty = 0;

        JButton addOutputPortButton = new JButton("AddPort");
        addOutputPortButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //TODO any exceptioins thrown???
                ActivityOutputPortDefinitionBean bean = new ActivityOutputPortDefinitionBean();
                bean.setDepth(0);
                bean.setGranularDepth(0);
                List<String> mimeTypes = new ArrayList<String>();
                mimeTypes.add("text/plain"); //FIXME this is not righ either!
                bean.setMimeTypes(mimeTypes);
                bean.setName("newOutput" + newOutputPortNumber);
                final MatActivityOutputViewer outputViewer = new MatActivityOutputViewer(
                        bean, true);
                outputViewerList.add(outputViewer);

                outputConstraints.gridx = 0;
                outputConstraints.gridy = outputGridy;
                final JTextField nameField = outputViewer.getNameField();
                outputConstraints.weightx = 0.1;
                outputEditPanel.add(nameField, outputConstraints);
                outputConstraints.gridx = 1;
                outputConstraints.weightx = 0;
                final JSpinner depthSpinner = outputViewer.getDepthSpinner();
                outputEditPanel.add(depthSpinner, outputConstraints);
                outputConstraints.gridx = 2;
                final JSpinner granularDepthSpinner = outputViewer.
                        getGranularDepthSpinner();
                outputEditPanel.add(granularDepthSpinner, outputConstraints);
                outputConstraints.gridx = 3;
                //mime stuff...
                //outputConstraints.gridx=4;
                final JButton removeButton = new JButton("remove");
                removeButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        outputViewerList.remove(outputViewer);
                        outputEditPanel.remove(nameField);
                        outputEditPanel.remove(depthSpinner);
                        outputEditPanel.remove(granularDepthSpinner);
                        outputEditPanel.remove(removeButton);
                        outputEditPanel.revalidate();
                    }
                });
                outputEditPanel.add(removeButton, outputConstraints);
                outputEditPanel.revalidate();
                newOutputPortNumber++;
                outputGridy++;
            }
        });
        JPanel buttonPanel = new JPanel(new GridBagLayout());

        JPanel filler = new JPanel();
        outerPanelConstraints.weightx = 0.1;
        outerPanelConstraints.weighty = 0;
        outerPanelConstraints.gridx = 0;
        outerPanelConstraints.gridy = 0;

        buttonPanel.add(filler, outerPanelConstraints);

        outerPanelConstraints.weightx = 0;
        outerPanelConstraints.weighty = 0;
        outerPanelConstraints.gridx = 1;
        outerPanelConstraints.gridy = 0;

        buttonPanel.add(addOutputPortButton, outerPanelConstraints);

        outerPanelConstraints.weightx = 0;
        outerPanelConstraints.weighty = 0;
        outerPanelConstraints.gridx = 0;
        outerPanelConstraints.gridy = 1;
        outerPanelConstraints.fill = GridBagConstraints.BOTH;
        outerOutputPanel.add(buttonPanel, outerPanelConstraints);
        outerPanelConstraints.gridx = 1;
        outerPanelConstraints.gridy = 0;

        return outerOutputPanel;
    }

    private JTabbedPane initPortsPanel() {
        JTabbedPane ports = new JTabbedPane();
        JPanel inputsPanel = initInputsPanel();
        JPanel outputsPanel = initOutputsPanel();
        ports.add("Input ports", inputsPanel);
        ports.add("Output Ports", outputsPanel);

        return ports;
    }

    private JPanel initScriptPanel() {
        JPanel scriptEditPanel = new JPanel(new BorderLayout());
        scriptText = new JEditTextArea();
        scriptText.setTokenMarker(new MatlabTokenMarker());
        scriptText.setText(configuration.getSctipt());
        scriptText.setCaretPosition(0);
        scriptText.setPreferredSize(new Dimension(0, 0));
        scriptText.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(
                    FocusEvent e) {
                configuration.setSctipt(scriptText.getText());
            }
        });

        scriptEditPanel.add(scriptText, BorderLayout.CENTER);

        return scriptEditPanel;
    }

    private void initialise() {
        //TODO: that javahelp thing...
        setSize(500,
                500);
        setLayout(new GridBagLayout());
        AbstractAction okAction = getOKAction();
        button = new JButton(okAction);
        button.setText("OK");
        button.setToolTipText("Click to configure with the new values");
        inputViewerList = new ArrayList<MatActivityInputViewer>();
        outputViewerList = new ArrayList<MatActivityOutputViewer>();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;

        JPanel scriptEditPanel = initScriptPanel();
        JTabbedPane portsPanel = initPortsPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Script", scriptEditPanel);
        tabbedPane.add("Ports", portsPanel);

        add(tabbedPane, constraints);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(button);

        JButton cancelButton = new JButton(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                configChanged = false;
                buttonClicked.actionPerformed(e);
            }
        });
        cancelButton.setText("Cancel");
        buttonPanel.add(cancelButton);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.gridy = 2;
        constraints.weighty = 0;

        add(buttonPanel, constraints);
    }
}
