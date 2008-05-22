package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;

public class BeanshellInputViewer extends JPanel {

	private ActivityInputPortDefinitionBean bean;

	private JTextField nameField;

	private JSpinner depthSpinner;

	private JTextArea refSchemeText;

	private JTextArea mimeTypeText;

	private JLabel translatedType;

	private JComboBox literalSelector;

	private boolean editable;

	public BeanshellInputViewer(ActivityInputPortDefinitionBean bean,
			boolean editable) {
		this.bean = bean;
		this.editable = editable;
		setBorder(javax.swing.BorderFactory.createEtchedBorder());
		initView();
		setEditMode();
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

		Vector<Boolean> literalSelectorList = new Vector<Boolean>();
		literalSelectorList.add(true);
		literalSelectorList.add(false);
		literalSelector = new JComboBox(literalSelectorList);
		if (!bean.getAllowsLiteralValues()) {
			literalSelector.setSelectedIndex(1);
		}
		outerConstraint.gridx = 1;
		add(literalSelector, outerConstraint);

		outerConstraint.gridx = 2;
		SpinnerNumberModel model = new SpinnerNumberModel(new Integer(bean
				.getDepth()), new Integer(0), new Integer(100), new Integer(1));
		depthSpinner = new JSpinner(model);
		depthSpinner.setEnabled(false);
		// depthSpinner.setValue(bean.getDepth());

		add(depthSpinner, outerConstraint);

		outerConstraint.gridx = 3;
		refSchemeText = new JTextArea();
		String refs = "";
		for (Object refScheme : bean.getHandledReferenceSchemes()) {
			refs = refs + refScheme.getClass().getSimpleName() + "\n";
		}
		refSchemeText.setText(refs);
		refSchemeText.setEditable(false);
		refSchemeText.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		add(refSchemeText, outerConstraint);

		outerConstraint.gridx = 4;
		mimeTypeText = new JTextArea();
		String mimes = "";
		for (String mimeType : bean.getMimeTypes()) {
			mimes = mimes + mimeType + "\n";
		}
		mimeTypeText.setText(mimes);
		mimeTypeText.setEditable(false);
		mimeTypeText.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		add(mimeTypeText, outerConstraint);

		outerConstraint.gridx = 5;
		translatedType = new JLabel(bean.getTranslatedElementType()
				.getSimpleName());
		add(translatedType, outerConstraint);
	}

	public JTextField getNameField() {
		return nameField;
	}

	public JTextArea getRefSchemeText() {
		return refSchemeText;
	}

	public JTextArea getMimeTypeText() {
		return mimeTypeText;
	}

	public JLabel getTranslatedType() {
		return translatedType;
	}

	public JComboBox getLiteralSelector() {
		return literalSelector;
	}

	public JSpinner getDepthSpinner() {
		return depthSpinner;
	}

	public ActivityInputPortDefinitionBean getBean() {
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
		this.nameField.setEditable(editable);
		this.literalSelector.setEnabled(editable);
		this.depthSpinner.setEnabled(editable);

	}

}
