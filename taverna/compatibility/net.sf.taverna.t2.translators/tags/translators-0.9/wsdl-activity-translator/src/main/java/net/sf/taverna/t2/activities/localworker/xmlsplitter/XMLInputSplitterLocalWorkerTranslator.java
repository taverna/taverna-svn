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
package net.sf.taverna.t2.activities.localworker.xmlsplitter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLInputSplitterActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.XMLExtensible;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * A translator specifically targeted at the local worker XMLInputSplitter.
 * 
 * It translates this type of local worker into a concrete Activity implementation rather than a beanshell script like most other versions.
 * The reason for this is that they need to know, during execution, input port details mime-type, depth and name.
 *
 * @author Stuart Owen
 *
 */
public class XMLInputSplitterLocalWorkerTranslator extends AbstractActivityTranslator<XMLSplitterConfigurationBean>{

	
	@Override
	protected XMLSplitterConfigurationBean createConfigType(Processor processor)
			throws ActivityTranslationException {
		XMLSplitterConfigurationBean bean = new XMLSplitterConfigurationBean();
		populateConfigurationBeanPortDetails(processor, bean);
		//TODO: doing this with introspection would remove the dependency on taverna-java-processor
		LocalServiceProcessor localServiceProcessor = (LocalServiceProcessor)processor;
		Element element = ((XMLExtensible)localServiceProcessor.getWorker()).provideXML();
		String xml = new XMLOutputter().outputString(element);
		bean.setWrappedTypeXML(xml);
		return bean;
	}
	
	@Override
	protected Activity<XMLSplitterConfigurationBean> createUnconfiguredActivity() {
		return new XMLInputSplitterActivity();
	}

	public boolean canHandle(Processor processor) {
		boolean result = false;
		if (processor != null) {
			if (processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor")) {
				try {
					String localworkerClassName = getWorkerClassName(processor);
					result = "org.embl.ebi.escience.scuflworkers.java.XMLInputSplitter".equals(localworkerClassName);
				}
				catch(Exception e) {
					result = false;
				}
			}
		}
		return result;
	}
	
	private String getWorkerClassName(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getWorkerClassName");
			return (String) method.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getWorkerClassName through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The service does not have the method getWorkerClassName, an therefore does not conform to being a local service",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getWorkerClassName on the local service had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getWorkerClassName on the local service",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getWorkerClassName on the local service",e);
		}
	}

}
