package net.sf.taverna.t2.lang.observer;

public interface Observer<Message> {

	public void notify(Observable<Message> sender, Message message);
}
