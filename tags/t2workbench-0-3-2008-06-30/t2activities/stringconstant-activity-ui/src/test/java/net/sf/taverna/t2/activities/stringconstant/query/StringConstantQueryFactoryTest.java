package net.sf.taverna.t2.activities.stringconstant.query;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.taverna.t2.partition.QueryFactory;
import net.sf.taverna.t2.partition.QueryFactoryRegistry;

import org.junit.Test;

public class StringConstantQueryFactoryTest {

	@Test
	public void testSPI() {
		List<QueryFactory> instances = QueryFactoryRegistry.getInstance().getInstances();
		assertTrue("There should be more than one instance found",instances.size()>0);
		boolean found = false;
		for (QueryFactory spi : instances) {
			if (spi instanceof StringConstantQueryFactory) {
				found=true;
				break;
			}
		}
		assertTrue("A StringConstantQueryFactory should have been found",found);
	}
	
	@Test
	public void testCreateQuery() {
		StringConstantQueryFactory f = new StringConstantQueryFactory();
		assertNotNull(f.createQuery(null));
		assertTrue(f.createQuery(null) instanceof StringConstantQuery);
		
	}
	
}
