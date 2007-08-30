package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.cloudone.bean.Beanable;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Superinterface for all entities within the data system. In the present
 * specification an entity is either a data document, an error document or a
 * list of entities. The enumeration of possible types is held in IDType.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 * @param <ID>
 */
public interface Entity<ID extends EntityIdentifier, Bean> extends
		Beanable<Bean> {
	public ID getIdentifier();
}
