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

import static net.sf.taverna.t2.platform.spring.RavenConstants.ARTIFACT_BEAN_ATTRIBUTE_NAME;
import static net.sf.taverna.t2.platform.spring.RavenConstants.REPOSITORY_BEAN_ATTRIBUTE_NAME;
import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;
import net.sf.taverna.t2.platform.raven.Raven;
import net.sf.taverna.t2.platform.raven.RavenException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.DecoratingClassLoader;
import org.springframework.util.ClassUtils;

/**
 * A subclass of DefaultListableBeanFactory which is aware of raven. Overrides
 * the class resolution mechanism within the bean factory to use raven's
 * artifact class loading if the appropriate attributes are defined on the bean
 * definition. These attributes are set by the ArtifactDefinitionDecorator,
 * which is in turn driven by a trivial extension to spring's XML based
 * configuration schema.
 * 
 * @author Tom Oinn
 * @author David Withers
 */
public class RavenAwareListableBeanFactory extends DefaultListableBeanFactory {

	private Log log = LogFactory.getLog(RavenAwareListableBeanFactory.class);
	private boolean donePreProcessing = false;

	@SuppressWarnings("unchecked")
	@Override
	protected Class resolveBeanClass(RootBeanDefinition rbd, String beanName,
			Class[] typesToMatch) throws CannotLoadBeanClassException {

		try {

			if (rbd.hasBeanClass()) {
				return rbd.getBeanClass();
			}
			// System.out.println(Thread.currentThread().toString()
			// + " Getting bean class for " + beanName);
			// Exception excep = new RuntimeException();
			// if (beanName.equals("t2.enactor.enactor")) {
			// for (StackTraceElement o : excep.getStackTrace()) {
			// System.out.println("     " + o.toString());
			// }
			// }
			if (typesToMatch != null && getTempClassLoader() != null) {
				String className = rbd.getBeanClassName();
				ClassLoader tempClassLoader = getTempClassLoader();
				if (tempClassLoader instanceof DecoratingClassLoader) {
					DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
					for (int i = 0; i < typesToMatch.length; i++) {
						dcl.excludeClass(typesToMatch[i].getName());
					}
				}
				return (className != null ? ClassUtils.forName(className,
						tempClassLoader) : null);
			}

			if (rbd.hasAttribute(REPOSITORY_BEAN_ATTRIBUTE_NAME)
					&& rbd.hasAttribute(ARTIFACT_BEAN_ATTRIBUTE_NAME)) {
				/**
				 * synchronized (this) { if (!donePreProcessing) { // Try to
				 * find and invoke any bean pre-processors if
				 * (containsBeanDefinition("platform.propertyPlaceholder")) { //
				 * System.out // .println("Attempting to apply pre-processing");
				 * try { RavenAwarePropertyPlaceholderConfigurer pc =
				 * (RavenAwarePropertyPlaceholderConfigurer)
				 * getBean("platform.propertyPlaceholder");
				 * pc.postProcessBeanFactory(this); } catch (ClassCastException
				 * cce) { cce.printStackTrace(); } } donePreProcessing = true; }
				 * }
				 */
				String repositoryBeanName = (String) rbd
						.getAttribute(REPOSITORY_BEAN_ATTRIBUTE_NAME);
				String artifact = (String) rbd
						.getAttribute(ARTIFACT_BEAN_ATTRIBUTE_NAME);
				if (containsBeanDefinition("platform.propertyPlaceholder")) {
					RavenAwarePropertyPlaceholderConfigurer pc = (RavenAwarePropertyPlaceholderConfigurer) getBean("platform.propertyPlaceholder");
					synchronized (this) {
						if (!donePreProcessing ) {
							pc.postProcessBeanFactory(this);
							donePreProcessing = true;
						}
					}
					artifact = pc.processProperty(artifact);
				}
				Raven repository = (Raven) getBean(repositoryBeanName);
				if (repository == null) {
					log.error("No repository bean with name '"
							+ repositoryBeanName + "' in this context");
					throw new CannotLoadBeanClassException(rbd
							.getResourceDescription(), beanName, rbd
							.getBeanClassName(), new ClassNotFoundException(
							"No such repository"));
				}
				synchronized (repository) {
					String[] s = artifact.split(":");
					if (s.length != 3) {
						log.error("Artifact specifier '" + artifact
								+ "' is badly formed for bean '" + beanName
								+ "'");
						throw new CannotLoadBeanClassException(rbd
								.getResourceDescription(), beanName, rbd
								.getBeanClassName(),
								new ClassNotFoundException(
										"Badly formed artifact specifier"));
					}
					ArtifactIdentifier a = new ArtifactIdentifier(s[0], s[1],
							s[2]);
					log.debug("Artifact " + a + " for bean name " + beanName);
					/**
					 * if (repository.getStatus(a) != ArtifactStatus.Ready) {
					 * log.debug(" has state " + repository.getStatus(a));
					 * repository.addArtifact(a); repository.update(); }
					 * 
					 * log.debug(" has state (2) " + repository.getStatus(a));
					 */
					try {
						// ClassLoader cl = repository.getLoader(a,
						// this.getClass()
						// .getClassLoader());
						ClassLoader cl = repository.getLoader(a, repository
								.getDefaultRepositoryList());
						log.debug("Loading class " + rbd.getBeanClassName()
								+ " from artifact " + a);
						Class beanClass = rbd.resolveBeanClass(cl);
						rbd.setBeanClass(beanClass);
						return beanClass;
					} catch (RavenException ex) {
						log.error("Raven exception '" + a.toString() + "'", ex);
						throw new CannotLoadBeanClassException(rbd
								.getResourceDescription(), beanName, rbd
								.getBeanClassName(),
								new ClassNotFoundException(ex.getMessage(), ex));
					}
				}
			} else {
				Class beanClass = rbd.resolveBeanClass(getBeanClassLoader());
				rbd.setBeanClass(beanClass);
				return beanClass;
			}
		} catch (ClassNotFoundException ex) {
			throw new CannotLoadBeanClassException(
					rbd.getResourceDescription(), beanName, rbd
							.getBeanClassName(), ex);
		} catch (LinkageError err) {
			throw new CannotLoadBeanClassException(
					rbd.getResourceDescription(), beanName, rbd
							.getBeanClassName(), err);
		}

	}

