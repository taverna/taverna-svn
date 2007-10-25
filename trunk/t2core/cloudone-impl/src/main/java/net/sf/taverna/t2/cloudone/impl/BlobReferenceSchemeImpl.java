package net.sf.taverna.t2.cloudone.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DataPeer;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.bean.Beanable;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;

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

	private String id;

	private String namespace;

	private String charset;

	public BlobReferenceSchemeImpl() {
		id = null;
		namespace = null;
	}

	public BlobReferenceSchemeImpl(String namespace, String id) {
		this(namespace, id, null);
	}
	
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

	public InputStream dereference(DataManager manager)
			throws DereferenceException {
		try {
			// TODO: might not be the right manager
			return manager.getBlobStore().retrieveAsStream(this);
		} catch (RetrievalException e) {
			throw new DereferenceException(e);
		} catch (NotFoundException e) {
			throw new DereferenceException(e);
		}
	}

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

	public BlobReferenceBean getAsBean() {
		BlobReferenceBean bean = new BlobReferenceBean();
		bean.setNamespace(getNamespace());
		bean.setId(getId());
		bean.setCharset(getCharset());
		return bean;
	}

	public Date getExpiry() {
		return null;
	}

	public String getId() {
		return id;
	}

	public String getNamespace() {
		return namespace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		return result;
	}

	public boolean isImmediate() {
		return false;
	}

	public void setFromBean(BlobReferenceBean bean)
			throws IllegalArgumentException {
		if (id != null) {
			throw new IllegalStateException("Can't initialise twice");
		}
		id = bean.getId();
		namespace = bean.getNamespace();
		charset = bean.getCharset();
	}

	@Override
	public String toString() {
		return "Blob [ns:" + getNamespace() + " id:" + getId() + "]";
	}

	public boolean validInContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation) {
		return true;
	}

	public Class<BlobReferenceBean> getBeanClass() {
		return BlobReferenceBean.class;
	}

	public String getCharset() {
		return charset;
	}

}
