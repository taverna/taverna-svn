/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class WorkflowProcessorFactoryDecoder extends ProcessorFactoryDecoder<WorkflowProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			WorkflowProcessorFactory encodedFactory) {
		targetSet.setProperty(encodedFactory, CommonKey.WorkflowDefinitionURLKey, new StringValue(encodedFactory.getDefinitionURL()));
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(WorkflowProcessorFactory.class) &&
				WorkflowProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
