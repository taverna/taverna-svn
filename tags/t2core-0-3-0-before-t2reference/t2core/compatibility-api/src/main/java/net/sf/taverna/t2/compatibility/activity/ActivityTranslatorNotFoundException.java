package net.sf.taverna.t2.compatibility.activity;

import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;

import org.embl.ebi.escience.scufl.Processor;

/**
 * <p>
 * An Exception indicating that a suitable {@link ActivityTranslator} cannot be found.
 * This will generally occur when no ActivityTranslator can be found for a given Taverna 1 Processor.
 * </p>
 * @author Stuart Owen
 *
 */
public class ActivityTranslatorNotFoundException extends Exception {

	private static final long serialVersionUID = 8779255468276952392L;
	
	private Processor taverna1Processor;

	/**
	 * @param msg a message describing the reason for the exception.
	 * @param tavena1Processor the Taverna 1 processor for which no translator could be found.
	 */
	public ActivityTranslatorNotFoundException(String msg, Processor taverna1Processor) {
		super(msg);
		this.taverna1Processor = taverna1Processor;
	}


	/**
	 * @return the Taverna 1 processor for which a translator could not be found
	 */
	public Processor getTaverna1Processor() {
		return taverna1Processor;
	}
	
	
}
