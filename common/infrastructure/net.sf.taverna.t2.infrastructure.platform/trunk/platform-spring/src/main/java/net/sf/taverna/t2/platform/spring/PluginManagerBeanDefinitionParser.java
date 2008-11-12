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
 * Compact specification parser for the plugin manager factory bean
 * 
 * @author Tom Oinn
 * 
 */
public class PluginManagerBeanDefinitionParser extends
		AbstractBeanDefinitionParser {

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext context) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder
				.rootBeanDefinition(PluginManagerFactoryBean.class);

		// Handle references to pom parser, jar manager etc
		//
		// <xsd:attribute name="downloadManager" type="xsd:string"
		// use="required" />
		// <xsd:attribute name="jarManager" type="xsd:string" use="required" />
		// <xsd:attribute name="raven" type="xsd:string" use="required" />
		// <xsd:attribute name="pluginParser" type="xsd:string" use="required"
		// />
		// <xsd:attribute name="base" type="xsd:string" use="required" />
		factory.addPropertyReference("downloadManager", element
				.getAttribute("downloadManager"));
		factory.addPropertyReference("jarManager", element
				.getAttribute("jarManager"));
		factory.addPropertyReference("raven", element.getAttribute("raven"));
		factory.addPropertyReference("pluginParser", element
				.getAttribute("pluginParser"));
		factory.addPropertyReference("base", element.getAttribute("base"));

		// Handle repository list
		Element repositoryListElement = DomUtils.getChildElementByTagName(
				element, "repositories");
		if (repositoryListElement != null) {
			List<Element> repositoryElements = DomUtils
					.getChildElementsByTagName(repositoryListElement, "url");
			if (repositoryElements != null && repositoryElements.size() > 0) {
				List<String> repositoryList = new ArrayList<String>();
				for (Element e : repositoryElements) {
					repositoryList.add(DomUtils.getTextValue(e));
				}
				factory
						.addPropertyValue("remoteRepositoryList",
								repositoryList);
			}
		}

		// Handle plugin list
		Element pluginListElement = DomUtils.getChildElementByTagName(element,
				"defaultPlugins");
		if (pluginListElement != null) {
			List<Element> pluginListElements = DomUtils
					.getChildElementsByTagName(pluginListElement, "plugin");
			if (pluginListElements != null && pluginListElements.size() > 0) {
				List<String> pluginList = new ArrayList<String>();
				for (Element e : pluginListElements) {
					pluginList.add(DomUtils.getTextValue(e));
				}
				factory.addPropertyValue("remoteRepositoryList", pluginList);
			}
		}

		return factory.getBeanDefinition();
	}
}
