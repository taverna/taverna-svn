package net.sf.taverna.t2.cloudone.datamanager;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * Thrown when a request is made for resolution of an entity or blob that cannot
 * be found by the store to which the request is made.
 *
 * @author Tom Oinn
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class NotFoundException extends Exception {

	private static final long serialVersionUID = -1069998094174721609L;

	public NotFoundException() {
	}

	@SuppressWarnings("unchecked")
	public NotFoundException(ReferenceScheme reference) {
		super("Can't find blob " + reference);
	}

	public NotFoundException(EntityIdentifier id) {
		super("Can't find entity " + id);
	}

	public NotFoundException(String msg) {
		super(msg);
	}

	public NotFoundException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public NotFoundException(Throwable throwable) {
		super(throwable);
	}

}
