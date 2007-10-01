/**
 * 
 */
package net.sf.taverna.t2.annotation;

import java.util.Set;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={Activity.class}, many=false)
public interface ExampleValueCombination extends WorkflowAnnotation {
	Set<ExampleValueSet> getExampleValueSets();
	
	// At most one ExampleValueSet per port in a given ExampleValueCombination
}
