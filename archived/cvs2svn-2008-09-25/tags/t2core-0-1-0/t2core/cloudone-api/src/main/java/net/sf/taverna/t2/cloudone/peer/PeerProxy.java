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
