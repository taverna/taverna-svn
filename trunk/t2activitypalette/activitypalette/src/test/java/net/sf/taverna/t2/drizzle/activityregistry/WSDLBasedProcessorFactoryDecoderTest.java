/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessorFactory;
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
public class WSDLBasedProcessorFactoryDecoderTest {
	private String TESTWSDL_BASE="http://www.mygrid.org.uk/taverna-tests/testwsdls/";
	private PropertiedObjectSet<ProcessorFactory> targetSet;
	private WSDLBasedProcessorFactoryDecoder testDecoder;
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
		Class<PropertiedObjectSet> c = PropertiedObjectSet.class;
		targetSet = ObjectFactory.getInstance(c);
		assertNotNull(targetSet);
		testDecoder = new WSDLBasedProcessorFactoryDecoder();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.WSDLBasedProcessorFactoryDecoder#canDecode(java.lang.Object)}.
	 */
	@Test
	public void testCanDecode() {
		assertTrue (testDecoder.canDecode(WSDLBasedProcessorFactory.class, WSDLBasedProcessorFactory.class));
		assertTrue (testDecoder.canDecode(WSDLBasedProcessorFactory.class, ProcessorFactory.class));
		assertFalse(testDecoder.canDecode(BiomobyProcessorFactory.class, WSDLBasedProcessorFactory.class));
		assertFalse (testDecoder.canDecode(WSDLBasedProcessorFactory.class, String.class));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.WSDLBasedProcessorFactoryDecoder#decode(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)}.
	 * @throws ScavengerCreationException 
	 */
	@Test
	public void testDecode() throws ScavengerCreationException {
		WSDLBasedScavenger scavenger = new WSDLBasedScavenger(
				TESTWSDL_BASE+"GUIDGenerator.wsdl");
		DefaultMutableTreeNode leaf = scavenger.getFirstLeaf();
		Object userObject = leaf.getUserObject();
		assertTrue(userObject instanceof WSDLBasedProcessorFactory);
		DecodeRunIdentification<ProcessorFactory> ident = testDecoder.decode(targetSet, (WSDLBasedProcessorFactory) leaf.getUserObject());
		Set<ProcessorFactory> factories = ident.getAffectedObjects();
		Set<ProcessorFactory> objects = targetSet.getObjects();
		assertEquals(1, objects.size());
		ProcessorFactory factory = objects.iterator().next();
		assertEquals(1, factories.size());
		assertTrue(factories.contains(factory));
		assertEquals(userObject, factory);
		assertNotNull(targetSet.getPropertyValue(factory, CommonKey.ProcessorClassKey));
		assertNotNull(targetSet.getPropertyValue(factory, CommonKey.WsdlLocationKey));
		assertNotNull(targetSet.getPropertyValue(factory, CommonKey.WsdlOperationKey));
		assertNotNull(targetSet.getPropertyValue(factory, CommonKey.WsdlPortTypeKey));
		
	}

}
