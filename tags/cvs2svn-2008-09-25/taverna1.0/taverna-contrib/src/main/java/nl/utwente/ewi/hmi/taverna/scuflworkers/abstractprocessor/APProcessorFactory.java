/**
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-11 15:08:48 $
 * $Revision: 1.1 $
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Class for constructing the abstract processor
 * 
 * @author Ingo Wassink
 * 
 */
public class APProcessorFactory extends ProcessorFactory {
	public APProcessorFactory() {
		setName("AbstractProcessor");
		setDescription("Processor for abstract taskdescriptions");
	}

	/**
	 * Function for getting the processor description
	 * 
	 * @return the description
	 */
	public String getProcessorDescription() {
		return super.getDescription();
	}

	/**
	 * Method for getting the class object of the processor
	 * 
	 * @return the class object of the APProcessor
	 */
	public Class getProcessorClass() {
		return APProcessor.class;
	}
}
