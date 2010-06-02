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
package net.sf.taverna.platform.spring;

import static net.sf.taverna.platform.spring.RavenConstants.ARTIFACT_XML_ATTRIBUTE_NAME;
import static net.sf.taverna.platform.spring.RavenConstants.REPOSITORY_XML_ATTRIBUTE_NAME;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler to associate the artifact definition decorator with the
 * raven attributes, specifically the 'repository' and 'artifact' attributes on
 * raven-enabled bean definitions.
 * 
 * @author Tom Oinn
 * 
 */
public class ArtifactSupportNamespaceHandler extends NamespaceHandlerSupport {

	private Log log = LogFactory.getLog(ArtifactSupportNamespaceHandler.class);

	public void init() {
		ArtifactDefinitionDecorator decorator = new ArtifactDefinitionDecorator();
		registerBeanDefinitionDecoratorForAttribute(
				ARTIFACT_XML_ATTRIBUTE_NAME, decorator);
		registerBeanDefinitionDecoratorForAttribute(
				REPOSITORY_XML_ATTRIBUTE_NAME, decorator);
		registerBeanDefinitionParser("repository",
				new RepositoryBeanDefinitionParser());
		log.debug("Registered handlers for raven support namespace");
	}

}
