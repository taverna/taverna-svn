/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import static org.junit.Assert.*;

import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.namespace.QName;

import net.sf.taverna.t2.drizzle.util.PropertiedObject;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.impl.PropertiedObjectSetImpl;

import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedScavenger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class WsdlProcessorFactoryDecoderTest {
	private String TESTWSDL_BASE="http://www.mygrid.org.uk/taverna-tests/testwsdls/";
	private PropertiedObjectSet<ProcessorFactory> targetSet;
	private WsdlProcessorFactoryDecoder testDecoder;
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
		targetSet = new PropertiedObjectSetImpl<ProcessorFactory> ();
		testDecoder = new WsdlProcessorFactoryDecoder();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.WsdlProcessorFactoryDecoder#canDecode(java.lang.Object)}.
	 */
	@Test
	public void testCanDecode() {
		assertTrue (testDecoder.canDecode(new WSDLBasedProcessorFactory("", "", new QName(""))));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.WsdlProcessorFactoryDecoder#decode(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)}.
	 * @throws ScavengerCreationException 
	 */
	@Test
	public void testDecode() throws ScavengerCreationException {
		WSDLBasedScavenger scavenger = new WSDLBasedScavenger(
				TESTWSDL_BASE+"GUIDGenerator.wsdl");
		Set<ProcessorFactory> factories;
		
		DefaultMutableTreeNode leaf = scavenger.getFirstLeaf();
		factories = testDecoder.decode(targetSet, leaf.getUserObject());
		Set<ProcessorFactory> objects = targetSet.getObjects();
		assertEquals(1, objects.size());
		ProcessorFactory factory = objects.iterator().next();
		PropertiedObject<ProcessorFactory> po = targetSet.getPropertiedObject(factory);
		assertNotNull(po);
		assertNotNull(po.getPropertyValue(CommonKey.ProcessorClassKey));
		assertNotNull(po.getPropertyValue(CommonKey.WsdlLocationKey));
		assertNotNull(po.getPropertyValue(CommonKey.WsdlOperationKey));
		assertNotNull(po.getPropertyValue(CommonKey.WsdlPortTypeKey));
		
	}

}
