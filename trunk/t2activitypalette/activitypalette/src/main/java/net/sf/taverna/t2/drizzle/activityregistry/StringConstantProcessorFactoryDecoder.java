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
import org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessorFactory;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public final class StringConstantProcessorFactoryDecoder extends ProcessorFactoryDecoder<StringConstantProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		add(CommonKey.StringConstantValueKey);
		}
	};
	
	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			StringConstantProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.StringConstantValueKey, new StringValue(encodedFactory.getValue()));
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(StringConstantProcessorFactory.class) &&
				StringConstantProcessorFactory.class.isAssignableFrom(sourceClass));
	}

	@Override
	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
