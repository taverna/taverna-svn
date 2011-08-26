package net.sf.taverna.t2.reference;

/**
 * Where possible ExternalReferenceSPI implementations should be able to
 * determine whether the data they refer to is textual or binary in nature. This
 * enumeration contains values for textual, binary and unknown data natures.
 * 
 * @author Tom Oinn
 * 
 */
public enum ReferencedDataNature {

	/**
	 * The data is binary, no character encoding will be specified.
	 */
	BINARY,

	/**
	 * The data is textual, character encoding may be defined.
	 */
	TEXT,

	/**
	 * Unknown data nature.
	 */
	UNKNOWN;

}
