package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class DeserializerTestsHelper {

	protected Edits edits = EditsRegistry.getEdits();
	
	protected Element loadXMLFragment(String resourceName) throws Exception {
		InputStream inStream = DeserializerImplTest.class
				.getResourceAsStream("/serialized-fragments/" + resourceName);

		if (inStream==null) throw new IOException("Unable to find resource for serialized fragment :"+resourceName);
		SAXBuilder builder = new SAXBuilder();
		return builder.build(inStream).detachRootElement();
	}
}
