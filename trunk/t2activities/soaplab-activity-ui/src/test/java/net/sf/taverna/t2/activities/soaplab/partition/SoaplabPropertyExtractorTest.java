package net.sf.taverna.t2.activities.soaplab.partition;

import static org.junit.Assert.assertTrue;

import java.util.List;

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
	
//	@Test
//	public void testExtractProperties() {
//		WSDLActivityItem item = new WSDLActivityItem();
//		item.setUse("USE");
//		item.setStyle("STYLE");
//		item.setOperation("OPERATION");
//		item.setUrl("URL");
//		Map<String,Object> props = new SoaplabPropertyExtractor().extractProperties(item);
//		assertEquals("missing or incorrect property","USE",props.get("use"));
//		assertEquals("missing or incorrect property","STYLE",props.get("style"));
//		assertEquals("missing or incorrect property","OPERATION",props.get("operation"));
//		assertEquals("missing or incorrect property","URL",props.get("url"));
//		assertEquals("missing or incorrect property","SOAP",props.get("type"));
//	}
//	
//	@Test
//	public void testExtractPropertiesNotWSDL() {
//		ActivityItem item = new ActivityItem() {
//			
//		};
//		Map<String,Object> props = new SoaplabPropertyExtractor().extractProperties(item);
//		assertNotNull("A map should have been returned, even though its empty",props);
//		assertEquals("There should be no properties",0,props.size());
//	}
}
