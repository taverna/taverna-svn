/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.cloudone.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.sf.taverna.t2.cloudone.bean.LiteralBean;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;

/**
 * Used to represent literal values such as booleans, integers, short strings
 * etc to avoid the overhead of going through the reference framework for
 * everything.
 * <p>
 * This class is both an {@link EntityIdentifier} and an {@link Entity},
 * attempts to resolve it through the
 * {@link net.sf.taverna.t2.cloudone.datamanager.DataManager} or get its
 * identity through the {@link Entity} interface will both return self.
 * 
 * @author Tom Oinn
 * 
 */

public class Literal extends EntityIdentifier implements
		Entity<Literal, LiteralBean> {

	private static final String ENCODING = "UTF-8";
	private static final String LITERAL = ".literal/";
	private static final String LONG = "long";
	private static final String DOUBLE = "double";
	private static final String FLOAT = "float";
	private static final String BOOLEAN = "boolean";
	private static final String INT = "int";
	private static final String STRING = "string";
	private static final String PREFIX = "urn:t2data:literal://";

	/**
	 * Build a new boolean literal.
	 * 
	 * @param value
	 *            The boolean value
	 * @return A boolean Literal
	 */
	public static Literal buildLiteral(Boolean value) {
		return new Literal(PREFIX + BOOLEAN + LITERAL + value.booleanValue());
	}

	/**
	 * Build a new double literal.
	 * 
	 * @param value
	 *            The double value
	 * @return A double Literal
	 */
	public static Literal buildLiteral(Double value) {
		return new Literal(PREFIX + DOUBLE + LITERAL + value.doubleValue());
	}

	/**
	 * Build a new Float literal.
	 * 
	 * @param value
	 *            The float value
	 * @return A float Literal
	 */
	public static Literal buildLiteral(Float value) {
		return new Literal(PREFIX + FLOAT + LITERAL + value.floatValue());
	}

	/**
	 * Build a new Integer literal.
	 * 
	 * @param value
	 *            The integer value
	 * @return An integer Literal
	 */
	public static Literal buildLiteral(Integer value) {
		return new Literal(PREFIX + INT + LITERAL + value.intValue());
	}

	/**
	 * Build a new Long literal.
	 * 
	 * @param value
	 *            The Long value
	 * @return A Long Literal
	 */
	public static Literal buildLiteral(Long value) {
		return new Literal(PREFIX + LONG + LITERAL + value.longValue());
	}

	/**
	 * Build a new String literal. The string is URL encoded using UTF-8.
	 * Although there is no upper limit to the length of an URL, URLs should
	 * generally not be longer than say 250 characters. Use a
	 * {@link net.sf.taverna.t2.cloudone.datamanager.BlobStore} to store larger
	 * strings.
	 * {@link net.sf.taverna.t2.cloudone.datamanager.DataFacade#register(Object)}
	 * will do this automatically depending on
	 * {@link import net.sf.taverna.t2.cloudone.DataManager#getMaxIDLength()}.
	 * 
	 * @param value
	 *            A string
	 * @return A String Literal.
	 */
	public static Literal buildLiteral(String value) {
		try {
			return new Literal(PREFIX + STRING + LITERAL
					+ URLEncoder.encode(value, ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new Error("Unexpected exception", e);
		}
	}

	/**
	 * The URI for this Literal. Parsed by {@link #getValue()}.
	 * 
	 */
	private String value;

	/**
	 * Construct a Literal that must immediately be populated by
	 * {@link #setFromBean(LiteralBean)}.
	 * 
	 */
	public Literal() {
		super();
	}

	/**
	 * Construct a Literal from an identifier string. Note that to create a
	 * Literal representing a string, use the static
	 * {@link #buildLiteral(String)}.
	 * 
	 * @param id
	 *            The literal identifier
	 * @throws MalformedIdentifierException
	 *             If the identifier was not a valid Literal identifier
	 */
	public Literal(String id) throws MalformedIdentifierException {
		super(id);
	}

	/**
	 * Check equality against an object.
	 * 
	 * @param obj
	 *            Object to check against.
	 * @return true if and only if <code>obj</code> is a {@link Literal} and
	 *         it's value is the same as the value of this Literal.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Literal other = (Literal) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public int getDepth() {
		return 0;
	}

	public Literal getIdentifier() {
		return this;
	}

	@Override
	public String getName() {
		return value;
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
		if (namespace.startsWith(STRING)) {
			try {
				result = URLDecoder.decode(value, ENCODING);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Did not support encoding "
						+ ENCODING, e);
			}
		} else if (namespace.startsWith(INT)) {
			result = Integer.parseInt(value);
		} else if (namespace.startsWith(BOOLEAN)) {
			result = Boolean.parseBoolean(value);
		} else if (namespace.startsWith(FLOAT)) {
			result = Float.parseFloat(value);
		} else if (namespace.startsWith(DOUBLE)) {
			result = Double.parseDouble(value);
		} else if (namespace.startsWith(LONG)) {
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

		if (namespace.startsWith(STRING)) {
			return String.class;
		} else if (namespace.startsWith(INT)) {
			return Integer.class;
		} else if (namespace.startsWith(BOOLEAN)) {
			return Boolean.class;
		} else if (namespace.startsWith(FLOAT)) {
			return Float.class;
		} else if (namespace.startsWith(DOUBLE)) {
			return Double.class;
		} else if (namespace.startsWith(LONG)) {
			return Long.class;
		}

		return Object.class;
	}

	/**
	 * Calculate the hashcode. The hashcode is based on the URI in
	 * {@link #getName()}.
	 * 
	 */
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	protected void validate(String identifierString) {
		if (identifierString.contains("/")) {
			throw new MalformedIdentifierException(
					"Document name can not contain a slash (/) character in "
							+ identifierString);
		}
		value = identifierString;
	}

	public LiteralBean getAsBean() {
		LiteralBean bean = new LiteralBean();
		bean.setLiteral(getAsURI());
		return bean;
	}

	public void setFromBean(LiteralBean bean) throws IllegalArgumentException {
		setFromURI(bean.getLiteral());
	}

}
