package net.sf.taverna.t2.lang.observer;

public interface Observable<Message> {
	public void registerObserver(Observer<Message> observer);

	public void removeObserver(Observer<Message> observer);
}
