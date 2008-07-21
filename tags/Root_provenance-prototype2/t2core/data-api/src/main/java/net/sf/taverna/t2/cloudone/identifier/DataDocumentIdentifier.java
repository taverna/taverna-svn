package net.sf.taverna.t2.cloudone.identifier;

/**
 * Identifier for a data document. The identifier is of the form :
 * <p>
 * urn:t2data:ddoc://&lt;namespace&gt;/&lt;name&gt;
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class DataDocumentIdentifier extends EntityIdentifier {
	private String name;

	/**
	 * Construct a DataDocumentIdentifier from a given URI.
	 * 
	 * @param id
	 *            The identifying URI
	 * @throws MalformedIdentifierException
	 *             If the identifier was not a valid DataDocumentIdentifier
	 */
	public DataDocumentIdentifier(String id)
			throws MalformedIdentifierException {
		super(id);
	}

	@Override
	public int getDepth() {
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected void validate(String identifierString)
			throws MalformedIdentifierException {
		if (identifierString.contains("/")) {
			throw new MalformedIdentifierException(
					"Document name can not contain a slash (/) character in "
							+ identifierString);
		}
		name = identifierString;
	}

}
