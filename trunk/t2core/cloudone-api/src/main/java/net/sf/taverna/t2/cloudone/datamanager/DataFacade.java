package net.sf.taverna.t2.cloudone.datamanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EmptyListException;
import net.sf.taverna.t2.cloudone.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.EntityRetrievalException;
import net.sf.taverna.t2.cloudone.MalformedListException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public class DataFacade {

	public static final int UNKNOWN_DEPTH = -1;
	private DataManager dManager;

	public DataFacade(DataManager manager) {
		if (manager == null) {
			throw new NullPointerException("Data Manager cannot be null");
		}
		this.dManager = manager;
	}

	public Object resolve(EntityIdentifier entityId)
			throws EntityRetrievalException, EntityNotFoundException {
		Entity<?, ?> ent = dManager.getEntity(entityId);
		if (ent instanceof Literal) {
			return ((Literal) ent).getValue();
		} else if (ent instanceof EntityList) {
			EntityList entityList = (EntityList) ent;
			List<Object> resolved = new ArrayList<Object>();
			for (EntityIdentifier id : entityList) {
				resolved.add(resolve(id));
			}
			return resolved;
		} else {
			throw new IllegalArgumentException("Type " + entityId.getType()
					+ " not yet supported");
		}
	}

	private EntityIdentifier registerList(List<Object> list, int listDepth) throws EmptyListException, MalformedListException {
		if (listDepth == 0) {
			throw new MalformedListException("Can't register list of 0 depth");
		}
		if (list.isEmpty()) {
			if (listDepth == UNKNOWN_DEPTH) {
				throw new EmptyListException("Can not register empty list with unknown depth");
			} else {
				return dManager.registerEmptyList(listDepth);
			}
		}
		
		List<EntityIdentifier> registered = new ArrayList<EntityIdentifier>(list.size());
		int childDepth;
		if (listDepth == UNKNOWN_DEPTH) { 
			childDepth = UNKNOWN_DEPTH; 
		} else {
			childDepth = listDepth - 1;
		}
		for (Object obj : list) {
			EntityIdentifier id;
			try {
				id = register(obj, childDepth);
				registered.add(id);
			} catch (EmptyListException ex) {
				registered.add(null);
				continue;
			}
			if (childDepth == UNKNOWN_DEPTH) {
				childDepth = id.getDepth();
				// could break; here, but would introduce iterator logic in second
				// for-loop below
			} else if (childDepth != id.getDepth()) {
				throw new MalformedListException(id + " has depth "
						+ id.getDepth() + ", expected " + childDepth);
			}
		}
		
		if (childDepth == UNKNOWN_DEPTH) {
			throw new EmptyListException("All child lists are empty");
		}
		
		List<EntityIdentifier> ids = new ArrayList<EntityIdentifier>();
		Iterator<EntityIdentifier> registeredIterator = registered.iterator();
		
		for (Object obj : list) {
			EntityIdentifier id = registeredIterator.next();
			if (id == null) {
				// Previously unknown depth
				id = register(obj, childDepth);
			}
			ids.add(id);
		}
		return dManager.registerList(ids.toArray(new EntityIdentifier[ids
				.size()]));
	}

	@SuppressWarnings("unchecked")
	public EntityIdentifier register(Object obj, int depth) throws EmptyListException, MalformedListException {
		if (obj instanceof List) {
			return registerList((List) obj, depth);
		}
		// TODO: Support errors
		
		if (depth > 0 || depth < UNKNOWN_DEPTH) {
			throw new IllegalArgumentException("Attempt to register non-list at depth " + depth);
		}
		
		if (obj instanceof Float) {
			return Literal.buildLiteral((Float) obj);
		} else if (obj instanceof Double) {
			return Literal.buildLiteral((Double) obj);
		} else if (obj instanceof Boolean) {
			return Literal.buildLiteral((Boolean) obj);
		} else if (obj instanceof Integer) {
			return Literal.buildLiteral((Integer) obj);
		} else if (obj instanceof Long) {
			return Literal.buildLiteral((Long) obj);
		} else if (obj instanceof String) {
			return Literal.buildLiteral((String) obj);
		} else {
			throw new IllegalArgumentException("Can't register unsupported "
					+ obj.getClass());
		}
	}


	public EntityIdentifier register(Object obj) throws EmptyListException, MalformedListException {		
		return register(obj, UNKNOWN_DEPTH);
	}
}
