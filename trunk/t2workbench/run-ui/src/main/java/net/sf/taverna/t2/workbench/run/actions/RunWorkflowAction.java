package net.sf.taverna.t2.workbench.run.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.run.DataflowRunsComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;

public class RunWorkflowAction extends AbstractAction {

	private DataflowRunsComponent runComponent;
	
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
					JOptionPane.showMessageDialog(null, "Inputs not implemented yet!");
				} else {
					runComponent.runDataflow(dataflow, null);
				}
			} else {
				showErrorDialog("Unable to validate workflow",
				"Workflow validation failed");
			}
		}
		
	}

	private void showErrorDialog(String string, String string2) {
		// TODO Auto-generated method stub
		
	}

}
