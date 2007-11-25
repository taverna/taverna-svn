/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.apiconsumer.APIConsumerProcessorFactory;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public final class APIConsumerProcessorFactoryDecoder extends ProcessorFactoryDecoder<APIConsumerProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		}
	};
	
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

	@Override
	public Set<PropertyKey> getPropertyKeyProfile() {
		// TODO Auto-generated method stub
		return keyProfile;
	}

}
