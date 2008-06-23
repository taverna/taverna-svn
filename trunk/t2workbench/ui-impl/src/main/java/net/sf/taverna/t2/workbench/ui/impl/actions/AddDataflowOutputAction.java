package net.sf.taverna.t2.workbench.ui.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;

public class AddDataflowOutputAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AddDataflowOutputAction.class);

	public AddDataflowOutputAction(Dataflow dataflow, Component component) {
		super(dataflow, component);
		putValue(SMALL_ICON, WorkbenchIcons.inputValueIcon);
		putValue(NAME, "Create New Output...");		
	}

	public void actionPerformed(ActionEvent event) {
		try {
			Set<String> usedPorts = new HashSet<String>();
			for (DataflowOutputPort outputPort : dataflow.getOutputPorts()) {
				usedPorts.add(outputPort.getName());
			}
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(usedPorts, "Duplicate output port.",
					"[\\p{L}\\p{Digit}_.]+", "Invalid port name.", "Workflow Output Port", "Create a new workflow output port", null);
			String outputName = vuid.show(component);
			if (outputName != null) {
				editManager.doDataflowEdit(dataflow, edits.getCreateDataflowOutputPortEdit(dataflow, outputName));
			}
		} catch (EditException e) {
			logger.debug("Create dataflow output port failed");
		}

	}

}
