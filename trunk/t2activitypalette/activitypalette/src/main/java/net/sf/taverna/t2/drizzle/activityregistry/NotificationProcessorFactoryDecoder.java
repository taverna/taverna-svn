/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.notification.NotificationProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class NotificationProcessorFactoryDecoder extends ProcessorFactoryDecoder<NotificationProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		}
	};
	
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

	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
