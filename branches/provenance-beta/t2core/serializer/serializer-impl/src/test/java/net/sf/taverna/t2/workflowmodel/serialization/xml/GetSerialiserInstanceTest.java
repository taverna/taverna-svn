package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.*;

import org.junit.Test;

public class GetSerialiserInstanceTest {
	
	@Test
	public void getTheInstance() {
		XMLSerializerRegistry instance = XMLSerializerRegistry.getInstance();
		assertNotNull(instance);
		XMLSerializer serializer = instance.getSerializer();
		assertNotNull(serializer);
	}

}
