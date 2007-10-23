package net.sf.taverna.t2.cloudone.util.spi;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.util.SPIRegistry;

import org.junit.Test;

public class SPIRegistryTest {

	SPIRegistry<DummySPI> registry = new SPIRegistry<DummySPI>(DummySPI.class);


	@Test
	public void getClassNames() throws Exception {
		Collection<String> classNames = registry.getClassNames();
		assertEquals(9, classNames.size());
		// Also includes invalid class names
	}
	
	@Test
	public void getClasses() {
		Map<String, Class<? extends DummySPI>> classes = registry.getClasses();
		assertEquals(3, classes.size());
		assertEquals(FirstDummySPI.class, 
				classes.get("net.sf.taverna.t2.cloudone.util.spi.FirstDummySPI"));
		assertEquals(SecondDummySPI.class,
				classes.get(SecondDummySPI.class.getCanonicalName()));
		
		// getClasses() don't know that MissingConstructor can't be constructed
		assertEquals(MissingConstructor.class,
				classes.get(MissingConstructor.class.getCanonicalName()));
	}
	
	
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
