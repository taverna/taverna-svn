package net.sf.taverna.t2.cloudone.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

	public static Object fromXML(Element elem, ClassLoader cl) {
		String configAsString = new XMLOutputter(Format.getRawFormat())
				.outputString(elem);
		XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(
				configAsString.getBytes()), null, null, cl);
		Object configObject = decoder.readObject();
		return configObject;
	}

	public static Object fromXMLFile(File file) throws JDOMException,
			IOException {
		BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream(file));
		try {
			XMLDecoder decoder = new XMLDecoder(stream);
			Object obj = decoder.readObject();
			decoder.close();
			return obj;
		} finally {
			stream.close();
		}

	}

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

	public static void toXMLFile(Object ent, File file) throws JDOMException,
			IOException {
		BufferedOutputStream stream = new BufferedOutputStream(
				new FileOutputStream(file));
		try {
			XMLEncoder xenc = new XMLEncoder(stream);
			xenc.writeObject(ent);
			xenc.close();
		} finally {
			stream.close();
		}
	}
}
