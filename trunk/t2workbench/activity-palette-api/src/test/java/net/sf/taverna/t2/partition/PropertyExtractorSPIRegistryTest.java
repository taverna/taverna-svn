package net.sf.taverna.t2.partition;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class PropertyExtractorSPIRegistryTest {

	@Test
	public void testGetAllPropertiesFor() {
		Map<String,Object> map = PropertyExtractorSPIRegistry.getInstance().getAllPropertiesFor("A String");
		assertEquals("There should be 2 items in the map",2,map.size());
		assertEquals("There should be an elment for james=>brown","brown",map.get("james"));
		assertEquals("There should be an elment for fred=>blogs","blogs",map.get("fred"));
		
		map = PropertyExtractorSPIRegistry.getInstance().getAllPropertiesFor(Integer.valueOf(1));
		assertEquals("There should be 1 items in the map",1,map.size());
		assertEquals("There should be an elment for one=>1","1",map.get("one"));
		
		map = PropertyExtractorSPIRegistry.getInstance().getAllPropertiesFor(Float.valueOf(1f));
		assertEquals("There should be 0 items in the map",0,map.size());
	}

}
