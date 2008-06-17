package net.sf.taverna.t2.activities.wsdl.partition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.activities.wsdl.query.WSDLActivityItem;
import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;
import net.sf.taverna.t2.partition.PropertyExtractorSPIRegistry;

import org.junit.Test;

public class WSDLPropertyExtractorTest {

	@Test
	public void testSPI() {
		List<PropertyExtractorSPI> instances = PropertyExtractorSPIRegistry.getInstance().getInstances();
		assertTrue("There should be more than one instance found",instances.size()>0);
		boolean found = false;
		for (PropertyExtractorSPI spi : instances) {
			if (spi instanceof WSDLPropertyExtractor) {
				found=true;
				break;
			}
		}
		assertTrue("A WSDLPropertyExtractor should have been found",found);
	}
	
	@Test
	public void testExtractProperties() {
		WSDLActivityItem item = new WSDLActivityItem();
		item.setUse("USE");
		item.setStyle("STYLE");
		item.setOperation("OPERATION");
		item.setUrl("URL");
		Map<String,Object> props = new WSDLPropertyExtractor().extractProperties(item);
		assertEquals("missing or incorrect property","USE",props.get("use"));
		assertEquals("missing or incorrect property","STYLE",props.get("style"));
		assertEquals("missing or incorrect property","OPERATION",props.get("operation"));
		assertEquals("missing or incorrect property","URL",props.get("url"));
		assertEquals("missing or incorrect property","SOAP",props.get("type"));
	}
	
	@Test
	public void testExtractPropertiesNotWSDL() {
		ActivityItem item = new ActivityItem() {

			public Transferable getActivityTransferable() {
				// TODO Auto-generated method stub
				return null;
			}

			public Icon getIcon() {
				// TODO Auto-generated method stub
				return null;
			}
			
			

			
			
		};
		Map<String,Object> props = new WSDLPropertyExtractor().extractProperties(item);
		assertNotNull("A map should have been returned, even though its empty",props);
		assertEquals("There should be no properties",0,props.size());
	}
}
