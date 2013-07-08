/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester
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
package uk.org.taverna.platform.execution.impl;

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import uk.org.taverna.platform.capability.api.ActivityConfigurationException;
import uk.org.taverna.platform.capability.api.ActivityNotFoundException;
import uk.org.taverna.platform.capability.api.DispatchLayerConfigurationException;
import uk.org.taverna.platform.capability.api.DispatchLayerNotFoundException;
import uk.org.taverna.platform.execution.api.ExecutionEnvironment;
import uk.org.taverna.platform.execution.api.ExecutionEnvironmentService;
import uk.org.taverna.platform.execution.api.ExecutionService;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.common.URITools;
import uk.org.taverna.scufl2.api.common.WorkflowBean;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.dispatchstack.DispatchStackLayer;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 * Implementation of the ExecutionEnvironmentService.
 *
 * @author David Withers
 */
public class ExecutionEnvironmentServiceImpl implements ExecutionEnvironmentService {

	private static final Logger logger = Logger.getLogger(ExecutionEnvironmentServiceImpl.class.getName());

	private static final URI SCUFL2 = URI.create("http://ns.taverna.org.uk/2010/scufl2#");

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();

	private final URITools uriTools = new URITools();

	private Set<ExecutionService> executionServices;

	@Override
	public Set<ExecutionEnvironment> getExecutionEnvironments() {
		Set<ExecutionEnvironment> executionEnvironments = new HashSet<ExecutionEnvironment>();
		for (ExecutionService executionService : executionServices) {
			executionEnvironments.addAll(executionService.getExecutionEnvivonments());
		}
		return executionEnvironments;
	}

	@Override
	public Set<ExecutionEnvironment> getExecutionEnvironments(Profile profile) {
		Set<ExecutionEnvironment> validExecutionEnvironments = new HashSet<ExecutionEnvironment>();
		for (ExecutionEnvironment executionEnvironment : getExecutionEnvironments()) {
			if (isValidExecutionEnvironment(executionEnvironment, profile)) {
				validExecutionEnvironments.add(executionEnvironment);
			}
		}
		return validExecutionEnvironments;
	}

	/**
	 * Sets the ExecutionServices that will be used to find ExecutionEnvironments.
	 *
	 * @param executionServices
	 *            the ExecutionServices that will be used to find ExecutionEnvironments
	 */
	public void setExecutionServices(Set<ExecutionService> executionServices) {
		this.executionServices = executionServices;
	}

