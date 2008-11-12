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

import net.sf.taverna.t2.platform.plugin.PluginException;
import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.plugin.SPIRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for the SpiRegistry functionality in the new Plug-in manager
 * component from the T2 platform.
 * 
 * @author Tom Oinn
 * 
 */
public class SPIRegistryFactoryBean implements FactoryBean {

	PluginManager manager = null;
	String spiClassName = null;

	private Log log = LogFactory.getLog(SPIRegistryFactoryBean.class);

	public Object getObject() throws Exception {
		if (manager != null && spiClassName != null) {
			try {
				return manager.getSPIRegistry(this.getClass().getClassLoader()
						.loadClass(spiClassName));
			} catch (ClassNotFoundException cnfe) {
				throw new PluginException("Unable to locate the SPI class '"
						+ spiClassName
						+ "' in the current context's class loader", cnfe);
			}
		}
		log.error("Must specify plug-in manager and "
				+ "spi class name for spi registry");
		throw new RuntimeException();
	}

	public void setPluginManager(PluginManager manager) {
		this.manager = manager;
	}

	public void setSpiClassName(String spiClassName) {
		this.spiClassName = spiClassName;
	}

	/**
	 * @return SPIRegistry.class
	 */
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return SPIRegistry.class;
	}

	/**
	 * Singleton by default
	 */
	public boolean isSingleton() {
		return true;
	}

}
