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

import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
import net.sf.taverna.t2.cloudone.datamanager.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;

/**
 * A reference scheme for blobs stored in a {@link BlobStore}.
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 * @param <BlobBean>
 *            Bean for serialising BlobReferenceScheme
 */
public interface BlobReferenceScheme<BlobBean extends ReferenceBean> extends
		ReferenceScheme<BlobBean> {

	/**
	 * Compare to another object. BlobReferenceScheme must be comparable to each
	 * other to support serialisation and being stored in {@link Set}s and
	 * {@link Map}s.
	 *
	 * @param obj
	 *            Object to compare
	 * @return true if and only if the object is an equivalent
	 *         BlobReferenceScheme with the same reference
	 */
	public abstract boolean equals(Object obj);

	/**
	 * Calculate hashcode. Different deserialisations of the same
	 * BlobReferenceScheme must return the same hashcode.
	 *
	 * @return The calculated hashcode
	 */
	public abstract int hashCode();

	/**
	 * Get the local identifier for the blob within the given namespace.
	 *
	 * @return Local identifier
	 */
	public String getId();

	/**
	 * Get the namespace for the blob. The namespace is normally as assigned by
	 * the creating {@link DataManager}.
	 *
	 * @return Namespace
	 */
	public abstract String getNamespace();

	/**
	 * Show a string representation of BlobReferenceScheme, for instance an URI.
	 *
	 * @return String representation of BlobReferenceScheme
	 */
	public abstract String toString();
}
