package net.sf.taverna.t2.reference;

/**
 * SPI for components that can convert an arbitrary object to an
 * ExternalReferenceSPI representing the value of that object. Used by
 * implementations of {@link ReferenceService#register(Object, int, boolean)} to map
 * arbitrary objects to ExternalReferenceSPI instances if encountered during the
 * registration process. This SPI is only used if the boolean useConverterSPI
 * parameter is set to true on that method.
 * 
 * @author Tom Oinn
 * 
 */
public interface ValueToReferenceConverterSPI {

	/**
	 * Can this SPI implementation convert the specified object to an
	 * ExternalReferenceSPI? This test should be as lightweight as possible, and
	 * will usually be based on the Class of the object supplied.
	 * 
	 * @return whether this converter is applicable to the specified object
	 */
	public boolean canConvert(Object o);

	/**
	 * Construct and return a new ExternalReferenceSPI implementation which is
	 * in some way equivalent to the supplied object. This is not intended to be
	 * a two-way process necessarily, although the conversion should attempt to
	 * be conservative (so not actually changing the data!).
	 * 
	 * @param o
	 * @return
	 * @throws ValueToReferenceConversionException
	 *             if any problem occurs during the conversion
	 */
	public ExternalReferenceSPI convert(Object o)
			throws ValueToReferenceConversionException;

}
