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

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;

/**
 * <p>
 * An Abstract implementation of an {@link ActivityTranslator}, which is
 * responsible for creating an Activity corresponding to a Taverna 1 Processor.
 * </p>
 * <p>
 * This abstract class contains the translations steps common to all Acivities.
 * Subclasses should implement additional steps for configuring more specific
 * properties for that Activity.
 * </p>
 * <p>
 * In particular, if an Activity configuration type is an implementation of
 * {@link ActivityPortsDefinitionBean} then
 * {@link AbstractActivityTranslator#populateConfigurationBeanPortDetails(Processor, ActivityPortsDefinitionBean)}
 * can be useful for setting up the input and output ports.
 * </p>
 * 
 * @author Stuart Owen
 * @author David Withers
 * 
 * @param <ConfigurationType>
 */
public abstract class AbstractActivityTranslator<ConfigurationType> implements
		ActivityTranslator<ConfigurationType> {

	/**
	 * <p>
	 * The entry point for carrying out a translation from a Taverna 1 Processor to a Taverna 2 Activity.<br>
	 * </p>
	 * 
	 * @param processor
	 * @return a translated Activity
	 * @throws ActivityTranslationException
	 * @throws ActivityConfigurationException
	 */
	public Activity<ConfigurationType> doTranslation(Processor processor)
			throws ActivityTranslationException,ActivityConfigurationException {

		Activity<ConfigurationType> activity = createUnconfiguredActivity();
		ConfigurationType configType = createConfigType(processor);
		activity.configure(configType);

		return activity;
	}

	/**
	 * @return an Activity that has yet to be fully configured.
	 */
	protected abstract Activity<ConfigurationType> createUnconfiguredActivity();

	/**
	 * Creates a configuration type and populates its properties with details
	 * extracted from the Processor required to configure a corresponding
	 * Activity.
	 * 
	 * @param processor
	 * @return the configuration type.
	 * @throws ActivityTranslationException
	 */
	protected abstract ConfigurationType createConfigType(Processor processor) throws ActivityTranslationException;

	/**
	 * If the configuration type for an Activity implements
	 * {@link ActivityPortsDefinitionBean} then this helper method is available to
	 * transfer input and output port details from the processor to the
	 * configuration type.
	 * 
	 * This is for use with activities that have their input and output ports defined
	 * implicitly rather than dynamically generated
	 * 
	 * @param processor
	 * @param configBean
	 */
	protected void populateConfigurationBeanPortDetails(Processor processor,
			ActivityPortsDefinitionBean configBean) {
		List<ActivityInputPortDefinitionBean> inputDefinitions = new ArrayList<ActivityInputPortDefinitionBean>();
		List<ActivityOutputPortDefinitionBean> outputDefinitions = new ArrayList<ActivityOutputPortDefinitionBean>();

		for (InputPort inputPort : processor.getInputPorts()) {
			ActivityInputPortDefinitionBean bean = new ActivityInputPortDefinitionBean();
			bean.setName(inputPort.getName());
			bean.setDepth(determineDepthFromSyntacticType(inputPort
					.getSyntacticType()));
			List<String> mimeTypes = new ArrayList<String>();
			mimeTypes.add(inputPort.getSyntacticType());
			bean.setMimeTypes(mimeTypes);
			bean.setHandledReferenceSchemes(new ArrayList<Class<? extends ExternalReferenceSPI>>());
			bean.setTranslatedElementType(determineClassFromSyntacticType(inputPort.getSyntacticType()));
			bean.setAllowsLiteralValues(true);
			inputDefinitions.add(bean);
		}

		for (OutputPort outPort : processor.getOutputPorts()) {
			ActivityOutputPortDefinitionBean bean = new ActivityOutputPortDefinitionBean();
			bean.setName(outPort.getName());
			bean.setDepth(determineDepthFromSyntacticType(outPort
					.getSyntacticType()));

			// TODO: check correct default value for granular depth. Setting
			// this to the same as depth will prevent streaming.
			bean.setGranularDepth(bean.getDepth());
			List<String> mimeTypes = new ArrayList<String>();
			mimeTypes.add(outPort.getSyntacticType());
			bean.setMimeTypes(mimeTypes);
			outputDefinitions.add(bean);

		}
		configBean.setInputPortDefinitions(inputDefinitions);
		configBean.setOutputPortDefinitions(outputDefinitions);
	}

	protected Class<?> determineClassFromSyntacticType(String syntacticType) {
		Class<?> result = String.class;
		if (syntacticType != null) {
			if (syntacticType.startsWith("l(")) {
				syntacticType = syntacticType.substring(syntacticType.lastIndexOf("l(") + 2);
			}
			if (syntacticType.startsWith("'")) {
				syntacticType = syntacticType.substring(1);
			}
			if (syntacticType.startsWith("application")) {
				result = byte[].class;
			} else if (syntacticType.startsWith("image")) {
				result = byte[].class;
			} else if (syntacticType.startsWith("'") || syntacticType.startsWith(")")) {
				result = byte[].class;
			}
		}
		return result;
	}

	/**
	 * @param syntacticType
	 * @return the depth determined from the syntactic mime type of the original
	 *         port. i.e text/plain = 0, l('text/plain') = 1, l(l('text/plain')) =
	 *         2, ... etc.
	 */
	protected int determineDepthFromSyntacticType(String syntacticType) {
		if (syntacticType == null) {
			return 0;
		} else {
			return syntacticType.split("l\\(").length - 1;
		}
	}
}
