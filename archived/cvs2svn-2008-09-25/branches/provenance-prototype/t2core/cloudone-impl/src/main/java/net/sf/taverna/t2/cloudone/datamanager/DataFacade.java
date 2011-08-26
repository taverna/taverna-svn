package net.sf.taverna.t2.cloudone.datamanager;

import static net.sf.taverna.t2.cloudone.datamanager.BlobStore.STRING_CHARSET;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

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

	private static Logger logger = Logger.getLogger(DataFacade.class);
	/*
	 * The DataManager responsible for storing the entities registered through
	 * the facade
	 */
	private DataManager dManager;

	/**
	 * Construct a DataFacade facading the specified {@link DataManager}.
	 * 
	 * @param manager
	 *            The {@link DataManager} to facade.
	 */
	public DataFacade(DataManager manager) {
		if (manager == null) {
			throw new NullPointerException("Data Manager cannot be null");
		}
		dManager = manager;
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
	 * @see #register(Object, int) for list of supported object types
	 * 
	 */
	public EntityIdentifier register(Object obj) throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException {
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
	 * @param unknownDepth
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
	 */
	public EntityIdentifier register(Object obj, int unknownDepth)
			throws EmptyListException, MalformedListException,
			UnsupportedObjectTypeException {
		return register(obj, unknownDepth, null);
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
	 * (stored as {@link Literal}s or blobs depending on length)
	 * <li>{@link Float}</li>
	 * <li>{@link Double}</li>
	 * <li>{@link Integer}</li>
	 * <li>{@link Long}</li>
	 * <li>{@link Boolean}</li>
	 * <li>{@link EntityIdentifier} (previously registered object)</li>
	 * <li>byte[]</li>
	 * (stored as blobs)
	 * <li>{@link InputStream}</li>
	 * (stored as blobs)
	 * <li>{@link List} containing any of the supported objects, including
	 * {@link List}</li>
	 * </ul>
	 * In addition any of these array types are treated as if they were
	 * {@link List}s:
	 * <ul>
	 * <li>Object[] (including String[] and arrays of other supported objects,
	 * such as long[][])</li>
	 * <li>int[]</li>
	 * <li>long[]</li>
	 * <li>float[]</li>
	 * <li>double[]</li>
	 * <li>boolean[]</li>
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
	 * @param charSet
	 *            Character set for byte[] or {@link InputStream}s registered.
	 *            If <code>null</code>, such objects will be considered
	 *            binaries and won't have an attached character set. (hence
	 *            can't be resolved as {@link String}s)
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
	 */
	@SuppressWarnings("unchecked")
	public EntityIdentifier register(Object obj, int depth, String charSet)
			throws EmptyListException, MalformedListException,
			UnsupportedObjectTypeException {
		if (obj instanceof EntityIdentifier) {
			return (EntityIdentifier) obj;
		}
		if (obj instanceof List) {
			return registerList((List) obj, depth, charSet);
		} else if (obj instanceof Object[]) {
			return register(Arrays.asList((Object[]) obj), depth, charSet);
		} else if (obj instanceof int[]) {
			return register(ArrayUtils.toObject((int[]) obj), depth, charSet);
		} else if (obj instanceof float[]) {
			return register(ArrayUtils.toObject((float[]) obj), depth, charSet);
		} else if (obj instanceof double[]) {
			return register(ArrayUtils.toObject((double[]) obj), depth, charSet);
		} else if (obj instanceof boolean[]) {
			return register(ArrayUtils.toObject((boolean[]) obj), depth,
					charSet);
		} else if (obj instanceof long[]) {
			return register(ArrayUtils.toObject((long[]) obj), depth, charSet);
		}
		// TODO: Support errors

		// Anything below is a non-list and must have depth 0
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
				// TODO: turn it into blob and store
				BlobStore blobStore = dManager.getBlobStore();
				BlobReferenceSchemeImpl blobRef = (BlobReferenceSchemeImpl) blobStore
						.storeFromString(str);
				Set<ReferenceScheme> blobSet = Collections
						.singleton((ReferenceScheme) blobRef);
				return dManager.registerDocument(blobSet);
			} else {
				return Literal.buildLiteral((String) obj);
			}
		} else if (obj instanceof byte[]) {
			BlobStore blobStore = dManager.getBlobStore();
			BlobReferenceSchemeImpl blobRef = (BlobReferenceSchemeImpl) blobStore
					.storeFromBytes((byte[]) obj, charSet);
			Set<ReferenceScheme> blobSet = Collections
					.singleton((ReferenceScheme) blobRef);
			return dManager.registerDocument(blobSet);
		} else if (obj instanceof InputStream) {
			BlobStore blobStore = dManager.getBlobStore();
			BlobReferenceSchemeImpl blobRef = (BlobReferenceSchemeImpl) blobStore
					.storeFromStream((InputStream) obj, charSet);
			Set<ReferenceScheme> blobSet = Collections
					.singleton((ReferenceScheme) blobRef);
			return dManager.registerDocument(blobSet);
		} else {
			throw new UnsupportedObjectTypeException(
					"Can't register unsupported " + obj.getClass());
		}
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
	 * @param charSet
	 *            Character set for byte[] or {@link InputStream}s registered.
	 *            If <code>null</code>, such objects will be considered
	 *            binaries and won't have an attached character set. (hence
	 *            can't be resolved as {@link String}s)
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
	 */
	public EntityIdentifier register(Object obj, String charSet)
			throws EmptyListException, MalformedListException,
			UnsupportedObjectTypeException {
		return register(obj, UNKNOWN_DEPTH, charSet);
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
	 * @see #resolve(EntityIdentifier, Class)
	 */
	public Object resolve(EntityIdentifier entity) throws RetrievalException,
			NotFoundException {
		return resolve(entity, Object.class);
	}

	/**
	 * Resolve identifier and return referenced entity, literal or a list of
	 * entities.
	 * <p>
	 * Note: If a {@link Literal} {@link String} is retrieved with desired type
	 * <code>byte[]</code> or {@link InputStream}, the string will be encoded
	 * as bytes using the character set UTF-8 to be consistent with byte
	 * retrieval of longer {@link String}s registered as
	 * {@link BlobReferenceScheme}s (which {@link BlobStore} will encode using
	 * UTF-8 {@link BlobStore#STRING_CHARSET} ).
	 * </p>
	 * 
	 * @param entityId
	 *            Identifier of the entity
	 * @param desiredType
	 *            The desired type for resolving {@link DataDocument}s.
	 *            Currently supported types are {@link InputStream} (the default
	 *            used by {@link #resolve(EntityIdentifier)}), {@link String}
	 *            and <code>byte[]</code>, in addition to the types supported
	 *            by {@link Literal}, such as {@link Integer} and
	 *            {@link Double}.
	 * @return The resolved object of the desired type, or a list containing
	 *         such objects (or deeper lists)
	 * @throws RetrievalException
	 *             If something goes wrong when retrieving the entity, such as
	 *             when no references can be resolved or if the referenced value
	 *             was not of the desired type.
	 * @throws NotFoundException
	 *             If the underlying {@link DataManager} can't find the entity
	 * @throws IllegalArgumentException
	 *             If the entity type is not yet supported by {@link DataFacade},
	 *             (ie. an {@link ErrorDocument}).
	 */
	@SuppressWarnings("unchecked")
	public Object resolve(EntityIdentifier entityId, Class<?> desiredType)
			throws RetrievalException, NotFoundException {
		Entity<?, ?> ent = dManager.getEntity(entityId);
		if (ent instanceof Literal) {
			Object value = ((Literal) ent).getValue();
			if (desiredType.isInstance(value)) {
				return value;
			} else if (value instanceof String) {
				String string = ((String) value);
				if (desiredType.isAssignableFrom(byte[].class)) {
					try {
						return string.getBytes(STRING_CHARSET);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException("Unsupported encoding "
								+ STRING_CHARSET, e);
					}
				} else if (desiredType.isAssignableFrom(InputStream.class)) {
					try {
						return IOUtils.toInputStream(string, STRING_CHARSET);
					} catch (IOException e) {
						throw new RetrievalException(
								"Could not convert string to InputStream", e);
					}
				}
			}
			throw new RetrievalException("Entity " + entityId
					+ " was not of desired " + desiredType + ", but was of "
					+ value.getClass());

		} else if (ent instanceof EntityList) {
			EntityList entityList = (EntityList) ent;
			List<Object> resolved = new ArrayList<Object>();
			for (EntityIdentifier id : entityList) {
				resolved.add(resolve(id, desiredType));
			}
			return resolved;
		} else if (ent instanceof DataDocument) {
			DataDocument doc = (DataDocument) ent;
			for (ReferenceScheme ref : doc.getReferenceSchemes()) {
				// TODO: Check if valid in context
				// if (ref.validInContext(contextSet, currentLocation)) {
				InputStream stream;
				try {
					stream = ref.dereference(dManager);
				} catch (DereferenceException e) {
					logger.warn("Could not dereference " + ref, e);
					continue; // try next one
				}
				if (desiredType.isAssignableFrom(InputStream.class)) {
					return stream;
				} else if (desiredType.isAssignableFrom(String.class)) {
					try {
						if (ref.getCharset() == null) {
							logger.warn("Can't resolve " + ref
									+ " as String: No charset");
							continue; // try next one
						}
						return IOUtils.toString(stream, ref.getCharset());
					} catch (IOException e) {
						logger.warn("Could not read " + ref, e);
						continue; // try next one
					} catch (DereferenceException e) {
						logger.warn("Could not dereference " + ref, e);
						continue; // try next one
					}
				} else if (desiredType.isAssignableFrom(byte[].class)) {
					try {
						return IOUtils.toByteArray(stream);
					} catch (IOException e) {
						logger.warn("Could not read " + ref, e);
						continue; // try next one
					}
				} else {
					throw new IllegalArgumentException(
							"Unsupported desired type " + desiredType);
				}
			}
			throw new RetrievalException(
					"No dereferencable reference schemes (for desired type) found for "
							+ entityId);

		} else {
			// TODO: Support the other types
			throw new IllegalArgumentException("Type " + entityId.getType()
					+ " not yet supported");
		}
	}
	/**
	 * Given an {@link EntityIdentifier} as a string which represents a {@link EntityList} return all the other 
	 * {@link EntityIdentifier}s which it contains as an XML Element
	 * @param entityIdentifier
	 * @return
	 * @throws NotFoundException 
	 * @throws RetrievalException 
	 */
	public org.jdom.Element resolveToElement(String entityIdentifier) throws RetrievalException, NotFoundException {

		org.jdom.Element element = new org.jdom.Element("a");
		
		if (EntityIdentifiers.findType(entityIdentifier).equals(IDType.Literal)) {
			element.setName("literal");
			element.setAttribute("id", entityIdentifier);
		} else if (EntityIdentifiers.findType(entityIdentifier).equals(IDType.Error)) {
			ErrorDocumentIdentifier parseErrorIdentifier = EntityIdentifiers.parseErrorIdentifier(entityIdentifier);
			Entity<?, ?> entity = dManager.getEntity(parseErrorIdentifier);
			String stackTrace = ((ErrorDocument)entity).getStackTrace();
			String message = ((ErrorDocument)entity).getMessage();
			element.setName("error");
			element.setAttribute("id", entityIdentifier);
			org.jdom.Element stackElement = new org.jdom.Element("stackTrace");
			stackElement.addContent(stackTrace);
			element.addContent(stackElement);
			org.jdom.Element messageElement = new org.jdom.Element("message");
			messageElement.addContent(message);
			element.addContent(messageElement);
		} else if (EntityIdentifiers.findType(entityIdentifier).equals(IDType.Data)) {
			element.setName("dataDocument");
			element.setAttribute("id", entityIdentifier);
			DataDocumentIdentifier parseDocumentIdentifier = EntityIdentifiers.parseDocumentIdentifier(entityIdentifier);
			Entity<?,?> dataDoc = dManager.getEntity(parseDocumentIdentifier);
			Set<ReferenceScheme> referenceSchemes = ((DataDocument)dataDoc).getReferenceSchemes();
			for (ReferenceScheme ref:referenceSchemes) {
				org.jdom.Element refElement = new org.jdom.Element("reference");
				refElement.addContent(ref.toString());
				element.addContent(refElement);
			}
		} else if (EntityIdentifiers.findType(entityIdentifier).equals(IDType.List)) {
			element.setName("list");
			element.setAttribute("id", entityIdentifier);
			EntityListIdentifier entityListIdentifier = EntityIdentifiers.parseListIdentifier(entityIdentifier);
			EntityList ent = (EntityList) dManager.getEntity(entityListIdentifier);
			for (EntityIdentifier entityId:ent) {
				element.addContent(resolveToElement(entityId.getAsURI()));
			}
		} else {
			//throw something (maybe a tantrum)
		}
		return element;
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
	 * @param charSet
	 *            Character set for byte[] or {@link InputStream}s registered.
	 *            If <code>null</code>, such objects will be considered
	 *            binaries and won't have an attached character set. (hence
	 *            can't be resolved as {@link String}s)
	 * @return {@link EntityIdentifier} for the registered list
	 * @throws EmptyListException
	 *             If attempting to register an empty list or list of empty
	 *             lists
	 * @throws MalformedListException
	 *             If the list or any of it's sublists are malformed
	 * @throws UnsupportedObjectTypeException
	 *             If the object, or an object within the list is not supported
	 */
	private EntityIdentifier registerList(List<Object> list, int listDepth,
			String charSet) throws EmptyListException, MalformedListException,
			UnsupportedObjectTypeException {
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
				id = register(obj, childDepth, charSet);
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
				id = register(obj, childDepth, charSet);
			}
			ids.add(id);
		}
		return dManager.registerList(ids.toArray(new EntityIdentifier[ids
				.size()]));
	}
}
