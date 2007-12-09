/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import static org.junit.Assert.*;

import java.util.List;

import javax.xml.namespace.QName;

import net.sf.taverna.t2.drizzle.decoder.PropertyDecoder;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoderRegistry;
import net.sf.taverna.t2.drizzle.decoder.processorfactory.WSDLBasedProcessorFactoryDecoder;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;

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
		// nothing to do
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// nothing to do
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// nothing to do
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// nothing to do
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoderRegistry#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		this.testRegistry = PropertyDecoderRegistry.getInstance();
		assertNotNull(this.testRegistry);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoderRegistry#getDecoders(java.lang.Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetDecoders() {
		List<PropertyDecoder<WSDLBasedProcessorFactory,ProcessorFactoryAdapter>> decoders = PropertyDecoderRegistry.getDecoders(WSDLBasedProcessorFactory.class, ProcessorFactoryAdapter.class);
		assertFalse (decoders.size() == 0);
		PropertyDecoder decoder = decoders.get(0);
		assertTrue (decoder instanceof WSDLBasedProcessorFactoryDecoder);
	}
	
	@Test
	public void testGetDecoder() {
		PropertyDecoder<WSDLBasedProcessorFactory, ProcessorFactoryAdapter> decoder = PropertyDecoderRegistry.getDecoder(WSDLBasedProcessorFactory.class, ProcessorFactoryAdapter.class);
		assertNotNull(decoder);
		
		WSDLBasedProcessorFactory factory = new WSDLBasedProcessorFactory("", "", new QName("")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Object o = factory;
		PropertyDecoder<?, ProcessorFactoryAdapter> objectDecoder = PropertyDecoderRegistry.getDecoder(o.getClass(), ProcessorFactoryAdapter.class);
		assertNotNull(objectDecoder);
	}

}
