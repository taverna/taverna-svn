package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class BeanshellOutputViewer extends JPanel{
	
	private ActivityOutputPortDefinitionBean bean;
	private JTextField nameField;
	private JSpinner depthSpinner;
	private JSpinner granularDepthSpinner;
	private JTextArea mimeTypeText;

	public BeanshellOutputViewer(ActivityOutputPortDefinitionBean bean) {
		this.bean = bean;
		setBorder(javax.swing.BorderFactory.createEtchedBorder());
		initView();
	}

	private void initView() {
		setLayout(new GridBagLayout());
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		outerConstraint.weighty = 0;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;

		nameField = new JTextField(bean.getName());
		add(nameField, outerConstraint);
		
		outerConstraint.gridx = 1;
		depthSpinner = new JSpinner();
		depthSpinner.setValue(bean.getDepth());
		add(depthSpinner, outerConstraint);
		
		outerConstraint.gridx = 2;
		granularDepthSpinner = new JSpinner();
		granularDepthSpinner.setValue(bean.getGranularDepth());
		add(granularDepthSpinner, outerConstraint);
		
		outerConstraint.gridx = 3;
		mimeTypeText = new JTextArea();
		String mimes = "";
		for (String mimeType:bean.getMimeTypes()) {
			mimes = mimes + mimeType +"\n";
		}
		mimeTypeText.setText(mimes);
		mimeTypeText.setEditable(false);
		add(mimeTypeText, outerConstraint);
	}

	public JTextField getNameField() {
		return nameField;
	}

	public JSpinner getDepthSpinner() {
		return depthSpinner;
	}

	public JSpinner getGranularDepthSpinner() {
		return granularDepthSpinner;
	}

	public JTextArea getMimeTypeText() {
		return mimeTypeText;
	}

}
