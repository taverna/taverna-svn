package net.sf.taverna.t2.cloudone.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.cloudone.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.DataDocument;
import net.sf.taverna.t2.cloudone.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.Entity;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.cloudone.EntityList;
import net.sf.taverna.t2.cloudone.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;

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

	private Map<EntityIdentifier, Entity<? extends EntityIdentifier>> contents;

	private String namespace;

	private int counter = 0;

	private DataDocumentIdentifier nextDataIdentifier() {
		try {
			return new DataDocumentIdentifier("urn:t2data:ddoc://" + namespace
					+ "/data" + (counter++));
		} catch (MalformedIdentifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private EntityListIdentifier nextListIdentifier(int depth) {
		try {
			return new EntityListIdentifier("urn:t2data:list://" + namespace
					+ "/list" + (counter++) + "/" + depth);
		} catch (MalformedIdentifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public InMemoryDataManager(String namespace, Set<LocationalContext> contexts) {
		this.contexts = contexts;
		this.namespace = namespace;
		this.contents = new HashMap<EntityIdentifier, Entity<? extends EntityIdentifier>>();
	}

	@SuppressWarnings("unchecked")
	public <EI extends EntityIdentifier> Entity<EI> getEntity(EI identifier)
			throws EntityNotFoundException {
		Entity ent = contents.get(identifier);
		if (contents.containsKey(identifier) == false) {
			throw new EntityNotFoundException("No entity found with id : "
					+ identifier);
		}
		// we know this is type-safe because we control what goes into the map
		return (Entity<EI>) ent;
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

	public DataDocument registerDocument(final Set<ReferenceScheme> references) {

		final DataDocumentIdentifier ddocid = nextDataIdentifier();
		DataDocument d = new DataDocument() {

			public Set<ReferenceScheme> getReferenceSchemes() {
				return references;
			}

			public DataDocumentIdentifier getIdentifier() {
				return ddocid;
			}

		};
		contents.put(ddocid, d);
		return d;
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
}
