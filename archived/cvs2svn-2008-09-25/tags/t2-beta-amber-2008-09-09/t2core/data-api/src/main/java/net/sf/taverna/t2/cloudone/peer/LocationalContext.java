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
package net.sf.taverna.t2.cloudone.peer;

/**
 * Defines locational context information mandated by a single type of reference
 * scheme.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public interface LocationalContext {

	/**
	 * Get the context type.
	 *
	 * @return The context type
	 */
	public String getContextType();

	/**
	 * Get a value associated with a key.
	 * <p>
	 * For a key specified as "some.key.thing", use
	 * <code>getValue("some", "key", "thing");</code>
	 *
	 * @param keyPath
	 *            One or more key paths
	 * @return The associated value
	 */
	public String getValue(String... keyPath);

}
