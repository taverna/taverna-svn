//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.10.31 at 02:43:23 PM GMT 
//

package net.sf.taverna.t2.platform.plugin.generated;

import javax.xml.bind.annotation.XmlRegistry;
import net.sf.taverna.t2.platform.plugin.generated.impl.AuthorDescriptionImpl;
import net.sf.taverna.t2.platform.plugin.generated.impl.ExternalLinkImpl;
import net.sf.taverna.t2.platform.plugin.generated.impl.PluginDescriptionImpl;
import net.sf.taverna.t2.platform.plugin.generated.impl.SpiImplementationImpl;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * net.sf.taverna.t2.platform.plugin.generated package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	@SuppressWarnings("unused")
	private final static Void _useJAXBProperties = null;

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package:
	 * net.sf.taverna.t2.platform.plugin.generated
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link SpiImplementation }
	 * 
	 */
	public SpiImplementation createSpiImplementation() {
		return new SpiImplementationImpl();
	}

	/**
	 * Create an instance of {@link ExternalLink }
	 * 
	 */
	public ExternalLink createExternalLink() {
		return new ExternalLinkImpl();
	}

	/**
	 * Create an instance of {@link PluginDescription }
	 * 
	 */
	public PluginDescription createPluginDescription() {
		return new PluginDescriptionImpl();
	}

	/**
	 * Create an instance of {@link AuthorDescription }
	 * 
	 */
	public AuthorDescription createAuthorDescription() {
		return new AuthorDescriptionImpl();
	}

}
