package net.sf.taverna.t2.partition;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public abstract class ActivityQuery implements Query<ActivityItem> {

	HashSetModel<ActivityItem> model = new HashSetModel<ActivityItem>();
	Date lastQueryTime = new Date(0); //defaults to 1970
	String property;
	

	public ActivityQuery(String property) {
		super();
		this.property = property;
	}

	public boolean add(ActivityItem item) {
		return model.add(item);
	}

	public boolean addAll(Collection<? extends ActivityItem> arg0) {
		return model.addAll(arg0);
	}

	public void addSetModelChangeListener(
			SetModelChangeListener<ActivityItem> listener) {
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

	public Iterator<ActivityItem> iterator() {
		return model.iterator();
	}

	public boolean remove(Object item) {
		return model.remove(item);
	}

	public boolean removeAll(Collection<?> arg0) {
		return model.removeAll(arg0);
	}

	public void removeSetModelChangeListener(
			SetModelChangeListener<ActivityItem> listener) {
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

	public abstract void doQuery();

	public Date getLastQueryTime() {
		return lastQueryTime;
	}

	protected String getProperty() {
		return property;
	}

}
