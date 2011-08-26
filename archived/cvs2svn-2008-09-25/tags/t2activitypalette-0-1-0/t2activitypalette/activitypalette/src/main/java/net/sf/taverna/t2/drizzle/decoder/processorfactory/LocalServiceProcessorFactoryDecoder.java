/**
 * 
 */
package net.sf.taverna.t2.drizzle.decoder.processorfactory;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.decoder.CommonKey;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessorFactory;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public final class LocalServiceProcessorFactoryDecoder extends ProcessorFactoryDecoder<LocalServiceProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		add(CommonKey.LocalServiceWorkerClassKey);
		add(CommonKey.CategoryKey);
		}
	};
	
	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactoryAdapter> targetSet,
			ProcessorFactoryAdapter adapter,
			LocalServiceProcessorFactory encodedFactory) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
		if (encodedFactory == null) {
			throw new NullPointerException("encodedFactory cannot be null"); //$NON-NLS-1$
		}
		if (encodedFactory.getWorkerClassName() != null) {
		targetSet.setProperty(adapter, CommonKey.LocalServiceWorkerClassKey, new StringValue(encodedFactory.getWorkerClassName()));
		}
		if (encodedFactory.getCategory() != null) {
		targetSet.setProperty(adapter, CommonKey.CategoryKey, new StringValue(encodedFactory.getCategory()));
		}
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.decoder.PropertyDecoder#canDecode(java.lang.Class, java.lang.Class)
	 */
	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		if (targetClass == null) {
			throw new NullPointerException("targetClass cannot be null"); //$NON-NLS-1$
		}
		return (targetClass.isAssignableFrom(ProcessorFactoryAdapter.class) &&
				LocalServiceProcessorFactory.class.isAssignableFrom(sourceClass));
	}

	@Override
	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
