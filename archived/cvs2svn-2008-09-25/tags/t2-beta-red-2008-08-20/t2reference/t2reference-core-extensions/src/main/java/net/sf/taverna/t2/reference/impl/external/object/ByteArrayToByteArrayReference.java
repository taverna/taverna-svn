package net.sf.taverna.t2.reference.impl.external.object;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ValueToReferenceConversionException;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;

/**
 * Convert a byte[] to a ByteArrayReference
 * 
 * @author Tom Oinn
 * 
 */
public class ByteArrayToByteArrayReference implements ValueToReferenceConverterSPI {

	/**
	 * Can convert if the object is an instance of byte[]
	 */
	public boolean canConvert(Object o, ReferenceContext context) {
		return (o instanceof byte[]);
	}

	/**
	 * Return a new InlineByteArrayReference wrapping the supplied byte[]
	 */
	public ExternalReferenceSPI convert(Object o, ReferenceContext context)
			throws ValueToReferenceConversionException {
		InlineByteArrayReference result = new InlineByteArrayReference();
		result.setValue((byte[]) o);
		return result;
	}

}
