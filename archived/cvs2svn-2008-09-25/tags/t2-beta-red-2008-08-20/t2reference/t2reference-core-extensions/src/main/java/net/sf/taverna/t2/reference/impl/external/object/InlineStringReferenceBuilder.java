package net.sf.taverna.t2.reference.impl.external.object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.taverna.t2.reference.ExternalReferenceBuilderSPI;
import net.sf.taverna.t2.reference.ExternalReferenceConstructionException;
import net.sf.taverna.t2.reference.ReferenceContext;

/**
 * Build an InlineStringReference from an InputStream
 * 
 * @author Tom Oinn
 * 
 */
public class InlineStringReferenceBuilder implements
		ExternalReferenceBuilderSPI<InlineStringReference> {

	public InlineStringReference createReference(InputStream byteStream,
			ReferenceContext context) {
		try {
			String contents = StreamToStringConverter
					.readFile(new BufferedReader(new InputStreamReader(
							byteStream)));
			InlineStringReference ref = new InlineStringReference();
			ref.setContents(contents);
			return ref;
		} catch (IOException e) {
			throw new ExternalReferenceConstructionException(e);
		}
	}

	public float getConstructionCost() {
		return 0.1f;
	}

	public Class<InlineStringReference> getReferenceType() {
		return InlineStringReference.class;
	}

	public boolean isEnabled(ReferenceContext context) {
		return true;
	}

}
