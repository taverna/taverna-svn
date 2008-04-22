package net.sf.taverna.t2.cloudone.identifier;

import java.io.Serializable;

/**
 * Identifier within the data cloud system for a single DataDocument, error or
 * list object.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public abstract class EntityIdentifier implements Serializable{
	


	/**
	 * Check if a string is a <em>valid name</em>. A valid name is not null,
	 * containing 1 or more characters, but only containing alphanumeric
	 * characters (A-Z, a-z, 0-9) and the characters <code>- . _</code>
	 * <p>
	 * A valid name can be included in a structured URI, for instance
	 * {@link #getNamespace()} is required to be a valid name.
	 * 
	 * @param name
	 *            Name to check if is valid
	 * @return true if the name is considered <em>valid</em>.
	 */
	public final static boolean isValidName(String name) {
		return name != null && name.matches("[a-zA-Z0-9\\-_\\.]+");
	}

	private String identifier;

	private IDType type;

	private String namespace;

	/**
	 * Construct from a valid identifier.
	 * 
	 * @param identifier
	 *            The {@link EntityIdentifier}
	 * @throws MalformedIdentifierException
	 *             if the identifier was not a valid {@link EntityIdentifier}.
	 */
	public EntityIdentifier(String identifier)
			throws MalformedIdentifierException {
		setFromURI(identifier);
	}

	/**
	 * Construct an {@link EntityIdentifier} that must immediately be populated
	 * by {@link #setFromURI(String)}
	 * 
	 */
	protected EntityIdentifier() {
	}

	/**
	 * Check equality against object.
	 * 
	 * @param obj
	 *            Object against which to compare
	 * @return true if and only if <code>obj</code> is an
	 *         {@link EntityIdentifier} and it's {@link #identifier} is the same
	 *         as this {@link #identifier}.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EntityIdentifier)) {
			return false;
		}
		return ((EntityIdentifier) obj).identifier.equals(identifier);
	}

	/**
	 * Get as a String URI that is the same identifier as passed to
	 * {@link #EntityIdentifier(String)}. An {@link EntityIdentifier} can be
	 * reconstructed from this URI using {@link EntityIdentifiers#parse(String)}.
	 * 
	 * @return A String (URI) serialising this EntityIdentifier.
	 */
	public String getAsURI() {
		return identifier;
	}

	/**
	 * Return the conceptual depth of the entity identified by this identifier.
	 * 
	 * @return The conceptual depth
	 */
	public abstract int getDepth();

	/**
	 * Get the local name of the entity.
	 */
	public abstract String getName();

	/**
	 * Get the namespace allocated by the creating
	 * {@link net.sf.taverna.t2.cloudone.datamanager.DataManager}.
	 * 
	 * @return The namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Get the type of this identifier.
	 * 
	 * @return {@link IDType type} of identifier
	 */
	public IDType getType() {
		return type;
	}

	/**
	 * Calculate the hashcode for the identifier. The hashcode is based on the
	 * identifier's hashcode.
	 * 
	 * @return Calculated hashcode.
	 * 
	 */
	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	/**
	 * Populate from the serialised identifier URI String. This method can only
	 * be called once and only when the identifier was constructed using
	 * {@link #EntityIdentifier()}.
	 * 
	 * @param id
	 *            The identifier as earlier serialised using {@link #getAsURI()}
	 * @throws IllegalStateException
	 *             If the method is called twice or if object was not
	 *             constructed using {@link #EntityIdentifier()}
	 * @throws MalformedIdentifierException
	 *             if the string was not a valid identifier
	 */
	protected void setFromURI(String id) throws MalformedIdentifierException {
		if (identifier != null) {
			throw new IllegalStateException("Can't populate twice");
		}

		type = EntityIdentifiers.findType(id);
		String[] split = id.split(":");

		String path = split[3];
		if (!path.startsWith("//")) {
			throw new MalformedIdentifierException(
					"Expected urn:t2data:<type>:// in " + id);
		}

		String[] pathParts = path.split("/", 4);
		namespace = pathParts[2];
		if (!isValidName(namespace)) {
			throw new MalformedIdentifierException("Invalid namespace '"
					+ namespace + "' in " + id);
		}
		validate(pathParts[3]);
		identifier = id;
	}

	/**
	 * Return a string version of the identifier. This is the same URI as given
	 * by {@link #getAsURI()} and the string passed to the constructor
	 * {@link #EntityIdentifier(String)}.
	 */
	@Override
	public String toString() {
		return identifier;
	}

	/**
	 * Validate the specified identifier and extract any extra information.
	 * Called by the constructor and {@link #setFromURI(String)}. At minimum
	 * this would normally populate {@link #getName()}.
	 * 
	 * @param identifierString
	 *            The last part of the URI
	 * @throws MalformedIdentifierException
	 *             If the identifier string was not valid.
	 */
	protected abstract void validate(String identifierString)
			throws MalformedIdentifierException;

}
