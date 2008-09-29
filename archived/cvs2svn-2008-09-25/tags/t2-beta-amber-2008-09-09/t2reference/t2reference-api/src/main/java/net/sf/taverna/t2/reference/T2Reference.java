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
package net.sf.taverna.t2.reference;

import java.net.URI;

/**
 * The T2Reference is used within the workflow system to refer to any entity
 * within the reference management system, whether reference set, list or error
 * document. The reference carries certain properties which can be evaluated
 * without resolution, these include a depth property and whether the reference
 * either is or contains an error document at any point in its structure.
 * 
 * @author Tom Oinn
 * 
 */
public interface T2Reference {

	/**
	 * To determine the entity that this reference points to we use an
	 * enumeration of possible entity types
	 * 
	 * @return the type of entity to which this reference refers.
	 */
	public T2ReferenceType getReferenceType();

	/**
	 * All entities identified by a T2Reference have a conceptual depth. In the
	 * case of lists the depth is the (uniform) depth of any item in the list
	 * plus one, in the case of reference sets the depth is 0. Error documents
	 * and empty lists may also have non zero depth; error documents in
	 * particular have a depth corresponding to the depth of the list or
	 * reference set that would have been created if there was no error.
	 * 
	 * @return the depth of the entity identified by this T2Reference
	 */
	public int getDepth();

	/**
	 * Error documents always return true, as do any lists where at least one
	 * immediate child of the list returns true when this property is evaluated.
	 * As lists are immutable this property is actually set on list
	 * registration. This is used to determine whether to allow POJO resolution
	 * of the entity identified by this T2Reference - this is configurable by
	 * the caller but there will be some cases where an attempt to render a
	 * collection containing errors to a POJO should return an error and other
	 * occasions when it should return a collection containing error objects.
	 * <p>
	 * ReferenceSet implementations always return false.
	 * 
	 * @return whether the entity identified by this T2Reference either is or
	 *         contains an error document. Containment is transitive, so a list
	 *         containing a list that contained an error would return true.
	 */
	public boolean containsErrors();

	/**
	 * T2Reference instances retain a reference to the reference manager which
	 * created them in the form of a namespace. This is an opaque string
	 * matching the regular expression [a-zA-Z_0-9]+, and is immutable once
	 * assigned (as are the other properties of this interface). The reference
	 * manager infrastructure uses this namespace property primarily to
	 * differentiate between cases where a reference cannot be resolved because
	 * of a lack of connection to the appropriate remote reference manager and
	 * those where the reference simply does not exist anywhere.
	 * 
	 * @return the namespace of this T2Reference as a string.
	 */
	public String getNamespacePart();

	/**
	 * In addition to the namespace the T2Reference contains a local identifier.
	 * This identifier is unique in the context of the namespace and is also
	 * represented as a string matching the regular expression [a-z_A-Z0-9]+
	 * 
	 * @return the local part of this T2Reference as a string.
	 */
	public String getLocalPart();

	/**
	 * All T2Reference instances can be represented as a URI.
	 * 
	 * @return representation of this T2Reference as a URI
	 */
	public URI toUri();

}
