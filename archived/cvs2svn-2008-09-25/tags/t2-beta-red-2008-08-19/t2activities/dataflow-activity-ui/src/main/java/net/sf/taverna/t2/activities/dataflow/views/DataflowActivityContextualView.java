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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.filemanager.NestedDataflowSource;
import net.sf.taverna.t2.dataflow.actions.DataflowActivityConfigurationAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class DataflowActivityContextualView extends
		HTMLBasedActivityContextualView<Dataflow> {

	private static Logger logger = Logger
			.getLogger(DataflowActivityContextualView.class);

	private T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private FileManager fileManager = FileManager.getInstance();

	@Override
	protected DataflowActivity getActivity() {
		return (DataflowActivity) super.getActivity();
	}

	@Override
	protected JComponent getMainFrame() {
		JComponent mainFrame = super.getMainFrame();
		JButton viewWorkflowButton = new JButton("Edit workflow");
		viewWorkflowButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				NestedDataflowSource nestedDataflowSource = new NestedDataflowSource(
						fileManager.getCurrentDataflow(), getActivity());

				try {
					fileManager.openDataflow(T2_FLOW_FILE_TYPE,
							nestedDataflowSource);
				} catch (OpenException e1) {
					logger.error(
							"Could not open nested dataflow from activity "
									+ getActivity(), e1);
					JOptionPane.showMessageDialog(
							DataflowActivityContextualView.this,
							"Could not open nested dataflow:\n"
									+ e1.getMessage(),
							"Could not open nested dataflow",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

		});
		JButton configureButton = new JButton("Open from file");
		configureButton
				.addActionListener(new DataflowActivityConfigurationAction(
						getActivity()));

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

		return (getActivity()).getConfiguration().getLocalName();
	}

	@Override
	protected String getViewTitle() {
		return "Dataflow Contextual View";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return null;
		// return new DataflowActivityConfigurationAction(
		// (DataflowActivity) getActivity(), owner);
	}

}
