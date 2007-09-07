package net.sf.taverna.t2.cloudone.datamanager.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;

/**
 * Naive but functional implementation of DataManager which stores all entities
 * in a single Map object. Ironically given that this took an hour to write it's
 * still about ten thousand times more efficient than the data system in Taverna
 * 1.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class InMemoryDataManager extends AbstractDataManager {

	private Map<EntityIdentifier, Entity<? extends EntityIdentifier, ?>> contents;
	
	private InMemoryBlobStore blobStore;
	
	private int counter = 0;
	
	private static final int MAX_ID_LENGTH = 80;  //for debug reasons

	public InMemoryDataManager(String namespace, Set<LocationalContext> contexts) {
		super(namespace, contexts);
		blobStore = new InMemoryBlobStore();
		this.contents = new HashMap<EntityIdentifier, Entity<? extends EntityIdentifier, ?>>();
	}

	@Override
	protected String generateId(IDType type) {
		if (type.equals(IDType.Literal)) {
			throw new IllegalArgumentException("Can't generate IDs for Literal");
		}
		return "urn:t2data:" + type.uripart + "://" + getCurrentNamespace() + "/"
				+ type.toString().toLowerCase() + counter++;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <ID extends EntityIdentifier> Entity<ID, ?> retrieveEntity(ID id) {
		return (Entity<ID, ?>) contents.get(id);
	}
	
	@Override
	protected <Bean> void storeEntity(Entity<?, Bean> entity) {
		contents.put(entity.getIdentifier(), entity);
	}

	public int getMaxIDLength() {
		return MAX_ID_LENGTH;
	}

	public InMemoryBlobStore getBlobStore() {
		return blobStore;
	}


}
