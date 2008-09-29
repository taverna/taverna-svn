/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.t2.matlabactivity.views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;

/**
 *
 * @author user
 */
class MatActivityInputViewer extends JPanel {

    private static final long serialVersionUID = 1941239337441422779L;
    private ActivityInputPortDefinitionBean bean;
    private JTextField nameField;
    private JSpinner depthSpinner;
    private JTextArea refSchemeText;
    private JTextArea mimeTypeText;
    private JLabel translatedType;
    private JComboBox literalSelector;
    private boolean editable;

    public MatActivityInputViewer(ActivityInputPortDefinitionBean bean,
            boolean editable) {
        this.bean = bean;
        this.editable = editable;
        setBorder(BorderFactory.createEtchedBorder());
        initView();
        setEditMode();
    }

    public ActivityInputPortDefinitionBean getBean() {
        return bean;
    }

    public void setBean(ActivityInputPortDefinitionBean bean) {
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

    public JComboBox getLiteralSelector() {
        return literalSelector;
    }

    public void setLiteralSelector(JComboBox literalSelector) {
        this.literalSelector = literalSelector;
    }

    public JTextArea getMimeTypeText() {
        return mimeTypeText;
    }

    public void setMimeTypeText(JTextArea mimeTypeText) {
        this.mimeTypeText = mimeTypeText;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public void setNameField(JTextField nameField) {
        this.nameField = nameField;
    }

    public JTextArea getRefSchemeText() {
        return refSchemeText;
    }

    public void setRefSchemeText(JTextArea refSchemeText) {
        this.refSchemeText = refSchemeText;
    }

    public JLabel getTranslatedType() {
        return translatedType;
    }

    public void setTranslatedType(JLabel translatedType) {
        this.translatedType = translatedType;
    }

    private void initView() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 0;
        constraints.weightx = 0.1;
        constraints.fill = GridBagConstraints.BOTH;

        nameField = new JTextField(bean.getName());
        add(nameField, constraints);

        Vector<Boolean> literalSelectorList = new Vector<Boolean>();
        literalSelectorList.add(true);
        literalSelectorList.add(false);
        literalSelector = new JComboBox(literalSelectorList);
        if (!bean.getAllowsLiteralValues()) {
            literalSelector.setSelectedIndex(1);
        }
        constraints.gridx = 1;
        add(literalSelector, constraints);

        constraints.gridx = 2;
        SpinnerNumberModel model = new SpinnerNumberModel(new Integer(bean.
                getDepth()), new Integer(0), new Integer(100), new Integer(1));
        depthSpinner = new JSpinner(model);
        depthSpinner.setEnabled(false);
        add(depthSpinner, constraints);

        constraints.gridx = 3;
        refSchemeText = new JTextArea();
        String refs = "";
        for (Object refScheme : bean.getHandledReferenceSchemes()) {
            refs = refs + refScheme.getClass().getSimpleName() + "\n";
        }
        refSchemeText.setText(refs);
        refSchemeText.setEditable(false);
        refSchemeText.setBorder(BorderFactory.createEtchedBorder());

        //TODO mime stuff

        constraints.gridx = 4;
        translatedType = new JLabel(bean.getTranslatedElementType().
                getSimpleName());
    //add...
    }

    private void setEditMode() {
        nameField.setEditable(editable);
        literalSelector.setEnabled(editable);
        depthSpinner.setEnabled(editable);
    }
}
