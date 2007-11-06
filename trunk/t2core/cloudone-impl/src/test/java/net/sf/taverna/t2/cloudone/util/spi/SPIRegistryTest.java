package net.sf.taverna.t2.cloudone.util.spi;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.spi.SPIRegistry;

import org.junit.Test;

public class SPIRegistryTest {

	SPIRegistry<DummySPI> registry = new SPIRegistry<DummySPI>(DummySPI.class);


	@Test
	public void getClassNames() throws Exception {
		Collection<String> classNames = registry.getClassNames();
		assertEquals(4, classNames.size());
		//Includes classes that can be found, but not necessarily instantiated.
	}
	
	@Test
	public void getClasses() {
		Map<String, Class<? extends DummySPI>> classes = registry.getClasses();
		assertEquals(4, classes.size());
		assertEquals(FirstDummySPI.class, 
				classes.get("net.sf.taverna.t2.cloudone.util.spi.FirstDummySPI"));
		
		//doesn't know that DummySPI is only an interface.
		assertEquals(DummySPI.class,
				classes.get(DummySPI.class.getCanonicalName()));
		
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
