package net.sf.taverna.t2.activities.wsdl.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.activities.wsdl.actions.AddXMLInputSplitterAction;
import net.sf.taverna.t2.activities.wsdl.actions.AddXMLOutputSplitterAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class WSDLActivityContextualView extends
		HTMLBasedActivityContextualView<WSDLActivityConfigurationBean> {

	private static final long serialVersionUID = -4329643934083676113L;

	public WSDLActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	/**
	 * Gets the component from the {@link HTMLBasedActivityContextualView} and
	 * adds buttons to it allowing XML splitters to be added
	 */
	@Override
	protected JComponent getMainFrame() {
		final JComponent mainFrame = super.getMainFrame();

		JButton addInputSplitter = new JButton("Add input XML splitter");

		addInputSplitter.addActionListener(new AddXMLInputSplitterAction(
				getActivity(), mainFrame));

		JButton addOutputSplitter = new JButton("Add output XML splitter");

		addOutputSplitter.addActionListener(new AddXMLOutputSplitterAction(
				getActivity(), mainFrame));
		JPanel flowPanel = new JPanel(new FlowLayout());
		flowPanel.add(addInputSplitter);
		flowPanel.add(addOutputSplitter);
		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		return mainFrame;
	}

	@Override
	protected String getViewTitle() {
		return "WSDL-based activity";
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summary = "<tr><td>WSDL</td><td>" + getConfigBean().getWsdl();
		summary += "</td></tr><tr><td>Operation</td><td>"
				+ getConfigBean().getOperation() + "</td></tr>";
		boolean securityConfigured = getConfigBean().getSecurityProfileString() != null;
		summary += "<tr><td>Secured?</td><td>"
				+ Boolean.toString(securityConfigured) + "</td></tr>";
		summary += "</tr>";
		return summary;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		// return new
		// WSDLActivityConfigureAction((WSDLActivity)getActivity(),owner);
		return null;
	}

}
