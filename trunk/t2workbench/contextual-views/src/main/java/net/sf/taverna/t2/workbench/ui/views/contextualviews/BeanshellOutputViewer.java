package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class BeanshellOutputViewer extends JPanel{
	
	private ActivityOutputPortDefinitionBean bean;
	private JTextField nameField;
	private JSpinner depthSpinner;
	private JSpinner granularDepthSpinner;
	private JTextArea mimeTypeText;
	private boolean editable;

	public BeanshellOutputViewer(ActivityOutputPortDefinitionBean bean, boolean editable) {
		this.bean = bean;
		this.editable = editable;
		setBorder(javax.swing.BorderFactory.createEtchedBorder());
		initView();
		setEditable(editable);
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
		SpinnerNumberModel depthModel = new SpinnerNumberModel(new Integer(bean.getDepth()), new Integer(0), new Integer(100), new Integer(1));
		depthSpinner = new JSpinner(depthModel);
//		depthSpinner.setValue(bean.getDepth());
		add(depthSpinner, outerConstraint);
		
		outerConstraint.gridx = 2;
		SpinnerNumberModel granularModel = new SpinnerNumberModel(new Integer(bean.getDepth()), new Integer(0), new Integer(100), new Integer(1));
		granularDepthSpinner = new JSpinner(granularModel);
//		granularDepthSpinner.setValue(bean.getGranularDepth());
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

	public ActivityOutputPortDefinitionBean getBean() {
		return bean;
	}
	
	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
		setEditMode();
	}
	
	public void setEditMode() {
		this.depthSpinner.setEnabled(editable);
		this.granularDepthSpinner.setEnabled(editable);
		this.nameField.setEditable(editable);

	}

}
