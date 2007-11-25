/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.biomoby.client.taverna.plugin.BiomobyProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;


/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public final class BiomobyProcessor2FactoryDecoder extends
		ProcessorFactoryDecoder<BiomobyProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		add(CommonKey.MobyAuthorityKey);
		add(CommonKey.MobyEndpointKey);
		}
	};
	

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			BiomobyProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.MobyAuthorityKey,
				new StringValue(encodedFactory.getAuthorityName()));
		targetSet.setProperty(encodedFactory, CommonKey.MobyEndpointKey,
				new StringValue(encodedFactory.getMobyEndpoint()));

	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(BiomobyProcessorFactory.class) &&
				BiomobyProcessorFactory.class.isAssignableFrom(sourceClass));
		
	}

	@Override
	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
