package org.embl.ebi.escience.scuflui.workbench;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessor;

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
		for (Processor processor : processorsOfType) {
			processorURL.put(processor, ((ScuflWorkflowProcessor) processor)
					.getDefinitionURL());
			((ScuflWorkflowProcessor) processor).setDefinitionURL(null);
		}
	}

	/**
	 * Set the URLs where the nested workflow comes from to its original value
	 */
	public void makeNestedReferenced() {
		Processor[] processorsOfType = this.model
				.getProcessorsOfType(ScuflWorkflowProcessor.class);
		for (Processor processor : processorsOfType) {
			((ScuflWorkflowProcessor) processor).setDefinitionURL(processorURL
					.get(processor));
		}
		processorURL.clear();
	}

}
