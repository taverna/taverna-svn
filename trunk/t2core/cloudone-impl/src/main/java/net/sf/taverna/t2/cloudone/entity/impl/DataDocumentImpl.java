/**
 * 
 */
package net.sf.taverna.t2.cloudone.entity.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
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

	public DataDocumentBean getAsBean() {
		DataDocumentBean bean = new DataDocumentBean();
		bean.setIdentifier(identifier.getAsBean());
		List<String> urlReferences = new ArrayList<String>();
		for (ReferenceScheme refSchema : referenceSchemes) {
			if (! (refSchema instanceof URLReferenceScheme)) {
				// TODO: Support other types of reference schema
				continue;
			}
			String url = ((URLReferenceScheme) refSchema).getAsBean();
			urlReferences.add(url);
		}
		bean.setUrlReferences(urlReferences);
		return bean;
	}

	public void setFromBean(DataDocumentBean bean) {
		if (identifier != null || ! referenceSchemes.isEmpty()) {
			throw new IllegalStateException("Can't initialise twice");
		}
		identifier = EntityIdentifiers.parseDocumentIdentifier(bean.getIdentifier());
		for (String url : bean.getUrlReferences()) {
			// TODO: Support other reference schemes
			URLReferenceScheme refScheme = new URLReferenceScheme();
			refScheme.setFromBean(url);
			referenceSchemes.add(refScheme);	
		}
	}
}