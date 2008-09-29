package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

public class RenameProcessorAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RenameProcessorAction.class);

	private Processor processor;

	public RenameProcessorAction(Dataflow dataflow, Processor processor, Component component) {
		super(dataflow, component);
		this.processor = processor;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Rename Processor...");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Set<String> usedProcessors = new HashSet<String>();
			for (Processor usedProcessor : dataflow.getProcessors()) {
				if (!usedProcessor.getLocalName().equals(processor.getLocalName())) {
					usedProcessors.add(usedProcessor.getLocalName());
				}
			}
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(usedProcessors, "Duplicate processor.",
					"[\\p{L}\\p{Digit}_.]+", "Invalid processor name.", "Processor Name", "Set the processor name.",
					processor.getLocalName());
			String processorName = vuid.show(component);
			if (processorName != null && !processorName.equals(processor.getLocalName())) {
				editManager.doDataflowEdit(dataflow, edits.getRenameProcessorEdit(processor, processorName));
			}
		} catch (EditException e1) {
			logger.debug("Rename processor failed", e1);
		}
	}

}
