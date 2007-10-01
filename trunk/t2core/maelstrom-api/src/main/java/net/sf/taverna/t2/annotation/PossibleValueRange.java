/**
 * 
 */
package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.workflowmodel.InputPort;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={InputPort.class}, many=false)
// TODO How does this overlap with PossibleValueSet?
public interface PossibleValueRange extends WorkflowAnnotation {
	AnnotationValue getMinimumValue();
	AnnotationValue getMaximumValue();
}
