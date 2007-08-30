/**
 * 
 */
package net.sf.taverna.t2.cloudone.entity.impl;

import java.util.Set;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

public class DataDocumentImpl implements DataDocument {
	private DataDocumentIdentifier identifier;
	private Set<ReferenceScheme> referenceSchemes;

	public DataDocumentImpl() {
		identifier = null;
		referenceSchemes = null;
	}
	
	public DataDocumentImpl(DataDocumentIdentifier ddocid,
			Set<ReferenceScheme> references) {
		this.identifier = ddocid;
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

	public String getAsBean() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFromBean(String bean) {
		// TODO Auto-generated method stub
		
	}
}