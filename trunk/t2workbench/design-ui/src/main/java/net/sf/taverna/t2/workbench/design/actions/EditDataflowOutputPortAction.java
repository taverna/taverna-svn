package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.design.ui.DataflowOutputPortPanel;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;

/**
 * Action for editing a dataflow output port.
 *
 * @author David Withers
 */
public class EditDataflowOutputPortAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(EditDataflowOutputPortAction.class);

	private DataflowOutputPort port;

	public EditDataflowOutputPortAction(Dataflow dataflow, DataflowOutputPort port, Component component) {
		super(dataflow, component);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Edit Output Port...");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Set<String> usedOutputPorts = new HashSet<String>();
			for (DataflowOutputPort usedOutputPort : dataflow.getOutputPorts()) {
				if (!usedOutputPort.getName().equals(port.getName())) {
					usedOutputPorts.add(usedOutputPort.getName());
				}
			}

			DataflowOutputPortPanel inputPanel = new DataflowOutputPortPanel();
			
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Edit Workflow Output Port", inputPanel);
			vuid.addTextComponentValidation(inputPanel.getPortNameField(),
					"Set the output port name.", usedOutputPorts,
					"Duplicate output port.", "[\\p{L}\\p{Digit}_.]+",
					"Invalid output port name.");
			vuid.setSize(new Dimension(400, 200));

			inputPanel.setPortName(port.getName());

			if (vuid.show(component)) {
				String portName = inputPanel.getPortName();
				editManager.doDataflowEdit(dataflow, edits.getRenameDataflowOutputPortEdit(port, portName));
			}
		} catch (EditException e1) {
			logger.debug("Rename dataflow output port failed", e1);
		}
	}

}
