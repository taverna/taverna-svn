package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.cloudone.entity.EntityList;

/**
 * An identifier for an {@link EntityList}.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 * As a hack, I've included the presence or otherwise of error tokens within the
 * collection in the identifier. This is nasty and will need to be fixed
 * properly when we do the data manager refactor but for now it should work.
 * (tmo, 27th March 2008). The ID now looks like /name/depth/[t|f] where the
 * last is 'true' or 'false' returned by the 'getContainsErrors' method.
 */
public class EntityListIdentifier extends EntityIdentifier {

	private int depth;

	private String name;

	private boolean containsErrors;

	/**
	 * Construct an EntityListIdentifier from an identifier string.
	 * 
	 * @param id
	 *            The identifier string
	 * @throws MalformedIdentifierException
	 *             If the identifier was not a valid EntityListIdentifier
	 */
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

	public boolean getContainsErrors() {
		return containsErrors;
	}

	@Override
	protected void validate(String identifierString)
			throws MalformedIdentifierException {
		String[] parts = identifierString.split("/");
		if (parts.length != 3 && parts.length != 2) {
			throw new MalformedIdentifierException(
					"List ID must have identifier, depth and error status in "
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
		if (parts.length == 3) {
			containsErrors = (parts[2].equals("t"));
		} else {
			// Hack to avoid having to rewrite half a million unit tests which
			// expected the old form of the URI *sigh*
			containsErrors = false;
		}
		name = parts[0];
	}

}
