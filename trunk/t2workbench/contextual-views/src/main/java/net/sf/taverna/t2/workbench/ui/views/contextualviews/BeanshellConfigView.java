package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.TextAreaDefaults;
import org.syntax.jedit.tokenmarker.JavaTokenMarker;

public class BeanshellConfigView extends JPanel {

	private JEditTextArea scriptText;

	private JTable inputTable;

	private JTable outputTable;

	private JButton addInputButton;

	private JTextField addInputField;

	private JButton addOutputButton;

	private JTextField addOutputField;

	private JPanel panel;

	private Map<String, Object> propertyMap;

	private List<BeanshellInputViewer> inputViewList;

	private List<BeanshellOutputViewer> outputViewList;

	private BeanshellActivity activity;

	private BeanshellActivityConfigurationBean configuration;

	public BeanshellConfigView(BeanshellActivity activity) {
		this.activity = activity;
		configuration = activity.getConfiguration();
		System.out.println("config view");
		setLayout(new GridBagLayout());
		initialise();
	}

	private void initialise() {
		System.out.println("initialise");
		setSize(500, 500);
		JButton OKButton = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				for (BeanshellInputViewer view : inputViewList) {

					view.getBean().setAllowsLiteralValues(
							(Boolean) view.getLiteralSelector()
									.getSelectedItem());
					view.getBean().setDepth(
							(Integer) view.getDepthSpinner().getValue());
					view.getBean().setName(view.getName());

				}

				for (BeanshellOutputViewer view : outputViewList) {

					view.getBean().setDepth(
							(Integer) view.getDepthSpinner().getValue());
					view.getBean()
							.setGranularDepth(
									(Integer) view.getGranularDepthSpinner()
											.getValue());
					view.getBean().setName(view.getName());

				}

				configuration.setScript(scriptText.getText());

			}

		});
		OKButton.setText("OK");
		OKButton.setToolTipText("Click to set the beanshell with the new values");
		inputViewList = new ArrayList<BeanshellInputViewer>();
		outputViewList = new ArrayList<BeanshellOutputViewer>();
		propertyMap = new HashMap<String, Object>();
