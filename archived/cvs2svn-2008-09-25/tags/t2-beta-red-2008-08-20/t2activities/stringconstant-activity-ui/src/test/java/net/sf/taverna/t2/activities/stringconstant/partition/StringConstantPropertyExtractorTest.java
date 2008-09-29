package net.sf.taverna.t2.activities.stringconstant.partition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.stringconstant.query.StringConstantActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;
import net.sf.taverna.t2.partition.PropertyExtractorSPIRegistry;

import org.junit.Test;

public class StringConstantPropertyExtractorTest {

	@Test
	public void testSPI() {
		List<PropertyExtractorSPI> instances = PropertyExtractorSPIRegistry.getInstance().getInstances();
		assertTrue("There should be more than one instance found",instances.size()>0);
		boolean found = false;
		for (PropertyExtractorSPI spi : instances) {
			if (spi instanceof StringConstantPropertyExtractor) {
				found=true;
				break;
			}
		}
		assertTrue("A StringConstantPropertyExtractor should have been found",found);
	}
	
	@Test
	public void testExtractProperties() {
		StringConstantActivityItem item = new StringConstantActivityItem();
		
		Map<String,Object> props = new StringConstantPropertyExtractor().extractProperties(item);

		assertEquals("missing or incorrect property","String Constant",props.get("type"));
	}

}
