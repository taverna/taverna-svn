/**
 * Implementation of the observer pattern.  {@link Observer}s registers with an 
 * {@link Observable} using {@link Observable#addObserver(Observer)}, and will receive 
 * notifications as a call to {@link Observer#notify(Observable, Object)}. 
 * <p>
 * Typical implementations of {@link Observable} will be delegating to a 
 * {@link MultiCaster} to do the boring observer registration and message 
 * dispatching.
 * </p>
 * <p>
 * Example of Observable:
 * <pre>
 * public class MyObservable implements Observable<MyEvent> {
 * 	 public static class MyEvent {
 * 		// ..
 * 	 }
 * 	 private MultiCaster&lt:MyEvent&gt; multiCaster = new MultiCaster&lt:MyEvent&gt;(this);
 * 
 *	 public void doStuff() {
 *		multiCaster.notify(new MyEvent());
 *	 }
 * 
 * 	 public void addObserver(Observer<MyEvent> observer) {
 * 		multiCaster.addObserver(observer);
 * 	 }
 * 
 * 	 public List<Observer<MyEvent>> getObservers() {
 * 		return multiCaster.getObservers();
 * 	 }
 * 
 * 	 public void removeObserver(Observer<MyEvent> observer) {
 * 		multiCaster.removeObserver(observer);
 * 	 }
 * }
 * </pre>
 * And an observer that is notified when MyObservable.doStuff() is called:
 * <pre>
 * public class MyObserver implements Observer<MyEvent> {
 *	 public void notify(Observable<MyEvent> sender, MyEvent message) {
 *		System.out.println("Receieved " + message + " from " + sender);
 * 	 }
 * }
 * </pre>
 * Example of usage:
 * <pre>
 * 		MyObservable observable = new MyObservable();
 *		MyObserver observer = new MyObserver();
 *		observable.addObserver(observer);
 *		observable.doStuff();
 *	</pre>
 */
package net.sf.taverna.t2.lang.observer;

