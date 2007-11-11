/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class WsdlProcessorFactoryDecoder extends ProcessorFactoryDecoder<WSDLBasedProcessorFactory> {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoder#canDecode(java.lang.Object)
	 */
	public boolean canDecode(Object encodedObject) {
		boolean result = (encodedObject instanceof WSDLBasedProcessorFactory);
		return result;
	}

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			WSDLBasedProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.WsdlLocationKey, new StringValue(encodedFactory.getWSDLLocation()));
		targetSet.setProperty(encodedFactory, CommonKey.WsdlOperationKey, new StringValue(encodedFactory.getOperationName()));
		targetSet.setProperty(encodedFactory, CommonKey.WsdlPortTypeKey, new StringValue(encodedFactory.getPortTypeName().getLocalPart()));
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(WSDLBasedProcessorFactory.class) &&
				WSDLBasedProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
