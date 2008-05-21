package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.TextAreaDefaults;
import org.syntax.jedit.tokenmarker.JavaTokenMarker;

public class BeanshellContextualView extends
		ActivityView<BeanshellActivityConfigurationBean> {
	
	private JEditTextArea scriptText;

	private JTable inputTable;

	private JTable outputTable;

	private JButton addInputButton;

	private JTextField addInputField;

	private JButton addOutputButton;

	private JTextField addOutputField;	
	
	private JPanel panel;

	private Map<String, Object> propertyMap;

	public BeanshellContextualView(BeanshellActivityConfigurationBean configBean) {
		super(configBean);
	}


	private void initialise() {
		propertyMap = new HashMap<String, Object>();
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		panel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				null, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		panel.setSize(500, 500);
		BeanshellActivityConfigurationBean configBean = getConfigBean();
		
		JPanel scriptEditPanel = new JPanel(new BorderLayout());
//		scriptEditPanel.add(buttonPanel, BorderLayout.PAGE_END);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Script", scriptEditPanel);
		tabbedPane.addTab("Ports", setPortPanel());
		
		JPanel dependenciesPanel = new JPanel();
		
		tabbedPane.addTab("Dependencies", dependenciesPanel);
		panel.add(tabbedPane, outerConstraint);
		
		scriptText = new JEditTextArea(new TextAreaDefaults());
		scriptText.setText(configBean.getScript());
		scriptText.setTokenMarker(new JavaTokenMarker());
		scriptText.setCaretPosition(0);
		scriptText.setPreferredSize(new Dimension(0, 0));
		scriptText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
//				getConfigBean().setScript(scriptText.getText());
				getConfigBean().setScript(scriptText.getText());
			}
		});
		scriptEditPanel.add(scriptText, BorderLayout.CENTER);
		
		
		
	}
	
	private JPanel setPortPanel() {
		
		JPanel portEditPanel = new JPanel(new GridLayout(0, 2));
		
		GridBagConstraints panelConstraint = new GridBagConstraints();
		panelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		panelConstraint.gridx = 0;
		panelConstraint.gridy = 0;
		panelConstraint.weightx = 0.1;
		panelConstraint.weighty = 0.1;
		panelConstraint.fill = GridBagConstraints.BOTH;
		
		portEditPanel.add(new JScrollPane(setInputPanel()), panelConstraint);
		
		panelConstraint.gridy = 1;
		portEditPanel.add(new JScrollPane(setOutputPanel()), panelConstraint);
		
		return portEditPanel;
	}
	

	@Override
	protected JComponent getMainFrame() {
		if (panel == null) {
			initialise();
		}
		return panel;
	}

	@Override
	protected String getViewTitle() {
		return "Beanshell configuration";
	}
	
	private JPanel setInputPanel() {
		JPanel inputEditPanel = new JPanel(new GridBagLayout());
		inputEditPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Inputs"));
		
		GridBagConstraints inputConstraint = new GridBagConstraints();
		inputConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		inputConstraint.gridx = 0;
		inputConstraint.gridy = 0;
		inputConstraint.weightx = 0.1;
		inputConstraint.fill = GridBagConstraints.BOTH;
		
		inputEditPanel.add(new JLabel("Name"), inputConstraint);
		inputConstraint.gridx = 1;
		inputEditPanel.add(new JLabel("Allow Literal"), inputConstraint);
		inputConstraint.gridx = 2;
		inputEditPanel.add(new JLabel("Depth"), inputConstraint);
		inputConstraint.gridx = 3;
		inputEditPanel.add(new JLabel("Handled Reference Schemes"), inputConstraint);
		inputConstraint.gridx = 4;
		inputEditPanel.add(new JLabel("Mime Types"), inputConstraint);
		inputConstraint.gridx = 5;
		inputEditPanel.add(new JLabel("Translated Element Type"), inputConstraint);
		int gridy = 1;
		inputConstraint.gridx = 0;
		for (ActivityInputPortDefinitionBean inputBean: getConfigBean().getInputPortDefinitions()) {
			inputConstraint.gridy = gridy;
			BeanshellInputViewer beanshellInputViewer = new BeanshellInputViewer(inputBean);
			inputConstraint.gridx = 0;
			inputEditPanel.add(beanshellInputViewer.getNameField(), inputConstraint);
			inputConstraint.gridx = 1;
			inputEditPanel.add(beanshellInputViewer.getLiteralSelector(), inputConstraint);
			inputConstraint.gridx = 2;
			inputEditPanel.add(beanshellInputViewer.getDepthSpinner(), inputConstraint);
			inputConstraint.gridx = 3;
			inputEditPanel.add(beanshellInputViewer.getRefSchemeText(), inputConstraint);
			inputConstraint.gridx = 4;
			inputEditPanel.add(beanshellInputViewer.getMimeTypeText(), inputConstraint);
			inputConstraint.gridx = 5;
			inputEditPanel.add(beanshellInputViewer.getTranslatedType(), inputConstraint);
			
			gridy++;
		}
		JPanel filler = new JPanel();
		inputConstraint.weighty = 0.1;
		inputConstraint.gridy = gridy;
		inputEditPanel.add(filler, inputConstraint);
		return inputEditPanel;
	}
	
	private JPanel setOutputPanel() {
		JPanel outputEditPanel = new JPanel(new GridBagLayout());
		outputEditPanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(), "Outputs"));
		
		GridBagConstraints outputConstraint = new GridBagConstraints();
		outputConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outputConstraint.gridx = 0;
		outputConstraint.gridy = 0;
		outputConstraint.weightx = 0.1;
		outputConstraint.fill = GridBagConstraints.BOTH;
		
		outputEditPanel.add(new JLabel("Name"), outputConstraint);
		outputConstraint.gridx = 1;
		outputEditPanel.add(new JLabel("Depth"), outputConstraint);
		outputConstraint.gridx = 2;
		outputEditPanel.add(new JLabel("GranularDepth"), outputConstraint);
		outputConstraint.gridx = 3;
		outputEditPanel.add(new JLabel("Mime Types"), outputConstraint);
		
		int gridy=1;
		for (ActivityOutputPortDefinitionBean outputBean: getConfigBean().getOutputPortDefinitions()) {
			outputConstraint.gridy = gridy;
			BeanshellOutputViewer beanshellOutputViewer = new BeanshellOutputViewer(
					outputBean);
			outputConstraint.gridx = 0;
			outputEditPanel.add(beanshellOutputViewer.getNameField(), outputConstraint);
			outputConstraint.gridx = 1;
			outputEditPanel.add(beanshellOutputViewer.getDepthSpinner(), outputConstraint);
			outputConstraint.gridx = 2;
			outputEditPanel.add(beanshellOutputViewer.getGranularDepthSpinner(), outputConstraint);
			outputConstraint.gridx = 3;
			outputEditPanel.add(beanshellOutputViewer.getMimeTypeText(), outputConstraint);
			
			gridy++;
		}
		JPanel filler2 = new JPanel();
		outputConstraint.weighty = 0.1;
		outputConstraint.gridy = gridy;
		outputEditPanel.add(filler2, outputConstraint);
		
		return outputEditPanel;
	}

}
