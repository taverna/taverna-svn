/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.notification.NotificationProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class NotificationProcessorFactoryDecoder extends ProcessorFactoryDecoder<NotificationProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			NotificationProcessorFactory encodedFactory) {
		// Nothing to do
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(NotificationProcessorFactory.class) &&
				NotificationProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
