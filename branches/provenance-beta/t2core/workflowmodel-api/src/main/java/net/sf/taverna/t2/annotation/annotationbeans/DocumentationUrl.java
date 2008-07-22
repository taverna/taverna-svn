package net.sf.taverna.t2.annotation.annotationbeans;

import java.net.URL;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A link to documentation for the target element contained at a particular
 * Uniform Resource Locator (URL)
 * 
 * @author Tom Oinn
 * @author Alan Williams
 */
@AppliesTo(targetObjectType = { Port.class, Activity.class, Processor.class, Dataflow.class }, many = true)
public class DocumentationUrl implements AnnotationBeanSPI {

	private URL documentationURL;

	/**
	 * Default constructor as mandated by java bean specification
	 */
	public DocumentationUrl() {
		//
	}

	public URL getDocumentationURL() {
		return documentationURL;
	}

	public void setDocumentationURL(URL documentationURL) {
		this.documentationURL = documentationURL;
	}

}
