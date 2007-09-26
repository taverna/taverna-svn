package net.sf.taverna.t2.cyclone.translators;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.embl.ebi.escience.scufl.Processor;

/**
 * <p>
 * An interface defining an ActivityTranslator tied to translating a Taverna 1 Processor into a corresponding Taverna 2 Activity.<br>
 * </p>
 * 
 * @author Stuart Owen
 * @author David Withers
 *
 * @param <ConfigurationType>
 */
public interface ActivityTranslator<ConfigurationType> {
	
	/**
	 * <p>
	 * The entry point for carrying out a translation from a Taverna 1 Processor to a Taverna 2 Activity.<br>
	 * </p>
	 * 
	 * @param processor
	 * @return a translated Activity
	 * @throws ActivityConfigurationException
	 */
	Activity<ConfigurationType> doTranslation(Processor processor) throws ActivityConfigurationException;
}
