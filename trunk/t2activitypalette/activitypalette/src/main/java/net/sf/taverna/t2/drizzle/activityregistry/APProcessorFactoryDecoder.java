/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor.APProcessorFactory;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class APProcessorFactoryDecoder extends ProcessorFactoryDecoder<APProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			APProcessorFactory encodedFactory) {
		// Nothing to do
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(APProcessorFactory.class) &&
				APProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
