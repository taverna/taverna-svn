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
package net.sf.taverna.t2.platform.spring;

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
	 * object when fully instantiated.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext context) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder
				.rootBeanDefinition(RepositoryFactoryBean.class);

		// Handle references to pom parser and jar manager
		factory.addPropertyReference("pomParser", element
				.getAttribute("pomParser"));
		factory.addPropertyReference("jarManager", element
				.getAttribute("jarManager"));

		Element systemArtifactElement = DomUtils.getChildElementByTagName(
				element, "systemArtifacts");
		List<Element> systemChildElements = DomUtils.getChildElementsByTagName(
				systemArtifactElement, "artifact");
		if (systemChildElements != null && systemChildElements.size() > 0) {
			List<String> systemArtifactList = new ArrayList<String>();
			for (Element e : systemChildElements) {
				systemArtifactList.add(DomUtils.getTextValue(e));
			}
			factory.addPropertyValue("systemArtifacts", systemArtifactList);
		}

		Element repositoriesElement = DomUtils.getChildElementByTagName(
				element, "repositories");
		List<Element> repositoriesElements = DomUtils
				.getChildElementsByTagName(repositoriesElement, "url");
		if (repositoriesElements != null && systemChildElements.size() > 0) {
			List<String> repositoryList = new ArrayList<String>();
			for (Element e : repositoriesElements) {
				repositoryList.add(DomUtils.getTextValue(e));
			}
			factory.addPropertyValue("remoteRepositoryList", repositoryList);
		}

		return factory.getBeanDefinition();
	}
}
