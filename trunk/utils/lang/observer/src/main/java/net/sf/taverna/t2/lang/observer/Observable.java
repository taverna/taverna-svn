package net.sf.taverna.t2.lang.observer;

import java.util.List;

/**
 * Implements this if you want to notify other classes about changes
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <Message>
 */
public interface Observable<Message> {
	/**
	 * Register an {@link Observer}
	 * 
	 * @param observer
	 *            the class who wants notified of changes
	 */
	public void addObserver(Observer<Message> observer);

	/**
	 * Remove a class who is currently observing
	 * 
	 * @param observer
	 *            the class who no longer wants notified
	 */
	public void removeObserver(Observer<Message> observer);

	/**
	 * A list of all the currently registered {@link Observer}s
	 * 
	 * @return
	 */
	public List<Observer<Message>> getObservers();
}
