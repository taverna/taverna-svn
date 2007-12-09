package net.sf.taverna.t2.annotation.annotationbeans;

import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.workflowmodel.Port;

/**
 * A single MIME type, intended to be used to annotate an input or output port
 * within the workflow to denote the type within that system of data produced or
 * consumed by the port.
 * 
 * @author Tom Oinn
 * 
 */
@AppliesTo(targetObjectType = { Port.class })
public class MimeType extends AbstractTextualValueAssertion {

	/**
	 * Default constructor as mandated by java bean specification
	 */
	public MimeType() {
		super();
	}

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
	@Override
	public String getText() {
		return super.getText();
	}

}
