package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.t2.workbench.configuration.mimetype.MimeTypeManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class BeanshellOutputViewer extends JPanel{
	
	private ActivityOutputPortDefinitionBean bean;
	private JTextField nameField;
	private JSpinner depthSpinner;
	private JSpinner granularDepthSpinner;
	private JTextArea mimeTypeText;
	private boolean editable;
	private final JList mimeDropList = new JList();
	private JButton addMimeTypeButton;
	private JPanel mimeTypePanel;

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
		mimeTypePanel = new JPanel();
		mimeTypePanel.setLayout(new GridBagLayout());
		GridBagConstraints mimeConstraint = new GridBagConstraints();
		mimeConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		mimeConstraint.gridx = 0;
		mimeConstraint.gridy = 0;
		mimeConstraint.weighty = 0;
		mimeConstraint.weightx = 0.1;
		mimeConstraint.fill = GridBagConstraints.BOTH;
		final Map<String, Object> propertyMap = MimeTypeManager.getInstance().getPropertyMap();
		Set<Entry<String, Object>> mimeTypes = propertyMap.entrySet();
		DefaultListModel mimeModel = new DefaultListModel();
		for (Entry entry:mimeTypes) {
			mimeModel.addElement((String) entry.getValue());
		}
		mimeDropList.setModel(mimeModel);
		addMimeTypeButton = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				String text = mimeTypeText.getText();
				text = text + "\n" + propertyMap.get(mimeDropList.getSelectedValue());
				
				// TODO add to original mime list
				
			}
			
		});
		addMimeTypeButton.setText("Add");
		mimeTypeText = new JTextArea();
		String mimes = "";
		List<String> originalMimes = new ArrayList<String>();
		for (String mimeType:bean.getMimeTypes()) {
			originalMimes.add(mimeType);
			mimes = mimes + mimeType +"\n";
		}
		mimeTypeText.setText(mimes);
		mimeTypeText.setEditable(false);
		mimeTypePanel.add(mimeTypeText, mimeConstraint);
		
		JPanel mimeTypePanel2 = new JPanel();
		GridBagConstraints mimeConstraint2 = new GridBagConstraints();
		mimeConstraint2.anchor = GridBagConstraints.FIRST_LINE_START;
		mimeConstraint2.gridx = 0;
		mimeConstraint2.gridy = 0;
		mimeConstraint2.weighty = 0;
		mimeConstraint2.weightx = 0.1;
		mimeConstraint2.fill = GridBagConstraints.BOTH;
		mimeTypePanel2.add(mimeDropList, mimeConstraint2);
		mimeConstraint2.gridx = 1;
		mimeTypePanel2 .add(addMimeTypeButton, mimeConstraint2);
		mimeTypePanel.add(mimeTypeText, mimeConstraint);
		mimeTypePanel.add(mimeTypePanel2, mimeConstraint);
		add(mimeTypePanel, outerConstraint);
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
		this.addMimeTypeButton.setVisible(editable);
		this.mimeDropList.setVisible(editable);
		this.depthSpinner.setEnabled(editable);
		this.granularDepthSpinner.setEnabled(editable);
		this.nameField.setEditable(editable);

	}

	public JPanel getMimeTypePanel() {
		return mimeTypePanel;
	}

}
