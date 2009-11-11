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
package net.sf.taverna.t2.activities.apiconsumer.translator;

import java.util.LinkedHashSet;

import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivityConfigurationBean;
import net.sf.taverna.t2.activities.dependencyactivity.AbstractAsynchronousDependencyActivity;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.apiconsumer.APIConsumerDefinition;
import org.embl.ebi.escience.scuflworkers.apiconsumer.APIConsumerProcessor;

public class ApiConsumerActivityTranslator extends AbstractActivityTranslator<ApiConsumerActivityConfigurationBean>{

	@Override
	protected ApiConsumerActivityConfigurationBean createConfigType(Processor proc)
			throws ActivityTranslationException {
		
		APIConsumerDefinition definition = ((APIConsumerProcessor) proc).getDefinition();
		ApiConsumerActivityConfigurationBean configurationBean = new ApiConsumerActivityConfigurationBean();	
		configurationBean.setDescription(definition.getDescription());
		configurationBean.setClassName(definition.getClassName());
		configurationBean.setMethodName(definition.getMethodName());
		configurationBean.setParameterNames(definition.getPNames());
		configurationBean.setParameterDimensions(definition.getPDimensions());
		configurationBean.setParameterTypes(definition.getPTypes());
		configurationBean.setReturnType(definition.getTName());
		configurationBean.setReturnDimension(definition.getTDimension());
		configurationBean.setDescription(definition.getDescription());
		configurationBean.setIsMethodConstructor(definition.isConstructor());
		configurationBean.setIsMethodStatic(definition.isStatic());
		
		org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing clSharing = ((APIConsumerProcessor) proc).getClassLoaderSharing();
		// 'Fresh' and 'iteration' classloading are not in use any more - deafults to 'workflow'
		if (clSharing == org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing.fresh){
			configurationBean.setClassLoaderSharing(AbstractAsynchronousDependencyActivity.ClassLoaderSharing.workflow);
		}
		else if (clSharing == org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing.iteration){
			configurationBean.setClassLoaderSharing(AbstractAsynchronousDependencyActivity.ClassLoaderSharing.workflow);
		}
		else if (clSharing == org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing.workflow){
			configurationBean.setClassLoaderSharing(AbstractAsynchronousDependencyActivity.ClassLoaderSharing.workflow);
		}
		else if (clSharing == org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing.system){
			configurationBean.setClassLoaderSharing(AbstractAsynchronousDependencyActivity.ClassLoaderSharing.system);
		}
		configurationBean.setLocalDependencies(((APIConsumerProcessor)proc).localDependencies);
		configurationBean.setLocalDependencies(new LinkedHashSet<String>(((APIConsumerProcessor)proc).localDependencies));
		configurationBean.setArtifactDependencies(new LinkedHashSet<BasicArtifact>(((APIConsumerProcessor)proc).artifactDependencies));
		return configurationBean;
	}

	@Override
	protected Activity<ApiConsumerActivityConfigurationBean> createUnconfiguredActivity() {
		return new ApiConsumerActivity();
	}

	public boolean canHandle(Processor processor) {
		return (processor!=null && processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.apiconsumer.APIConsumerProcessor"));
	}

}
