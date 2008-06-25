package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.workbench.configuration.mimetype.MimeTypeManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 * A view representing {@link ActivityInputPortDefinitionBean}s of a
 * {@link BeanshellActivity} and the various parts which can be edited,
 * primarily the name, depth and granular depth.
 * 
 * @author Ian Dunlop
 * 
 */
@SuppressWarnings("serial")
public class BeanshellOutputViewer extends JPanel {
	/** The bean which defines this view */
	private ActivityOutputPortDefinitionBean bean;
	/** The name of the port */
	private JTextField nameField;
	/** The depth of the port */
	private JSpinner depthSpinner;
	/** The granular depth of the port */
	private JSpinner granularDepthSpinner;
	/** The mime types which the output port can handle */
	private JTextArea mimeTypeText;
	/** Whether the values in the bean can be edited */
	private boolean editable;
	// private final JList mimeDropList = new JList();
	private JButton addMimeTypeButton;
	private JPanel mimeTypePanel;
	private final JPopupMenu mimePopup = new JPopupMenu();
	private final Vector<String> originalMimes = new Vector<String>();
	private JList mimeList;
	
	private MimeTypeConfig mimeTypeConfig;
	private JButton addMimeButton;

	/**
	 * Sets the look and feel of the view through {@link #initView()} and sets
	 * the edit state using {@link #editable}
	 * 
	 * @param bean
	 *            One of the output ports of the overall activity
	 * @param editable
	 *            whether the values of the bean are editable
	 */
	public BeanshellOutputViewer(ActivityOutputPortDefinitionBean bean,
			boolean editable) {
		this.bean = bean;
		this.editable = editable;
		setBorder(javax.swing.BorderFactory.createEtchedBorder());
		initView();
		setEditable(editable);
	}

	/**
	 * Uses {@link GridBagLayout} for the layout. Adds components to edit the
	 * name, depth and granular depth
	 */
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
		SpinnerNumberModel depthModel = new SpinnerNumberModel(new Integer(bean
				.getDepth()), new Integer(0), new Integer(100), new Integer(1));
		depthSpinner = new JSpinner(depthModel);
		// depthSpinner.setValue(bean.getDepth());
		add(depthSpinner, outerConstraint);

		outerConstraint.gridx = 2;
		SpinnerNumberModel granularModel = new SpinnerNumberModel(new Integer(
				bean.getDepth()), new Integer(0), new Integer(100),
				new Integer(1));
		granularDepthSpinner = new JSpinner(granularModel);
		// granularDepthSpinner.setValue(bean.getGranularDepth());
		add(granularDepthSpinner, outerConstraint);

		outerConstraint.gridx = 3;
		
		outerConstraint.gridx = 4;
		
		mimeTypeConfig = new MimeTypeConfig();
		addMimeButton = new JButton("Add mime type");
		addMimeButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				mimeTypeConfig.setVisible(true);
			}
			
		});
		add(addMimeButton, outerConstraint);

		// add(setMimeTypePanel(), outerConstraint);
	}

	/**
	 * Get the component which edits the name of the
	 * {@link ActivityOutputPortDefinitionBean}
	 * 
	 * @return
	 */
	public JTextField getNameField() {
		return nameField;
	}

	/**
	 * The component which allows the depth of the
	 * {@link ActivityOutputPortDefinitionBean} to be changed
	 * 
	 * @return
	 */
	public JSpinner getDepthSpinner() {
		return depthSpinner;
	}

	/**
	 * The component which allows the granular depth of the
	 * {@link ActivityOutputPortDefinitionBean} to be changed
	 * 
	 * @return
	 */
	public JSpinner getGranularDepthSpinner() {
		return granularDepthSpinner;
	}

	/**
	 * The mime types which are handled by this
	 * {@link ActivityOutputPortDefinitionBean}
	 * 
	 * @return
	 */
	public JTextArea getMimeTypeText() {
		return mimeTypeText;
	}

	/**
	 * The actual {@link ActivityOutputPortDefinitionBean} described by this
	 * view
	 * 
	 * @return
	 */
	public ActivityOutputPortDefinitionBean getBean() {
		return bean;
	}

	/**
	 * Can the bean be edited by this view?
	 * 
	 * @return
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Set the editable state of the view
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
		setEditMode();
	}

	/**
	 * Sets the depth, granular depth and name components to be editable
	 */
	public void setEditMode() {
		// this.addMimeTypeButton.setVisible(editable);
		// this.mimeDropList.setVisible(editable);
		this.depthSpinner.setEnabled(editable);
		this.granularDepthSpinner.setEnabled(editable);
		this.nameField.setEditable(editable);

	}

	/**
	 * The panel which has all the mime type components in it
	 * 
	 * @return
	 */
	public JPanel getMimeTypePanel() {
		return mimeTypePanel;
	}

	@SuppressWarnings("serial")
	private JPanel setMimeTypePanel() {

		for (String mimeType : bean.getMimeTypes()) {
			originalMimes.add(mimeType);
		}

		final Map<String, Object> propertyMap = MimeTypeManager.getInstance()
				.getPropertyMap();
		Set<Entry<String, Object>> mimeTypes = propertyMap.entrySet();
		for (Entry<String,Object> entry : mimeTypes) {
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

	public MimeTypeConfig getMimeTypeConfig() {
		return mimeTypeConfig;
	}

	public JButton getAddMimeButton() {
		return addMimeButton;
	}

}
