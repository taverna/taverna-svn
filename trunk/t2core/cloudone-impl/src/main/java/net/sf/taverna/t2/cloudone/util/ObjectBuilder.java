package net.sf.taverna.t2.cloudone.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.EntityRetrievalException;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;

/**
 * Utility class with a method to take an EntityIdentifier and build a POJO from
 * it. This loses any naming information but understands literal values and
 * lists.
 * <p>
 * TODO - this class does not currently pull data out of data documents
 * 
 * @author Tom Oinn
 * 
 */
public class ObjectBuilder {

	/**
	 * Build an object structure consisting of lists and terminal objects from
	 * the specified ID using the given DataManager to resolve data documents
	 * and traverse lists. Literal values are handled directly.
	 * 
	 * @param dManager
	 * @param id
	 * @return
	 * @throws EntityNotFoundException
	 */
	public static Object buildObject(DataManager dManager, EntityIdentifier id)
			throws EntityNotFoundException, EntityRetrievalException{
		Object result = null;
		if (id instanceof Literal) {
			result = ((Literal) id).getValue();
		} else if (id instanceof DataDocumentIdentifier) {
			// TODO - implement dereference to object here
			throw new RuntimeException(
					"Dereference of full data document not implemented yet");
			// DataDocumentIdentifier ddi = (DataDocumentIdentifier) id;
			// DataDocument doc = (DataDocument) dManager.getEntity(ddi);
		} else if (id instanceof EntityListIdentifier) {
			List<Object> list = new ArrayList<Object>();
			Iterator<ContextualizedIdentifier> children = dManager.traverse(id,
					id.getDepth() - 1);
			while (children.hasNext()) {
				EntityIdentifier child = children.next().getDataRef();
				list.add(buildObject(dManager, child));
			}
			result = list;
		}
		return result;
	}

}
