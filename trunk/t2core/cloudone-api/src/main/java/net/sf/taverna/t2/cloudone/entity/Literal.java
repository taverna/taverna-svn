package net.sf.taverna.t2.cloudone.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Used to represent literal values such as booleans, integers, short strings
 * etc to avoid the overhead of going through the reference framework for
 * everything.
 * <p>
 * This class is both an EntityIdentifier and an Entity, attempts to resolve it
 * through the DataManager or get its identity through the Entity interface will
 * both return self.
 * 
 * @author Tom Oinn
 * 
 */
public class Literal extends EntityIdentifier implements
		Entity<Literal, String> {

	private static String prefix = "urn:t2data:literal://";

	private String value;

	public Literal(String id) throws MalformedIdentifierException {
		super(id);
	}

	@Override
	public int getDepth() {
		return 0;
	}

	@Override
	public String getName() {
		return value;
	}

	@Override
	protected void validate(String identifierString) {
		if (identifierString.contains("/")) {
			throw new MalformedIdentifierException(
					"Document name can not contain a slash (/) character in "
							+ identifierString);
		}
		this.value = identifierString;
	}

	public Literal getIdentifier() {
		return this;
	}

	/**
	 * Build a new Boolean literal
	 * 
	 * @param value
	 * @return
	 */
	public static Literal buildLiteral(Boolean value) {
		return new Literal(prefix + "boolean.literal/" + value.booleanValue());
	}

	/**
	 * Build a new Double literal
	 * 
	 * @param value
	 * @return
	 */
	public static Literal buildLiteral(Double value) {
		return new Literal(prefix + "double.literal/" + value.doubleValue());
	}

	/**
	 * Build a new Float literal
	 * 
	 * @param value
	 * @return
	 */
	public static Literal buildLiteral(Float value) {
		return new Literal(prefix + "float.literal/" + value.floatValue());
	}

	/**
	 * Build a new Integer literal
	 * 
	 * @param value
	 * @return
	 */
	public static Literal buildLiteral(Integer value) {
		return new Literal(prefix + "int.literal/" + value.intValue());
	}

	/**
	 * Build a new Long literal
	 * 
	 * @param value
	 * @return
	 */
	public static Literal buildLiteral(Long value) {
		return new Literal(prefix + "long.literal/" + value.longValue());
	}

	/**
	 * Build a new String literal, the string is URL encoded using UTF-8
	 * 
	 * @param value
	 * @return
	 */
	public static Literal buildLiteral(String value) {
		try {
			return new Literal(prefix + "string.literal/"
					+ URLEncoder.encode(value, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new Error("Unexpected exception", e);
		}
	}

	/**
	 * Return the value which this literal represents. Strings are decoded using
	 * the UTF-8 encoding; boolean, int, float, double and long are returned as
	 * their respective object wrapper types.
	 * 
	 * @return underlying value for this literal
	 */
	public Object getValue() {
		Object result = null;
		String namespace = getNamespace();

		if (namespace.startsWith("string")) {
			try {
				result = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (namespace.startsWith("int")) {
			result = Integer.parseInt(value);
		} else if (namespace.startsWith("boolean")) {
			result = Boolean.parseBoolean(value);
		} else if (namespace.startsWith("float")) {
			result = Float.parseFloat(value);
		} else if (namespace.startsWith("double")) {
			result = Double.parseDouble(value);
		} else if (namespace.startsWith("long")) {
			result = Long.parseLong(value);
		}
		return result;
	}

	/**
	 * Get the type of object this literal represents.
	 * 
	 * @return the Class of the object that would be returned by getValue()
	 */
	public Class<?> getValueType() {
		String namespace = getNamespace();

		if (namespace.startsWith("string")) {
			return String.class;
		} else if (namespace.startsWith("int")) {
			return Integer.class;
		} else if (namespace.startsWith("boolean")) {
			return Boolean.class;
		} else if (namespace.startsWith("float")) {
			return Float.class;
		} else if (namespace.startsWith("double")) {
			return Double.class;
		} else if (namespace.startsWith("long")) {
			return Long.class;
		}

		return Object.class;
	}

}
