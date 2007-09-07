/**
 * 
 */
package net.sf.taverna.t2.cloudone.entity.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceBean;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceBean;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;

public class DataDocumentImpl implements DataDocument {
	private DataDocumentIdentifier identifier;
	private Set<ReferenceScheme> referenceSchemes;

	public DataDocumentImpl() {
		identifier = null;
		referenceSchemes = new HashSet<ReferenceScheme>();
	}
	
	public DataDocumentImpl(DataDocumentIdentifier identifier,
			Set<ReferenceScheme> references) {
		this.identifier = identifier;
		this.referenceSchemes = references;
	}

	public Set<ReferenceScheme> getReferenceSchemes() {
		return referenceSchemes;
	}

	public DataDocumentIdentifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(DataDocumentIdentifier identifier) {
		this.identifier = identifier;
	}

	public void setReferenceSchemes(Set<ReferenceScheme> referenceSchemes) {
		this.referenceSchemes = referenceSchemes;
	}

	@SuppressWarnings("unchecked")
	public DataDocumentBean getAsBean() {
		DataDocumentBean bean = new DataDocumentBean();
		bean.setIdentifier(identifier.getAsBean());
		List<ReferenceBean> references = new ArrayList<ReferenceBean>();
		for (ReferenceScheme refSchema : referenceSchemes) {
			// FIXME: Make ReferenceScheme Beanable<? extends RefererenceBean> to
			// avoid these instanceof tests
			if (refSchema instanceof URLReferenceScheme) {
			URLReferenceBean refBean = ((URLReferenceScheme) refSchema)
						.getAsBean();
				references.add(refBean);			
			} else if (refSchema instanceof BlobReferenceScheme<?>) {
				BlobReferenceScheme<BlobReferenceBean> blobRef = (BlobReferenceScheme<BlobReferenceBean>) refSchema;
				BlobReferenceBean refBean = blobRef.getAsBean();
				references.add(refBean);
			} else {
				//logger.warn("Unsupported reference schema " + refSchema);
				continue;
			}
		}
		bean.setReferences(references);
		return bean;
	}

	public void setFromBean(DataDocumentBean bean) {
		if (identifier != null || ! referenceSchemes.isEmpty()) {
			throw new IllegalStateException("Can't initialise twice");
		}
		identifier = EntityIdentifiers.parseDocumentIdentifier(bean.getIdentifier());
		for (ReferenceBean refBean : bean.getReferences()) {
			// TODO: Use registry
			if (refBean.getType().equals(URLReferenceBean.TYPE)) {
				URLReferenceBean urlRefBean = (URLReferenceBean) refBean;
				URLReferenceScheme urlRefScheme = new URLReferenceScheme();
				urlRefScheme.setFromBean(urlRefBean);
				referenceSchemes.add(urlRefScheme);
			} else if(refBean.getType().equals(BlobReferenceBean.TYPE)) {
				BlobReferenceBean blobRefBean = (BlobReferenceBean) refBean;
				BlobReferenceSchemeImpl blobRef = new BlobReferenceSchemeImpl();
				blobRef.setFromBean(blobRefBean);
				referenceSchemes.add(blobRef);
			} else {
				// logger.warn("Unsupported type " + refBean.getType());
				continue;
			}
 		}
	}
}