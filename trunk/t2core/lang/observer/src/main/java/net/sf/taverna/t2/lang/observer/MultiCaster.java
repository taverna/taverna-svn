package net.sf.taverna.t2.lang.observer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MultiCaster<Message> implements Observable<Message> {

	private static Logger logger = Logger.getLogger(MultiCaster.class);

	private Observable<Message> observable;

	protected List<Observer<Message>> observers = new ArrayList<Observer<Message>>();

	public MultiCaster(Observable<Message> observable) {
		this.observable = observable;
	}

	@SuppressWarnings("unchecked")
	public void notify(Message message) {
		// Use a copy that can be iterated even if register/remove is called
		for (Observer<Message> observer : getObservers()) {
			try {
				observer.notify(observable, message);
			} catch (Exception ex) {
				logger.warn("Could not notify " + observer, ex);
			}
		}
	}

	public synchronized void addObserver(Observer<Message> observer) {
		observers.add(observer);
	}

	public synchronized void removeObserver(Observer<Message> observer) {
		observers.remove(observer);
	}

	public synchronized List<Observer<Message>> getObservers() {
		return new ArrayList<Observer<Message>>(observers);
	}

}
