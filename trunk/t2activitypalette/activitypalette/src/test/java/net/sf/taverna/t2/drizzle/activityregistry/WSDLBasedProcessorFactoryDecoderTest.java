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

import net.sf.taverna.t2.drizzle.decoder.processorfactory.WSDLBasedProcessorFactoryDecoder;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.query.DecodeRunIdentification;
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
	private String TESTWSDL_BASE="http://www.mygrid.org.uk/taverna-tests/testwsdls/"; //$NON-NLS-1$
	private PropertiedObjectSet<ProcessorFactoryAdapter> targetSet;
	private WSDLBasedProcessorFactoryDecoder testDecoder;
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
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		Class<PropertiedObjectSet> c = PropertiedObjectSet.class;
		this.targetSet = ObjectFactory.getInstance(c);
		assertNotNull(this.targetSet);
		this.testDecoder = new WSDLBasedProcessorFactoryDecoder();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// nothing to do
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.WSDLBasedProcessorFactoryDecoder#canDecode(java.lang.Object)}.
	 */
	@Test
	public void testCanDecode() {
		assertTrue (this.testDecoder.canDecode(WSDLBasedProcessorFactory.class, WSDLBasedProcessorFactory.class));
		assertTrue (this.testDecoder.canDecode(WSDLBasedProcessorFactory.class, ProcessorFactory.class));
		assertFalse(this.testDecoder.canDecode(BiomobyProcessorFactory.class, WSDLBasedProcessorFactory.class));
		assertFalse (this.testDecoder.canDecode(WSDLBasedProcessorFactory.class, String.class));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.activityregistry.WSDLBasedProcessorFactoryDecoder#decode(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)}.
	 * @throws ScavengerCreationException 
	 */
	@Test
	public void testDecode() throws ScavengerCreationException {
		WSDLBasedScavenger scavenger = new WSDLBasedScavenger(
				this.TESTWSDL_BASE+"GUIDGenerator.wsdl"); //$NON-NLS-1$
		DefaultMutableTreeNode leaf = scavenger.getFirstLeaf();
		Object userObject = leaf.getUserObject();
		assertTrue(userObject instanceof WSDLBasedProcessorFactory);
		DecodeRunIdentification<ProcessorFactoryAdapter> ident = this.testDecoder.decode(this.targetSet, (WSDLBasedProcessorFactory) leaf.getUserObject());
		Set<ProcessorFactoryAdapter> factories = ident.getAffectedObjects();
		Set<ProcessorFactoryAdapter> objects = this.targetSet.getObjects();
		int i = 1;
		assertEquals(new Integer(i), new Integer(objects.size()));
		ProcessorFactoryAdapter adapter = objects.iterator().next();
		ProcessorFactory factory = adapter.getTheFactory();
		assertEquals(new Integer(1), new Integer(factories.size()));
		assertTrue(factories.contains(factory));
		assertEquals(userObject, factory);
		assertNotNull(this.targetSet.getPropertyValue(adapter, CommonKey.ProcessorClassKey));
		assertNotNull(this.targetSet.getPropertyValue(adapter, CommonKey.WsdlLocationKey));
		assertNotNull(this.targetSet.getPropertyValue(adapter, CommonKey.WsdlOperationKey));
		assertNotNull(this.targetSet.getPropertyValue(adapter, CommonKey.WsdlPortTypeKey));
		
	}

}
