/**
 * 
 */
package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * @author alanrw
 *
 */
//TODO
@AppliesTo(workflowObjectType={Activity.class}, many=false)
public interface HostInstitution extends WorkflowAnnotation {

}
