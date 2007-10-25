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
import net.sf.taverna.t2.cloudone.impl.http.HttpReferenceBean;
import net.sf.taverna.t2.cloudone.impl.http.HttpReferenceScheme;

import org.apache.log4j.Logger;

public class DataDocumentImpl implements DataDocument {
	private static Logger logger = Logger.getLogger(DataDocumentImpl.class);

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

	public DataDocumentBean getAsBean() {
		DataDocumentBean bean = new DataDocumentBean();
		bean.setIdentifier(identifier.getAsBean());
		List<ReferenceBean> references = new ArrayList<ReferenceBean>();
		for (ReferenceScheme<? extends ReferenceBean> refSchema : referenceSchemes) {
			references.add(refSchema.getAsBean());
		}
		bean.setReferences(references);
		return bean;
	}

	public DataDocumentIdentifier getIdentifier() {
		return identifier;
	}

	public Set<ReferenceScheme> getReferenceSchemes() {
		return referenceSchemes;
	}

	@SuppressWarnings("unchecked")
	public void setFromBean(DataDocumentBean bean) {
		if (identifier != null || !referenceSchemes.isEmpty()) {
			throw new IllegalStateException("Can't initialise twice");
		}
		identifier = EntityIdentifiers.parseDocumentIdentifier(bean
				.getIdentifier());
		for (ReferenceBean refBean : bean.getReferences()) {
			Class<? extends ReferenceScheme> ownerClass = refBean
					.getOwnerClass();
			ReferenceScheme refScheme;
			try {
				refScheme = ownerClass.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException("Can't instantiate reference scheme "
						+ ownerClass, e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Can't access reference scheme "
						+ ownerClass, e);
			}
			refScheme.setFromBean(refBean);
			referenceSchemes.add(refScheme);
		}
	}

	public void setIdentifier(DataDocumentIdentifier identifier) {
		this.identifier = identifier;
	}

	public void setReferenceSchemes(Set<ReferenceScheme> referenceSchemes) {
		this.referenceSchemes = referenceSchemes;
	}

	public Class<DataDocumentBean> getBeanClass() {
		return DataDocumentBean.class;
	}
}