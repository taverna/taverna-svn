package net.sf.taverna.t2.cloudone.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestAsynchRunnable {

	private static final String SLEEP_STRING = "The string that slept 1000 ms";

	@Test
	public void callAsync() throws Exception {
		final Object hasStarted = new Object();
		AbstractAsynchRunnable<String> runnable = new AbstractAsynchRunnable<String>() {

			@Override
			protected String execute() throws Exception {
				synchronized (hasStarted) {
					hasStarted.notify();
				}
				Thread.sleep(1000);
				return SLEEP_STRING;
			}

		};
		Thread t = new Thread(runnable);
		synchronized (hasStarted) {
			t.start();
			hasStarted.wait(1200);
		}
		assertFalse(runnable.isFinished());
		t.join(1200);
		assertTrue(runnable.isFinished());
		assertNull(runnable.getException());
		assertEquals(SLEEP_STRING, runnable.getResult());
	}

	@Test(expected=IllegalStateException.class)
	public void failOnPrematureGetResult() throws Exception {
		final Object hasStarted = new Object();
		AbstractAsynchRunnable<String> runnable = new AbstractAsynchRunnable<String>() {

			@Override
			protected String execute() throws Exception {
				synchronized (hasStarted) {
					hasStarted.notify();
				}
				Thread.sleep(1000);
				return SLEEP_STRING;
			}

		};
		Thread t = new Thread(runnable);
		synchronized (hasStarted) {
			t.start();
			hasStarted.wait(1200);
		}
		assertFalse(runnable.isFinished());
		runnable.getResult();
	}
	
	@Test(expected=IllegalStateException.class)
	public void failOnPrematureGetException() throws Exception {
		final Object hasStarted = new Object();
		AbstractAsynchRunnable<String> runnable = new AbstractAsynchRunnable<String>() {

			@Override
			protected String execute() throws Exception {
				synchronized (hasStarted) {
					hasStarted.notify();
				}
				Thread.sleep(1000);
				return SLEEP_STRING;
			}

		};
		Thread t = new Thread(runnable);
		synchronized (hasStarted) {
			t.start();
			hasStarted.wait(1200);
		}
		assertFalse(runnable.isFinished());
		runnable.getException();
	}
	
	@Test
	public void getException() throws Exception {
		AbstractAsynchRunnable<String> runnable = new AbstractAsynchRunnable<String>() {

			@Override
			protected String execute() throws Exception {
				throw new NullPointerException();
			}

		};
		Thread t = new Thread(runnable);
		t.start();
		t.join(1200);
		assertTrue(runnable.isFinished());
		assertTrue(runnable.getException() instanceof NullPointerException);
	}
	
	public void gailOnGetResultAfterException() throws Exception {
		AbstractAsynchRunnable<String> runnable = new AbstractAsynchRunnable<String>() {

			@Override
			protected String execute() throws Exception {
				throw new NullPointerException();
			}

		};
		Thread t = new Thread(runnable);
		t.start();
		t.join(1200);
		assertTrue(runnable.isFinished());
		try { 
			runnable.getResult();
			fail("Did not throw IllegalStateException");
		} catch (IllegalStateException ex) { 
			assertTrue(ex.getCause() instanceof NullPointerException);
		}	
	}
	
}
