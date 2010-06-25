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
package net.sf.taverna.t2.compatibility.activity;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslatorNotFoundException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;

/**
 * <p>
 * A Factory class responsible for providing the appropriate
 * {@link ActivityTranslator} for a given class of a Taverna 1 style Processor.
 * This translator is responsible for providing a {@link Activity} that has
 * similar capabilities of the original Processor.
 * </p>
 * 
 * @author Stuart Owen
 * @author David Withers
 * 
 */
public class ActivityTranslatorFactory {
	

	private static Map<String, ActivityTranslator<?>> translatorMap = new HashMap<String, ActivityTranslator<?>>();

	private static ActivityTranslatorSPIRegistry registry = new ActivityTranslatorSPIRegistry();
	/**
	 * <p>
	 * Given a particular Processor class it returns an appropriate
	 * ActivityTranslator
	 * </p>
	 * 
	 * @param processor -
	 *            the Processor requiring an ActivityTranslator
	 * @return an appropriate ActivityTranslator
	 * @throws ActivityTranslatorNotFoundException
	 */
	public static ActivityTranslator<?> getTranslator(Processor processor)
			throws ActivityTranslatorNotFoundException {
		String processorKey = generateProcessorKey(processor);
		if (!translatorMap.containsKey(processorKey)) {
			boolean foundTranslator = false;
			for (ActivityTranslator<?> translator : getRegistry().getInstances()) {
				if (translator.canHandle(processor)) {
					translatorMap.put(processorKey, translator);
					foundTranslator = true;
					break;
				}
			}
			if (!foundTranslator) {
				throw new ActivityTranslatorNotFoundException(
						"Unable to find Activity Translator for:"
								+ processor.getClass(),processor);
			}
		}

		return translatorMap.get(processorKey);
	}

	/**
	 * Normally the key is simply the fully qualified class name, unless it is a LocalServiceProcessor.
	 * If a LocalServiceProcessor then the key is LocalServiceProcessor: plus the worker classname.
	 * @param processor
	 * @return the key
	 */
	protected static String generateProcessorKey(Processor processor) {
		if (!(processor instanceof LocalServiceProcessor)) {
			return processor.getClass().getName();
		}
		else {
			//TODO: would be more desirable to do this through introspection to avoid the added dependency on taverna-java-processor
			String key="LocalServiceProcessor:";
			key+=((LocalServiceProcessor)processor).getWorkerClassName();
			return key;
		}
	}

	protected static ActivityTranslatorSPIRegistry getRegistry() {
		return registry;
	}
}
