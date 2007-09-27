package net.sf.taverna.t2.cloudone.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DataPeer;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;

public class BlobReferenceSchemeImpl implements
		BlobReferenceScheme<BlobReferenceBean> {

	private String id;

	private String namespace;

	public BlobReferenceSchemeImpl() {
		this.id = null;
		this.namespace = null;
	}

	public BlobReferenceSchemeImpl(String namespace, String id) {
		if (id == null || namespace == null) {
			throw new NullPointerException("id and namespace can't be null");
		}
		if (!EntityIdentifier.isValidName(namespace)) {
			throw new MalformedIdentifierException("Invalid namespace: "
					+ namespace);
		}
		this.id = id;
		this.namespace = namespace;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BlobReferenceSchemeImpl))
			return false;
		final BlobReferenceSchemeImpl other = (BlobReferenceSchemeImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}

	public BlobReferenceBean getAsBean() {
		BlobReferenceBean bean = new BlobReferenceBean();
		bean.setNamespace(getNamespace());
		bean.setId(getId());
		return bean;
	}

	public Date getExpiry() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeInterface#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeInterface#getNamespace()
	 */
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
		// TODO Auto-generated method stub
		return false;
	}

	public void setFromBean(BlobReferenceBean bean)
			throws IllegalArgumentException {
		if (id != null) {
			throw new IllegalStateException("Can't initialise twice");
		}
		this.id = bean.getId();
		this.namespace = bean.getNamespace();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeInterface#toString()
	 */
	@Override
	public String toString() {
		return "Blob " + getNamespace() + " " + getId();
	}

	public boolean validInContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation) {
		// TODO Auto-generated method stub
		return false;
	}

}
