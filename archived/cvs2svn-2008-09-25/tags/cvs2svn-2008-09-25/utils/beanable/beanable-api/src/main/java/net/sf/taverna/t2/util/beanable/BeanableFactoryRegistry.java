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
package net.sf.taverna.t2.util.beanable;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;

import org.apache.log4j.Logger;

/**
 * Registers all the individual {@link BeanableFactory}s discovered using an
 * SPI lookup
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@SuppressWarnings("unchecked")
public class BeanableFactoryRegistry extends SPIRegistry<BeanableFactory> {
	private static BeanableFactoryRegistry instance;

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(BeanableFactoryRegistry.class);
			
	/**
	 * Singleton instance of the {@link BeanableFactoryRegistry}
	 * 
	 * @return A singleton instance of the factory
	 */
	public static synchronized BeanableFactoryRegistry getInstance() {
		if (instance == null) {
			instance = new BeanableFactoryRegistry();
		}
		return instance;
	}

	private CheckOnUpdateObserver checkOnUpdateObserver = new CheckOnUpdateObserver();

	protected BeanableFactoryRegistry() {
		super(BeanableFactory.class);
		this.addObserver(checkOnUpdateObserver);
		checkFactories();
	}

	/**
	 * Find all the {@link BeanableFactory}s and log any problems
	 */
	public void checkFactories() {
		Set<Class> beanTypes = new HashSet<Class>();
		Set<Class> beanableTypes = new HashSet<Class>();
		Set<String> beanableTypeNames = new HashSet<String>();

		for (BeanableFactory factory : getInstances()) {
			Class beanType = factory.getBeanType();
			if (beanTypes.contains(beanType)) {
				logger.warn("Factory  " + factory
						+ " has non-unique bean type " + beanType);
			}
			beanTypes.add(beanType);

			Class beanableType = factory.getBeanableType();
			if (beanableTypes.contains(beanableType)) {
				logger.warn("Factory  " + factory
						+ " has non-unique beanable type " + beanableType);
			}
			beanableTypes.add(beanableType);
			// TODO: Remove non-unique entries to highlight the problem

			String beanableTypeName = beanableType.getCanonicalName();
			if (beanableTypeNames.contains(beanableTypeName)) {
				logger.warn("Factory " + factory
						+ " has non-unique beanable class name "
						+ beanableTypeName);
			}
		}
	}

	/**
	 * Given a {@link Beanable} class name find the factory which can create the
	 * appropriate bean
	 * 
	 * @param beanableClassName
	 *            the name of the {@link Beanable}
	 * @return he appropraite factory for creating the bean
	 */
	public BeanableFactory getFactoryForBeanableType(String beanableClassName) {
		for (BeanableFactory factory : getInstances()) {
			if (factory.getBeanableType().getCanonicalName().equals(
					beanableClassName)) {
				return factory;
			}
		}
		throw new IllegalArgumentException(
				"Can't find factory for beanable class " + beanableClassName);
	}

	/**
	 * Given the class of bean find the factory which can create it
	 * 
	 * @param beanType
	 *            the class of bean eg. {@link net.sf.taverna.t2.cloudone.bean.DataDocumentBean}
	 * @return the appropriate factory
	 */
	public BeanableFactory getFactoryForBeanType(Class beanType) {
		for (BeanableFactory factory : getInstances()) {
			if (factory.getBeanType().equals(beanType)) {
				return factory;
			}
		}
		throw new IllegalArgumentException("Can't find factory for bean class "
				+ beanType);
	}

	public class CheckOnUpdateObserver implements Observer<SPIRegistryEvent> {
		public void notify(Observable<SPIRegistryEvent> sender,
				SPIRegistryEvent message) throws Exception {
			checkFactories();
		}
	}

}
