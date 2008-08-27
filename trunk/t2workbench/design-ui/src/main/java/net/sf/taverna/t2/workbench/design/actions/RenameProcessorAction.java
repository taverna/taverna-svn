package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.design.ui.ProcessorPanel;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

/**
 * Action for renaming a processor.
 * 
 * @author David Withers
 */
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

			ProcessorPanel inputPanel = new ProcessorPanel();
			
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Rename Processor", inputPanel);
			vuid.addTextComponentValidation(inputPanel.getProcessorNameField(),
					"Set the processor name.", usedProcessors,
					"Duplicate processor.", "[\\p{L}\\p{Digit}_.]+",
					"Invalid processor name.");
			vuid.setSize(new Dimension(400, 200));

			inputPanel.setProcessorName(processor.getLocalName());

			if (vuid.show(component)) {
				String processorName = inputPanel.getProcessorName();
				editManager.doDataflowEdit(dataflow, edits.getRenameProcessorEdit(processor, processorName));
			}
		
		} catch (EditException e1) {
			logger.debug("Rename processor failed", e1);
		}
	}

}
