/**
 * 
 */
package net.sf.taverna.t2.annotation;

import java.net.URL;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * @author alanrw
 *
 */
@AppliesTo(workflowObjectType={Activity.class}, many=false)
public interface LocationUrl extends WorkflowAnnotation {
	URL getUrl();
}
