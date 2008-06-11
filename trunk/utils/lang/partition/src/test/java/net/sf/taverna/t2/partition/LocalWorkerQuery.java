package net.sf.taverna.t2.partition;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class LocalWorkerQuery implements Query<LocalWorkerActivityItem>{
	HashSetModel<LocalWorkerActivityItem> model = new HashSetModel<LocalWorkerActivityItem>();

	public boolean add(LocalWorkerActivityItem item) {
		return model.add(item);
	}

	public boolean addAll(Collection<? extends LocalWorkerActivityItem> arg0) {
		return model.addAll(arg0);
	}

	public void addSetModelChangeListener(
			SetModelChangeListener<LocalWorkerActivityItem> listener) {
		model.addSetModelChangeListener(listener);
	}

	public void clear() {
		model.clear();
	}

	public Object clone() {
		return model.clone();
	}

	public boolean contains(Object arg0) {
		return model.contains(arg0);
	}

	public boolean containsAll(Collection<?> arg0) {
		return model.containsAll(arg0);
	}

	public boolean equals(Object arg0) {
		return model.equals(arg0);
	}

	public int hashCode() {
		return model.hashCode();
	}

	public boolean isEmpty() {
		return model.isEmpty();
	}

	public Iterator<LocalWorkerActivityItem> iterator() {
		return model.iterator();
	}

	public boolean remove(Object item) {
		return model.remove(item);
	}

	public boolean removeAll(Collection<?> arg0) {
		return model.removeAll(arg0);
	}

	public void removeSetModelChangeListener(
			SetModelChangeListener<LocalWorkerActivityItem> listener) {
		model.removeSetModelChangeListener(listener);
	}

	public boolean retainAll(Collection<?> arg0) {
		return model.retainAll(arg0);
	}

	public int size() {
		return model.size();
	}

	public Object[] toArray() {
		return model.toArray();
	}

	public <T> T[] toArray(T[] arg0) {
		return model.toArray(arg0);
	}

	public String toString() {
		return model.toString();
	}

	public void doQuery() {
		add(new LocalWorkerActivityItem("LocalWorker","split_string_by_regex","text"));
		add(new LocalWorkerActivityItem("LocalWorker","get_web_page","net"));
		add(new LocalWorkerActivityItem("LocalWorker","get_file_from_ftp","net"));
		add(new LocalWorkerActivityItem("LocalWorker","fetch_genes","genes"));
	}

	public Date getLastQueryTime() {
		// TODO Auto-generated method stub
		return null;
	}
}
