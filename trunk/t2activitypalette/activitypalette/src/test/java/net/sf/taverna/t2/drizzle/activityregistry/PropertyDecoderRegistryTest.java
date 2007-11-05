/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import static org.junit.Assert.*;

import java.util.List;

import javax.xml.namespace.QName;

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
		WSDLBasedProcessorFactory testFactory = new WSDLBasedProcessorFactory("", "", new QName(""));
		List<PropertyDecoder> decoders = PropertyDecoderRegistry.getInstance().getDecoders(testFactory);
		assertFalse (decoders.size() == 0);
		PropertyDecoder decoder = decoders.get(0);
		assertTrue (decoder instanceof WsdlProcessorFactoryDecoder);
	}

}
