package net.sf.taverna.t2.platform.plugin.impl;

import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import net.sf.taverna.t2.platform.plugin.generated.ObjectFactory;
import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;
import net.sf.taverna.t2.platform.plugin.generated.impl.PluginDescriptionImpl;
import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;

/**
 * Provides PluginDescription to/from XML functionality based on JAXB
 * annotations
 * 
 * @author Tom Oinn
 * 
 */
public class PluginDescriptionXMLHandler {

	private static Unmarshaller unmarshaller;
	private static Marshaller marshaller;
	private static ObjectFactory factory = new ObjectFactory();

	private static void initContext() throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance("net.sf.taverna.t2.platform.plugin.generated");
		unmarshaller = context.createUnmarshaller();
		marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	}

	/**
	 * Unmarshall a PluginDescription from a URL
	 */
	public synchronized static PluginDescription getDescription(URL url)
			throws JAXBException {
		initContext();
		return (PluginDescription) unmarshaller.unmarshal(url);
	}

	/**
	 * Unmarshall a PluginDescription from a File
	 */
	public synchronized static PluginDescription getDescription(File file)
			throws JAXBException {
		initContext();
		return (PluginDescription) unmarshaller.unmarshal(file);
	}

	/**
	 * Marshall a PluginDescription to a Document
	 * 
	 * @throws JAXBException
	 */
	public synchronized static void writeDocument(
			PluginDescription description, OutputStream out)
			throws JAXBException {
		initContext();
		marshaller.marshal(new JAXBElement<PluginDescriptionImpl>(new QName(
				"http://taverna.sf.net/pluginDescription", "plugin"),
				PluginDescriptionImpl.class,
				(PluginDescriptionImpl) description), out);
		// marshaller.marshal(new JAXBElement(new QName("uri", "local"),
		// PluginDescription.class, description), out);
	}

	/**
	 * Build a new PluginDescription implementation, populating it with
	 * sensible(ish) default values
	 * 
	 * @return a new PluginDescription
	 */
	public static PluginDescription createDescription() {

		PluginDescription description = factory.createPluginDescription();
		description.setName("Plugin name");
		description.setDescription("Plugin description goes here");
		try {
			description.setBackgroundImage(new URL(
					"http://www.example.com/foo.bar"));
			description.getArtifactList().add(
					new ArtifactIdentifier("foo", "bar", "v"));
			description.getJarList().add(
					new URL("http://some.jar.host/mycode.jar"));
		} catch (MalformedURLException mue) {
			//
		}
		return description;

	}

}
