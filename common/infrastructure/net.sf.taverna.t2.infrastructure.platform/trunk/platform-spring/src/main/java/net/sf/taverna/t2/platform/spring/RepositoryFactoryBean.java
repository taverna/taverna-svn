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

import static net.sf.taverna.t2.platform.spring.PropertyInterpolator.interpolate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.pom.JarManager;
import net.sf.taverna.t2.platform.pom.PomParser;
import net.sf.taverna.t2.platform.raven.Raven;
import net.sf.taverna.t2.platform.raven.impl.RavenImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * A FactoryBean used to configure and instantiate a raven InternalRaven object.
 * Allows construction of remote repository list and system artifact list
 * through setter injection from within spring, requires a pom parser and jar
 * manager to be injected
 * <p>
 * Because this implements FactoryBean, spring will return the result of the
 * getObject method rather than the bean itself.
 * 
 * @author Tom Oinn
 */
public class RepositoryFactoryBean implements FactoryBean {

	private Log log = LogFactory.getLog(RepositoryFactoryBean.class);

	private List<String> systemArtifactStrings = null;
	private List<String> remoteRepositories = null;
	private PomParser pomParser = null;
	private JarManager jarManager = null;

	public Object getObject() throws Exception {
		Set<ArtifactIdentifier> systemArtifacts = new HashSet<ArtifactIdentifier>();
		for (String systemArtifactSpec : systemArtifactStrings) {
			systemArtifacts.add(new ArtifactIdentifier(
					interpolate(systemArtifactSpec)));
		}
		ClassLoader parentLoader = this.getClass().getClassLoader();
		Raven raven = new RavenImpl(parentLoader, pomParser, jarManager,
				systemArtifacts);
		List<URL> remoteRepositoryURLs = new ArrayList<URL>();

		for (String repositoryLocationString : remoteRepositories) {
			try {
				remoteRepositoryURLs.add(new URL(
						interpolate(repositoryLocationString)));
			} catch (RuntimeException ex) {
				// Don't add repositories which cause an error on instantiation,
				// this can be because the URL is invalid but can also occur if
				// the interpolation attempts to use a property that isn't
				// defined. This can be used intentionally to add repositories
				// only if a property is set.
				log
						.warn("Missing property in interpolation, ignoring remote repository entry "
								+ repositoryLocationString);
			} catch (MalformedURLException mue) {
				log.error("Malformed remote repository URL, not using", mue);
			}
		}

		InternalRaven result = new InternalRaven(raven, remoteRepositoryURLs);

		return result;

	}

	public void setSystemArtifacts(List<String> systemArtifacts) {
		this.systemArtifactStrings = systemArtifacts;
	}

	public void setRemoteRepositoryList(List<String> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	public void setPomParser(PomParser parser) {
		this.pomParser = parser;
	}
	
	public void setJarManager(JarManager manager) {
		this.jarManager = manager;
	}
	
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return InternalRaven.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
