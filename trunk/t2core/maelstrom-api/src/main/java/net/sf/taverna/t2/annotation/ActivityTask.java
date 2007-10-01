/**
 * 
 */
package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={Processor.class, Activity.class}, many=true)
public interface ActivityTask extends OntologyTermAnnotation {

}
