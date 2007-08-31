package net.sf.taverna.t2.cloudone.datamanager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.EntityRetrievalException;
import net.sf.taverna.t2.cloudone.EntityStorageException;
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

public abstract class AbstractDataManager implements DataManager {

	private Set<LocationalContext> contexts;

	private String namespace;

	public AbstractDataManager(String namespace, Set<LocationalContext> contexts) {
		this.namespace = namespace;
		this.contexts = contexts;
	}

	public String getCurrentNamespace() {
		return namespace;
	}

	@SuppressWarnings("unchecked")
	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI id)
			throws EntityNotFoundException, EntityRetrievalException {
		if (id instanceof Literal) {
			return (Entity<EI, ?>) id;
		}
		if (!getManagedNamespaces().contains(id.getNamespace())) {
			// TODO: Retrieve from other DataManagers on p2p
			throw new EntityNotFoundException("Unknown namespace "
					+ id.getNamespace());
		}
		Entity ent = retrieveEntity(id);
		if (ent == null) {
			throw new EntityNotFoundException("No entity found with id : " + id);
		}
		// we know this is type-safe because we control what goes into the store
		return (Entity<EI, ?>) ent;
	}

	public Set<LocationalContext> getLocationalContexts() {
		return contexts;
	}

	public List<String> getManagedNamespaces() {
		return Collections.singletonList(namespace);
	}

	public DataDocumentIdentifier nextDataIdentifier() {
		String id = generateId(IDType.Data);
		return new DataDocumentIdentifier(id);
	}

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
	
	public EntityListIdentifier nextListIdentifier(int depth)
			throws IllegalArgumentException {
		if (depth < 1) {
			throw new IllegalArgumentException("Depth must be at least 1");
		}
		String id = generateId(IDType.List) + "/" + depth;
		return new EntityListIdentifier(id);
	}

	public DataDocumentIdentifier registerDocument(
			final Set<ReferenceScheme> references) throws EntityStorageException{
		final DataDocumentIdentifier id = nextDataIdentifier();
		DataDocument d = new DataDocumentImpl(id, references);
		storeEntity(d);
		return id;
	}

	public EntityListIdentifier registerEmptyList(int depth) throws EntityStorageException{
		EntityListIdentifier id = nextListIdentifier(depth);
		EntityList newList = new EntityList(id, Collections
				.<EntityIdentifier> emptyList());
		storeEntity(newList);
		return id;
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg) throws EntityStorageException {
		return registerError(depth, implicitDepth, msg, null);
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg, Throwable throwable) throws EntityStorageException {
		final ErrorDocumentIdentifier id = nextErrorIdentifier(depth,
				implicitDepth);
		ErrorDocument ed = new ErrorDocument(id, msg, throwable);
		storeEntity(ed);
		return id;
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			Throwable throwable) throws EntityStorageException{
		return registerError(depth, implicitDepth, null, throwable);
	}

	public EntityListIdentifier registerList(EntityIdentifier[] identifiers) throws EntityStorageException{
		if (identifiers.length == 0) {
			throw new IndexOutOfBoundsException(
					"Cannot register an empty list through registerList method");
		}
		EntityListIdentifier id = nextListIdentifier(identifiers[0].getDepth() + 1);
		EntityList newList = new EntityList(id, Arrays.asList(identifiers));
		storeEntity(newList);
		return id;
	}

	public Iterator<ContextualizedIdentifier> traverse(
			EntityIdentifier identifier, int desiredDepth) throws EntityRetrievalException {
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
					} catch (EntityNotFoundException enfe) {
						throw new AssertionError(
								"Entity referenced within list but not found within this data manager");
					}
					break;
				case Data:
					throw new AssertionError(
							"Should never be trying to drill inside a data document identifier");
				case Error:
					newSet
							.add(new ContextualizedIdentifier(
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


	
	protected abstract <ID extends EntityIdentifier> Entity<ID, ?> retrieveEntity(ID id) throws EntityRetrievalException;
	
	protected abstract <Bean> void storeEntity(Entity<?, Bean> entity) throws EntityStorageException;

	private static int[] addIndex(int[] current, int head) {
		int[] result = new int[current.length + 1];
		System.arraycopy(current, 0, result, 0, current.length);
		result[current.length] = head;
		return result;
	}

}
