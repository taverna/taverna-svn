/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class LocalServiceProcessorFactoryDecoder extends ProcessorFactoryDecoder<LocalServiceProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		add(CommonKey.LocalServiceWorkerClassKey);
		add(CommonKey.LocalServiceCategoryKey);
		}
	};
	
	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			LocalServiceProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.LocalServiceWorkerClassKey, new StringValue(encodedFactory.getWorkerClassName()));
		targetSet.setProperty(encodedFactory, CommonKey.LocalServiceCategoryKey, new StringValue(encodedFactory.getCategory()));
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(LocalServiceProcessorFactory.class) &&
				LocalServiceProcessorFactory.class.isAssignableFrom(sourceClass));
	}

	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
