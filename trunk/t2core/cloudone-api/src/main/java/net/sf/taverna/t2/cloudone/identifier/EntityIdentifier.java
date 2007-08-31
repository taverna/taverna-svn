package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
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

	private String id;

	private IDType type;

	private String namespace;

	/**
	 * For use in combination with {@link #setFromBean(EntityIdentifierBean)}
	 * 
	 */
	public EntityIdentifier() {
		id = null;
		type = null;
		namespace = null;
	}

	public EntityIdentifier(String id) throws MalformedIdentifierException {
		setFromBean(id);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof EntityIdentifier)) {
			return false;
		}
		return ((EntityIdentifier) o).id.equals(this.id);
	}

	public String getAsBean() {
		return this.id;
	}

	/**
	 * Return the conceptual depth of the entity identified by this identifier
	 * 
	 * @return
	 */
	public abstract int getDepth();

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
	 * Get the type of this identifier
	 * 
	 * @return
	 */
	public IDType getType() {
		return this.type;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public void setFromBean(String id) throws MalformedIdentifierException {
		type = EntityIdentifiers.findType(id);
		String[] split = id.split(":");
		if (!split[3].startsWith("//")) {
			throw new MalformedIdentifierException(
					"Expected urn:t2data:<type>:// in " + id);
		}

		String[] pathParts = split[3].split("/", 4);
		this.namespace = pathParts[2];
		if (! isValidName(namespace)) {
			throw new MalformedIdentifierException("Invalid namespace '"
					+ namespace + "' in " + id);
		}
		validate(pathParts[3]);
		if (! isValidName(getName())) {
			throw new MalformedIdentifierException("Invalid name '" + getName()
					+ "' in " + id);
		}
		this.id = id;
	}

	public final static boolean isValidName(String name) {
		return name != null && name.matches("[a-zA-Z0-9\\-_\\.]+");
	}
	
	@Override
	public String toString() {
		return this.id;
	}

	/**
	 * Get the local name of the entity
	 */
	public abstract String getName();

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
