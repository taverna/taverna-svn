package org.embl.ebi.escience.scufl;

import java.util.Properties;

import junit.framework.TestCase;

public class PortTest extends TestCase {
	class DummyPort extends Port {
		public DummyPort(Processor processor, String name) throws DuplicatePortNameException, PortCreationException {
			super(processor, name);			
		}		
				
	}
	class EditablePort extends DummyPort {
		public EditablePort(Processor processor, String name) throws DuplicatePortNameException, PortCreationException {
			super(processor, name);			
		}

		public boolean isNameEditable() {
			return true;
		}
	}
	
	class DummyProcessor extends Processor {
		public DummyProcessor(ScuflModel model, String name) throws ProcessorCreationException, DuplicateProcessorNameException {			
			super(model, name);			
		}
		public Properties getProperties() {
			return new Properties();
		}			
	}
	ScuflModel model;
	Processor proc;
	
	
	protected void setUp() throws ProcessorCreationException, DuplicateProcessorNameException {
		model = new ScuflModel();
		proc  = new DummyProcessor(model, "proc");
	}
	
	
	public void testCreation() throws DuplicatePortNameException, PortCreationException {				
		new DummyPort(proc, "sillyport");		
	}	
	
	public void testCreationNullProc() throws Exception {
		try {
			new DummyPort(null, "sillyport");
		} catch (PortCreationException e) {
			// expected
			return;
		}
		throw new Exception("Should not allow null processor");
	}
	public void testCreationNullName() throws Exception {
		try {
			new DummyPort(proc, null);
		} catch (PortCreationException e) {
			// expected
			return;
		}
		throw new Exception("Should not allow null name");	
	}
	public void testCreationWeirdName() throws Exception {
		// We have to allow weird names because of Soaplab
		String weirdName = "p-sillyport with spaces and !! 1337";
		Port port = new DummyPort(proc, weirdName);
		assertEquals(weirdName, port.getName());
	}
	public void testCreationIllegalName() throws Exception {
		try {
			new DummyPort(proc, "p:sillyport");
		} catch (PortCreationException e) {
			// expected
			return;
		}
		throw new Exception("Should not allow : in name");
	}
	
	public void testCreationDuplicate() throws Exception {		
		proc.addPort(new DummyPort(proc, "sillyport"));
		try {
			// Should ignore case
			new DummyPort(proc, "SillyPort");
		} catch (DuplicatePortNameException e) {
			// expected
			return;
		}
		throw new Exception("Should not allow duplicate port names");
	}
	public void testCreationDuplicateOtherType() throws Exception {
		// Duplicates are OK if they are different classes.
		// (But what if they are subclassing each other?)
		proc.addPort(new InputPort(proc, "sillyport"));		
		new DummyPort(proc, "sillyport");
	}
	
	public void testNameUneditable() throws DuplicatePortNameException, PortCreationException {		
		Port port = new DummyPort(proc, "sillyport");
		port.setName("somethingelse");
		assertEquals("sillyport", port.getName());		
	}
	
	public void testNameEdit() throws DuplicatePortNameException, PortCreationException {		
		Port port = new EditablePort(proc, "sillyport");
		assertEquals("sillyport", port.getName());
		port.setName("Something illegal with spaces");
		assertEquals("sillyport", port.getName());
		port.setName("somethingelse");
		assertEquals("somethingelse", port.getName());		
	}
	
	public void testToString() throws DuplicatePortNameException, PortCreationException {
		Object port = new DummyPort(proc, "sillyport");
		assertEquals("sillyport", port.toString()); 
	}
		
}
