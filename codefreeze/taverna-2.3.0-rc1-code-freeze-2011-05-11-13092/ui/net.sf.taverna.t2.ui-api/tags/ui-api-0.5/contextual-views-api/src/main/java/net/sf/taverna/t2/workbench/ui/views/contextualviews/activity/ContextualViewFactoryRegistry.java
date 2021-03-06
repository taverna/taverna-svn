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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * An SPI registry for discovering ActivityViewFactories for a given object,
 * like an {@link net.sf.taverna.t2.workflowmodel.processor.activity.Activity}.
 * <p>
 * For {@link ContextualViewFactory factories} to be found, its full qualified
 * name needs to be defined as a resource file
 * <code>/META-INF/services/net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualViewFactory</code>
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @author Stian Soiland-Reyes
 * 
 * @see ContextualViewFactory
 * 
 */
@SuppressWarnings("unchecked")
public class ContextualViewFactoryRegistry extends
		SPIRegistry<ContextualViewFactory> {

	/**
	 * Get the singleton instance of this registry
	 * 
	 * @return The ContextualViewFactoryRegistry singleton
	 */
	public static synchronized ContextualViewFactoryRegistry getInstance() {
		return Singleton.instance;
	}

	protected ContextualViewFactoryRegistry() {
		super(ContextualViewFactory.class);
	}

	/**
	 * Discover and return an ContextualViewFactory associated to the provided
	 * activity. This is accomplished by returning the first discovered
	 * {@link ContextualViewFactory#canHandle(Object)} that returns true for
	 * that Activity.
	 * 
	 * @param object
	 * @return
	 * 
	 * @see ContextualViewFactory#canHandle(Object)
	 */
	public ContextualViewFactory<?> getViewFactoryForObject(Object object) {

		for (ContextualViewFactory<?> factory : getInstances()) {
			if (factory.canHandle(object)) {
				return factory;
			}
		}
		throw new IllegalArgumentException(
				"Can't find factory for view class " + object);
	}

	private static class Singleton {
		private static final ContextualViewFactoryRegistry instance = new ContextualViewFactoryRegistry();
	}

}
