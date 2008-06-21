package net.sf.taverna.t2referencetest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.taverna.t2.reference.ExternalReferenceBuilderSPI;
import net.sf.taverna.t2.reference.ExternalReferenceConstructionException;
import net.sf.taverna.t2.reference.ReferenceContext;

/**
 * Trivially build a GreenReference from an InputStream, implementing the
 * ExternalReferenceBuilderSPI interface. Used in the augmentation test cases.
 * 
 * @author Tom Oinn
 * 
 */
public class GreenBuilder implements
		ExternalReferenceBuilderSPI<GreenReference> {

	/**
	 * Construct a new GreenReference from the given input stream, ignoring the
	 * otherwise helpful context as we don't need any resources from it. We
	 * assume UTF-8 encoding as that's what all the test reference types use,
	 * again, with a real example this might have to be a bit smarter!
	 * 
	 * @throws ExternalReferenceConstructionException
	 *             if there are any issues building the new GreenReference
	 *             (which there won't be)
	 */
	public GreenReference createReference(InputStream is,
			ReferenceContext context)
			throws ExternalReferenceConstructionException {
		GreenReference newReference = new GreenReference();
		// Read input stream into the 'contents' property of the reference
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		try {
			newReference.setContents(in.readLine());
		} catch (IOException e) {
			throw new ExternalReferenceConstructionException(e);
		} finally {
			try {
				is.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return newReference;
	}

	/**
	 * Construction cost fixed at 1.5f
	 * 
	 * @return <code>1.5f</code>
	 */
	public float getConstructionCost() {
		return 1.5f;
	}

	/**
	 * @return <code>{@link net.sf.taverna.t2referencetest.GreenReference GreenReference}.class</code>
	 */
	public Class<GreenReference> getReferenceType() {
		return GreenReference.class;
	}

	/**
	 * Doesn't use any context resources so is always enabled
	 * 
	 * @return <code>true</code>
	 */
	public boolean isEnabled(ReferenceContext arg0) {
		return true;
	}

}
