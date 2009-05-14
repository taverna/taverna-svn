/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.platform.plugin.InstanceInitializer;
import net.sf.taverna.t2.platform.plugin.InstanceRegistry;
import net.sf.taverna.t2.platform.plugin.SPIRegistry;
import net.sf.taverna.t2.platform.plugin.impl.InstanceRegistryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Factory bean for the SPI instance registry functionality in Raven. Inject an
 * SpiRegistry and a list of constructor arguments to initialize.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public class InstanceRegistryFactoryBean implements FactoryBean,
		BeanFactoryAware {

	private SPIRegistry<?> classRegistry = null;
	private List<Object> constructorArgs = new ArrayList<Object>();
	private Log log = LogFactory.getLog(InstanceRegistryFactoryBean.class);

	private BeanFactory beanFactory;
	private boolean initializeBeans = false;

	public boolean isInitializeBeans() {
		return initializeBeans;
	}

	/**
	 * Return a newly constructed InstanceRegistry. If the spi registry property
	 * isn't defined this method throws a runtime exception and logs the problem
	 * to the logging system.
	 */
	@SuppressWarnings("unchecked")
	public Object getObject() throws Exception {
		if (classRegistry != null) {

			BeanInitializer beanInitializer = null;
			if (initializeBeans && beanFactory instanceof ListableBeanFactory) {
				beanInitializer = new BeanInitializer();
			}
			return new InstanceRegistryImpl(classRegistry, constructorArgs,
					beanInitializer);
		} else {
			log.error("Must specify spiRegistry property"
					+ " to construct an InstanceRegistry");
			throw new RuntimeException();
		}
	}

	/**
	 * Define whether to attempt to initialize beans on construction. This
	 * defaults to false, if set to true the following occurs: for each bean
	 * property on the constructed object the property type is obtained and the
	 * application context searched for all instances of this type. If there is
	 * exactly one instance the property is set to that instance.
	 * 
	 * @param initializeBeans
	 */
	public void setInitializeBeans(boolean initializeBeans) {
		this.initializeBeans = initializeBeans;
	}

	/**
	 * Inject an SpiRegistry instance to provide classes to instantiate objects
	 * from within the instance registry
	 */
	public void setSpiRegistry(SPIRegistry<?> registry) {
		this.classRegistry = registry;
	}

	/**
	 * Optionally specify a list of arguments to the constructors of classes in
	 * the spi registry. If this property is undefined it defaults to using an
	 * empty constructor list, in general this is the required behaviour when
	 * constructing beans as they'll always have a default constructor.
	 */
	public void setConstructorArgs(List<Object> args) {
		this.constructorArgs = args;
	}

	/**
	 * @return InstanceRegistry.class
	 */
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return InstanceRegistry.class;
	}

	/**
	 * Singleton by default
	 */
	public boolean isSingleton() {
		return true;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	private final class BeanInitializer implements InstanceInitializer<Object> {

		ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;

		public void initialize(Object newInstance) {
			try {
				BeanInfo beanInfo = Introspector.getBeanInfo(newInstance
						.getClass());
				for (PropertyDescriptor propDesc : beanInfo
						.getPropertyDescriptors()) {
					String propertyName = propDesc.getName();
					String[] namesForType = listableBeanFactory
							.getBeanNamesForType(propDesc.getPropertyType());
					if (namesForType.length == 0) {
						log.trace("Could not set " + propertyName + " on "
								+ newInstance + ", no beans of class "
								+ propDesc.getPropertyType());
					} else if (namesForType.length == 1) {
						Object bean = listableBeanFactory
								.getBean(namesForType[0]);
						log.debug("Set the property " + propertyName + " to "
								+ bean + " for " + newInstance);
						propDesc.getWriteMethod().invoke(newInstance, bean);
					} else {
						log.info("Could not set " + propertyName + " on "
								+ newInstance + ", too many beans of class "
								+ propDesc.getPropertyType());
					}
				}
			} catch (IntrospectionException e) {
				log.warn("Could not initialize " + newInstance, e);
			} catch (IllegalArgumentException e) {
				log.warn("Could not initialize " + newInstance, e);
			} catch (IllegalAccessException e) {
				log.warn("Could not initialize " + newInstance, e);
			} catch (InvocationTargetException e) {
				log.warn("Could not initialize " + newInstance, e);
			}
		}
	}

}
