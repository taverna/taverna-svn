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
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessorFactory;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public final class SoaplabProcessorFactoryDecoder extends ProcessorFactoryDecoder<SoaplabProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		add(CommonKey.SoaplabEndpointKey);
		add(CommonKey.SoaplabCategoryKey);
		}
	};
	
	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			SoaplabProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.SoaplabEndpointKey, new StringValue(encodedFactory.getEndpoint()));
		targetSet.setProperty(encodedFactory, CommonKey.SoaplabCategoryKey, new StringValue(encodedFactory.getCategory()));
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(SoaplabProcessorFactory.class) &&
				SoaplabProcessorFactory.class.isAssignableFrom(sourceClass));
	}

	@Override
	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
