package net.sf.taverna.t2.cloudone.identifier;

/**
 * Identifier for a single
 * {@link net.sf.taverna.t2.cloudone.entity.ErrorDocument}.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class ErrorDocumentIdentifier extends EntityIdentifier {

	private int depth;

	private int implicitDepth;

	private String name;

	/**
	 * Construct an ErrorDocumentIdentifier from an identifier string.
	 * 
	 * @param id
	 *            The identifier string
	 * @throws MalformedIdentifierException
	 *             If the identifier was not a valid ErrorDocumentIdentifier
	 */
	public ErrorDocumentIdentifier(String id)
			throws MalformedIdentifierException {
		super(id);
	}

	/**
	 * Create an {@link ErrorDocumentIdentifier} which {@link #getDepth()} is
	 * one level less than this ErrorDocumentIdentifier. Its
	 * {@link #getImplicitDepth()} is one level higher to reflect this drilling.
	 * 
	 * @return An {@link ErrorDocumentIdentifier} that is one level higher than
	 *         this
	 */
	public ErrorDocumentIdentifier drill() {
		if (depth > 0) {
			try {
				return new ErrorDocumentIdentifier("urn:t2data:error://"
						+ getNamespace() + "/" + getName() + "/"
						+ (getDepth() - 1) + "/" + (getImplicitDepth() + 1));
			} catch (MalformedIdentifierException e) {
				// Should never reach this point if the logic above is correct
				throw new AssertionError(e);
			}
		} else {
			throw new IndexOutOfBoundsException(
					"Cannot drill into an error with depth 0");
		}
	}

	@Override
	public int getDepth() {
		return depth;
	}

	public int getImplicitDepth() {
		return implicitDepth;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected void validate(String identifierString)
			throws MalformedIdentifierException {
		String[] parts = identifierString.split("/");
		if (parts.length != 3) {
			throw new MalformedIdentifierException(
					"List ID must have identifier, depth and implicit depth in "
							+ identifierString);
		}
		try {
			depth = Integer.parseInt(parts[1]);
		} catch (NumberFormatException nfe) {
			throw new MalformedIdentifierException(
					"Depth of error must be specified as decimal integer in "
							+ identifierString, nfe);
		}
		if (depth < 0) {
			throw new MalformedIdentifierException(
					"Depth of error must not be negative in "
							+ identifierString);
		}
		try {
			implicitDepth = Integer.parseInt(parts[2]);
		} catch (NumberFormatException nfe) {
			throw new MalformedIdentifierException(
					"Implicit depth of error must be specified as decimal integer in "
							+ identifierString, nfe);
		}
		if (implicitDepth < 0) {
			throw new MalformedIdentifierException(
					"Implicit depth of error must not be negative in "
							+ identifierString);
		}

		name = parts[0];
	}


}
