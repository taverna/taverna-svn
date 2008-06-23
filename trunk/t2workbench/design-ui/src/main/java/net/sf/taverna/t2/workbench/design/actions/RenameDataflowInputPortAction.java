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
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

public class RenameDataflowInputPortAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RenameDataflowInputPortAction.class);

	private DataflowInputPort port;

	public RenameDataflowInputPortAction(Dataflow dataflow, DataflowInputPort port, Component component) {
		super(dataflow, component);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Rename Input Port...");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Set<String> usedProcessors = new HashSet<String>();
			for (Processor usedProcessor : dataflow.getProcessors()) {
				if (!usedProcessor.getLocalName().equals(port.getName())) {
					usedProcessors.add(usedProcessor.getLocalName());
				}
			}
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(usedProcessors, "Duplicate input port.",
					"[\\p{L}\\p{Digit}_.]+", "Invalid input port name.", "Input Port Name", "Set the input port name.",
					port.getName());
			String portName = vuid.show(component);
			if (portName != null && !portName.equals(port.getName())) {
				editManager.doDataflowEdit(dataflow, edits.getRenameDataflowInputPortEdit(port, portName));
			}
		} catch (EditException e1) {
			logger.debug("Rename processor failed");
		}
	}

}