	/**
	 * @param executionEnvironment
	 * @param profile
	 * @return
	 */
	private boolean isValidExecutionEnvironment(ExecutionEnvironment executionEnvironment,
			Profile profile) {
		NamedSet<ProcessorBinding> processorBindings = profile.getProcessorBindings();
		for (ProcessorBinding processorBinding : processorBindings) {
			Activity activity = processorBinding.getBoundActivity();
			if (!executionEnvironment.activityExists(activity.getType())) {
				logger.fine(MessageFormat.format("{0} does not contain activity {1}",
						executionEnvironment.getName(), activity.getType()));
				return false;
			}
			Configuration activityConfiguration = scufl2Tools.configurationFor(activity, profile);
			if (!isValidActivityConfiguration(executionEnvironment, activityConfiguration, activity)) {
				logger.fine(MessageFormat.format("Invalid activity configuration for {1} in {0}",
						executionEnvironment.getName(), activity.getType()));
				return false;
			}
			Processor processor = processorBinding.getBoundProcessor();
			for (DispatchStackLayer dispatchStackLayer : processor.getDispatchStack()) {
				if (!executionEnvironment.dispatchLayerExists(dispatchStackLayer
						.getType())) {
					logger.fine(MessageFormat.format("{0} does not contain dispatch layer {1}",
							executionEnvironment.getName(),
							dispatchStackLayer.getType()));
					return false;
				}

				List<Configuration> dispatchLayerConfigurations = scufl2Tools.configurationsFor(dispatchStackLayer, profile);
				if (dispatchLayerConfigurations.size() > 1) {
					logger.fine(MessageFormat.format("{0} contains multiple configurations for dispatch layer {1}",
							executionEnvironment.getName(),
							dispatchStackLayer.getType()));
				} else if (dispatchLayerConfigurations.size() == 1) {
					if (!isValidDispatchLayerConfiguration(executionEnvironment, dispatchLayerConfigurations.get(0), dispatchStackLayer)) {
						logger.fine(MessageFormat.format("Invalid dispatch layer configuration for {1} in {0}",
								executionEnvironment.getName(), dispatchStackLayer.getType()));
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean isValidActivityConfiguration(ExecutionEnvironment executionEnvironment,
			Configuration configuration, Activity activity) {
		try {
			configuration.getJson();
			configuration.getJsonSchema();
			JsonNode environmentSchema = executionEnvironment.getActivityConfigurationSchema(activity.getType());
			// TODO validate against schema
		} catch (ActivityNotFoundException e) {
			logger.fine(MessageFormat.format("{0} does not contain activity {1}",
					executionEnvironment.getName(), activity.getType()));
			return false;
		} catch (ActivityConfigurationException e) {
			logger.fine(MessageFormat.format("Configuration for {1} is incorrect in {0}",
					executionEnvironment.getName(), activity.getType()));
			return false;
		}
		return true;
	}

	private boolean isValidDispatchLayerConfiguration(ExecutionEnvironment executionEnvironment,
			Configuration configuration, DispatchStackLayer dispatchLayer) {
		try {
			JsonNode environmentSchema = executionEnvironment.getDispatchLayerConfigurationSchema(dispatchLayer.getType());
			// TODO validate against schema
		} catch (DispatchLayerNotFoundException e) {
			logger.fine(MessageFormat.format("{0} does not contain dispatch layer {1}",
					executionEnvironment.getName(), dispatchLayer.getType()));
			return false;
		} catch (DispatchLayerConfigurationException e) {
			logger.fine(MessageFormat.format("Configuration for {1} is incorrect in {0}",
					executionEnvironment.getName(), dispatchLayer.getType()));
			return false;
		}
		return true;
	}

//	/**
//	 * @param propertyResourceDefinition
//	 * @param propertyResource
//	 * @return
//	 */
//	private boolean isValidPropertyResource(Configuration configuration,
//			PropertyResourceDefinition propertyResourceDefinition, PropertyResource propertyResource) {
//		if (!propertyResourceDefinition.getTypeURI().equals(propertyResource.getTypeURI())) {
//			logger.fine(MessageFormat.format(
//					"Property type {0} does not equal property definition type {1}",
//					propertyResource.getTypeURI(), propertyResourceDefinition.getTypeURI()));
//			return false;
//		}
//		List<PropertyDefinition> propertyDefinitions = propertyResourceDefinition
//				.getPropertyDefinitions();
//		Map<URI, SortedSet<PropertyObject>> properties = propertyResource.getProperties();
//		for (PropertyDefinition propertyDefinition : propertyDefinitions) {
//			SortedSet<PropertyObject> propertySet = properties.get(propertyDefinition
//					.getPredicate());
//			if (propertySet == null) {
//				if (propertyDefinition.isRequired()) {
//					logger.fine(MessageFormat.format("Required property {0} is missing",
//							propertyDefinition.getPredicate()));
//					return false;
//				}
//			} else {
//				if (propertySet.size() == 0 && propertyDefinition.isRequired()) {
//					logger.fine(MessageFormat.format("Required property {0} is missing",
//							propertyDefinition.getPredicate()));
//					return false;
//				}
//				if (propertySet.size() > 1 && !propertyDefinition.isMultiple()) {
//					logger.fine(MessageFormat.format(
//							"{0} properties found for singleton property {1}", propertySet.size(),
//							propertyDefinition.getPredicate()));
//					return false;
//				}
//				if (propertySet.size() > 1 && propertyDefinition.isMultiple() && propertyDefinition.isOrdered()) {
//					logger.fine(MessageFormat.format(
//							"{0} property lists found for property {1}", propertySet.size(),
//							propertyDefinition.getPredicate()));
//					return false;
//				}
//				for (PropertyObject property : propertySet) {
//					if (propertyDefinition.isMultiple() && propertyDefinition.isOrdered()) {
//						if (property instanceof PropertyList) {
//							PropertyList propertyList = (PropertyList) property;
//							for (PropertyObject propertyObject : propertyList) {
//								if (!isValidProperty(configuration, propertyDefinition, propertyObject)) {
//									logger.fine(MessageFormat.format("Property {0} is invalid",
//											propertyDefinition.getPredicate()));
//									return false;
//								}
//							}
//						}
//
//					} else if (!isValidProperty(configuration, propertyDefinition, property)) {
//						logger.fine(MessageFormat.format("Property {0} is invalid",
//								propertyDefinition.getPredicate()));
//						return false;
//					}
//				}
//			}
//		}
//		return true;
//	}
//
//	/**
//	 * @param propertyDefinition
//	 * @param property
//	 * @return
//	 */
//	private boolean isValidProperty(Configuration configuration,
//			PropertyDefinition propertyDefinition, PropertyObject property) {
//		if (propertyDefinition instanceof PropertyLiteralDefinition) {
//			if (property instanceof PropertyLiteral) {
//				PropertyLiteralDefinition propertyLiteralDefinition = (PropertyLiteralDefinition) propertyDefinition;
//				PropertyLiteral propertyLiteral = (PropertyLiteral) property;
//				if (!propertyLiteral.getLiteralType().equals(
//						propertyLiteralDefinition.getLiteralType())) {
//					logger.fine(MessageFormat.format(
//							"Property type {0} does not equal property definition type {1}",
//							propertyLiteral.getLiteralType(),
//							propertyLiteralDefinition.getLiteralType()));
//					return false;
//				}
//				LinkedHashSet<String> options = propertyLiteralDefinition.getOptions();
//				if (options != null && options.size() > 0) {
//					if (!options.contains(propertyLiteral.getLiteralValue())) {
//						logger.fine(MessageFormat.format("Property value {0} is not permitted",
//								propertyLiteral.getLiteralValue()));
//						return false;
//					}
//				}
//			} else {
//				logger.fine(MessageFormat.format("Expected a PropertyLiteral but got a {0}",
//						property.getClass().getSimpleName()));
//				return false;
//			}
//		} else if (propertyDefinition instanceof PropertyReferenceDefinition) {
//			if (property instanceof PropertyReference) {
//				PropertyReferenceDefinition propertyReferenceDefinition = (PropertyReferenceDefinition) propertyDefinition;
//				PropertyReference propertyReference = (PropertyReference) property;
//				LinkedHashSet<URI> options = propertyReferenceDefinition.getOptions();
//				if (options != null && options.size() > 0) {
//					if (!options.contains(propertyReference.getResourceURI())) {
//						logger.fine(MessageFormat.format("Property value {0} is not permitted",
//								propertyReference.getResourceURI()));
//						return false;
//					}
//				}
//			} else {
//				logger.fine(MessageFormat.format("Expected a PropertyReference but got a {0}",
//						property.getClass().getSimpleName()));
//				return false;
//			}
//		} else if (propertyDefinition instanceof PropertyResourceDefinition) {
//			if (property instanceof PropertyResource) {
//				PropertyResourceDefinition propertyResourceDefinition = (PropertyResourceDefinition) propertyDefinition;
//				PropertyResource propertyResource = (PropertyResource) property;
//				return isValidPropertyResource(configuration, propertyResourceDefinition,
//						propertyResource);
//			} else if (property instanceof PropertyReference) {
//				// special cases where a PropertyResource is actually a reference to a WorkflowBundle component
//				PropertyReference propertyReference = (PropertyReference) property;
//				WorkflowBundle workflowBundle = scufl2Tools.findParent(WorkflowBundle.class,
//						configuration);
//				URI configUri = uriTools.uriForBean(configuration);
//				URI referenceUri = configUri.resolve(propertyReference.getResourceURI());
//				if (workflowBundle != null) {
//					URI predicate = propertyDefinition.getPredicate();
//					WorkflowBean workflowBean = uriTools.resolveUri(referenceUri, workflowBundle);
//					if (workflowBean == null) {
//						logger.fine(MessageFormat.format(
//								"Cannot resolve {0} in WorkflowBundle {1}",
//								propertyReference.getResourceURI(), workflowBundle.getName()));
//					}
//					if (predicate.equals(SCUFL2.resolve("#definesInputPort"))) {
//						if (workflowBean == null) {
//							return false;
//						}
//						if (!(workflowBean instanceof InputActivityPort)) {
//							logger.fine(MessageFormat.format(
//									"{0} resolved to a {1}, expected a InputActivityPort",
//									propertyReference.getResourceURI(), workflowBean.getClass()
//											.getSimpleName()));
//							return false;
//						}
//					} else if (predicate.equals(SCUFL2.resolve("#definesOutputPort"))) {
//						if (workflowBean == null) {
//							return false;
//						}
//						if (!(workflowBean instanceof OutputActivityPort)) {
//							logger.fine(MessageFormat.format(
//									"{0} resolved to a {1}, expected a OutputActivityPort",
//									propertyReference.getResourceURI(), workflowBean.getClass()
//											.getSimpleName()));
//							return false;
//						}
//					} else {
//						logger.fine(MessageFormat.format("Unexpected reference to {0}", predicate));
//					}
//				} else {
//					logger.fine(MessageFormat
//							.format("Cannot resolve reference to {0} because Configuration {1} not contained within a WorkflowBundle",
//									referenceUri, configuration.getName()));
//				}
//			} else {
//				logger.fine(MessageFormat.format("Expected a PropertyResource or PropertyReference but got a {0}",
//						property.getClass().getSimpleName()));
//				return false;
//			}
//		} else {
//			logger.fine(MessageFormat.format("Unknown propery definition class {0}",
//					propertyDefinition.getClass().getSimpleName()));
//			return false;
//		}
//		return true;
//	}

}
