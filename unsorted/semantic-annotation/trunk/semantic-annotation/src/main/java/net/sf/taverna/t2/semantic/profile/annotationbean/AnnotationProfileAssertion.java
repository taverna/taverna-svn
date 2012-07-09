/**
 * 
 */
package net.sf.taverna.t2.semantic.profile.annotationbean;

import java.net.URL;

import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
@AppliesTo(targetObjectType = { Dataflow.class }, many = false)
public class AnnotationProfileAssertion implements AnnotationBeanSPI {
	
	private URL url;

	public AnnotationProfileAssertion() {
		super();
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}
}
