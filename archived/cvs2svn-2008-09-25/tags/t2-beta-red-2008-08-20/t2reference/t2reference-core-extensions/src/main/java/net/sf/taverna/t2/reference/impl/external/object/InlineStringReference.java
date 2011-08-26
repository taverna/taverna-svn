package net.sf.taverna.t2.reference.impl.external.object;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.sf.taverna.t2.reference.AbstractExternalReference;
import net.sf.taverna.t2.reference.DereferenceException;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.ValueCarryingExternalReference;

/**
 * Contains and references a String value
 * 
 * @author Tom Oinn
 * 
 */
public class InlineStringReference extends AbstractExternalReference implements
		ValueCarryingExternalReference<String> {

	// Hold the 'value' of this reference, probably the simplest backing store
	// possible for an ExternalReferenceSPI implementation :)
	private String contents;

	/**
	 * Set the 'value' of this reference as a string. It's not really a
	 * reference type in any true sense of the word, but it'll do for testing
	 * the augmentation system. This method is really here so you can configure
	 * test beans from spring.
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}

	/**
	 * Get the 'value' of this reference as a string, really just returns the
	 * internal string representation.
	 */
	public String getContents() {
		return this.contents;
	}

	/**
	 * Fakes a de-reference operation, returning a byte stream over the string
	 * data.
	 */
	public InputStream openStream(ReferenceContext arg0) {
		try {
			return new ByteArrayInputStream(this.contents
					.getBytes(getCharset()));
		} catch (UnsupportedEncodingException e) {
			throw new DereferenceException(e);
		}
	}

	/**
	 * Default resolution cost of 0.0f whatever the contents
	 */
	@Override
	public float getResolutionCost() {
		return 0.0f;
	}

	/**
	 * Data nature set to 'ReferencedDataNature.TEXT'
	 */
	@Override
	public ReferencedDataNature getDataNature() {
		return ReferencedDataNature.TEXT;
	}

	/**
	 * Character encoding set to 'UTF-8' by default
	 */
	@Override
	public String getCharset() {
		return "UTF-8";
	}

	/**
	 * String representation for testing, returns <code>string{CONTENTS}</code>
	 */
	@Override
	public String toString() {
		return "string{" + contents + "}";
	}

	public String getValue() {
		return getContents();
	}

	public Class<String> getValueType() {
		return String.class;
	}

}
