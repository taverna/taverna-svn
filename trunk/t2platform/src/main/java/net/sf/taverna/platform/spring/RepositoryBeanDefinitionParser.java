package net.sf.taverna.platform.spring;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Not yet implemented - placeholder to insert a customized version of the
 * configuration for the repository bean. This would allow a more compact form
 * of the repository specification, and potentially error checking through IDE
 * schema support which is not available when we just instantiate the helper
 * factory bean directly.
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
	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext context) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder
				.rootBeanDefinition(RepositoryFactoryBean.class);

		return factory.getBeanDefinition();
	}

}
