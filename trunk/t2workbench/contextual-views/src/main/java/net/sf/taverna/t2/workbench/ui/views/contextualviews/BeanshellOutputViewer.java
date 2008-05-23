package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
	private final JPopupMenu mimePopup = new JPopupMenu();
	private final Vector<String> originalMimes = new Vector<String>();
	private JList mimeList;
	

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
		
//		add(setMimeTypePanel(), outerConstraint);
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
//		this.addMimeTypeButton.setVisible(editable);
//		this.mimeDropList.setVisible(editable);
		this.depthSpinner.setEnabled(editable);
		this.granularDepthSpinner.setEnabled(editable);
		this.nameField.setEditable(editable);

	}

	public JPanel getMimeTypePanel() {
		return mimeTypePanel;
	}
	
	private JPanel setMimeTypePanel() {
		
		for (String mimeType:bean.getMimeTypes()) {
			originalMimes.add(mimeType);
		}
		
		final Map<String, Object> propertyMap = MimeTypeManager.getInstance().getPropertyMap();
		Set<Entry<String, Object>> mimeTypes = propertyMap.entrySet();
		DefaultListModel mimeModel = new DefaultListModel();
		for (Entry entry:mimeTypes) {
			final JMenuItem item = new JMenuItem();
			item.setText((String) entry.getValue());
			item.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent arg0) {
					System.out.println(item.getText());
					originalMimes.add((String) propertyMap.get(item.getText()));
					mimeList.revalidate();
					mimePopup.setVisible(false);
				}
				
			});
			mimePopup.add(item);
			
		}
		
		mimeTypePanel = new JPanel();
		mimeTypePanel.setLayout(new GridBagLayout());
		GridBagConstraints mimeConstraint = new GridBagConstraints();
		mimeConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		mimeConstraint.gridx = 0;
		mimeConstraint.gridy = 0;
		mimeConstraint.weighty = 0;
		mimeConstraint.weightx = 0.1;
		mimeConstraint.fill = GridBagConstraints.BOTH;
		
		addMimeTypeButton = new JButton();
		addMimeTypeButton.setText("Add");
		addMimeTypeButton.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent arg0) {
				mimePopup.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			}
			
		});
		mimeList = new JList(originalMimes);
		
		mimeTypeText = new JTextArea();
		String mimes = "";
		mimeTypeText.setText(mimes);
		mimeTypeText.setEditable(false);
		
		mimeTypePanel.add(mimeList, mimeConstraint);
		mimeConstraint.gridx = 1;
		mimeTypePanel.add(addMimeTypeButton, mimeConstraint);
		return mimeTypePanel;
	}

}
