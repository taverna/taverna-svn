package net.sf.taverna.t2.cloudone;

/**
 * Identifier within the data cloud system for a single DataDocument, error or
 * list object.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public abstract class EntityIdentifier {

	private final String id;

	private final IDType type;

	private final String namespace;

	public EntityIdentifier(String id) throws MalformedIdentifierException {
		String[] split = id.split(":");
		if (split.length != 4) {
			throw new MalformedIdentifierException(
					"Exta / missing colons in ID : " + id);
		}
		if (!split[0].equals("urn")) {
			throw new MalformedIdentifierException("ID must start 'urn:' in "
					+ id);
		}
		if (!split[1].equals("t2data")) {
			throw new MalformedIdentifierException(
					"ID must start 'urn:t2data:' in " + id);
		}
		if (split[2].equals("list")) {
			type = IDType.List;
		} else if (split[2].equals("ddoc")) {
			type = IDType.Data;
		} else if (split[2].equals("error")) {
			type = IDType.Error;
		} else if (split[2].equals("literal")) {
			type = IDType.Literal;
		} else {
			throw new MalformedIdentifierException("Unrecognized ID type : "
					+ split[2] + " in " + id);
		}
		if (!split[3].startsWith("//")) {
			throw new MalformedIdentifierException(
					"Expected urn:t2data:<type>:// in " + id);
		}

		String[] pathParts = split[3].split("/", 4);
		this.namespace = pathParts[2];
		if (!namespace.matches("[a-zA-Z0-9\\-_\\.]+")) {
			throw new MalformedIdentifierException("Invalid namespace '"
					+ namespace + "' in " + id);
		}
		validate(pathParts[3]);
		this.id = id;
	}

	@Override
	public String toString() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof EntityIdentifier)) {
			return false;
		}
		return ((EntityIdentifier) o).id.equals(this.id);
	}

	/**
	 * Get the type of this identifier
	 * 
	 * @return
	 */
	public IDType getType() {
		return this.type;
	}

	/**
	 * Each identifier has a namespace allocated by the datamanager that created
	 * it
	 * 
	 * @return
	 */
	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * Return the conceptual depth of the entity identified by this identifier
	 * 
	 * @return
	 */
	public abstract int getDepth();

	/**
	 * Called in the constructor to validate the specified identifier. Do not
	 * screw around with it!
	 * 
	 * @param identifierString
	 * @throws MalformedIdentifierException
	 */
	protected abstract void validate(String identifierString)
			throws MalformedIdentifierException;

	/**
	 * Get the local name of the entity
	 */
	protected abstract String getName();

}
