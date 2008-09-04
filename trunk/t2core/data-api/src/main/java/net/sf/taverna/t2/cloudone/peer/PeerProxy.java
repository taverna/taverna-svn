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

import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * A proxy interface defining the set of operations a peer container makes
 * available to a data peer on remote peers. Instances of this interface are
 * created specifically by implementations of the peer container interface, they
 * handle the communication layer between the local and remote container.
 *
 * TODO - this interface should probably actually be written...
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public interface PeerProxy {

	/**
	 * Return an exported copy of the specified entity. The behaviour is to
	 * return errors and lists unaltered from the remote peer and to use the
	 * export method to transform data documents appropriately such that all
	 * reference schemes are valid.
	 *
	 * @param identifier Entity to be exported
	 * @return An exported copy of the specified entity
	 */
	public Entity<?, ?> export(EntityIdentifier identifier)
			throws NotFoundException;

}
