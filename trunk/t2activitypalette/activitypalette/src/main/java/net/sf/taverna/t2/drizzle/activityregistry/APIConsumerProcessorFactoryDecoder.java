/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.apiconsumer.APIConsumerProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class APIConsumerProcessorFactoryDecoder extends ProcessorFactoryDecoder<APIConsumerProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			APIConsumerProcessorFactory encodedFactory) {
		// No details
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(APIConsumerProcessorFactory.class) &&
				APIConsumerProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
