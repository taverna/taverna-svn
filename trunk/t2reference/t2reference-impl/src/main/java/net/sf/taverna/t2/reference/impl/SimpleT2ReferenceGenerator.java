package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;
import net.sf.taverna.t2.reference.h3.ReferenceSetT2ReferenceImpl;

public class SimpleT2ReferenceGenerator implements T2ReferenceGenerator {

	private String namespace = null;
	private int counter = 0;
	
	public void setNamespace(String newNamespace) {
		this.namespace = newNamespace;
	}
	
	public String getNamespace() {
		return namespace;
	}

	public synchronized T2Reference nextReferenceSetReference() {
		ReferenceSetT2ReferenceImpl r = new ReferenceSetT2ReferenceImpl();
		r.setNamespacePart(namespace);
		String localPart = "test"+(counter++);
		r.setLocalPart(localPart);
		return r;
	}

}
