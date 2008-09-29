/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.Map;

import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A task to invoke a LocalServiceProcessor
 * 
 * @author Tom Oinn
 */
public class LocalServiceTask implements ProcessorTaskWorker {

	private Processor proc;

	public LocalServiceTask(Processor p) {
		this.proc = p;
	}

	public Map execute(Map inputMap, IProcessorTask parentTask) throws TaskExecutionException {
		LocalServiceProcessor theProcessor = (LocalServiceProcessor) proc;
		return theProcessor.getWorker().execute(inputMap);
	}

}
