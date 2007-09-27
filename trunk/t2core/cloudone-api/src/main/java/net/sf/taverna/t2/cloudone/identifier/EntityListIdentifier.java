package net.sf.taverna.t2.cloudone.identifier;

/**
 * An identifier for an EntityList. Naming of this gets a bit confusing as
 * really the EntityList is an EntityIdentifierList but that would have led to
 * this class being called EntityIdentifierListIdentifier and there are limits
 * to the sadism of the API designers.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class EntityListIdentifier extends EntityIdentifier {

	private int depth;

	private String name;

	public EntityListIdentifier(String id) throws MalformedIdentifierException {
		super(id);
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected void validate(String identifierString)
			throws MalformedIdentifierException {
		String[] parts = identifierString.split("/");
		if (parts.length != 2) {
			throw new MalformedIdentifierException(
					"List ID must have identifier and depth in "
							+ identifierString);
		}
		try {
			depth = Integer.parseInt(parts[1]);
		} catch (NumberFormatException nfe) {
			throw new MalformedIdentifierException(
					"Depth of list must be specified as decimal integer in "
							+ identifierString, nfe);
		}
		if (depth < 1) {
			throw new MalformedIdentifierException(
					"Depth of list must be at least 1 in " + identifierString);
		}
		name = parts[0];
	}

}
