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
package net.sf.taverna.t2.workflowmodel.impl;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.AddDispatchLayerEdit;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;

public class DefaultDispatchStackEdit extends AbstractProcessorEdit {
	private Edit<?> compoundEdit = null;
	private static final int MAX_JOBS = 1;
	private static final long BACKOFF_FACTOR = (long) 1.1;
	private static final int MAX_DELAY = 5000;
	private static final int INITIAL_DELAY = 1000;
	private static final int MAX_RETRIES = 0;
	private static final String layerPackage = "net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.";

	private PluginManager manager;

	public DefaultDispatchStackEdit(Processor processor, PluginManager manager) {
		super(processor);
		this.manager = manager;
		DispatchStackImpl stack = ((ProcessorImpl) processor)
				.getDispatchStack();
		// Top level parallelise layer
		int layer = 0;
		List<Edit<?>> edits = new ArrayList<Edit<?>>();

		edits.add(new AddDispatchLayerEdit(stack,
				(DispatchLayer<?, ?>) createObjectFromClassName(layerPackage
						+ "Parallelize", MAX_JOBS), layer++));
		edits.add(new AddDispatchLayerEdit(stack,
				(DispatchLayer<?, ?>) createObjectFromClassName(layerPackage
						+ "Failover"), layer++));
		edits.add(new AddDispatchLayerEdit(stack,
				(DispatchLayer<?, ?>) createObjectFromClassName(layerPackage
						+ "Retry", MAX_RETRIES, INITIAL_DELAY, MAX_DELAY,
						BACKOFF_FACTOR), layer++));
		edits.add(new AddDispatchLayerEdit(stack,
				(DispatchLayer<?, ?>) createObjectFromClassName(layerPackage
						+ "Invoke"), layer++));
		compoundEdit = new CompoundEdit(edits);
	}

	private Object createObjectFromClassName(String className,
			Object... constructorArgs) {
		Object result = null;
		for (PluginDescription desc : manager.getActivePluginList()) {
			try {

				ClassLoader loader = manager.getPluginClassLoader(desc.getId(),
						new ArrayList<URL>());
				Class<?> c = loader.loadClass(className);
				Class<?>[] constructorArgTypes = new Class<?>[constructorArgs.length];
				for (int i = 0; i < constructorArgs.length; i++) {
					constructorArgTypes[i] = constructorArgs[i].getClass();
				}
				for (Constructor<?> cons : c.getConstructors()) {
					Class<?>[] type = cons.getParameterTypes();
					if (type.length == constructorArgTypes.length) {
						for (int i = 0; i < type.length; i++) {
							try {
								result = cons.newInstance(constructorArgs);
								return result;
							} catch (Exception ex) {
								//
							}
						}
					}
				}

				Constructor<?> cons = c.getConstructor(constructorArgTypes);
				result = cons.newInstance(constructorArgs);
				return result;
			} catch (Exception ex) {
				//
			}
		}
		throw new RuntimeException("Unable to locate layer class " + className);
	}

	@Override
	protected void doEditAction(ProcessorImpl processor) throws EditException {
		compoundEdit.doEdit();
	}

	@Override
	protected void undoEditAction(ProcessorImpl processor) {
		compoundEdit.undo();
	}

}
