package net.sf.taverna.t2.lang.observer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Send notifications to registered observers about changes to models
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <Message>
 */
public class MultiCaster<Message> implements Observable<Message> {

	private static Logger logger = Logger.getLogger(MultiCaster.class);

	private Observable<Message> observable;

	protected List<Observer<Message>> observers = new ArrayList<Observer<Message>>();

	/**
	 * Set the {@link #observable} ie. the class that changes are happening to
	 * and it's Message for this {@link MultiCaster}
	 * 
	 * @param observable
	 */
	public MultiCaster(Observable<Message> observable) {
		this.observable = observable;
	}

	/**
	 * Tell all the registered observers about the change to the model
	 * 
	 * @param message
	 */
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

	/**
	 * Register an observer ie. someone who wants informed about changes
	 */
	public synchronized void addObserver(Observer<Message> observer) {
		observers.add(observer);
	}

	/**
	 * Remove the observer and no longer send out any notifications about it
	 */
	public synchronized void removeObserver(Observer<Message> observer) {
		observers.remove(observer);
	}

	/**
	 * A list of all the classes currently registered with this
	 * {@link MultiCaster}
	 */
	public synchronized List<Observer<Message>> getObservers() {
		return new ArrayList<Observer<Message>>(observers);
	}

}
