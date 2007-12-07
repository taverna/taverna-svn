package net.sf.taverna.t2.lang.observer;

import java.util.List;

public interface Observable<Message> {
	public void addObserver(Observer<Message> observer);

	public void removeObserver(Observer<Message> observer);

	public List<Observer<Message>> getObservers();
}
