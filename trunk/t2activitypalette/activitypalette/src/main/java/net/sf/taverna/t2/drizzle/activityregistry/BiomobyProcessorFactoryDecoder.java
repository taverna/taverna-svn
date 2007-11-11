/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class BiomobyProcessorFactoryDecoder extends
		ProcessorFactoryDecoder<BiomobyProcessorFactory> {

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

}
