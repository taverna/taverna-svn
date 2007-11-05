/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class BiomobyProcessorFactoryDecoder implements PropertyDecoder {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoder#canDecode(java.lang.Object)
	 */
	public boolean canDecode(Object encodedObject) {
		boolean result = (encodedObject instanceof BiomobyProcessorFactory);
		return result;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoder#decode(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)
	 */
	public Set<ProcessorFactory> decode(
			PropertiedObjectSet<ProcessorFactory> target, Object encodedObject) {
		Set<ProcessorFactory> result = new HashSet<ProcessorFactory> ();
		if (! (encodedObject instanceof WSDLBasedProcessorFactory)) {
			return result;
		}
		BiomobyProcessorFactory encodedFactory = (BiomobyProcessorFactory) encodedObject;
		target.addObject(encodedFactory);
		result.add(encodedFactory);
		target.setProperty(encodedFactory, CommonKey.ProcessorClassKey, CommonKey.BiomobyValue);
		target.setProperty(encodedFactory, CommonKey.MobyEndpointKey, new StringValue(encodedFactory.getMobyEndpoint()));
		target.setProperty(encodedFactory, CommonKey.MobyAuthorityKey, new StringValue(encodedFactory.getAuthorityName()));
		target.setProperty(encodedFactory, CommonKey.NameKey, new StringValue(encodedFactory.getName()));
		return result;
	}

}
