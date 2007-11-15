/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import static org.junit.Assert.*;

import java.util.List;

import javax.xml.namespace.QName;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class PropertyDecoderRegistryTest {

	private PropertyDecoderRegistry testRegistry = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoderRegistry#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		testRegistry = PropertyDecoderRegistry.getInstance();
		assertNotNull(testRegistry);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoderRegistry#getDecoders(java.lang.Object)}.
	 */
	@Test
	public void testGetDecoders() {
		List<PropertyDecoder<WSDLBasedProcessorFactory,ProcessorFactory>> decoders = PropertyDecoderRegistry.getDecoders(WSDLBasedProcessorFactory.class, ProcessorFactory.class);
		assertFalse (decoders.size() == 0);
		PropertyDecoder decoder = decoders.get(0);
		assertTrue (decoder instanceof WSDLBasedProcessorFactoryDecoder);
	}
	
	@Test
	public void testGetDecoder() {
		PropertyDecoder<WSDLBasedProcessorFactory, ProcessorFactory> decoder = PropertyDecoderRegistry.getDecoder(WSDLBasedProcessorFactory.class, ProcessorFactory.class);
		assertNotNull(decoder);
		
		WSDLBasedProcessorFactory factory = new WSDLBasedProcessorFactory("", "", new QName(""));
		Object o = factory;
		PropertyDecoder<?, ProcessorFactory> objectDecoder = PropertyDecoderRegistry.getDecoder(o.getClass(), ProcessorFactory.class);
		assertNotNull(objectDecoder);
	}

}
