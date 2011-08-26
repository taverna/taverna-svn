package net.sf.taverna.t2.activities.soaplab.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.taverna.t2.partition.QueryFactory;
import net.sf.taverna.t2.partition.QueryFactoryRegistry;

import org.junit.Test;

public class SoaplabQueryFactoryTest {

	@Test
	public void testSPI() {
		List<QueryFactory> instances = QueryFactoryRegistry.getInstance().getInstances();
		assertTrue("There should be more than one instance found",instances.size()>0);
		boolean found = false;
		for (QueryFactory spi : instances) {
			if (spi instanceof SoaplabQueryFactory) {
				found=true;
				break;
			}
		}
		assertTrue("A WSDLQueryFactory should have been found",found);
	}
	
	@Test
	public void testKey() {
		SoaplabQueryFactory f = new SoaplabQueryFactory();
		assertEquals("taverna.defaultsoaplab",f.getPropertyKey());
	}
}
