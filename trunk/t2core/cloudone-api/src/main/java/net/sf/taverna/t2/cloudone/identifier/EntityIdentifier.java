package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.cloudone.bean.Beanable;

/**
 * Identifier within the data cloud system for a single DataDocument, error or
 * list object.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public abstract class EntityIdentifier implements Beanable<String> {

	public final static boolean isValidName(String name) {
		return name != null && name.matches("[a-zA-Z0-9\\-_\\.]+");
	}

	private String identifier;

	private IDType type;

	private String namespace;

	/**
	 * For use in combination with {@link #setFromBean(EntityIdentifierBean)}
	 *
	 */
	public EntityIdentifier() {
		identifier = null;
		type = null;
		namespace = null;
	}

	public EntityIdentifier(String identifier) throws MalformedIdentifierException {
		setFromBean(identifier);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof EntityIdentifier)) {
			return false;
		}
		return ((EntityIdentifier) o).identifier.equals(identifier);
	}

	public String getAsBean() {
		return identifier;
	}

	/**
	 * Return the conceptual depth of the entity identified by this identifier
	 *
	 * @return
	 */
	public abstract int getDepth();

	/**
	 * Get the local name of the entity
	 */
	public abstract String getName();

	/**
	 * Each identifier has a namespace allocated by the datamanager that created
	 * it
	 *
	 * @return
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Get the type of this identifier
	 *
	 * @return
	 */
	public IDType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	public void setFromBean(String id) throws MalformedIdentifierException {
		type = EntityIdentifiers.findType(id);
		String[] split = id.split(":");
		if (!split[3].startsWith("//")) {
			throw new MalformedIdentifierException(
					"Expected urn:t2data:<type>:// in " + id);
		}

		String[] pathParts = split[3].split("/", 4);
		namespace = pathParts[2];
		if (!isValidName(namespace)) {
			throw new MalformedIdentifierException("Invalid namespace '"
					+ namespace + "' in " + id);
		}
		validate(pathParts[3]);
		identifier = id;
	}

	@Override
	public String toString() {
		return identifier;
	}

	/**
	 * Called in the constructor to validate the specified identifier. Do not
	 * screw around with it!
	 *
	 * @param identifierString
	 * @throws MalformedIdentifierException
	 */
	protected abstract void validate(String identifierString)
			throws MalformedIdentifierException;

}
