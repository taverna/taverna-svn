package net.sf.taverna.t2.cloudone.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Based on net.sf.taverna.t2.workflowmodel.impl.Tools
 */

public class EntitySerialiser {
	
	public static Element toXML(Object ent) throws JDOMException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder xenc = new XMLEncoder(bos);
		xenc.writeObject(ent);
		xenc.close();
		byte[] bytes = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		Element configElement = new SAXBuilder().build(bis).getRootElement();
		configElement.getParent().removeContent(configElement);
		return configElement;
	}

	public static Object fromXML(Element elem, ClassLoader cl) {
		String configAsString = new XMLOutputter(Format.getRawFormat())
				.outputString(elem);
		XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(
				configAsString.getBytes()), null, null, cl);
		Object configObject = decoder.readObject();
		return configObject;
	}
}
