package net.sf.taverna.t2.lang.observer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MultiCaster<Message> implements Observable<Message> {

	private static Logger logger = Logger.getLogger(MultiCaster.class);

	private Observable<Message> observable;

	List<Observer<Message>> observers = new ArrayList<Observer<Message>>();

	public MultiCaster(Observable<Message> observable) {
		this.observable = observable;
	}

	@SuppressWarnings("unchecked")
	public void notify(Message message) {
		// Make a copy that can be iterated even if register/remove is called
		Observer<Message>[] observersCopy = observers.toArray(new Observer[0]);
		for (Observer<Message> observer : observersCopy) {
			try {
				observer.notify(observable, message);
			} catch (Exception ex) {
				logger.warn("Could not notify " + observer, ex);
			}
		}
	}

	public void registerObserver(Observer<Message> observer) {
		observers.add(observer);
	}

	public void removeObserver(Observer<Message> observer) {
		observers.remove(observer);
	}

}
