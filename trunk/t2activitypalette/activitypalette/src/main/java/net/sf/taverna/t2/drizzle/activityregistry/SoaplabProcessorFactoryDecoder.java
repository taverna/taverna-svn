/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class SoaplabProcessorFactoryDecoder extends ProcessorFactoryDecoder<SoaplabProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			SoaplabProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.SoaplabEndpointKey, new StringValue(encodedFactory.getEndpoint()));
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(SoaplabProcessorFactory.class) &&
				SoaplabProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
