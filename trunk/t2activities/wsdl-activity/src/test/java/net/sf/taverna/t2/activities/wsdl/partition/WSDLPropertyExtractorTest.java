package net.sf.taverna.t2.activities.wsdl.partition;

import java.util.List;

import net.sf.taverna.t2.partition.PropertyExtractorSPI;
import net.sf.taverna.t2.partition.PropertyExtractorSPIRegistry;

import org.junit.Test;
import static org.junit.Assert.*;

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
}
