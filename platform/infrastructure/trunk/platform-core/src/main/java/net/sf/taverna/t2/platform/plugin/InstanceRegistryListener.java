/***********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
 ***********************************************************************/
package net.sf.taverna.t2.platform.plugin;

import java.util.Set;

/**
 * Listener for changes over an InstanceRegistry
 * 
 * @author Tom Oinn
 * 
 * @param <T>
 *            the assignable supertype for all objects in the instance registry
 */
public interface InstanceRegistryListener<T> {

	/**
	 * Called when membership of the instance registry is changed through
	 * addition or removal of SPI implementations.
	 * 
	 * @param entriesAdded
	 *            a set of new instances added
	 * @param entriesRemoved
	 *            a set of previous instances which are no longer present
	 * @param currentMembership
	 *            the current membership of the instance registry for
	 *            convenience, this is the membership after any changes have
	 *            occured
	 */
	void spiMembershipChanged(Set<T> entriesAdded, Set<T> entriesRemoved,
			Set<T> currentMembership);

}
