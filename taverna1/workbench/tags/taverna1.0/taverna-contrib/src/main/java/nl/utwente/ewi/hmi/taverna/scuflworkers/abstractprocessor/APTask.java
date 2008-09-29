/**
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-11 15:08:48 $
 * $Revision: 1.1 $
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor;

import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Class for executing the abstract process The process executes it's alternate,
 * if it has one, otherwise it shows its content
 * 
 * @author Ingo Wassink
 * @author Stuart Owen
 * 
 */
public class APTask implements ProcessorTaskWorker {
	APProcessor processor;

	private static Logger logger = Logger.getLogger(APTask.class);

	/**
	 * Constructor of the task object
	 * 
	 * @param processor
	 *            the processor to be executed
	 */
	public APTask(Processor processor) {
		this.processor = (APProcessor) processor;
	}

	/**
	 * The method which is called when the process needs to be executed
	 */
	public Map execute(Map inputMap, IProcessorTask parentTask)
			throws TaskExecutionException {

		if (parentTask.getProcessor().getAlternatesList().size() == 0) {
			JOptionPane.showMessageDialog(null,
					"No implementation for the abstract processor. Add an alternate to "
							+ processor.getName(), "Abstract processor "
							+ processor.getName() + " incomplete",
					JOptionPane.ERROR_MESSAGE);

			throw new TaskExecutionException(
					"No implementation for the abstract processor. Add an alternate to "
							+ processor.getName());
		} else {
			logger.info("Encountered Abstract Processor, using alternate.");
			throw new TaskExecutionException("Using alternate to "
					+ processor.getName());
		}
	}

}
