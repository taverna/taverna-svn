package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;

public class BeanshellContextualView extends
		ActivityView<BeanshellActivityConfigurationBean> {

	public BeanshellContextualView(BeanshellActivityConfigurationBean configBean) {
		super(configBean);
	}

	private JPanel panel;

	private void initialise() {
		panel = new JPanel();
		panel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				null, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		BeanshellActivityConfigurationBean configBean = getConfigBean();
		String script = configBean.getScript();
		System.out.println("bean script: " + script);
		JTextArea textArea = new JTextArea(script);
		panel.add(textArea);
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
		return "Beanshell Configuration";
	}

}
