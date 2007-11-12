/**
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-11 15:08:48 $
 * $Revision: 1.1 $
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor;

import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * The abstract processor is a processor that contains a set of input ports and
 * output ports and a brief description what it should do If the processor has
 * an alternate, it executes the alternate, otherwise it does nothing
 * 
 * @author Ingo Wassink
 * 
 */
public class APProcessor extends Processor {
	private static final long serialVersionUID = 8799117109992658036L;

	private String taskDescription;

	/**
	 * Constructor of the abstract processor
	 * 
	 * @param model
	 *            the model to which this processor belongs to
	 * @param name
	 *            the name of the processor
	 * @throws ProcessorCreationException
	 *             never happens
	 * @throws DuplicateProcessorNameException
	 *             if the processor already exists
	 */
	public APProcessor(ScuflModel model, String name)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, name);

		taskDescription = "Enter taskdescription";
	}

	/**
	 * Method for setting the task description
	 * 
	 * @param taskDescription
	 *            the new task description
	 */
	public void setTaskDescription(String taskDescription) {
		this.taskDescription = (taskDescription == null) ? "" : taskDescription;
	}

	/**
	 * Method for getting the task description
	 * 
	 * @return the task description
	 */
	public String getTaskDescription() {
		return taskDescription;
	}

	/**
	 * Method for getting the processor properties
	 * 
	 * @return the properties
	 */
	public Properties getProperties() {
		Properties properties = new Properties();

		properties.put("Name", this.getName());
		properties.put("Task description", this.getTaskDescription());
		properties.put("Number of ports", this.getPorts().length);

		return properties;
	}

}
