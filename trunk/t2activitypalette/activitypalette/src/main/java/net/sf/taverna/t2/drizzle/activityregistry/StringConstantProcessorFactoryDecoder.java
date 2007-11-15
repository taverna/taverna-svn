/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class StringConstantProcessorFactoryDecoder extends ProcessorFactoryDecoder<StringConstantProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			StringConstantProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.StringConstantValueKey, new StringValue(encodedFactory.getValue()));
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(StringConstantProcessorFactory.class) &&
				StringConstantProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
