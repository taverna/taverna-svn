package net.sf.taverna.t2.lang.observer;

import static org.junit.Assert.*;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.junit.Test;

public class ObserverTest {

	@Test
	public void registerObserver() throws Exception {
		MyObservable observable = new MyObservable();
		MyObserver observer1 = new MyObserver();
		MyObserver observer2 = new MyObserver();

		observable.triggerEvent(); // don't notify, but increase count
		assertNull(observer1.lastMessage);
		observable.registerObserver(observer1);
		assertNull(observer1.lastMessage);
		assertNull(observer2.lastMessage);
		assertNull(observer1.lastSender);
		assertNull(observer2.lastSender);
		observable.triggerEvent();
		assertEquals("This is message 1", observer1.lastMessage);
		assertSame(observable, observer1.lastSender);
		assertNull(observer2.lastSender);

		observable.registerObserver(observer2);
		assertNull(observer2.lastMessage);
		observable.triggerEvent();
		assertEquals("This is message 2", observer1.lastMessage);
		assertEquals("This is message 2", observer2.lastMessage);
		assertSame(observable, observer1.lastSender);
		assertSame(observable, observer2.lastSender);

		MyObservable otherObservable = new MyObservable();
		otherObservable.registerObserver(observer2);
		otherObservable.triggerEvent();
		// New instance, should start from 0
		assertEquals("This is message 0", observer2.lastMessage);
		assertSame(otherObservable, observer2.lastSender);

		// observer1 unchanged
		assertEquals("This is message 2", observer1.lastMessage);
		assertSame(observable, observer1.lastSender);

	}

	public class MyObservable implements Observable<String> {

		private int counter = 0;
		MultiCaster<String> multiCaster = new MultiCaster<String>(this);

		public void registerObserver(Observer<String> observer) {
			multiCaster.registerObserver(observer);
		}

		public void removeObserver(Observer<String> observer) {
			multiCaster.removeObserver(observer);
		}

		public void triggerEvent() {
			multiCaster.notify("This is message " + counter++);
		}
	}

	public class MyObserver implements Observer<String> {
		String lastMessage = null;
		Observable<String> lastSender = null;

		public void notify(Observable<String> sender, String message) {
			lastSender = sender;
			lastMessage = message;
		}
	}

}
