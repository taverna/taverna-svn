package net.sf.taverna.t2.annotation.annotationbeans;

import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * An unconstrained textual description held as a String
 * 
 * @author Tom Oinn
 * 
 */
@AppliesTo(targetObjectType = { Dataflow.class, Processor.class,
		Activity.class, Port.class, Datalink.class, Condition.class }, many = false)
public class FreeTextDescription extends AbstractTextualValueAssertion {

	/**
	 * Default constructor as mandated by java bean specification
	 */
	public FreeTextDescription() {
		//
	}

}
