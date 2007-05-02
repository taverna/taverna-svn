package net.sf.taverna.t2.cloudone;

/**
 * Static methods to construct new instances of EntityIdentifier subclasses
 * without exposing their constructors outside the API.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public final class EntityIdentifiers {
	protected EntityIdentifiers() {
	}

	public static EntityIdentifier parse(String id)
			throws MalformedIdentifierException {
		String[] bits = id.split(":");
		if (bits.length < 3) {
			throw new MalformedIdentifierException(
					"Exta / missing colons in ID : " + id);
		}

		String type = bits[2];
		if ("ddoc".equals(type)) {
			return parseDocumentIdentifier(id);
		} else if ("error".equals(type)) {
			return parseErrorIdentifier(id);
		} else if ("list".equals(type)) {
			return parseListIdentifier(id);
		} else {
			throw new MalformedIdentifierException("Unknown identifier type '"
					+ type + "' in " + id);
		}
	}

	public static EntityListIdentifier parseListIdentifier(String id)
			throws MalformedIdentifierException {
		return new EntityListIdentifier(id);
	}

	public static ErrorDocumentIdentifier parseErrorIdentifier(String id)
			throws MalformedIdentifierException {
		return new ErrorDocumentIdentifier(id);
	}

	public static DataDocumentIdentifier parseDocumentIdentifier(String id)
			throws MalformedIdentifierException {
		return new DataDocumentIdentifier(id);
	}
}
