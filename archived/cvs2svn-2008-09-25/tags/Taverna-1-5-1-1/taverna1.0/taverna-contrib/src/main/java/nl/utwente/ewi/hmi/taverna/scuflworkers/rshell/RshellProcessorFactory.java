/*
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-20 14:51:32 $
 * $Revision: 1.1 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Implementation of ProcessorFactory that creates Rshell nodes
 * 
 * @author Stian Soiland, Ingo Wassink
 */
public class RshellProcessorFactory extends ProcessorFactory {

	private RshellProcessor prototype = null;

	/**
	 * The constructor
	 */
	public RshellProcessorFactory() {
		setName("RShell");
		setDescription("Run R/S scripts through RServe");

		// bring the manager into live
		RshellConnectionManager.INSTANCE.getClass();
	}

	/**
	 * The constructor
	 * 
	 * @param prototype
	 *            the prototype
	 */
	public RshellProcessorFactory(RshellProcessor prototype) {
		this.prototype = prototype;
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
	 * @return the class object of the RshellProcessor
	 */
	public Class getProcessorClass() {
		return RshellProcessor.class;
	}

	/**
	 * function for getting the prototype processor
	 * 
	 * @return the prototype
	 */
	public RshellProcessor getPrototype() {
		return prototype;
	}
}
