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
package net.sf.taverna.t2.cloudone.identifier;

/**
 * Identifier for a data document. The identifier is of the form :
 * <p>
 * urn:t2data:ddoc://&lt;namespace&gt;/&lt;name&gt;
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class DataDocumentIdentifier extends EntityIdentifier {
	private String name;

	/**
	 * Construct a DataDocumentIdentifier from a given URI.
	 * 
	 * @param id
	 *            The identifying URI
	 * @throws MalformedIdentifierException
	 *             If the identifier was not a valid DataDocumentIdentifier
	 */
	public DataDocumentIdentifier(String id)
			throws MalformedIdentifierException {
		super(id);
	}

	@Override
	public int getDepth() {
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected void validate(String identifierString)
			throws MalformedIdentifierException {
		if (identifierString.contains("/")) {
			throw new MalformedIdentifierException(
					"Document name can not contain a slash (/) character in "
							+ identifierString);
		}
		name = identifierString;
	}

}
