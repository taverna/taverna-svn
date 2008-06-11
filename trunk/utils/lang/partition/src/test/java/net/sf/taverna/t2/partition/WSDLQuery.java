package net.sf.taverna.t2.partition;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;


public class WSDLQuery implements Query<WSDLActivityItem> {

	HashSetModel<WSDLActivityItem> model = new HashSetModel<WSDLActivityItem>();
	
	public void doQuery() {
		add(new WSDLActivityItem("WSDL","KEGG","getGenes"));
		add(new WSDLActivityItem("WSDL","KEGG","getProtein"));	
	}

	public Date getLastQueryTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean add(WSDLActivityItem item) {
		return model.add(item);
	}

	public boolean addAll(Collection<? extends WSDLActivityItem> c) {
		return model.addAll(c);
	}

	public void addSetModelChangeListener(
			SetModelChangeListener<WSDLActivityItem> listener) {
		model.addSetModelChangeListener(listener);
	}

	public void clear() {
		model.clear();
	}

	public Object clone() {
		return model.clone();
	}

	public boolean contains(Object o) {
		return model.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return model.containsAll(c);
	}

	public boolean equals(Object o) {
		return model.equals(o);
	}

	public int hashCode() {
		return model.hashCode();
	}

	public boolean isEmpty() {
		return model.isEmpty();
	}

	public Iterator<WSDLActivityItem> iterator() {
		return model.iterator();
	}

	public boolean remove(Object item) {
		return model.remove(item);
	}

	public boolean removeAll(Collection<?> c) {
		return model.removeAll(c);
	}

	public void removeSetModelChangeListener(
			SetModelChangeListener<WSDLActivityItem> listener) {
		model.removeSetModelChangeListener(listener);
	}

	public boolean retainAll(Collection<?> c) {
		return model.retainAll(c);
	}

	public int size() {
		return model.size();
	}

	public Object[] toArray() {
		return model.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return model.toArray(a);
	}

	public String toString() {
		return model.toString();
	}



}
