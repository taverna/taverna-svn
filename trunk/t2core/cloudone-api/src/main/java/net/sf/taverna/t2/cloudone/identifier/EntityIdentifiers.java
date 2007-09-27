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

	public static DataDocumentIdentifier parseDocumentIdentifier(String id)
			throws MalformedIdentifierException {
		return new DataDocumentIdentifier(id);
	}

	public static ErrorDocumentIdentifier parseErrorIdentifier(String id)
			throws MalformedIdentifierException {
		return new ErrorDocumentIdentifier(id);
	}

	public static EntityListIdentifier parseListIdentifier(String id)
			throws MalformedIdentifierException {
		return new EntityListIdentifier(id);
	}

	private static Literal parseLiteralIdentifier(String id) {
		return new Literal(id);
	}

	protected EntityIdentifiers() {
	}

}
