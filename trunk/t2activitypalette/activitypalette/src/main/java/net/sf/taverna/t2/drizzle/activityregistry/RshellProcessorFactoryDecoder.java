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
@SuppressWarnings("serial")
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
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
		if (encodedFactory == null) {
			throw new NullPointerException("encodedFactory cannot be null"); //$NON-NLS-1$
		}
		// TODO Look at prototype
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		if (targetClass == null) {
			throw new NullPointerException("targetClass cannot be null"); //$NON-NLS-1$
		}
		return (targetClass.isAssignableFrom(RshellProcessorFactory.class) &&
				RshellProcessorFactory.class.isAssignableFrom(sourceClass));
	}

	@Override
	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
