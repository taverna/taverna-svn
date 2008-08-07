package net.sf.taverna.t2.activities.dataflow.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.help.CSH;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.dataflow.actions.DataflowActivityConfigurationAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class DataflowActivityContextualView extends
		HTMLBasedActivityContextualView<Dataflow> {

	@Override
	protected JComponent getMainFrame() {
		JComponent mainFrame = super.getMainFrame();
		JButton viewWorkflowButton = new JButton("View Workflow");
		viewWorkflowButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				FileManager.getInstance().setCurrentDataflow((Dataflow) getActivity().getConfiguration(), true);
			}
			
		});
		JButton configureButton = new JButton("Configure Workflow");
		configureButton.addActionListener(new DataflowActivityConfigurationAction(
				(DataflowActivity) getActivity(), mainFrame));
		
		JPanel flowPanel = new JPanel(new FlowLayout());
		flowPanel.add(viewWorkflowButton);
		flowPanel.add(configureButton);
		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		return mainFrame;
	}

	public DataflowActivityContextualView(Activity<?> activity) {
		super(activity);
		CSH
				.setHelpIDString(this,
						"net.sf.taverna.t2.activities.dataflow.views.DataflowActivityContextualView");
	}

	@Override
	protected String getRawTableRowsHtml() {

		return ((DataflowActivity) getActivity()).getConfiguration()
				.getLocalName();
	}

	@Override
	protected String getViewTitle() {
		return "Dataflow Contextual View";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return null;
//		return new DataflowActivityConfigurationAction(
//				(DataflowActivity) getActivity(), owner);
	}

}
