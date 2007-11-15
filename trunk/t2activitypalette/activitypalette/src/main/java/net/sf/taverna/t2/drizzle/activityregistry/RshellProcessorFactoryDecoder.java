/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;
import nl.utwente.ewi.hmi.taverna.scuflworkers.rshell.RshellProcessorFactory;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class RshellProcessorFactoryDecoder extends ProcessorFactoryDecoder<RshellProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			RshellProcessorFactory encodedFactory) {
		// TODO Look at prototype
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(RshellProcessorFactory.class) &&
				RshellProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
