package net.sf.taverna.t2.workbench.run.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.WorkflowLaunchPanel;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.run.DataflowRunsComponent;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;

public class RunWorkflowAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private DataflowRunsComponent runComponent;
	
	private PerspectiveSPI resultsPerspective;
	
	public RunWorkflowAction() {
		runComponent = DataflowRunsComponent.getInstance();
		putValue(SMALL_ICON, WorkbenchIcons.runIcon);
		putValue(NAME, "Run workflow...");		
		putValue(SHORT_DESCRIPTION, "Run the current workflow");
		
	}
	
	public void actionPerformed(ActionEvent e) {
		Object model = ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
		if (model instanceof Dataflow) {
			Dataflow dataflow = (Dataflow) model;
			DataflowValidationReport report = dataflow.checkValidity();
			if (report.isValid()) {
				List<? extends DataflowInputPort> inputPorts = dataflow.getInputPorts();
				if (!inputPorts.isEmpty()) {
					showInputDialog(dataflow);
				} else {
					switchToResultsPerspective();
					runComponent.runDataflow(dataflow, null);
				}
			} else {
				showErrorDialog("Unable to validate workflow",
				"Workflow validation failed");
			}
		}
		
	}
	
	private void switchToResultsPerspective() {
		if (resultsPerspective == null) {
			for (PerspectiveSPI perspective : Workbench.getInstance().getPerspectives().getPerspectives()) {
				if (perspective.getText().equalsIgnoreCase("results")) {
					resultsPerspective = perspective;
					break;
				}
			}
		}
		if (resultsPerspective != null) {
			ModelMap.getInstance().setModel(ModelMapConstants.CURRENT_PERSPECTIVE, resultsPerspective);
		}
	}

	private void showInputDialog(final Dataflow dataflow) {
		// Create and set up the window.
		JFrame frame = new JFrame("Workflow input builder");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		WorkflowLaunchPanel wlp = new WorkflowLaunchPanel(runComponent.getReferenceService(),
				runComponent.getReferenceContext()) {
			@Override
			public void handleLaunch(Map<String, T2Reference> workflowInputs) {
				switchToResultsPerspective();
				runComponent.runDataflow(dataflow, workflowInputs);
			}
		};
		wlp.setOpaque(true); // content panes must be opaque

		for (DataflowInputPort input : dataflow.getInputPorts()) {
			wlp.addInputTab(input.getName(), input.getDepth());
		}

		frame.setContentPane(wlp);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	private void showErrorDialog(String string, String string2) {
		JOptionPane.showMessageDialog(runComponent, string, string2, JOptionPane.ERROR_MESSAGE);		
	}

}
