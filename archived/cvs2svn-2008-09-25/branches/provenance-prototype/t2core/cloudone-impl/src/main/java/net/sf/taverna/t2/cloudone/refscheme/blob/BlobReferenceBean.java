package net.sf.taverna.t2.cloudone.refscheme.blob;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

/**
 * {@link ReferenceBean} for serialising a {@link BlobReferenceSchemeImpl}.
 * 
 * @see BlobReferenceSchemeImpl#getAsBean()
 * @see BlobReferenceSchemeImpl#setFromBean(BlobReferenceBean)
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@Entity
@XmlRootElement(namespace = "http://taverna.sf.net/t2/cloudone/refscheme/blob/", name = "blobReferenceScheme")
@XmlType(namespace = "http://taverna.sf.net/t2/cloudone/refscheme/blob/", name = "blobReferenceScheme")
public class BlobReferenceBean extends ReferenceBean {

	private String id;

	private String namespace;

	private String charset;

	public String getCharset() {
		return charset;
	}

	public String getId() {
		return id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public Class<BlobReferenceSchemeImpl> getOwnerClass() {
		return BlobReferenceSchemeImpl.class;
	}
}
