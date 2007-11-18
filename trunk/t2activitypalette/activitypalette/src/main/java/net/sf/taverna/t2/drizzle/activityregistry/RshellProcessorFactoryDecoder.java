/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import nl.utwente.ewi.hmi.taverna.scuflworkers.rshell.RshellProcessorFactory;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class RshellProcessorFactoryDecoder extends ProcessorFactoryDecoder<RshellProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		}
	};
	
	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			RshellProcessorFactory encodedFactory) {
		// TODO Look at prototype
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(RshellProcessorFactory.class) &&
				RshellProcessorFactory.class.isAssignableFrom(sourceClass));
	}

	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
