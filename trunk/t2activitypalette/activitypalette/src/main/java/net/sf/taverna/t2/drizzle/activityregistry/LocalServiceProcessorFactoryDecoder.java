/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class LocalServiceProcessorFactoryDecoder extends ProcessorFactoryDecoder<LocalServiceProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			LocalServiceProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.LocalServiceWorkerClassKey, new StringValue(encodedFactory.getWorkerClassName()));
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(LocalServiceProcessorFactory.class) &&
				LocalServiceProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
