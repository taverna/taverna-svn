package net.sf.taverna.t2.cloudone.refscheme.blob;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.cloudone.datamanager.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * A {@link BlobReferenceScheme} that is {@link Beanable} as a
 * {@link BlobReferenceBean}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class BlobReferenceSchemeImpl implements
		BlobReferenceScheme<BlobReferenceBean> {

	private static Logger logger = Logger
			.getLogger(BlobReferenceSchemeImpl.class);

	private String id;

	private String namespace;

	private String charset;

	/**
	 * Construct for immediate initialisation using
	 * {@link #setFromBean(BlobReferenceBean)}.
	 * 
	 */
	public BlobReferenceSchemeImpl() {
		id = null;
		namespace = null;
	}

	/**
	 * Construct a BlobReferenceSchemeImpl with the given namespace and id, and
	 * no character set. This blob can't automatically be converted to a
	 * {@link String} by the {@link DataFacade}.
	 * 
	 * @param namespace
	 *            Namespace
	 * @param id
	 *            Identifier
	 */
	public BlobReferenceSchemeImpl(String namespace, String id) {
		this(namespace, id, null);
	}

	/**
	 * Construct a BlobReferenceSchemeImpl with the given namespace, id, and
	 * character set. Since a character set is given, the blob can be converted
	 * to a {@link String} by the {@link DataFacade}.
	 * 
	 * @param namespace
	 *            Namespace
	 * @param id
	 *            Identifier
	 * @param charset
	 *            A valid character set, for example
	 *            {@value BlobStore#STRING_CHARSET}
	 */
	public BlobReferenceSchemeImpl(String namespace, String id, String charset) {
		if (id == null || namespace == null) {
			throw new NullPointerException("id and namespace can't be null");
		}
		if (!EntityIdentifier.isValidName(namespace)) {
			throw new MalformedIdentifierException("Invalid namespace: "
					+ namespace);
		}
		this.id = id;
		this.namespace = namespace;
		this.charset = charset;
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream dereference(DataManager manager)
			throws DereferenceException {
		try {
			return manager.getBlobStore().retrieveAsStream(this);
		} catch (RetrievalException e) {
			throw new DereferenceException(e);
		} catch (NotFoundException e) {
			throw new DereferenceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BlobReferenceSchemeImpl)) {
			return false;
		}
		final BlobReferenceSchemeImpl other = (BlobReferenceSchemeImpl) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (namespace == null) {
			if (other.namespace != null) {
				return false;
			}
		} else if (!namespace.equals(other.namespace)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceBean getAsBean() {
		BlobReferenceBean bean = new BlobReferenceBean();
		bean.setNamespace(getNamespace());
		bean.setId(getId());
		bean.setCharset(getCharset());
		return bean;
	}

	/**
	 * Get the character set for interpreting this blob as a {@link String}, or
	 * <code>null</code> if blob is a binary or the character set is unknown.
	 * 
	 * @return A valid character set, or <code>null</code>
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getExpiry() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isImmediate() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFromBean(BlobReferenceBean bean)
			throws IllegalArgumentException {
		if (id != null) {
			throw new IllegalStateException("Can't initialise twice");
		}
		id = bean.getId();
		namespace = bean.getNamespace();
		charset = bean.getCharset();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Blob [ns:" + getNamespace() + " id:" + getId() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean validInContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation) {
		return validInBlobContext(contextSet, currentLocation);
	}

	/**
	 * Check if a {@link BlobReferenceSchemeImpl} would be valid in
	 * currentLocation given contextSet of {@link LocationalContext}s.
	 * <p>
	 * Similar to {@link #validInContext(Set, DataPeer)}
	 * 
	 * @param contextSet
	 * @param currentLocation
	 * @return true if a blob from contextSet would be valid
	 */
	public static boolean validInBlobContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation) {
		for (LocationalContext currentContext : currentLocation
				.getLocationalContexts()) {
			if (!currentContext.getContextType().equals(
					BlobStore.LOCATIONAL_CONTEXT_TYPE)) {
				continue;
			}
			String currentUuid = currentContext
					.getValue(BlobStore.LOCATIONAL_CONTEXT_KEY_UUID);
			if (currentUuid == null) {
				logger.warn("Invalid current BlobStore LocationalContext "
						+ currentContext);
				continue;
			}
			for (LocationalContext context : contextSet) {
				if (!context.getContextType().equals(
						BlobStore.LOCATIONAL_CONTEXT_TYPE)) {
					continue;
				}
				String uuid = context
						.getValue(BlobStore.LOCATIONAL_CONTEXT_KEY_UUID);
				if (uuid == null) {
					logger.warn("Invalid BlobStore LocationalContext "
							+ currentContext);
					continue;
				}
				if (currentUuid.equals(uuid)) {
					return true;
				}
			}
		}
		return false;
	}

}
