package net.sf.taverna.t2.reference.impl;

import java.util.List;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.h3.HibernateMappedEntity;

public class T2ReferenceListImpl extends IdentifiedArrayList<T2Reference>
		implements HibernateMappedEntity {

	public T2ReferenceListImpl() {
		super();
	}

	@SuppressWarnings("unchecked")
	public List getListContents() {
		return this.listDelegate;
	}

	@SuppressWarnings("unchecked")
	public void setListContents(List newList) {
		this.listDelegate = newList;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getId().toString()+"\n");
		int counter = 0;
		for (T2Reference ref : listDelegate) {
			sb.append("  "+(++counter)+") "+ref.toString()+"\n");
		}		
		return sb.toString();
	}

}
