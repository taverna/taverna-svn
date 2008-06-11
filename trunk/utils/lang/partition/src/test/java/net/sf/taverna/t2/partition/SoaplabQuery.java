package net.sf.taverna.t2.partition;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class SoaplabQuery implements Query<SoaplabActivityItem>{

	HashSetModel<SoaplabActivityItem> model = new HashSetModel<SoaplabActivityItem>();

	public boolean add(SoaplabActivityItem item) {
		return model.add(item);
	}

	public boolean addAll(Collection<? extends SoaplabActivityItem> c) {
		return model.addAll(c);
	}

	public void addSetModelChangeListener(
			SetModelChangeListener<SoaplabActivityItem> listener) {
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

	public Iterator<SoaplabActivityItem> iterator() {
		return model.iterator();
	}

	public boolean remove(Object item) {
		return model.remove(item);
	}

	public boolean removeAll(Collection<?> c) {
		return model.removeAll(c);
	}

	public void removeSetModelChangeListener(
			SetModelChangeListener<SoaplabActivityItem> listener) {
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

	public void doQuery() {
		add(new SoaplabActivityItem("Soaplab","EBI","genes","find_gene"));
		add(new SoaplabActivityItem("Soaplab","EBI","fruit-flies","find_fly"));
		add(new SoaplabActivityItem("Soaplab","KEGG","net","some_net_stuff"));
	}

	public Date getLastQueryTime() {
		// TODO Auto-generated method stub
		return null;
	}

	
	

}
