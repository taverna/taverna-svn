package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.*;

import org.junit.Test;

public class GetSerialiserInstance {
	
	@Test
	public void getTheInstance() {
		XMLSerializerRegistry instance = XMLSerializerRegistry.getInstance();
		assertNotNull(instance);
	}

}