//		panel = new JPanel();
//		panel.setLayout(new GridBagLayout());
		setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				null, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		setSize(500, 500);
		final BeanshellActivityConfigurationBean configBean = activity
				.getConfiguration();

		JPanel scriptEditPanel = new JPanel(new BorderLayout());
		// scriptEditPanel.add(buttonPanel, BorderLayout.PAGE_END);

		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Script", scriptEditPanel);
		tabbedPane.addTab("Ports", setPortPanel());

		tabbedPane.addTab("Dependencies", setDependencies());

		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		
		outerConstraint.fill = GridBagConstraints.BOTH;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		add(tabbedPane, outerConstraint);
		

		scriptText = new JEditTextArea(new TextAreaDefaults());
		scriptText.setText(configBean.getScript());
		scriptText.setTokenMarker(new JavaTokenMarker());
		scriptText.setCaretPosition(0);
		scriptText.setPreferredSize(new Dimension(0, 0));
		scriptText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				// getConfigBean().setScript(scriptText.getText());
				configBean.setScript(scriptText.getText());
			}
		});
		scriptEditPanel.add(scriptText, BorderLayout.CENTER);
		
		outerConstraint.gridy = 1;
		outerConstraint.fill = GridBagConstraints.NONE;
		outerConstraint.anchor = GridBagConstraints.LINE_END;
		outerConstraint.weighty = 0;
		add(OKButton, outerConstraint);
		System.out.println("initialise over");
	}

	private JPanel setDependencies() {
		System.out.println("dependencies");
		// TODO Auto-generated method stub
		JPanel dependencies = new JPanel(new GridBagLayout());
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		JLabel label = new JLabel("Group");
		label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		dependencies.add(label, outerConstraint);
		outerConstraint.gridx = 1;
		JLabel label2 = new JLabel("Aritifact");
		label2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		dependencies.add(label2, outerConstraint);
		outerConstraint.gridx = 2;
		JLabel label3 = new JLabel("Version");
		label3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		dependencies.add(label3, outerConstraint);

		int gridy = 1;
		for (String dependency : configuration.getDependencies()) {
			outerConstraint.gridy = gridy;
			String[] split = dependency.split(":");
			outerConstraint.gridx = 0;
			JLabel label4 = new JLabel(split[0]);
			label4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
			dependencies.add(label4, outerConstraint);
			outerConstraint.gridx = 1;
			JLabel label5 = new JLabel(split[1]);
			label5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
			dependencies.add(label5, outerConstraint);
			outerConstraint.gridx = 2;
			JLabel label6 = new JLabel(split[02]);
			label6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
			dependencies.add(label6, outerConstraint);
			gridy++;
		}
		JPanel filler = new JPanel();
		outerConstraint.gridy = gridy;
		outerConstraint.weighty = 0.1;
		dependencies.add(filler, outerConstraint);
		System.out.println("dependencies over");
		return dependencies;
	}

	private JTabbedPane setPortPanel() {
		System.out.println("set port pane");
		JTabbedPane ports = new JTabbedPane();

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
		ports.add("Inputs Ports", new JScrollPane(setInputPanel()));
		portEditPanel.add(new JScrollPane(setOutputPanel()), panelConstraint);
		ports.add("Output Ports", new JScrollPane(setOutputPanel()));
		System.out.println("set port pane over");
		return ports;
	}

	private JPanel setInputPanel() {
		System.out.println("set input pane");
		JPanel inputEditPanel = new JPanel(new GridBagLayout());
		inputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Inputs"));

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
		inputEditPanel.add(new JLabel("Handled Reference Schemes"),
				inputConstraint);
		inputConstraint.gridx = 4;
		inputEditPanel.add(new JLabel("Mime Types"), inputConstraint);
		inputConstraint.gridx = 5;
		inputEditPanel.add(new JLabel("Translated Element Type"),
				inputConstraint);
		int gridy = 1;
		inputConstraint.gridx = 0;
		for (ActivityInputPortDefinitionBean inputBean : configuration
				.getInputPortDefinitions()) {
			inputConstraint.gridy = gridy;
			BeanshellInputViewer beanshellInputViewer = new BeanshellInputViewer(
					inputBean, true);
			inputViewList.add(beanshellInputViewer);
			inputConstraint.gridx = 0;
			inputEditPanel.add(beanshellInputViewer.getNameField(),
					inputConstraint);
			inputConstraint.gridx = 1;
			inputEditPanel.add(beanshellInputViewer.getLiteralSelector(),
					inputConstraint);
			inputConstraint.gridx = 2;
			inputEditPanel.add(beanshellInputViewer.getDepthSpinner(),
					inputConstraint);
			inputConstraint.gridx = 3;
			inputEditPanel.add(beanshellInputViewer.getRefSchemeText(),
					inputConstraint);
			// inputConstraint.gridx = 4;
			// inputEditPanel.add(beanshellInputViewer.getMimeTypeText(),
			// inputConstraint);
			inputConstraint.gridx = 4;
			inputEditPanel.add(beanshellInputViewer.getTranslatedType(),
					inputConstraint);

			gridy++;
		}
		JPanel filler = new JPanel();
		inputConstraint.weighty = 0.1;
		inputConstraint.gridy = gridy;
		inputEditPanel.add(filler, inputConstraint);
		System.out.println("set input pane over");
		return inputEditPanel;
	}

	private JPanel setOutputPanel() {
		System.out.println("set output pane");
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

		int gridy = 1;
		for (ActivityOutputPortDefinitionBean outputBean : configuration
				.getOutputPortDefinitions()) {
			outputConstraint.gridy = gridy;
			BeanshellOutputViewer beanshellOutputViewer = new BeanshellOutputViewer(
					outputBean, true);
			outputViewList.add(beanshellOutputViewer);
			outputConstraint.gridx = 0;
			outputEditPanel.add(beanshellOutputViewer.getNameField(),
					outputConstraint);
			outputConstraint.gridx = 1;
			outputEditPanel.add(beanshellOutputViewer.getDepthSpinner(),
					outputConstraint);
			outputConstraint.gridx = 2;
			outputEditPanel.add(
					beanshellOutputViewer.getGranularDepthSpinner(),
					outputConstraint);
			outputConstraint.gridx = 3;
			outputEditPanel.add(beanshellOutputViewer.getMimeTypeText(),
					outputConstraint);
			outputConstraint.gridx = 4;
			outputEditPanel.add(beanshellOutputViewer.getMimeTypePanel(),
					outputConstraint);
			gridy++;
		}
		JPanel filler2 = new JPanel();
		outputConstraint.weighty = 0.1;
		outputConstraint.gridy = gridy;
		outputEditPanel.add(filler2, outputConstraint);
		System.out.println("set output pane over");
		return outputEditPanel;
	}

	private void setNewValues() {
		boolean edit = false;
		if (inputViewList.get(0).isEditable()) {
			edit = true;
		}
		if (edit) {
			for (BeanshellInputViewer view : inputViewList) {
				view.getBean().setAllowsLiteralValues(
						(Boolean) view.getLiteralSelector().getSelectedItem());
				view.getBean().setDepth(
						(Integer) view.getDepthSpinner().getValue());
				view.getBean().setName(view.getName());
			}
			for (BeanshellOutputViewer view : outputViewList) {
				view.getBean().setDepth(
						(Integer) view.getDepthSpinner().getValue());
				view.getBean().setGranularDepth(
						(Integer) view.getGranularDepthSpinner().getValue());
				view.getBean().setName(view.getName());
			}
			configuration.setScript(this.scriptText.getText());
		}

	}

	// @Override
	// protected Action getConfigureAction() {
	//
	// Action configureAction = new AbstractAction() {
	//
	// public void actionPerformed(ActionEvent arg0) {
	// for (BeanshellInputViewer view : inputViewList) {
	// view.setEditable(true);
	// }
	//
	// for (BeanshellOutputViewer view : outputViewList) {
	// view.setEditable(true);
	// }
	// }
	//
	// };
	// return configureAction;
	// }

	private Action getOKAction() {

		Action OKAction = new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				boolean edit = false;
				for (BeanshellInputViewer view : inputViewList) {
					if (view.isEditable()) {
						edit = true;
						view.getBean().setAllowsLiteralValues(
								(Boolean) view.getLiteralSelector()
										.getSelectedItem());
						view.getBean().setDepth(
								(Integer) view.getDepthSpinner().getValue());
						view.getBean().setName(view.getName());
					}
				}

				for (BeanshellOutputViewer view : outputViewList) {
					if (view.isEditable()) {
						view.getBean().setDepth(
								(Integer) view.getDepthSpinner().getValue());
						view.getBean().setGranularDepth(
								(Integer) view.getGranularDepthSpinner()
										.getValue());
						view.getBean().setName(view.getName());
					}
				}
				if (edit) {
					configuration.setScript(scriptText.getText());
				}
			}

		};
		return OKAction;
	}

}
