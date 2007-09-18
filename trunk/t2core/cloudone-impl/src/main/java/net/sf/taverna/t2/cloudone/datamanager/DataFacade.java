package net.sf.taverna.t2.cloudone.datamanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;

/**
 * <p>
 * Facade for registering and resolving data objects with a {@link DataManager}.
 * </p>
 * <p>
 * Java objects such as {@link String}, {@link Integer} etc, and {@link List}s
 * of such objects can be registered using {@link #register(Object)} or
 * {@link #register(Object, int)}. This will serialise and store the data in
 * the underlying {@link DataManager}.
 * </p>
 * <p>
 * The returned {@link EntityIdentifier} can be used with
 * {@link #resolve(EntityIdentifier)} to retrieve the data. For instance, if a
 * <code>List&lt;String&gt;</code> was registered with
 * {@link #register(Object)}, {@link #resolve(EntityIdentifier)} would return a
 * reconstructed <code>List&lt;String&gt;</code>.
 * </p>
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class DataFacade {

	public static final int UNKNOWN_DEPTH = -1;
	private DataManager dManager;

	public DataFacade(DataManager manager) {
		if (manager == null) {
			throw new NullPointerException("Data Manager cannot be null");
		}
		this.dManager = manager;
	}

	/**
	 * <p>
	 * Use to register Literals or a list with unknown depth.
	 * </p>
	 * <p>
	 * Attempts will be made to guess the depth, which could fail with
	 * {@link EmptyListException} if the object is a list and it's either empty
	 * or all of it's children are empty lists.
	 * </p>
	 * 
	 * @param obj
	 *            Supported object as defined by {@link #register(Object, int)}
	 * @param depth
	 *            Number of levels in the parent List
	 * @return EntityIdentifier of the Object passed in
	 * @throws EmptyListException
	 *             If attempting to register an empty list (or a list containing
	 *             only empty lists). Use {@link #register(Object, int)}
	 *             instead.
	 * @throws MalformedListException
	 *             If attempting to register a malformed list, as detailed in
	 *             {@link MalformedListException}
	 * @throws UnsupportedObjectTypeException
	 *             If the object, or an object within the list is not supported
	 * @throws IOException
	 * @see #register(Object, int) for list of supported object types
	 * 
	 */
	public EntityIdentifier register(Object obj) throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException, IOException {
		return register(obj, UNKNOWN_DEPTH);
	}

	/**
	 * <p>
	 * Determine the Object type, registers it with the DataManager and return
	 * the referencing EntityIdentifier. Long strings, byte[] and
	 * {@link InputStream} are stored in a {@link BlobStore} if available.
	 * </p>
	 * <p>
	 * Currently supported object types:
	 * </p>
	 * <ul>
	 * <li>{@link String}</li>
	 * <li>{@link Float}</li>
	 * <li>{@link Double}</li>
	 * <li>{@link Integer}</li>
	 * <li>{@link Long}</li>
	 * <li>{@link Boolean}</li>
	 * <li>{@link EntityIdentifier} (previously registered object)</li>
	 * <li>byte[]</li>
	 * <li>{@link InputStream}</li>
	 * <li>{@link List} containing any of the supported objects, including
	 * {@link List}</li>
	 * </ul>
	 * <p>
	 * Attempts to registering an unsupported object type will throw an
	 * 
	 * @param obj
	 *            A supported object
	 * @param depth
	 *            Number of levels in the parent List (for instance, a List of a
	 *            List of strings is depth 2), or {@link #UNKNOWN_DEPTH} if
	 *            unknown. Values less than -1 are invalid.
	 * @return EntityIdentifier of the Object passed in
	 * @throws EmptyListException
	 *             If attempting to register an empty list (or a list containing
	 *             only empty lists), with depth defined as -1 (UNKNOWN_DEPTH).
	 *             (As when invoked from {@link #register(Object)} )
	 * @throws MalformedListException
	 *             If attempting to register a malformed list, as detailed in
	 *             {@link MalformedListException}
	 * @throws UnsupportedObjectTypeException
	 *             If the object, or an object within the list is not supported
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public EntityIdentifier register(Object obj, int depth)
			throws EmptyListException, MalformedListException,
			UnsupportedObjectTypeException, IOException {
		if (obj instanceof EntityIdentifier) {
			return (EntityIdentifier) obj;
		}
		if (obj instanceof List) {
			return registerList((List) obj, depth);
		}
		// TODO: Support errors

		if (depth > 0 || depth < UNKNOWN_DEPTH) {
			throw new IllegalArgumentException(
					"Attempt to register non-list at depth " + depth);
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
			String str = (String) obj;
			if (str.length() > dManager.getMaxIDLength()) {
				//TODO: turn it into blob and store
				BlobStore blobStore = dManager.getBlobStore();
				BlobReferenceSchemeImpl blobRef = (BlobReferenceSchemeImpl) blobStore.storeFromBytes(str.getBytes("utf8"));
				Set<ReferenceScheme> blobSet = Collections.singleton((ReferenceScheme)blobRef);
				return dManager.registerDocument(blobSet);
			} else {
				return Literal.buildLiteral((String) obj);
			}
		} else if (obj instanceof byte[]) {
			BlobStore blobStore = dManager.getBlobStore();
			BlobReferenceSchemeImpl blobRef = (BlobReferenceSchemeImpl) blobStore.storeFromBytes((byte[])obj);
			Set<ReferenceScheme> blobSet = Collections.singleton((ReferenceScheme)blobRef);
			return dManager.registerDocument(blobSet);
		} else if (obj instanceof InputStream) {
			BlobStore blobStore = dManager.getBlobStore();
			BlobReferenceSchemeImpl blobRef = (BlobReferenceSchemeImpl) blobStore.storeFromStream((InputStream)obj); 
			Set<ReferenceScheme> blobSet = Collections.singleton((ReferenceScheme)blobRef);
			return dManager.registerDocument(blobSet);
		}
		else {
			throw new UnsupportedObjectTypeException("Can't register unsupported "
					+ obj.getClass());
		}
	}

	/**
	 * Resolve identifier and return referenced entity, literal or a list of
	 * entities.
	 * 
	 * @param entityId
	 *            Identifier of the entity
	 * @return Entity, literal or list of entities/literals
	 * @throws RetrievalException
	 *             If something goes wrong when retrieving the entity
	 * @throws NotFoundException
	 *             If the underlying {@link DataManager} can't find the entity
	 * @throws IllegalArgumentException
	 *             If the entity type is not yet supported by {@link DataFacade}
	 */
	public Object resolve(EntityIdentifier entityId)
			throws RetrievalException, NotFoundException {
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
		} else if (ent instanceof DataDocument) {
			DataDocument doc = (DataDocument) ent;
			for (ReferenceScheme ref : doc.getReferenceSchemes()) {
				// TODO: Check if valid in context
			//	if (ref.validInContext(contextSet, currentLocation)) {
				try {
					return ref.dereference(dManager);
				} catch (DereferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue; // try next one
				}
			}
			throw new RetrievalException(
					"No dereferencable reference schemes found for " + entityId);
			
		} else {
			// TODO: Support the other types
			throw new IllegalArgumentException("Type " + entityId.getType()
					+ " not yet supported");
		}
	}

	/**
	 * Used internally by the DataFacade to register List Objects. These can be
	 * Lists of Lists of Lists etc. Checks that they conform to the protocol of
	 * each child having the same depth.
	 * 
	 * @param list
	 *            Objects of the list
	 * @param listDepth
	 *            Depth of the list, it's children must have the depth
	 *            <code>listDepth</code>-1
	 * @return {@link EntityIdentifier} for the registered list
	 * @throws EmptyListException
	 *             If attempting to register an empty list or list of empty
	 *             lists
	 * @throws MalformedListException
	 *             If the list or any of it's sublists are malformed
	 * @throws UnsupportedObjectTypeException
	 *             If the object, or an object within the list is not supported
	 * @throws IOException 
	 */
	private EntityIdentifier registerList(List<Object> list, int listDepth)
			throws EmptyListException, MalformedListException, UnsupportedObjectTypeException, IOException {
		if (listDepth == 0) {
			throw new MalformedListException("Can't register list of 0 depth");
		}
		if (list.isEmpty()) {
			if (listDepth == UNKNOWN_DEPTH) {
				throw new EmptyListException(
						"Can not register empty list with unknown depth");
			} else {
				return dManager.registerEmptyList(listDepth);
			}
		}

		List<EntityIdentifier> registered = new ArrayList<EntityIdentifier>(
				list.size());
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
				// could break; here, but would introduce iterator logic in
				// second
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
}
