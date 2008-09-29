package org.embl.ebi.escience.scuflui.workbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessor;

/**
 * Given a {@link ScuflModel} it retrieves all the
 * {@link ScuflWorkflowProcessor}s ie. the nested workflows and can set their
 * URLs to null or back to their original value. Setting the URL to null forces
 * the full XML to be written out when saving instead of just a file reference
 * 
 * @author Ian
 * 
 */
public class NestedWorkflowSwitcher {

	private Map<Processor, String> processorURL;
	private ScuflModel model;

	public NestedWorkflowSwitcher(ScuflModel model) {
		this.model = model;
		processorURL = new HashMap<Processor, String>();
	}

	/**
	 * Set the URLs where the nested workflow comes from to null and store the
	 * processor and its original URL in the {@link #processorURL} {@link Map}
	 */
	public void makeNestedEmbedded() {
		Processor[] processorsOfType = this.model
				.getProcessorsOfType(ScuflWorkflowProcessor.class);
		List<Processor> processorList = new ArrayList<Processor>();
		for (Processor processor : processorsOfType) {
			processorList.add((Processor) processor);
		}

		List<Processor> allWorkflowProcessors = getAllWorkflowProcessors(processorList);
		for (Processor processor : allWorkflowProcessors) {
			processorURL.put(processor, ((ScuflWorkflowProcessor) processor)
					.getDefinitionURL());
			((ScuflWorkflowProcessor) processor).setDefinitionURL(null);
		}

	}

	/**
	 * Set the URLs where the nested workflow comes from to its original value
	 */
	public void makeNestedReferenced() {

		for (Processor processor : processorURL.keySet()) {
			((ScuflWorkflowProcessor) processor).setDefinitionURL(processorURL
					.get(processor));
		}

		processorURL.clear();
	}

	/**
	 * Recurse over all the processors including any nested-nested-nested etc.
	 * workflows
	 * 
	 * @param processorList
	 * @return
	 */
	private List<Processor> getAllWorkflowProcessors(
			List<Processor> processorList) {
		List<Processor> allProcessors = new ArrayList<Processor>();
		for (Processor processor : processorList) {
			ScuflModel internalModel = ((ScuflWorkflowProcessor) processor)
					.getInternalModel();
			Processor[] processorsOfType = internalModel
					.getProcessorsOfType(ScuflWorkflowProcessor.class);
			List<Processor> moreProcessors = new ArrayList<Processor>();
			for (Processor proc : processorsOfType) {
				moreProcessors.add(proc);
			}
			List<Processor> allWorkflowProcessors = getAllWorkflowProcessors(moreProcessors);
			if (!allWorkflowProcessors.isEmpty()) {
				allProcessors.addAll(allWorkflowProcessors);
			}
			allProcessors.add(processor);
		}

		return allProcessors;

	}
}
