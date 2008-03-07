package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.cloudone.entity.Literal;

/**
 * Static methods to construct new instances of EntityIdentifier subclasses
 * without exposing their constructors outside the API.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public final class EntityIdentifiers {
	/**
	 * Find type of given {@link EntityIdentifier}.
	 *
	 * @param id
	 *            The identifier
	 * @return The {@link IDType}
	 * @throws MalformedIdentifierException
	 *             If the identifier was not valid or its type was unrecognised
	 */
	public static IDType findType(String id)
			throws MalformedIdentifierException {
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
		if (split[2].equals(IDType.List.uripart)) {
			return IDType.List;
		} else if (split[2].equals(IDType.Data.uripart)) {
			return IDType.Data;
		} else if (split[2].equals(IDType.Error.uripart)) {
			return IDType.Error;
		} else if (split[2].equals(IDType.Literal.uripart)) {
			return IDType.Literal;
		} else {
			throw new MalformedIdentifierException("Unrecognized ID type : "
					+ split[2] + " in " + id);
		}
	}

	/**
	 * Parse a identifier string and construct the appropriate
	 * {@link EntityIdentifier}.
	 *
	 * @param id
	 *            The identifier string
	 * @return The constructed {@link EntityIdentifier}
	 * @throws MalformedIdentifierException
	 *             If the string was not a valid identifier
	 */
	public static EntityIdentifier parse(String id)
			throws MalformedIdentifierException {
		IDType type = findType(id);
		if (type.equals(IDType.Data)) {
			return parseDocumentIdentifier(id);
		} else if (type.equals(IDType.Error)) {
			return parseErrorIdentifier(id);
		} else if (type.equals(IDType.List)) {
			return parseListIdentifier(id);
		} else if (type.equals(IDType.Literal)) {
			return parseLiteralIdentifier(id);
		} else {
			throw new MalformedIdentifierException("Unknown identifier type '"
					+ type + "' in " + id);
		}
	}

	/**
	 * Construct a {@link DataDocumentIdentifier} from identifier string.
	 *
	 * @param id
	 *            Identifier string
	 * @return Constructed {@link DataDocumentIdentifier}
	 * @throws MalformedIdentifierException
	 *             If the string was not a valid identifier
	 */
	public static DataDocumentIdentifier parseDocumentIdentifier(String id)
			throws MalformedIdentifierException {
		return new DataDocumentIdentifier(id);
	}

	/**
	 * Construct a {@link ErrorDocumentIdentifier} from identifier string.
	 *
	 * @param id
	 *            Identifier string
	 * @return Constructed {@link ErrorDocumentIdentifier}
	 * @throws MalformedIdentifierException
	 *             If the string was not a valid identifier
	 */
	public static ErrorDocumentIdentifier parseErrorIdentifier(String id)
			throws MalformedIdentifierException {
		return new ErrorDocumentIdentifier(id);
	}

	/**
	 * Construct a {@link EntityListIdentifier} from identifier string.
	 *
	 * @param id
	 *            Identifier string
	 * @return Constructed {@link EntityListIdentifier}
	 * @throws MalformedIdentifierException
	 *             If the string was not a valid identifier
	 */
	public static EntityListIdentifier parseListIdentifier(String id)
			throws MalformedIdentifierException {
		return new EntityListIdentifier(id);
	}

	/**
	 * Construct a {@link Literal} from identifier string.
	 *
	 * @param id
	 *            Identifier string
	 * @return Constructed {@link Literal}
	 * @throws MalformedIdentifierException
	 *             If the string was not a valid identifier
	 */
	private static Literal parseLiteralIdentifier(String id) {
		return new Literal(id);
	}

	/**
	 * Protected constructor, use static methods only.
	 */
	protected EntityIdentifiers() {
	}

}
