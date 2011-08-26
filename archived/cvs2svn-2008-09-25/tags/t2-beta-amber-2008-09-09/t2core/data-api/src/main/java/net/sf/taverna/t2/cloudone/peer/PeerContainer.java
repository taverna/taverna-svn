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

/**
 * Contains a single data peer and provides that peer with network awareness and
 * communication facilities. The container is responsible for discovery of other
 * containers (and their peers implicitly) and the creation of peer proxy
 * objects to be used for peer to peer communication in the form of data export
 * and namespace migration.
 *
 * TODO - interface needs to be written
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public interface PeerContainer {

	/**
	 * Get proxy for accessing given namespace.
	 * <p>
	 * Discovery request, called by implementation of {@link DataPeer} on its
	 * enclosing container when trying to resolve an entity identifier in a
	 * namespace not managed by the data peer's data manager.
	 *
	 * @param namespace Namespace to proxy
	 * @return A {@link PeerProxy} that can access namespace
	 * @throws NotFoundException If no proxy can be found for given namespace
	 */
	public PeerProxy getProxyForNamespace(String namespace) throws NotFoundException;

	/**
	 * Is the peer container connected to the network fabric? 
	 */
	public boolean isConnected();
	

}
