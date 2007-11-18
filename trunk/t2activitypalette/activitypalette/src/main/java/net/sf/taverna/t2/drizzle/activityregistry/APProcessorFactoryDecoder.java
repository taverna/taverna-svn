/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor.APProcessorFactory;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class APProcessorFactoryDecoder extends ProcessorFactoryDecoder<APProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		}
	};
	

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

	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
