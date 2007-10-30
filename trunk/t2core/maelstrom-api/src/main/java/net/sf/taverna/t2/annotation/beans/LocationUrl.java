/**
 * 
 */
package net.sf.taverna.t2.annotation.beans;

import java.net.URL;

import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={Activity.class}, many=false)
public interface LocationUrl extends WorkflowAnnotation {
	URL getUrl();
}
