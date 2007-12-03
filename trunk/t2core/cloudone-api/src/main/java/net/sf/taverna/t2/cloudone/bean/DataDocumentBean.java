package net.sf.taverna.t2.cloudone.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * Bean for serialising a {@link DataDocument}. A DataDocument is serialised as
 * a String identifier from {@link #getIdentifier()}, and a list of
 * {@link ReferenceBean}s (serialised {@link ReferenceScheme}s) from
 * {@link #getReferences()}
 *
 * @see Beanable
 * @see DataDocument
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class DataDocumentBean {

	private String identifier;

	private List<ReferenceBean> references = new ArrayList<ReferenceBean>();
	
	public String getIdentifier() {
		return identifier;
	}

	public List<ReferenceBean> getReferences() {
		return references;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setReferences(List<ReferenceBean> references) {
		this.references = references;
	}

}
