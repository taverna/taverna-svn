package net.sf.taverna.t2.cloudone.datamanager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;

/**
 * Abstract {@link DataManager}. Handles all register* methods, namespaces and
 * locational contexts, subclasses only need to implement
 * {@link #storeEntity(Entity)}, {@link #retrieveEntity(EntityIdentifier)} and
 * {@link #generateId(IDType)}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public abstract class AbstractDataManager implements DataManager {

	private static int[] addIndex(int[] current, int head) {
		int[] result = new int[current.length + 1];
		System.arraycopy(current, 0, result, 0, current.length);
		result[current.length] = head;
		return result;
	}

	private String namespace;
	private Set<LocationalContext> initialContexts;
	private Set<LocationalContext> locationalContexts = null;

	/**
	 * Construct an AbstractDataManager with a given namespace and a set of
	 * contexts.
	 * 
	 * @param namespace
	 *            Unique namespace that will be the
	 *            {@link #getCurrentNamespace()}
	 * @param contexts
	 *            A {@link Set} of {@link LocationalContext}s this datamanager
	 *            can handle
	 */
	public AbstractDataManager(String namespace, Set<LocationalContext> contexts) {
		if (!EntityIdentifier.isValidName(namespace)) {
			throw new MalformedIdentifierException("Invalid namespace: "
					+ namespace);
		}
		this.namespace = namespace;
		this.initialContexts = contexts;
	}

	/**
	 * The current namespace. Identifiers created with
	 * {@link #nextDataIdentifier()} etc. will use this namespace.
	 * 
	 */
	public String getCurrentNamespace() {
		return namespace;
	}

	/**
	 * Retrieve entity from Data manager. If the entity is a {@link Literal} the
	 * instance will be returned directly. Otherwise, if the entity is within
	 * the {@link DataManager}s {@link #getManagedNamespaces()}, it will be
	 * retrieved using {@link #retrieveEntity(EntityIdentifier)}.
	 */
	@SuppressWarnings("unchecked")
	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI id)
			throws NotFoundException, RetrievalException {
		if (id instanceof Literal) {
			return (Entity<EI, ?>) id;
		}
		Entity ent = retrieveEntity(id);
		if (ent == null) {
			throw new NotFoundException(id);
		}
		// we know this is type-safe because we control what goes into the store
		return ent;
	}

	public synchronized Set<LocationalContext> getLocationalContexts() {
		if (locationalContexts == null) {
			locationalContexts = new HashSet<LocationalContext>(initialContexts);
			BlobStore blobStore = getBlobStore();
			if (blobStore != null) {
				locationalContexts.addAll(blobStore.getLocationalContexts());
			}
		}
		return locationalContexts;
	}

	public List<String> getManagedNamespaces() {
		return Collections.singletonList(namespace);
	}

	/**
	 * Generate a new {@link DataDocumentIdentifier} in the current namespace.
	 * 
	 * @return The new {@link DataDocumentIdentifier}.
	 */
	public DataDocumentIdentifier nextDataIdentifier() {
		String id = generateId(IDType.Data);
		return new DataDocumentIdentifier(id);
	}

	/**
	 * Generate a new {@link ErrorDocumentIdentifier} in the current namespace.
	 * 
	 * @param depth
	 *            The depth of the error document
	 * @param implicitDepth
	 *            The implicit depth of the error document
	 * @return The new {@link ErrorDocumentIdentifier}.
	 */
	public ErrorDocumentIdentifier nextErrorIdentifier(int depth,
			int implicitDepth) throws IllegalArgumentException {
		if (depth < 0 || implicitDepth < 0) {
			throw new IllegalArgumentException(
					"Depth and implicit depth must be at least 0");
		}
		String id = generateId(IDType.Error) + "/" + depth + "/"
				+ implicitDepth;
		return new ErrorDocumentIdentifier(id);
	}

	/**
	 * Generate a new {@link EntityListIdentifier} in the current namespace.
	 * 
	 * @param depth
	 *            The depth of the list
	 * @return The new {@link EntityListIdentifier}.
	 */
	public EntityListIdentifier nextListIdentifier(int depth)
			throws IllegalArgumentException {
		if (depth < 1) {
			throw new IllegalArgumentException("Depth must be at least 1");
		}
		String id = generateId(IDType.List) + "/" + depth;
		return new EntityListIdentifier(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public DataDocumentIdentifier registerDocument(
			ReferenceScheme... references) throws StorageException {
		HashSet<ReferenceScheme> referenceSet = new HashSet<ReferenceScheme>(
				Arrays.asList(references));
		return registerDocument(referenceSet);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public DataDocumentIdentifier registerDocument(
			final Set<ReferenceScheme> references) throws StorageException {
		final DataDocumentIdentifier id = nextDataIdentifier();
		DataDocument d = new DataDocumentImpl(id, references);
		storeEntity(d);
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListIdentifier registerEmptyList(int depth)
			throws StorageException {
		EntityListIdentifier id = nextListIdentifier(depth);
		EntityList newList = new EntityList(id, Collections
				.<EntityIdentifier> emptyList());
		storeEntity(newList);
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg) throws StorageException {
		return registerError(depth, implicitDepth, msg, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg, Throwable throwable) throws StorageException {
		final ErrorDocumentIdentifier id = nextErrorIdentifier(depth,
				implicitDepth);
		ErrorDocument ed = new ErrorDocument(id, msg, throwable);
		storeEntity(ed);
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			Throwable throwable) throws StorageException {
		return registerError(depth, implicitDepth, null, throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListIdentifier registerList(EntityIdentifier[] identifiers)
			throws StorageException {
		if (identifiers.length == 0) {
			throw new IndexOutOfBoundsException(
					"Cannot register an empty list through registerList method");
		}
		EntityListIdentifier id = nextListIdentifier(identifiers[0].getDepth() + 1);
		EntityList newList = new EntityList(id, Arrays.asList(identifiers));
		storeEntity(newList);
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<ContextualizedIdentifier> traverse(
			EntityIdentifier identifier, int desiredDepth)
			throws RetrievalException {
		if (desiredDepth < 0) {
			throw new IllegalArgumentException(
					"Cannot traverse to a negative depth");
		}
		Set<ContextualizedIdentifier> workingSet = new HashSet<ContextualizedIdentifier>();
		workingSet.add(new ContextualizedIdentifier(identifier, new int[0]));
		int currentDepth = identifier.getDepth();
		while (currentDepth > desiredDepth) {
			Set<ContextualizedIdentifier> newSet = new HashSet<ContextualizedIdentifier>();
			for (ContextualizedIdentifier ci : workingSet) {
				switch (ci.getDataRef().getType()) {
				case List:
					EntityListIdentifier listIdentifier = (EntityListIdentifier) ci
							.getDataRef();
					try {
						EntityList list = (EntityList) getEntity(listIdentifier);
						int position = 0;
						for (EntityIdentifier ei : list) {
							newSet.add(new ContextualizedIdentifier(ei,
									addIndex(ci.getIndex(), position++)));
						}
					} catch (NotFoundException enfe) {
						throw new AssertionError(
								"Entity referenced within list but not found within this data manager");
					}
					break;
				case Data:
					throw new AssertionError(
							"Should never be trying to drill inside a data document identifier");
				case Error:
					newSet.add(new ContextualizedIdentifier(
									((ErrorDocumentIdentifier) ci.getDataRef())
											.drill(),
									addIndex(ci.getIndex(), 0)));
					break;
				default:
					throw new AssertionError(
							"Fallen off end of case statement, something bad happened.");
				}
			}
			currentDepth--;
			workingSet = newSet;
		}
		return workingSet.iterator();
	}

	protected abstract String generateId(IDType error);

	/**
	 * Retrieve the entity.
	 * 
	 * @param <ID>
	 *            The type of {@link EntityIdentifier}
	 * @param id
	 *            The identifier for the entity
	 * @return The retrieved {@link Entity}
	 * @throws RetrievalException
	 *             If the entity could not be retrieved
	 */
	protected abstract <ID extends EntityIdentifier> Entity<ID, ?> retrieveEntity(
			ID id) throws RetrievalException;

	/**
	 * Store the entity.
	 * 
	 * @param <Bean>
	 *            Bean that can be serialised
	 * @param entity
	 *            Entity to store
	 * @throws StorageException
	 *             If the entity could not be stored
	 */
	protected abstract <Bean> void storeEntity(Entity<?, Bean> entity)
			throws StorageException;

}
