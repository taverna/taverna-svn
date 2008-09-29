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
package net.sf.taverna.t2.cloudone.refscheme;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * A reference to data used in a DataDocument. In addition, the set of
 * properties used to determine whether a reference using this scheme is within
 * the visible scope of a given DataManager.
 * 
 * The reference scheme must be a bean.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 */
public interface ReferenceScheme<BeanType extends ReferenceBean> extends Beanable<BeanType> {

	/**
	 * Dereference this reference scheme. Return an input stream accessing the
	 * underlying value of the reference using the specified {@link DataManager}
	 * to resolve any security or other resolution requirements.
	 * 
	 * @return {@link InputStream} from where the referenced data can be read *
	 * @throws DereferenceException
	 *             If the reference could not be dereferenced
	 * 
	 */
	public InputStream dereference(DataManager manager)
			throws DereferenceException;

	/**
	 * Get expiry date of reference. If the reference is immediate this defines
	 * the date at which it will become invalid. If the reference is not
	 * immediate this has no meaning and may return null.
	 * 
	 * @see #isImmediate()
	 * @return {@link Date} at which an immediate reference becomes invalid, or
	 *         <code>null</code> if the reference is not immediate.
	 */
	public Date getExpiry();

	/**
	 * Check if reference is immediate. When a reference scheme is exported from
	 * a data peer there are some cases where it must be dereferenced
	 * immediately. This happens primarily in cases where the peer is shutting
	 * down and is exporting a reference to a data object physically located on
	 * that peer which must be dereferenced before the shutdown can complete. In
	 * such cases the receiving peer should dereference this reference scheme
	 * and remove it from the data document - this behaviour can be implemented
	 * in the peer container as part of the proxy implementation for
	 * convenience.
	 * 
	 * @return <code>true</code> if the reference should be resolved
	 *         immediately.
	 */
	public boolean isImmediate();

	/**
	 * Check if a reference scheme will be valid within the specified contexts.
	 * The current location is specified by a {@link DataPeer} reference,
	 * implementations can use this to get the current local context set.
	 * 
	 * @param contextSet
	 *            Contexts where the reference can be valid
	 * @return <code>true</code> if the reference scheme is valid
	 */
	public boolean validInContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation);

	/**
	 * Get the character set used in the InputStream returned by
	 * {@link #dereference(DataManager)}, or <code>null</code> if the stream
	 * is binary or charset is unknown. The character set can be used to decode
	 * the {@link InputStream} to a {@link String}.
	 * 
	 * @return String character set, for example "utf-8", or <code>null</code>
	 *         if binary or unknown
	 * @throws DereferenceException
	 *             If the reference could not be dereferenced
	 */
	public String getCharset() throws DereferenceException;

}
