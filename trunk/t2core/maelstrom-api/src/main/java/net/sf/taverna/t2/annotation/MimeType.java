package net.sf.taverna.t2.annotation;

/**
 * A single MIME type, intended to be used to annotate an input or output port
 * within the workflow to denote the type within that system of data produced or
 * consumed by the port.
 * 
 * @author Tom Oinn
 * 
 */
public interface MimeType extends WorkflowAnnotation {

	/**
	 * Return the MIME type as a string, mime types look like 'part/part'. We
	 * may want to consider whether it's possible to make this a genuine
	 * enumeration driven off a canonical list of MIME types or whether it's
	 * best kept as the current (free) string. The advantage of an enumerated
	 * type is that we could attach description to the MIME types which would
	 * help with the UI construction but maybe this isn't the place to put it
	 * (should this link be in the UI layer? probably)
	 * 
	 * @return the MIME type as a string.
	 */
	public String getMIMEType();

}
