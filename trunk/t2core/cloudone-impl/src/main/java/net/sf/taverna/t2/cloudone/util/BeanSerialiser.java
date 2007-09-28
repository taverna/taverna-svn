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

import net.sf.taverna.t2.cloudone.bean.Beanable;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Serialise or deserialise a bean (normally produced by a {@link Beanable}) as
 * XML. Code based on
 * {@link net.sf.taverna.t2.workflowmodel.impl.Tools#beanAsElement(Object)}.
 * Currently beans are serialised to XML using {@link XMLEncoder}.
 *
 * @author Stian Soiland
 * @author Ian Dunlop
 */

public class BeanSerialiser {

	/**
	 * Deserialise bean from XML element.
	 *
	 * @see #fromXML(Element, ClassLoader)
	 * @see #toXML(Object)
	 * @param element
	 *            Element containing serialisation of bean using
	 *            {@link #toXML(Object)}
	 * @param classLoader
	 *            {@link ClassLoader} from where to construct bean classes
	 * @return Deserialised bean
	 */
	public static Object fromXML(Element element, ClassLoader classLoader) {
		String configAsString = new XMLOutputter(Format.getRawFormat())
				.outputString(element);
		XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(
				configAsString.getBytes()), null, null, classLoader);
		Object configObject = decoder.readObject();
		return configObject;
	}

	/**
	 * Deserialise bean from XML file.
	 *
	 * @see #fromXML(Element, ClassLoader)
	 * @see #toXML(Object)
	 * @param file
	 *            File containing XML serialisation of bean using
	 *            {@link #toXML(Object)}
	 * @param classLoader
	 *            {@link ClassLoader} from where to construct bean classes
	 * @return Deserialised bean
	 */
	public static Object fromXMLFile(File file, ClassLoader classLoader)
			throws JDOMException, IOException {
		BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream(file));
		try {
			XMLDecoder decoder = new XMLDecoder(stream, null, null, classLoader);
			Object obj = decoder.readObject();
			decoder.close();
			return obj;
		} finally {
			stream.close();
		}

	}

	/**
	 * Serialise bean as XML. The bean must conform to the {@link XMLEncoder}
	 * serialisation rules. The bean can be deserialised using
	 * {@link #fromXML(Element, ClassLoader)}.
	 *
	 * @see #fromXML(Element, ClassLoader)
	 * @see #toXMLFile(Object, File)
	 * @param bean
	 *            Bean to serialise
	 * @return XML {@link Element} of serialised bean
	 */
	public static Element toXML(Object bean) {
		// TODO: Support Beanable directly, and Beans containing Beanable's
		// TODO: Serialise Raven classloaders
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder xenc = new XMLEncoder(bos);
		xenc.writeObject(bean);
		xenc.close();
		byte[] bytes = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		Element configElement;
		try {
			configElement = new SAXBuilder().build(bis).getRootElement();
		} catch (JDOMException ex) {
			throw new RuntimeException("Unexpected XML parsing error", ex);
		} catch (IOException ex) {
			throw new RuntimeException("Unexpected XML reading error", ex);
		}
		configElement.getParent().removeContent(configElement);
		return configElement;
	}

	/**
	 * Serialise bean as XML and store in file. The bean must conform to the
	 * {@link XMLEncoder} serialisation rules. The bean can be deserialised
	 * using {@link #fromXMLFile(File, ClassLoader)}.
	 *
	 * @see #fromXML(Element, ClassLoader)
	 * @see #toXMLFile(Object, File)
	 * @param bean
	 *            Bean to serialise
	 * @param file
	 *            File where to store XML
	 * @throws IOException
	 *             If the file could not be written to
	 */
	public static void toXMLFile(Object bean, File file) throws IOException {
		BufferedOutputStream stream = new BufferedOutputStream(
				new FileOutputStream(file));
		try {
			XMLEncoder xenc = new XMLEncoder(stream);
			xenc.writeObject(bean);
			xenc.close();
		} finally {
			stream.close();
		}
	}

	/**
	 * Protected constructor, use static methods only.
	 */
	protected BeanSerialiser() {
	}
}
