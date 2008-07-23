package net.sf.taverna.t2.activities.soaplab.partition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.soaplab.query.SoaplabActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;
import net.sf.taverna.t2.partition.PropertyExtractorSPIRegistry;

import org.junit.Test;

public class SoaplabPropertyExtractorTest {

	@Test
	public void testSPI() {
		List<PropertyExtractorSPI> instances = PropertyExtractorSPIRegistry.getInstance().getInstances();
		assertTrue("There should be more than one instance found",instances.size()>0);
		boolean found = false;
		for (PropertyExtractorSPI spi : instances) {
			if (spi instanceof SoaplabPropertyExtractor) {
				found=true;
				break;
			}
		}
		assertTrue("A WSDLPropertyExtractor should have been found",found);
	}
	
	@Test
	public void testExtractProperties() {
		SoaplabActivityItem item = new SoaplabActivityItem();
		item.setCategory("CATEGORY");
		item.setOperation("OPERATION");
		item.setUrl("URL");
		Map<String,Object> props = new SoaplabPropertyExtractor().extractProperties(item);
		assertEquals("missing or incorrect property","CATEGORY",props.get("category"));
		assertEquals("missing or incorrect property","OPERATION",props.get("operation"));
		assertEquals("missing or incorrect property","URL",props.get("url"));
		assertEquals("missing or incorrect property","Soaplab",props.get("type"));
	}

}
