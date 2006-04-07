package org.embl.ebi.escience.scuflworkers.rserv;

import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;

public class RservProcessorTest extends TestCase {
    RservProcessor processor;

    protected void setUp() throws ProcessorCreationException,
            DuplicateProcessorNameException {
        this.processor = new RservProcessor(null, "Rserv");
    }

    public void testCreation() {
        assertNotNull(processor);
    }

    public void testDuplicate() throws ProcessorCreationException, DuplicateProcessorNameException {
        ScuflModel model = new ScuflModel();        
        model.addProcessor(new RservProcessor(model, "Duplicate"));
        try {
            new RservProcessor(model, "Duplicate");
            fail("Should have raised DuplicateProcessorNameException for 2x'Rserv'");
        } catch (DuplicateProcessorNameException e) {            
        }
        
    }

    public void testDefaultValues() {
        assertEquals(processor.getScript(), "");
        assertEquals(processor.getHostname(), "");
        assertEquals(processor.getPort(), 0);
        assertEquals(processor.getPassword(), "");
        assertEquals(processor.getUsername(), "");
        OutputPort[] outports = processor.getOutputPorts();
        System.out.println(outports);
        assertEquals(outports.length, 1);
        assertEquals(outports[0].getName(), "value");
        assertEquals(outports[0].getSyntacticType(), "l(text/plain)");
    }

    public void testSetValues() {
        processor.setHostname("some.host");
        assertEquals(processor.getHostname(), "some.host");
        processor.setPort(1337);
        assertEquals(processor.getPort(), 1337);
        processor.setUsername("joe");
        assertEquals(processor.getUsername(), "joe");
        processor.setPassword("s3cret");
        assertEquals(processor.getPassword(), "s3cret");        
    }

    public void testSetWrongValues() {
        
        try {
            // cannot be negative
            processor.setPort(-1);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {            
        }
        assertEquals(processor.getPort(), 0);        
        try {
            // cannot be >65535
            processor.setPort(65536);        
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {            
        }
        assertEquals(processor.getPort(), 0);
        
        // Assert null -> ""
        processor.setHostname("myserver");
        processor.setHostname(null);
        assertEquals(processor.getHostname(), "");        
        
        processor.setUsername("jloser");
        processor.setUsername(null);
        assertEquals(processor.getUsername(), "");
        
        processor.setPassword("fish");
        processor.setPassword(null);
        assertEquals(processor.getPassword(), "");
    }

}
