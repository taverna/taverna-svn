package net.sf.taverna.t2.cloudone;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;

/**
 * The data manager is the primary access interface for external (i.e. not in
 * the data layer) code. It contains methods for registration and resolution of
 * data documents, error documents and lists. The design philosophy is that each
 * node in a potential cloud of data manager peers accesses its own data manager
 * through this interface whether the data manager is implemented as a pure
 * network proxy on a lightweight device or as a storage backed long term store
 * on a data server.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public interface DataManager {

	/**
	 * Fetch the named entity by identifier.
	 */
	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI identifier)
			throws EntityNotFoundException, EntityRetrievalException;

	/**
	 * Initiates a traversal of the specified data reference, traversing to
	 * whatever level of depth is required such that all identifiers returned
	 * within the iterator have the specified depth. The context (i.e. the index
	 * path from the originally specified reference to each reference within the
	 * iteration) is included through use of the ContextualizedIdentifier
	 * wrapper class
	 * 
	 * @param identifier
	 * @param desiredDepth
	 * @return
	 * @throws EntityRetrievalException If an entity could not be retrieved
	 */
	public Iterator<ContextualizedIdentifier> traverse(
			EntityIdentifier identifier, int desiredDepth) throws EntityRetrievalException;

	/**
	 * Register a new list from an array of identifiers. Returns the identifier
	 * of the new list, blocking until creation has finished and the list is
	 * available for resolution.
	 * <p>
	 * We require a depth parameter explicitly stated here to cope with
	 * registration of empty lists, something that Taverna 1 had huge issues
	 * with. A list is allowed to be empty and has a conceptual depth even in
	 * this case.
	 */
	public EntityListIdentifier registerList(EntityIdentifier[] identifiers)
			throws EntityStorageException;

	/**
	 * Register a new empty list. Returns the identifier of the new list,
	 * blocking until creation has finished and the list is available for
	 * resolution.
	 * <p>
	 * We require a depth parameter explicitly stated here to cope with
	 * registration of empty lists, something that Taverna 1 had huge issues
	 * with. A list is allowed to be empty and has a conceptual depth even in
	 * this case.
	 */
	public EntityListIdentifier registerEmptyList(int depth)
			throws EntityStorageException;

	/**
	 * Take a set of references, all of which must point to byte equivalent data
	 * where resolvable, build, name and return a new DataDocument object
	 * containing these references and named within the data manager's active
	 * namespace
	 * 
	 * @param references
	 * @return
	 */
	public DataDocumentIdentifier registerDocument(
			Set<ReferenceScheme> references) throws EntityStorageException;


	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg) throws EntityStorageException;

	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			Throwable throwable) throws EntityStorageException;

	/**
	 * Register a single error with the data manager. An error has both a depth
	 * and an implicit depth, and either a message or a Throwable cause, or
	 * both.
	 * 
	 * @param depth
	 * @param implicitDepth
	 * @param msg Error message
	 * @param throwable Cause for error
	 * @return An {@link ErrorDocumentIdentifier}
	 * @throws EntityStorageException If the error document could not be stored
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg, Throwable throwable) throws EntityStorageException;

	public String getCurrentNamespace();

	public Set<LocationalContext> getLocationalContexts();

	public List<String> getManagedNamespaces();

}
