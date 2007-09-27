package net.sf.taverna.t2.cloudone.bean;

import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;

/**
 * Abstract bean for serialising references, such as from
 * {@link DataDocumentBean}.
 *
 * @see Beanable
 * @see DataDocumentBean
 * @see net.sf.taverna.t2.cloudone.impl.BlobReferenceBean
 * @see net.sf.taverna.t2.cloudone.impl.url.URLReferenceBean
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public abstract class ReferenceBean {
	String type;

	/**
	 * Get the type of reference scheme, for instance
	 * {@link net.sf.taverna.t2.cloudone.impl.url.URLReferenceBean#TYPE}. This
	 * is used by {@link DataDocumentImpl#setFromBean(DataDocumentBean)} to
	 * determine the correct reference scheme to instantiate.
	 *
	 * @return The type identifier of reference scheme
	 */
	public abstract String getType();

	/**
	 * Set the type of reference scheme.
	 *
	 * @param type
	 *            Reference scheme identifier, for instance
	 *            {@link net.sf.taverna.t2.cloudone.impl.url.URLReferenceBean#TYPE}
	 */
	public void setType(String type) {
		this.type = type;
	}

}
