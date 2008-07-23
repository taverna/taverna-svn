package net.sf.taverna.t2.reference.impl.external.object;

import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.t2.reference.ExternalReferenceBuilderSPI;
import net.sf.taverna.t2.reference.ExternalReferenceConstructionException;
import net.sf.taverna.t2.reference.ReferenceContext;

/**
 * Build an InlineByteArrayReference from an InputStream
 * 
 * @author Tom Oinn
 * 
 */
public class InlineByteArrayReferenceBuilder implements
		ExternalReferenceBuilderSPI<InlineByteArrayReference> {

	public InlineByteArrayReference createReference(InputStream byteStream,
			ReferenceContext context) {
		try {
			byte[] contents = StreamToByteArrayConverter.readFile(byteStream);
			InlineByteArrayReference ref = new InlineByteArrayReference();
			ref.setValue(contents);
			return ref;
		} catch (IOException e) {
			throw new ExternalReferenceConstructionException(e);
		}
	}

	public float getConstructionCost() {
		return 0.1f;
	}

	public Class<InlineByteArrayReference> getReferenceType() {
		return InlineByteArrayReference.class;
	}

	public boolean isEnabled(ReferenceContext context) {
		return true;
	}

}
