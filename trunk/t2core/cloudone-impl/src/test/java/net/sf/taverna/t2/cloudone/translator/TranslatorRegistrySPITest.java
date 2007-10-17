package net.sf.taverna.t2.cloudone.translator;

import static org.junit.Assert.*;

import java.util.List;

import net.sf.taverna.t2.cloudone.util.SPIRegistry;

import org.junit.Test;

public class TranslatorRegistrySPITest {

	SPIRegistry<Translator> registry = TranslatorRegistry.getInstance();
	
	@Test
	public void getInstances() {
		List<Translator> instances = registry.getInstances();
		assertEquals(2, instances.size());
		assertTrue(instances.get(0) instanceof AnyToBlobTranslator);
		assertTrue(instances.get(1) instanceof AnyToFileURLTranslator);

	}
	
}
