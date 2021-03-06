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
package net.sf.taverna.t2.activities.beanshell.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;

import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;


/**
 * An ActivityTranslator specifically for translating Taverna 1 Beanshell Processors to a Taverna 2 Beanshell Activity
 * 
 * @see ActivityTranslator
 * @author Stuart Owen
 */
public class BeanshellActivityTranslator extends AbstractActivityTranslator<BeanshellActivityConfigurationBean> {

	@Override
	protected Activity<BeanshellActivityConfigurationBean> createUnconfiguredActivity() {
		return new BeanshellActivity();
	}

	@Override
	protected BeanshellActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		populateConfigurationBeanPortDetails(processor, bean);
		bean.setScript(determineScript(processor));
		bean.setLocalDependencies(new LinkedHashSet<String>(((BeanshellProcessor)processor).localDependencies));
		bean.setArtifactDependencies(new LinkedHashSet<BasicArtifact>(((BeanshellProcessor)processor).artifactDependencies));

		return bean;
	}
	
	private String determineScript(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getScript");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("There was a security exception whilst trying to invoke getString through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The service does not have the method getScript, an therefore does not conform to being a Beanshell service",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getScript on the Beanshell service had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getScript on the Beanshell service",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getScript on the Beanshell service",e);
		}
	}

	public boolean canHandle(Processor processor) {
		return (processor!=null && processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor"));
	}

}
