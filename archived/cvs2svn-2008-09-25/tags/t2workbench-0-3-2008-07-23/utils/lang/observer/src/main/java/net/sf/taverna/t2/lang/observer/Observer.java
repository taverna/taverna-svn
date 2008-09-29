package net.sf.taverna.t2.lang.observer;

/**
 * Implement if you want to register with an {@link Observable}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <Message>
 */
public interface Observer<Message> {
	/**
	 * Called by the {@link Observable} to notify the implementing class of
	 * changes
	 * 
	 * @param sender
	 *            the class where the changes have happened
	 * @param message
	 *            what has changed
	 * @throws Exception
	 */
	public void notify(Observable<Message> sender, Message message)
			throws Exception;
}
