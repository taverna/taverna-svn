package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

public class RenameDataflowOutputPortAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RenameDataflowOutputPortAction.class);

	private DataflowOutputPort port;

	public RenameDataflowOutputPortAction(Dataflow dataflow, DataflowOutputPort port, Component component) {
		super(dataflow, component);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Rename Output Port...");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Set<String> usedOutputPorts = new HashSet<String>();
			for (DataflowOutputPort usedOutputPort : dataflow.getOutputPorts()) {
				if (!usedOutputPort.getName().equals(port.getName())) {
					usedOutputPorts.add(usedOutputPort.getName());
				}
			}
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(usedOutputPorts, "Duplicate output port.",
					"[\\p{L}\\p{Digit}_.]+", "Invalid output port name.", "Output Port Name", "Set the output port name.",
					port.getName());
			String portName = vuid.show(component);
			if (portName != null && !portName.equals(port.getName())) {
				editManager.doDataflowEdit(dataflow, edits.getRenameDataflowOutputPortEdit(port, portName));
			}
		} catch (EditException e1) {
			logger.debug("Rename dataflow output port failed", e1);
		}
	}

}
