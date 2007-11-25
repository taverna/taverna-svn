/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessorFactory;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public final class WorkflowProcessorFactoryDecoder extends ProcessorFactoryDecoder<WorkflowProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		add(CommonKey.WorkflowDefinitionURLKey);
		}
	};
	
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

	@Override
	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
