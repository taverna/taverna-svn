package net.sf.taverna.t2.cloudone.util.spi;

import static org.junit.Assert.*;

import java.util.List;

import net.sf.taverna.t2.cloudone.util.SPIRegistry;

import org.junit.Test;

public class SPIRegistryTest {

	SPIRegistry<DummySPI> registry = new SPIRegistry<DummySPI>(DummySPI.class);

	@Test
	public void getInstances() {
		List<DummySPI> instances = registry.getInstances();
		
		assertEquals(2, instances.size());
		// Test that they were instantiated
		for (DummySPI spi : instances) {
			assertEquals("Wrong name", spi.getName(), spi.getClass().getSimpleName());
		}
	}

}
