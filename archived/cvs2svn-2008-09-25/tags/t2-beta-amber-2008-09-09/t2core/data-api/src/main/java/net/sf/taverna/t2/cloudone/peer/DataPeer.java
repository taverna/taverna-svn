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

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

/**
 * Interface defining methods used by the DataPeerContainer. This interface
 * should only ever be accessed from an instance of PeerContainer to handle a
 * message received by that container.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public interface DataPeer {

	/**
	 * Export data document. Return a potentially modified copy of the specified
	 * data document after ensuring that all reference schemes within the
	 * specified document are valid in the remote context.
	 *
	 * @param remoteContexts
	 *            Contexts within which reference schemes should be valid
	 * @param identifier
	 *            Data document identifier
	 * @return A potentially modified copy of the specified data document
	 */
	public DataDocument exportDataDocument(
			Set<LocationalContext> remoteContexts,
			DataDocumentIdentifier identifier) throws NotFoundException;

	/**
	 * Get the current namespace for this DataManager. New data documents and
	 * lists registered with this manager will have this namespace in their
	 * identifiers.
	 *
	 * @return Current namespace
	 */
	public String getCurrentNamespace();

	/**
	 * Get the associated {@link DataManager}.
	 *
	 * @return The associated {@link DataManager}
	 */
	public DataManager getDataManager();

	/**
	 * Get the set of locational contexts for this DataManager instance.
	 *
	 * @return A {@link Set} of {@link LocationalContext}s
	 */
	public Set<LocationalContext> getLocationalContexts();

	/**
	 * Get all namespaces for which the associated data manager is the
	 * authority. This will always include at least the namespace returned by
	 * the {@link #getCurrentNamespace()}.
	 *
	 * @return A {@link List} of namespaces
	 */
	public List<String> getManagedNamespaces();

}
