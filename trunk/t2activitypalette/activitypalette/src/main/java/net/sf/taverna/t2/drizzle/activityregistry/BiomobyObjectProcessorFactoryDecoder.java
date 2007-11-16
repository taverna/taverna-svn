/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.biomoby.client.taverna.plugin.BiomobyObjectProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class BiomobyObjectProcessorFactoryDecoder extends
		ProcessorFactoryDecoder<BiomobyObjectProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			BiomobyObjectProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.MobyAuthorityKey,
				new StringValue(encodedFactory.getAuthorityName()));
		targetSet.setProperty(encodedFactory, CommonKey.MobyEndpointKey,
				new StringValue(encodedFactory.getMobyEndpoint()));

	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(BiomobyObjectProcessorFactory.class) &&
				BiomobyObjectProcessorFactory.class.isAssignableFrom(sourceClass));
		
	}

}
