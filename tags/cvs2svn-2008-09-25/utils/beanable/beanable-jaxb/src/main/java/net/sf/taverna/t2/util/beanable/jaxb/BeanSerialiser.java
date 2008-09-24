/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.util.beanable.jaxb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.spi.SPIRegistry.SPIRegistryEvent;
import net.sf.taverna.t2.util.beanable.Beanable;
import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;
import net.sf.taverna.t2.util.beanable.RetrievalException;
import net.sf.taverna.t2.util.beanable.StorageException;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Serialise or deserialise a bean (normally produced by a {@link Beanable}) as
 * XML. Code based on
 * {@link net.sf.taverna.t2.workflowmodel.impl.Tools#beanAsElement(Object)}.
 * Currently beans are serialised to XML using JAXB.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 */

public class BeanSerialiser {
	private BeanableFactoryRegistry beanableFactoryRegistry = BeanableFactoryRegistry
			.getInstance();

	private static BeanSerialiser instance;

	private  JAXBContext jaxbContext;

	private static Logger logger = Logger.getLogger(BeanSerialiser.class);

	/**
	 * Get {@link BeanSerialiser} singleton.
	 * <p>
	 * Note that to avoid issues at initialisation time of the
	 * {@link SPIRegistry} it is not recommended to assign this instance as a
	 * static field in a class. The recommended usage is to assign the instance
	 * to a private field:
	 * 
	 * <pre>
	 * public class MyClass {
	 *     private BeanSerialiser beanSerialiser = BeanSerialiser.getInstance();
	 * ..
	 * }
	 * </pre>
	 * 
	 * @return
	 * @throws JAXBException
	 */
	public static BeanSerialiser getInstance() {
		if (instance == null) {
			instance = new BeanSerialiser();
		}
		return instance;
	}

	/**
	 * Protected constructor, use singleton access {@link #getInstance()}
	 * instead
	 * 
	 * @throws JAXBException
	 */
	protected BeanSerialiser() {
		try {
			jaxbContext = makeJAXBContext();
		} catch (JAXBException e) {
			throw new RuntimeException("Can't initialise JAXB context", e);
		}
		beanableFactoryRegistry.addObserver(refreshBindingsObserver);
	}
	private RefreshBindingsObserver refreshBindingsObserver = new RefreshBindingsObserver();
	
	public class RefreshBindingsObserver implements Observer<SPIRegistryEvent> {

		public void notify(Observable<SPIRegistryEvent> sender,
				SPIRegistryEvent message) throws Exception {
			refreshBindings();
		}
	}

	/**
	 * Refresh the bindings from the {@link BeanableFactoryRegistry}, typically
	 * called after a plugin has modified the {@link SPIRegistry}.
	 * 
	 * @throws JAXBException
	 */
	public void refreshBindings() throws JAXBException {
		jaxbContext = makeJAXBContext();
	}

	@SuppressWarnings("unchecked")
	protected <Bean> Beanable<?> beanableFromBean(Object beanObject)
			throws RetrievalException {
		BeanableFactory<? extends Beanable, Bean> beanableFactory = beanableFactoryRegistry
				.getFactoryForBeanType(beanObject.getClass());
		Bean bean = beanableFactory.getBeanType().cast(beanObject);
		return beanableFactory.createFromBean(bean);
	}

	/**
	 * Given an XML representation of a beanable entity (see
	 * {@link #beanableToXMLElement(Beanable)}), use the
	 * {@link BeanableFactory} to return the beanable
	 * 
	 * @param <Bean>
	 * @param elem
	 * @return The Beanable entity contained in the XML
	 */
	public <Bean> Beanable<?> beanableFromXMLElement(Element elem) {
		Object beanObject = beanFromXMLElement(elem);
		return beanableFromBean(beanObject);
	}

	/**
	 * Given a {@link File} containing the XML representation of a
	 * {@link Beanable} entity, return the {@link Beanable}
	 * 
	 * @param <Bean>
	 *            the bean type used to create the {@link Beanable}
	 * @param xmlFile
	 *            XML representation of the beanable entity
	 * @return the {@link Beanable} recreated from the XML
	 * @throws RetrievalException
	 * @throws FileNotFoundException
	 */
	public <Bean> Beanable<?> beanableFromXMLFile(File xmlFile)
			throws RetrievalException, FileNotFoundException {
		Object beanObject = beanFromXMLFile(xmlFile);
		return beanableFromBean(beanObject);
	}

	/**
	 * Given an InputStream containing an XML representation of a
	 * {@link Beanable}, return this {@link Beanable}
	 * 
	 * @param <Bean>
	 *            the bean type used to create the {@link Beanable}
	 * @param inputStream
	 * @return the {@link Beanable} recreated from the stream
	 * @throws RetrievalException
	 */
	public <Bean> Beanable<?> beanableFromXMLStream(InputStream inputStream)
			throws RetrievalException {
		Object beanObject = beanFromXMLStream(inputStream);
		return beanableFromBean(beanObject);
	}

	/**
	 * Given a beanable entity return as XML with the class name as an element
	 * 
	 * @param beanable
	 * @return XML
	 */
	public Element beanableToXMLElement(Beanable<?> beanable)
			throws StorageException {
		return beanToXMLElement(beanable.getAsBean());
	}

	/**
	 * Given a {@link Beanable} entity store it in a file in XML format
	 * 
	 * @param beanable
	 *            the {@link Beanable} entity
	 * @param xmlFile
	 *            a file to serialise the {@link Beanable} out to
	 * @throws StorageException
	 * @throws IOException
	 */
	public void beanableToXMLFile(Beanable<?> beanable, File xmlFile)
			throws StorageException, IOException {
		beanToXMLFile(beanable.getAsBean(), xmlFile);
	}

	/**
	 * Given a {@link Beanable} entity store it in a stream in XML format
	 * 
	 * @param beanable
	 *            the {@link Beanable} entity
	 * @param outputStream
	 *            a stream to serialise the {@link Beanable} out to
	 * 
	 * @throws StorageException
	 */
	public void beanableToXMLStream(Beanable<?> beanable,
			OutputStream outputStream) throws StorageException {
		beanToXMLStream(beanable.getAsBean(), outputStream);
	}

	/**
	 * Deserialise bean from XML element.
	 * 
	 * @see #fromXML(Element, ClassLoader)
	 * @see #beanToXMLElement(Object)
	 * @param element
	 *            Element containing serialisation of bean using
	 *            {@link #beanToXMLElement(Object)}
	 * @return Deserialised bean
	 */
	protected Object beanFromXMLElement(Element element) {
		String configAsString = new XMLOutputter(Format.getRawFormat())
				.outputString(element);
		InputStream inputStream = new ByteArrayInputStream(configAsString
				.getBytes());
		Unmarshaller unmarshaller;
		Object bean;
		try {
			unmarshaller = jaxbContext.createUnmarshaller();
			bean = unmarshaller.unmarshal(inputStream);
		} catch (JAXBException e) {
			logger.warn(e);
			throw new RetrievalException("Could not de-serialise " + element
					+ " " + e);
		}
		return bean;
	}

	/**
	 * Deserialise the bean from an XML file
	 * 
	 * @param xmlFile
	 *            file containing XML serialisation of bean
	 * @return Deserialised bean
	 * @throws RetrievalException
	 * @throws FileNotFoundException
	 */
	public Object beanFromXMLFile(File xmlFile) throws RetrievalException,
			FileNotFoundException {
		FileInputStream fileInput = new FileInputStream(xmlFile);
		BufferedInputStream bufferedIn = new BufferedInputStream(fileInput);
		try {
			return beanFromXMLStream(bufferedIn);
		} finally {
			try {
				bufferedIn.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * Deserialise bean from XML stream.
	 * 
	 * @see #fromXML(Element, ClassLoader)
	 * @see #beanToXMLElement(Object)
	 * @param inputStream
	 *            stream containing XML serialisation of bean using
	 *            {@link #beanToXMLElement(Object)}
	 * @param classLoader
	 *            {@link ClassLoader} from where to construct bean classes
	 * @throws RetrievalException
	 *             If the bean could not be deserialised
	 * @return Deserialised bean
	 */
	protected Object beanFromXMLStream(InputStream inputStream)
			throws RetrievalException {
		Unmarshaller unmarshaller;
		Object bean;
		try {
			unmarshaller = jaxbContext.createUnmarshaller();
			bean = unmarshaller.unmarshal(inputStream);
		} catch (JAXBException e) {
			logger.warn(e);
			throw new RetrievalException("Could not de-serialise "
					+ inputStream + " " + e);
		}
		return bean;
	}

	/**
	 * Serialise bean as XML. The bean must conform to the {@link XMLEncoder}
	 * serialisation rules. The bean can be deserialised using
	 * {@link #fromXML(Element, ClassLoader)}.
	 * 
	 * @see #fromXML(Element, ClassLoader)
	 * @see #beanToXMLStream(Object, OutputStream)
	 * @param bean
	 *            Bean to serialise
	 * @return XML {@link Element} of serialised bean
	 */
	protected Element beanToXMLElement(Object bean) throws StorageException {
		// TODO: Support Beanable directly, and Beans containing Beanable's
		// TODO: Serialise Raven classloaders
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Marshaller marshaller;
		try {
			marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(bean, bos);
		} catch (JAXBException e) {
			logger.warn(e);
			throw new StorageException("Could not serialise bean " + bean + " "
					+ e);
		}

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
	 * Serialise the bean to file
	 * 
	 * @param bean
	 *            the bean type eg {@link net.sf.taverna.t2.cloudone.bean.DataDocumentBean}
	 * @param xmlFile
	 *            the file to write the serialised bean out to
	 * @throws IOException
	 * @throws StorageException
	 */
	protected void beanToXMLFile(Object bean, File xmlFile) throws IOException,
			StorageException {
		FileOutputStream fileOutput = new FileOutputStream(xmlFile);
		BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOutput);
		beanToXMLStream(bean, bufferedOut);
		bufferedOut.close();
	}

	/**
	 * Serialise bean as XML and store in file. The bean must conform to the
	 * {@link XMLEncoder} serialisation rules. The bean can be deserialised
	 * using {@link #fromXMLFile(File, ClassLoader)}.
	 * 
	 * @see #fromXML(Element, ClassLoader)
	 * @see #beanToXMLStream(Object, OutputStream)
	 * @param bean
	 *            Bean to serialise
	 * @param outputStream
	 *            File where to store XML
	 * @throws StorageException
	 *             If the bean could not be serialised
	 */
	protected void beanToXMLStream(Object bean, OutputStream outputStream)
			throws StorageException {
		// needs changed
		Marshaller marshaller;
		try {
			marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(bean, outputStream);
		} catch (JAXBException e) {
			logger.warn(e);
			throw new StorageException("Could not serialise bean " + bean + " "
					+ e);
		}
	}

	/**
	 * Use the {@link BeanableFactory} to find the annotated beans to create the
	 * JAXBContext.
	 * 
	 * @return
	 * 
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	protected JAXBContext makeJAXBContext() throws JAXBException {
		List<Class> beanClasses = new ArrayList<Class>();
		// JaxbIntros mergedConfig = new JaxbIntros();
		for (BeanableFactory beanableFactory : beanableFactoryRegistry
				.getInstances()) {
			beanClasses.add(beanableFactory.getBeanType());
		}

		JAXBContext context = JAXBContext.newInstance(beanClasses
				.toArray(new Class[0]));
		return context;
	}
}