	/**
	 * Override to always return false if the raven decorators are present,
	 * otherwise delegates to the superclass method
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean isTypeMatch(String beanName, Class type) {
		/**
		 * try { BeanDefinition bd = getBeanDefinition(beanName); if
		 * (bd.hasAttribute(ARTIFACT_BEAN_ATTRIBUTE_NAME)) { return false; } }
		 * catch (NoSuchBeanDefinitionException nsbde) { // Happens if we're
		 * passed a factory bean here - the raven aware // beans are never
		 * factories }
		 */
		return super.isTypeMatch(beanName, type);
	}

	/**
	 * Override to always return false if the raven decorators are present,
	 * otherwise delegates to the superclass method
	 */
	@Override
	protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
		/**
		 * if (mbd.hasAttribute(ARTIFACT_BEAN_ATTRIBUTE_NAME)) { return false; }
		 */
		return super.isFactoryBean(beanName, mbd);
	}

	/**
	 * Override to always return null if the raven decorators are present,
	 * otherwise delegates to the superclass method. This is used, as far as I
	 * can tell, to do pre-checks on beans to, for example, determine whether
	 * they are post processors. As raven aware beans are never such things (as
	 * they require instantiation of other beans prior to existing) this can
	 * always return null.
	 */
	@SuppressWarnings("unchecked")
	protected Class predictBeanType(String beanName, RootBeanDefinition mbd,
			Class[] typesToMatch) {
		// Returning null here prevents the PluginManager from finding beans
		// by type to inject into the plugins because it is used by isTypeMatch()
//		if (mbd.hasAttribute(ARTIFACT_BEAN_ATTRIBUTE_NAME)) {
//			return null;
//		}
		return super.predictBeanType(beanName, mbd, typesToMatch);
	}

}
