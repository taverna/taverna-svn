package net.sf.taverna.t2.util.beanable.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.util.beanable.Beanable;
import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;

import org.apache.log4j.Logger;
import org.jboss.jaxb.intros.IntroductionsAnnotationReader;
import org.jboss.jaxb.intros.IntroductionsConfigParser;
import org.jboss.jaxb.intros.configmodel.JaxbIntros;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sun.xml.bind.api.JAXBRIContext;

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

	public static final String DEFAULT_NAMESPACE = "http://taverna.sf.net/t2/cloudone/bean/unknown/";

	private static JAXBContext jaxbContext;

	private static final String JAVA = "java";

	private static Logger logger = Logger.getLogger(BeanSerialiser.class);

	private static BeanableFactoryRegistry beanableFactoryRegistry = BeanableFactoryRegistry
			.getInstance();

	private static final String CLASS_NAME = "className";
	private static final String BEANABLE = "beanable";

	private static BeanSerialiser instance;

	public static BeanSerialiser getInstance() throws JAXBException {
		if (instance == null) {
			instance = new BeanSerialiser();
		}
		return instance;
	}

	/**
	 * Deserialise bean from XML element.
	 * 
	 * @see #fromXML(Element, ClassLoader)
	 * @see #toXML(Object)
	 * @param element
	 *            Element containing serialisation of bean using
	 *            {@link #toXML(Object)}
	 * @return Deserialised bean
	 */
	public Object fromXML(Element element) {
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
	 * Deserialise bean from XML file.
	 * 
	 * @see #fromXML(Element, ClassLoader)
	 * @see #toXML(Object)
	 * @param file
	 *            File containing XML serialisation of bean using
	 *            {@link #toXML(Object)}
	 * @param classLoader
	 *            {@link ClassLoader} from where to construct bean classes
	 * @throws RetrievalException
	 *             If the bean could not be deserialised
	 * @return Deserialised bean
	 */
	public Object fromXMLFile(File file) throws RetrievalException {
		Unmarshaller unmarshaller;
		Object bean;
		try {
			unmarshaller = jaxbContext.createUnmarshaller();
			bean = unmarshaller.unmarshal(file);
		} catch (JAXBException e) {
			logger.warn(e);
			throw new RetrievalException("Could not de-serialise " + file + " "
					+ e);
		}
		return bean;

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
	public Element toXML(Object bean) {
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
	 * @throws StorageException
	 *             If the bean could not be serialised
	 */
	public void toXMLFile(Object bean, File file) throws StorageException {
		Marshaller marshaller;
		try {
			marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(bean, file);
		} catch (JAXBException e) {
			logger.warn(e);
			throw new StorageException("Could not serialise bean " + bean + " "
					+ e);
		}
	}

	/**
	 * Private constructor, use {@link #getInstance()}
	 * 
	 * @throws JAXBException
	 */
	private BeanSerialiser() throws JAXBException {
		makeJAXBContext();
	}

	/**
	 * Given a beanable entity return as XML with the class name as an element
	 * 
	 * @param beanable
	 * @return XML
	 */
	public Element beanableToXML(Beanable<?> beanable) {
		return toXML(beanable.getAsBean());
	}

	/**
	 * Given an XML representation of a beanable entity (see
	 * {@link #beanableToXML(Beanable)}), use the {@link BeanableFactory} to
	 * return the beanable
	 * 
	 * @param <Bean>
	 * @param elem
	 * @return The Beanable entity contained in the XML
	 */
	@SuppressWarnings("unchecked")
	public <Bean> Beanable<?> beanableFromXML(Element elem) {
		Object beanObject = fromXML(elem);
		BeanableFactory<? extends Beanable, Bean> beanableFactory = beanableFactoryRegistry
				.getFactoryForBeanType(beanObject.getClass());
		Bean bean = beanableFactory.getBeanType().cast(beanObject);
		return beanableFactory.createFromBean(bean);
	}

	/**
	 * Use the {@link BeanableFactory} to find the SPI'd JAXB annotations for
	 * each of the Entity Beans and create the JAXBContext
	 * 
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	public void makeJAXBContext() throws JAXBException {
		List<Class> beanClasses = new ArrayList<Class>();
		JaxbIntros mergedConfig = new JaxbIntros();
		for (BeanableFactory beanableFactory : beanableFactoryRegistry
				.getInstances()) {
			InputStream annotationStream = beanableFactory
					.getAnnotationIntroduction();
			beanClasses.add(beanableFactory.getBeanType());
			if (annotationStream == null) {
				logger.info("No annotation introduction found for "
						+ beanableFactory);
				continue;
			}
			JaxbIntros beanableConfig = IntroductionsConfigParser
					.parseConfig(annotationStream);
			mergedConfig.getClazz().addAll(beanableConfig.getClazz());
		}

		IntroductionsAnnotationReader reader = new IntroductionsAnnotationReader(
				mergedConfig);

		Map<String, Object> jaxbConfig = new HashMap<String, Object>();
		jaxbConfig.put(JAXBRIContext.ANNOTATION_READER, reader);

		jaxbConfig
				.put(JAXBRIContext.DEFAULT_NAMESPACE_REMAP, DEFAULT_NAMESPACE);

		JAXBContext context = JAXBContext.newInstance(beanClasses
				.toArray(new Class[0]), jaxbConfig);
		jaxbContext = context;
	}
}
