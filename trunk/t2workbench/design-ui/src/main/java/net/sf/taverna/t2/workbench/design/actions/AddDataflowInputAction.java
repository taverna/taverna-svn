package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;

public class AddDataflowInputAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(AddDataflowInputAction.class);

	public AddDataflowInputAction(Dataflow dataflow, Component component) {
		super(dataflow, component);
		putValue(SMALL_ICON, WorkbenchIcons.inputValueIcon);
		putValue(NAME, "Create New Input...");		
	}

	public void actionPerformed(ActionEvent event) {
		try {
			Set<String> usedPorts = new HashSet<String>();
			for (DataflowInputPort inputPort : dataflow.getInputPorts()) {
				usedPorts.add(inputPort.getName());
			}
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(usedPorts, "Duplicate input port.",
					"[\\p{L}\\p{Digit}_.]+", "Invalid port name.", "Workflow Input Port", "Create a new workflow input port", null);
			String inputName = vuid.show(component);
			if (inputName != null) {
				editManager.doDataflowEdit(dataflow, edits.getCreateDataflowInputPortEdit(dataflow, inputName, 0, 0));
			}
		} catch (EditException e) {
			logger.debug("Create dataflow input port failed");
		}

	}

}
