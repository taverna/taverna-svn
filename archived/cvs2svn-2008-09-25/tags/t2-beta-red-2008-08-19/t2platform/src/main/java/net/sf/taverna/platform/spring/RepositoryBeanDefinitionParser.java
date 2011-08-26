package net.sf.taverna.platform.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Customized version of the configuration for the repository bean. This allows
 * a more compact form of the repository specification, and potentially error
 * checking through IDE schema support which is not available when we just
 * instantiate the helper factory bean directly.
 * 
 * @author Tom Oinn
 * 
 */
public class RepositoryBeanDefinitionParser extends
		AbstractBeanDefinitionParser {

	/**
	 * The overall intent here is to construct a bean definition to the
	 * repository factory bean which will then allow it to produce a repository
	 * object when fully instantiated. Really just a convenience method to avoid
	 * having to set the bean manually. Not implemented yet!
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext context) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder
				.rootBeanDefinition(RepositoryFactoryBean.class);
		// Do the 'base' property
		factory.addPropertyValue("base", element.getAttribute("base"));

		Element systemArtifactElement = DomUtils.getChildElementByTagName(
				element, "system");
		List<Element> systemChildElements = DomUtils.getChildElementsByTagName(
				systemArtifactElement, "sys");
		if (systemChildElements != null && systemChildElements.size() > 0) {
			List<String> systemArtifactList = new ArrayList<String>();
			for (Element e : systemChildElements) {
				systemArtifactList.add(e.getAttribute("artifact"));
			}
			factory.addPropertyValue("systemArtifacts", systemArtifactList);
		}

		Element repositoriesElement = DomUtils.getChildElementByTagName(
				element, "repositories");
		List<Element> repositoriesElements = DomUtils
				.getChildElementsByTagName(repositoriesElement, "rep");
		if (repositoriesElements != null && systemChildElements.size() > 0) {
			List<String> repositoryList = new ArrayList<String>();
			for (Element e : repositoriesElements) {
				repositoryList.add(e.getAttribute("url"));
			}
			factory.addPropertyValue("remoteRepositoryList", repositoryList);
		}

		return factory.getBeanDefinition();
	}
}
