package net.sf.taverna.t2.reference;

import java.io.InputStream;

/**
 * A reference to a single piece of data. This may or may not be within the
 * enactment infrastructure, it could refer to data held in a file, a URL, a
 * grid storage system or anything of that nature. Ideally the data this
 * reference points to should not change - we'd like to say 'must not change' at
 * this point but this isn't always possible, implementations should aim to
 * provide the appearance that data are immutable.
 * <p>
 * When used within the workflow engine implementations of this interface are
 * always contained in a ReferenceSet instance.
 * <p>
 * Implementors of this interface are strongly advised to use the
 * AbstractExternalReference superclass - this provides the necessary primary
 * key information used by hibernate-based implementations of the reference
 * manager.
 * <p>
 * This interface is an SPI - while implementations are never constructed based
 * on the SPI registry it is used to discover all implementing classes and
 * automatically register their hibernate mapping files. Implementors should add
 * their implementation class name to a
 * META-INF/services/net.sf.taverna.t2.reference.ExternalReferenceSPI file in
 * the implementation artifact. For examples please refer to the
 * t2reference-core-extensions module, this contains implementations of this
 * interface for common basic reference types.
 * 
 * @author Tom Oinn
 */
public interface ExternalReferenceSPI {

	/**
	 * Determine, if possible, whether the data this reference refers to is
	 * textual or binary in nature. If this determination is impossible, either
	 * because the ExternalReference implementation does not know or because the
	 * data is not accessible for some reason then this should return
	 * ReferenceDataNature.UNKNOWN
	 * 
	 * @return the nature of the referenced data
	 */
	public ReferencedDataNature getDataNature();

	/**
	 * For textual data return the character set that should be used to pull
	 * data into a java String object. Callers must deal with a null return
	 * value in the event of either unknown charset or unknown or binary data
	 * types.
	 * 
	 * @return string character set, for example 'utf-8', or <code>null</code>
	 *         if binary or unknown type.
	 */
	public String getCharset();

	/**
	 * Open and return an InputStream to the data referenced using, if required,
	 * any facilities within the supplied context.
	 * 
	 * @param context
	 *            the ReferenceContext object used to obtain e.g. security
	 *            agents or other facilities required when de-referencing this
	 *            reference.
	 * @return an InputStream providing access to the referenced data
	 * @throws DereferenceException
	 *             if the reference cannot be de-referenced. This may be because
	 *             of problems with the context such as security failures, or it
	 *             may be because the reference is inherently not possible to
	 *             de-reference (as in the case of a non-serializable API
	 *             consumer reference).
	 */
	public InputStream openStream(ReferenceContext context)
			throws DereferenceException;

	/**
	 * Resolution cost is an informal guide to how costly the process of
	 * de-referencing this reference would be. It's used when assessing which
	 * out of a set of ExternalReferenceSPI implementations to use to get the
	 * value of the reference(s), in particular when rendering to POJOs or when
	 * translating throught the de-reference / construct from stream route. As
	 * this property is highly complex and potentially expensive in itself to
	 * evaluate there's no requirement for it to be absolutely correct, it's
	 * just used as a guide that, for example, it is easier to get bytes from a
	 * file on the local disk than to get them from a resource held on a grid on
	 * the other side of the planet.
	 * 
	 * @return a float representing some notion of resolution cost, lower values
	 *         represent cheaper de-reference paths.
	 */
	public float getResolutionCost();

}
