package net.sf.taverna.t2.cloudone.datamanager.memory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.impl.DataDocumentImpl;

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
public class InMemoryDataManager implements DataManager {

	private Set<LocationalContext> contexts;

	private Map<EntityIdentifier, Entity<? extends EntityIdentifier, ?>> contents;

	private String namespace;

	private int counter = 0;

	private DataDocumentIdentifier nextDataIdentifier() {
		String id = "urn:t2data:ddoc://" + namespace + "/data" + (counter++);
		try {
			return new DataDocumentIdentifier(id);
		} catch (MalformedIdentifierException e) {
			throw new RuntimeException("Malformed data identifier: " + id, e);
		}
	}
	
	private ErrorDocumentIdentifier nextErrorIdentifier(int depth, int implicitDepth) {
		String id = "urn:t2data:error://" + namespace + "/error" + (counter++)
				+ "/" + depth + "/" + implicitDepth;
		try {
			return new ErrorDocumentIdentifier(id);
		} catch (MalformedIdentifierException e) {
			throw new RuntimeException("Malformed data identifier: " + id, e);
		}
	}

	private EntityListIdentifier nextListIdentifier(int depth) {
		if (depth < 1) {
			throw new IllegalArgumentException("Depth must be at least 1");
		}
		String id = "urn:t2data:list://" + namespace + "/list" + (counter++)
				+ "/" + depth;
		try {
			return new EntityListIdentifier(id);
		} catch (MalformedIdentifierException e) {
			throw new RuntimeException("Malformed list identifier: " + id, e);
		}
	}

	public InMemoryDataManager(String namespace, Set<LocationalContext> contexts) {
		this.contexts = contexts;
		this.namespace = namespace;
		this.contents = new HashMap<EntityIdentifier, Entity<? extends EntityIdentifier, ?>>();
	}

	@SuppressWarnings("unchecked")
	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI identifier)
			throws EntityNotFoundException {
		if (identifier instanceof Literal) {
			return (Entity<EI, ?>) identifier;
		}
		Entity ent = contents.get(identifier);
		if (contents.containsKey(identifier) == false) {
			throw new EntityNotFoundException("No entity found with id : "
					+ identifier);
		}
		// we know this is type-safe because we control what goes into the map
		return (Entity<EI, ?>) ent;
	}

	public String getCurrentNamespace() {
		return this.namespace;
	}

	public Set<LocationalContext> getLocationalContexts() {
		return this.contexts;
	}

	public List<String> getManagedNamespaces() {
		return Collections.singletonList(namespace);
	}

	public DataDocumentIdentifier registerDocument(final Set<ReferenceScheme> references) {
		final DataDocumentIdentifier id = nextDataIdentifier();
		DataDocument d = new DataDocumentImpl(id, references);
		contents.put(id, d);
		return id;
	}

	public EntityListIdentifier registerEmptyList(int depth) {
		EntityListIdentifier id = nextListIdentifier(depth);
		EntityList newList = new EntityList(id, Collections
				.<EntityIdentifier> emptyList());
		contents.put(id, newList);
		return id;
	}

	public EntityListIdentifier registerList(EntityIdentifier[] identifiers) {
		if (identifiers.length == 0) {
			throw new IndexOutOfBoundsException(
					"Cannot register an empty list through registerList method");
		}
		EntityListIdentifier id = nextListIdentifier(identifiers[0].getDepth() + 1);
		EntityList newList = new EntityList(id, Arrays.asList(identifiers));
		contents.put(id, newList);
		return id;
	}

	public Iterator<ContextualizedIdentifier> traverse(
			EntityIdentifier identifier, int desiredDepth) {
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

	private static int[] addIndex(int[] current, int head) {
		int[] result = new int[current.length + 1];
		System.arraycopy(current, 0, result, 0, current.length);
		result[current.length] = head;
		return result;
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth, String msg) {
		return registerError(depth, implicitDepth, msg, null);
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth, Throwable throwable) {
		return registerError(depth, implicitDepth, null, throwable);		
	}

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth, String msg, Throwable throwable) {
		final ErrorDocumentIdentifier id = nextErrorIdentifier(depth, implicitDepth);
		ErrorDocument ed = new ErrorDocument(id, msg, throwable);
		contents.put(id, ed);
		return id;
	}
}
