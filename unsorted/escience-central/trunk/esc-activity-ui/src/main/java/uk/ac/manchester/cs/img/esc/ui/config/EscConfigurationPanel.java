package uk.ac.manchester.cs.img.esc.ui.config;

import java.awt.GridLayout;
import java.net.URI;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import uk.ac.manchester.cs.img.esc.EscActivity;
import uk.ac.manchester.cs.img.esc.EscActivityConfigurationBean;


@SuppressWarnings("serial")
public class EscConfigurationPanel
		extends
		ActivityConfigurationPanel<EscActivity, EscActivityConfigurationBean> {

	private EscActivity activity;
	private EscActivityConfigurationBean configBean;
	private JCheckBox produceReportBox;
	private JCheckBox produceWorkflowBox;
	private JCheckBox debugBox;
	private JTextField intervalField;

	public EscConfigurationPanel(EscActivity activity) {
		this.activity = activity;
		configBean = activity.getConfiguration();
		initGui();
	}

	protected void initGui() {
		removeAll();
		setLayout(new GridLayout(0, 2));

		JLabel reportString = new JLabel("Produce report:");
		add(reportString);
		produceReportBox = new JCheckBox();
		produceReportBox.setSelected(configBean.isProduceReport());
		add(produceReportBox);
		reportString.setLabelFor(produceReportBox);
		
		JLabel workflowString = new JLabel("Produce workflow:");
		add(workflowString);
		produceWorkflowBox = new JCheckBox();
		produceWorkflowBox.setSelected(configBean.isProduceWorkflow());
		add(produceWorkflowBox);
		workflowString.setLabelFor(produceWorkflowBox);

		JLabel debugString = new JLabel("Debug:");
		add(debugString);
		debugBox = new JCheckBox();
		debugBox.setSelected(configBean.isDebug());
		add(debugBox);
		debugString.setLabelFor(debugBox);

		JLabel intervalString = new JLabel("Polling interval:");
		add(intervalString);
		intervalField = new JTextField(5);
		intervalField.setText(Integer.toString(configBean.getPollingInterval()));
		add(intervalField);
		intervalString.setLabelFor(intervalField);

		// Populate fields from activity configuration bean
		refreshConfiguration();
	}

	/**
	 * Check that user values in UI are valid
	 */
	@Override
	public boolean checkValues() {
		int pollingValue = 0;
		try {
			pollingValue = Integer.parseInt(intervalField.getText());
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, ex.getCause().getMessage(),
					"Invalid polling interval", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (pollingValue <= 0) {
			JOptionPane.showMessageDialog(this, "",
					"Invalid polling interval", JOptionPane.ERROR_MESSAGE);
			return false;			
		}
		// All valid, return true
		return true;
	}

	/**
	 * Return configuration bean generated from user interface last time
	 * noteConfiguration() was called.
	 */
	@Override
	public EscActivityConfigurationBean getConfiguration() {
		// Should already have been made by noteConfiguration()
		return configBean;
	}

	/**
	 * Check if the user has changed the configuration from the original
	 */
	@Override
	public boolean isConfigurationChanged() {
		EscActivityConfigurationBean currentConfig = activity.getConfiguration();
		return (produceReportBox.isSelected() != currentConfig.isProduceReport()) ||
			(produceWorkflowBox.isSelected() != currentConfig.isProduceWorkflow()) ||
			(debugBox.isSelected() != currentConfig.isDebug()) ||
			(Integer.parseInt(intervalField.getText()) != currentConfig.getPollingInterval());
	}

	/**
	 * Prepare a new configuration bean from the UI, to be returned with
	 * getConfiguration()
	 */
	@Override
	public void noteConfiguration() {
		configBean = new EscActivityConfigurationBean();
		EscActivityConfigurationBean currentConfig = activity.getConfiguration();
		configBean.setDebug(debugBox.isSelected());
		configBean.setId(currentConfig.getId());
		configBean.setName(currentConfig.getName());
		configBean.setPollingInterval(Integer.parseInt(intervalField.getText()));
		configBean.setProduceReport(produceReportBox.isSelected());
		configBean.setProduceWorkflow(produceWorkflowBox.isSelected());
		configBean.setUrl(currentConfig.getUrl());
	}

	/**
	 * Update GUI from a changed configuration bean (perhaps by undo/redo).
	 * 
	 */
	@Override
	public void refreshConfiguration() {
		configBean = activity.getConfiguration();
		intervalField.setText(Integer.toString(configBean.getPollingInterval()));
		produceReportBox.setSelected(configBean.isProduceReport());
		produceWorkflowBox.setSelected(configBean.isProduceWorkflow());
		debugBox.setSelected(configBean.isDebug());
	}
}
