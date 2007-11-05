/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoder;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class WsdlProcessorFactoryDecoder implements PropertyDecoder {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoder#canDecode(java.lang.Object)
	 */
	public boolean canDecode(Object encodedObject) {
		boolean result = (encodedObject instanceof WSDLBasedProcessorFactory);
		return result;
	}

	public Set<ProcessorFactory> decode(PropertiedObjectSet<ProcessorFactory> target, Object encodedObject) {
		Set<ProcessorFactory> result = new HashSet<ProcessorFactory> ();
		if (! (encodedObject instanceof WSDLBasedProcessorFactory)) {
			return result;
		}
		WSDLBasedProcessorFactory encodedFactory = (WSDLBasedProcessorFactory) encodedObject;
		target.addObject(encodedFactory);
		result.add(encodedFactory);
		target.setProperty(encodedFactory, CommonKey.ProcessorClassKey, CommonKey.WsdlValue);
		target.setProperty(encodedFactory, CommonKey.WsdlLocationKey, new StringValue(encodedFactory.getWSDLLocation()));
		target.setProperty(encodedFactory, CommonKey.WsdlOperationKey, new StringValue(encodedFactory.getOperationName()));
		target.setProperty(encodedFactory, CommonKey.WsdlPortTypeKey, new StringValue(encodedFactory.getPortTypeName().getLocalPart()));
		target.setProperty(encodedFactory, CommonKey.NameKey, new StringValue(encodedFactory.getName()));
		return result;
	}

}
