package net.sf.taverna.t2.reference.impl.external.object;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ValueToReferenceConversionException;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;

/**
 * Convert a String to a StringReference
 * 
 * @author Tom Oinn
 * 
 */
public class StringToStringReference implements ValueToReferenceConverterSPI {

	/**
	 * Can convert if the object is an instance of java.lang.String
	 */
	public boolean canConvert(Object o, ReferenceContext context) {
		return (o instanceof java.lang.String);
	}

	/**
	 * Return a new InlineStringReference wrapping the supplied String
	 */
	public ExternalReferenceSPI convert(Object o, ReferenceContext context)
			throws ValueToReferenceConversionException {
		InlineStringReference result = new InlineStringReference();
		result.setContents((String) o);
		return result;
	}

}
