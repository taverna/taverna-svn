package net.sf.taverna.t2.cloudone.datamanager;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

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
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public interface DataManager {

	/**
	 * Get a {@link BlobStore} based on this {@link DataManager}, if available.
	 * Otherwise, return <code>null</code>.
	 * 
	 * @return A {@link BlobStore} based on the same storage mechanism as this
	 *         {@link DataManager}, or <code>null</code> if the
	 *         {@link DataManager} does not provide a {@link BlobStore}.
	 */
	public BlobStore getBlobStore();

	/**
	 * Get the current namespace of the data manager. Entities registered with
	 * this datamanager will be assigned identities within this namespace.
	 * 
	 * @return The current namespace
	 */
	public String getCurrentNamespace();

	/**
	 * Fetch the named entity by identifier.
	 */
	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI identifier)
			throws NotFoundException, RetrievalException;

	/**
	 * Get the set of {@link LocationalContext}s this Data manager knows about.
	 * 
	 * @return Set of {@link LocationalContext}
	 */
	public Set<LocationalContext> getLocationalContexts();

	/**
	 * Get the list of managed namespaces this data manager can retrieve
	 * entities from using {@link #getEntity(EntityIdentifier)}. This normally
	 * includes {@link #getCurrentNamespace()}.
	 * 
	 * @return List of managed namespaces
	 */
	public List<String> getManagedNamespaces();

	/**
	 * Get the maximum length in bytes of an (URL encoded) identifier. This is
	 * mainly used by {@link net.sf.taverna.t2.cloudone.datamanager.DataFacade}
	 * when determening to store a {@link String} as a {@link Literal} or using
	 * a {@link BlobStore}.
	 * 
	 * @return
	 */
	public int getMaxIDLength();

	/**
	 * Register a document. Take a set of references, all of which must point to
	 * byte equivalent data where resolvable, build, name and return a new
	 * DataDocument object containing these references and named within the data
	 * manager's active namespace.
	 * 
	 * @see #registerDocument(Set)
	 * @param references
	 *            One or more {@link ReferenceScheme}s
	 * @return Registered {@link DataDocumentIdentifier}
	 */

	@SuppressWarnings("unchecked")
	public DataDocumentIdentifier registerDocument(
			ReferenceScheme... references) throws StorageException;

	/**
	 * Register a document. Take a set of references, all of which must point to
	 * byte equivalent data where resolvable, build, name and return a new
	 * DataDocument object containing these references and named within the data
	 * manager's active namespace.
	 * 
	 * @param references
	 * @return Registered {@link DataDocumentIdentifier}
	 */
	@SuppressWarnings("unchecked")
	public DataDocumentIdentifier registerDocument(
			Set<ReferenceScheme> references) throws StorageException;

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
			throws StorageException;

	/**
	 * Register a single error with the data manager. An error has a depth, an
	 * implicit depth, and an error message.
	 * 
	 * @param depth
	 * @param implicitDepth
	 * @param msg
	 *            Error message
	 * @return An {@link ErrorDocumentIdentifier}
	 * @throws StorageException
	 *             If the error document could not be stored
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg) throws StorageException;

	/**
	 * Register a single error with the data manager. An error has both a depth
	 * and an implicit depth, and either a message or a Throwable cause, or
	 * both.
	 * 
	 * @param depth
	 * @param implicitDepth
	 * @param msg
	 *            Error message
	 * @param throwable
	 *            Cause for error
	 * @return An {@link ErrorDocumentIdentifier}
	 * @throws StorageException
	 *             If the error document could not be stored
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg, Throwable throwable) throws StorageException;

	/**
	 * Register a single error with the data manager. An error has a depth, an
	 * implicit depth, and a Throwable cause.
	 * 
	 * @param depth
	 * @param implicitDepth
	 * @param throwable
	 *            Cause for error
	 * @return An {@link ErrorDocumentIdentifier}
	 * @throws StorageException
	 *             If the error document could not be stored
	 */
	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			Throwable throwable) throws StorageException;

	/**
	 * Register a new list from a varargs/array of identifiers. Returns the
	 * identifier of the new list, blocking until creation has finished and the
	 * list is available for resolution.
	 * <p>
	 * This method can't register empty lists, use
	 * {@link #registerEmptyList(int)} instead.
	 */
	public EntityListIdentifier registerList(EntityIdentifier... identifiers)
			throws StorageException;

	/**
	 * Register a new list from a list of identifiers. Returns the identifier of
	 * the new list, blocking until creation has finished and the list is
	 * available for resolution.
	 * <p>
	 * This method can't register empty lists, use
	 * {@link #registerEmptyList(int)} instead.
	 */
	public EntityListIdentifier registerList(List<EntityIdentifier> identifiers)
			throws StorageException;

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
	 * @throws RetrievalException
	 *             If an entity could not be retrieved
	 */
	public Iterator<ContextualizedIdentifier> traverse(
			EntityIdentifier identifier, int desiredDepth)
			throws RetrievalException;

}
