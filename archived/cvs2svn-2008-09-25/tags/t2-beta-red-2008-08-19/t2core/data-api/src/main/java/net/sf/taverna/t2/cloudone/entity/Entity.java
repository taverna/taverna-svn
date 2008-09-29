package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.cloudone.bean.EntityListBean;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * Super-interface for all entities within the data system. In the present
 * specification an entity is either a data document, an error document or a
 * list of entities. The enumeration of possible types is held in IDType.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 * @param <ID>
 *            {@link EntityIdentifier} subclass, for instance
 *            {@link EntityListIdentifier}
 * @param <Bean>
 *            Serialisable bean for entity, for instance {@link EntityListBean}
 */
public interface Entity<ID extends EntityIdentifier, Bean> extends
		Beanable<Bean> {

	/**
	 * Get the identifier for the entity.
	 * 
	 * @return The identifier
	 */
	public ID getIdentifier();
}
