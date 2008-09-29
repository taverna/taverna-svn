package net.sf.taverna.t2.matlabactivity.views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.sf.taverna.t2.workbench.configuration.mimetype.MimeTypeManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.MimeTypeConfig;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 *
 * @author petarj
 */
public class MatActivityOutputViewer extends JPanel {

    private static final long serialVersionUID = -1037886886821164735L;
    private ActivityOutputPortDefinitionBean bean;
    private JTextField nameField;
    private JSpinner depthSpinner;
    private JSpinner granularDepthSpinner;
    private JTextArea mimeTypeText;
    private boolean editable;
    private JButton addMimeTypeButton;
    private JPanel mimeTypePanel;
    private final JPopupMenu mimePopup = new JPopupMenu();
    private final Vector<String> originalMimes = new Vector<String>();
    private JList mimeList;
    private MimeTypeConfig mimeTypeConfig;
    private JButton addMimeButton;
    private JFrame mimeFrame;

    MatActivityOutputViewer(ActivityOutputPortDefinitionBean outputBean,
            boolean editable) {
        this.bean = outputBean;
        setBorder(BorderFactory.createEtchedBorder());
        initView();
        setEditable(editable);
    }

    public ActivityOutputPortDefinitionBean getBean() {
        return bean;
    }

    public void setBean(ActivityOutputPortDefinitionBean bean) {
        this.bean = bean;
    }

    public JSpinner getDepthSpinner() {
        return depthSpinner;
    }

    public void setDepthSpinner(JSpinner depthSpinner) {
        this.depthSpinner = depthSpinner;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        setEditMode();
    }

    public JSpinner getGranularDepthSpinner() {
        return granularDepthSpinner;
    }

    public void setGranularDepthSpinner(JSpinner granularDepthSpinner) {
        this.granularDepthSpinner = granularDepthSpinner;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public void setNameField(JTextField nameField) {
        this.nameField = nameField;
    }

    private void initView() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.BOTH;

        nameField = new JTextField(bean.getName());
        add(nameField, constraints);

        constraints.gridx = 1;
        SpinnerNumberModel depthModel = new SpinnerNumberModel(new Integer(bean.
                getDepth()), new Integer(0),
                new Integer(100), new Integer(1));
        depthSpinner = new JSpinner(depthModel);
        add(depthSpinner, constraints);

        constraints.gridx = 2;
        SpinnerNumberModel granularModel = new SpinnerNumberModel(new Integer(bean.
                getDepth()), new Integer(0),
                new Integer(100), new Integer(1));
        granularDepthSpinner = new JSpinner(granularModel);
        add(granularDepthSpinner, constraints);

        constraints.gridx = 4;
        mimeFrame = new JFrame();
        mimeTypeConfig = new MimeTypeConfig();
        if (bean.getMimeTypes() != null) {
            mimeTypeConfig.setMimeTypeList(bean.getMimeTypes());
        }
        mimeFrame.add(mimeTypeConfig);
        mimeTypeConfig.setVisible(true);
        addMimeButton = new JButton("Add mime type");
//		addMimeButton.addActionListener(new AbstractAction() {
//
//			public void actionPerformed(ActionEvent e) {
//				mimeFrame.setVisible(true);
//			}
//			
//		});
        add(addMimeButton, constraints);
    }

    private JPanel initMimeTypePanel() {
        for (String mimeType : bean.getMimeTypes()) {
            originalMimes.add(mimeType);
        }

        final Map<String, String> propertyMap = MimeTypeManager.getInstance().
                getPropertyMap();
        Set<Entry<String, String>> mimeTypes = propertyMap.entrySet();
        for (Entry<String, String> entry : mimeTypes) {
            final JMenuItem item = new JMenuItem();
            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    originalMimes.add((String) propertyMap.get(item.getText()));
                    mimeList.revalidate();
                    mimePopup.setVisible(false);
                }
            });
            mimePopup.add(item);
        }

        mimeTypePanel = new JPanel();
        mimeTypePanel.setLayout(new GridBagLayout());
        GridBagConstraints mimeConstraints = new GridBagConstraints();
        mimeConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        mimeConstraints.gridx = 0;
        mimeConstraints.gridy = 0;
        mimeConstraints.weightx = 0.1;
        mimeConstraints.weighty = 0;
        mimeConstraints.fill = GridBagConstraints.BOTH;

        addMimeButton = new JButton();
        addMimeButton.setText("Add");
        addMimeButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                mimePopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        mimeList = new JList(originalMimes);

        mimeTypeText = new JTextArea();
        String mimes = "";
        mimeTypeText.setText(mimes);
        mimeTypeText.setEditable(false);

        mimeTypePanel.add(mimeList, mimeConstraints);
        mimeConstraints.gridx = 1;
        mimeTypePanel.add(addMimeButton, mimeConstraints);

        return mimeTypePanel;
    }

    private void setEditMode() {
        depthSpinner.setEnabled(editable);
        granularDepthSpinner.setEnabled(editable);
        nameField.setEditable(editable);
    }

    public MimeTypeConfig getMimeTypeConfig() {
        return mimeTypeConfig;
    }

    public JButton getAddMimeButton() {
        return addMimeButton;
    }

    public JFrame getMimeFrame() {
        return mimeFrame;
    }

    public JPanel getMimeTypePanel() {
        return mimeTypePanel;
    }
}
