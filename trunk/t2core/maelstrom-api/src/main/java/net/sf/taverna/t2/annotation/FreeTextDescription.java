package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.service.Service;

/**
 * An unconstrained textual description held as a String
 * 
 * @author Tom Oinn
 * 
 */
@AppliesTo(workflowObjectType = { Dataflow.class, Processor.class, Service.class }, many = false)
public interface FreeTextDescription extends WorkflowAnnotation {

	/**
	 * Returns the descriptive text
	 * 
	 * @return free text description
	 */
	public String getDescription();

}
