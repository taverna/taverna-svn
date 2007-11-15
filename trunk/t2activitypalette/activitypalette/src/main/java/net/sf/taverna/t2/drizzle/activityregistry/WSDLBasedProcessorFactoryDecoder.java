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
public final class WSDLBasedProcessorFactoryDecoder extends ProcessorFactoryDecoder<WSDLBasedProcessorFactory> {

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
