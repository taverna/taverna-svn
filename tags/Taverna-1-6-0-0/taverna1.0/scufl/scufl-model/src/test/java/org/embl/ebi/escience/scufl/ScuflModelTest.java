package org.embl.ebi.escience.scufl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

public class ScuflModelTest extends TestCase {
	private int original_sleep;
	
	public void setUp() {
		// Keep the static sleep variable as before we mess it up
		original_sleep = NotifyThread.max_sleep;
	}
	
	public void tearDown() {
		NotifyThread.max_sleep = original_sleep; 		
	}

	public void testConstruction() {
		ScuflModel model = new ScuflModel();
		assertFalse(model.offline);
		ArrayAssert.assertEquals(new Processor[0], model.getProcessors());
		ArrayAssert.assertEquals(new DataConstraint[0], model
				.getDataConstraints());
		ArrayAssert.assertEquals(new ConcurrencyConstraint[0], model
				.getConcurrencyConstraints());
		ArrayAssert.assertEquals(new ScuflModelEventListener[0], model
				.getListeners());
		ArrayAssert.assertEquals(new Port[0], model.getWorkflowSinkPorts());
		ArrayAssert.assertEquals(new Port[0], model.getWorkflowSourcePorts());
		assertNotNull(model.getDescription());
		// NOTE: the log level does not seem to be used for anything
		assertEquals(0, model.getLogLevel());
	}

	@SuppressWarnings("serial")
	public void testNameStuff() throws ProcessorCreationException,
			DuplicateProcessorNameException, UnknownProcessorException,
			UnknownPortException, MalformedNameException, SetOnlineException {
		ScuflModel model = new ScuflModel();
		String valid_name = model.getValidProcessorName("Fish: and-$problems");
		assertEquals("Fish__and__problems", valid_name);
		model.addProcessor(new Processor(model, valid_name) {
			public Properties getProperties() {
				return null;
			}
		});
		assertEquals(valid_name, model.getProcessors()[0].getName());
		assertEquals(1, model.getProcessorsOfType(Processor.class).length);
		assertEquals(valid_name, model.locateProcessor(valid_name).getName());

		// Try a duplicate (with a fancy name)
		valid_name = model.getValidProcessorName("Fish: and-$problems");
		assertEquals("Fish__and__problems1", valid_name);
		model.addProcessor(new Processor(model, valid_name) {
			public Properties getProperties() {
				return null;
			}
		});
		assertEquals(valid_name, model.getProcessors()[1].getName());

		try {
			model.locateProcessor("DoesNotExist");
			fail("Didn't throw UnknownProcessorException for 'DoesNotExist'");
		} catch (UnknownProcessorException ex) {
			// Expected
		}

		try {
			model.locatePort("invalid:port:spec");
			fail("Didn't throw MalformedNameException for 'invalid:port:spec'");
		} catch (MalformedNameException ex) {
			// Expected
		}

		try {
			model.locatePort("invalidportspec");
			fail("Didn't throw UnknownPortException for 'invalidportspec'");
		} catch (UnknownPortException ex) {
			// Expected
		}

		try {
			model.locatePort("not:there");
			fail("Didn't throw UnknownProcessorException for 'not:there'");
		} catch (UnknownProcessorException ex) {
			// Expected
		}
		try {
			model.locatePort(valid_name + ":notthere");
			fail("Didn't throw UnknownPortException for '" + valid_name
					+ ":notthere'");
		} catch (UnknownPortException ex) {
			// Expected
		}

		// Should create the port, which should then be findable
		String port_name = valid_name + ":input1";
		model.setOffline(true);
		model.locatePortOrCreate(port_name, true);
		assertEquals("input1", model.locatePort(port_name).getName());
	}

	String getEvent(List<ScuflModelEvent> events, NotifyThread notifier) {
		// Poke the notifier (again!) so it can process some events and we don't have to wait
		notifier.interrupt();
		ScuflModelEvent event;
		synchronized (events) {
			try {
				events.wait(1000);
			} catch (InterruptedException e) {
				// OK
			}
			if (events.size() == 0) {
				fail("Did not get any event");
			}
			assertEquals("Too many events", 1, events.size());
			event = events.get(0);
			events.remove(0);						
		}
		return event.getMessage();
	}
	
	public void testNotifyThread() {
		ScuflModel model = new ScuflModel();
		assertNull("notify thread should be null",model.notifyThread);
		ScuflModelEventListener listener = new ScuflModelEventListener() {
			public void receiveModelEvent(ScuflModelEvent event) {
			
			}
		};
		model.addListener(listener);
		assertNotNull("notify thread should not be null",model.notifyThread);
		model.removeListener(listener);
		assertNull("notify thread should be null",model.notifyThread);
	}

	@SuppressWarnings("serial")
	public void testEventListening() throws Throwable {
		// Very lower timeout for speeding up testing, we'll set it before any thread is started
		NotifyThread.max_sleep = 20;
				
		final List<ScuflModelEvent> events = new ArrayList<ScuflModelEvent>();		
				
		ScuflModel model = new ScuflModel();		
		model.addListener(new ScuflModelEventListener() {
			public void receiveModelEvent(ScuflModelEvent event) {
				synchronized (events) {
					events.add(event);
					events.notifyAll();
				}
			}
		});
		model.forceUpdate();
		assertEquals("Forced update", getEvent(events, model.notifyThread));	
		model.setOffline(true);
		assertEquals("Offline status change", getEvent(events, model.notifyThread));
		model.addProcessor(new Processor(model, "proc"){			
			public Properties getProperties() {				
				return null;
			}});
	
		assertTrue(getEvent(events, model.notifyThread).startsWith("Added ScuflModelTest"));
				
		NotifyThread notifyThread = model.notifyThread;
		assertTrue(notifyThread.loop);		
		model.clear();
		assertEquals("Reset model to initial state.", getEvent(events, model.notifyThread));
		// Check we didn't replace the thread
		assertSame(notifyThread, model.notifyThread);
		assertTrue(notifyThread.loop);		
		assertTrue(notifyThread.isAlive());
		
		// Check that ScuflModel destructor stops the thread
		notifyThread = model.notifyThread;
		// Pretend we are running GC enforced destructor 
		// (model=null;System.GC(); only works in 80% of cases)
		model.removeListeners();
		notifyThread.join((long)(NotifyThread.max_sleep*1.1));
		assertFalse(notifyThread.loop);
		assertFalse(notifyThread.isAlive());
	}
}
