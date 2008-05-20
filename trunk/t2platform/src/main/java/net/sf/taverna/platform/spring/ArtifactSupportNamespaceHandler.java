package net.sf.taverna.platform.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import static net.sf.taverna.platform.spring.RavenConstants.*;

/**
 * Namespace handler to associate the artifact definition decorator with the
 * raven attributes, specifically the 'repository' and 'artifact' attributes on
 * raven-enabled bean definitions.
 * 
 * @author Tom Oinn
 * 
 */
public class ArtifactSupportNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		ArtifactDefinitionDecorator decorator = new ArtifactDefinitionDecorator();
		registerBeanDefinitionDecoratorForAttribute(
				ARTIFACT_XML_ATTRIBUTE_NAME, decorator);
		registerBeanDefinitionDecoratorForAttribute(
				REPOSITORY_XML_ATTRIBUTE_NAME, decorator);
		registerBeanDefinitionParser("repository",
				new RepositoryBeanDefinitionParser());
	}

}
