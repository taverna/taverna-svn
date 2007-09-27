package net.sf.taverna.t2.cloudone;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

/**
 * Defines a reference to data used in a DataDocument. In addition defines the
 * set of properties used to determine whether a reference using this scheme is
 * within the visible scope of a given DataManager.
 * 
 * The reference scheme must be a bean.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 */
public interface ReferenceScheme {

	/**
	 * Dereference this reference scheme instance to return an input stream
	 * accessing the underlying value of the reference using the specified
	 * DataManager to resolve any security or other resolution requirements.
	 * 
	 * @return
	 */
	public InputStream dereference(DataManager manager)
			throws DereferenceException;

	/**
	 * If the reference is immediate this defines the date at which it will
	 * become invalid. If the reference is not immediate this has no meaning and
	 * may return null.
	 * 
	 * @return date at which an immediate reference becomes invalid.
	 */
	public Date getExpiry();

	/**
	 * When a reference scheme is exported from a data peer there are some cases
	 * where it must be dereferenced immediately. This happens primarily in
	 * cases where the peer is shutting down and is exporting a reference to a
	 * data object physically located on that peer which must be dereferenced
	 * before the shutdown can complete. In such cases the receiving peer should
	 * dereference this reference scheme and remove it from the data document -
	 * this behaviour can be implemented in the peer container as part of the
	 * proxy implementation for convenience.
	 * 
	 * @return whether the reference should be resolved immediately.
	 */
	public boolean isImmediate();

	/**
	 * Given a set of locational context specifiers will this reference scheme
	 * instance be valid within the specified context? The current location is
	 * specified by a DataPeer reference, implementations can use this to get
	 * the current local context set.
	 * 
	 * @param contextSet
	 * @return
	 */
	public boolean validInContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation);

}
