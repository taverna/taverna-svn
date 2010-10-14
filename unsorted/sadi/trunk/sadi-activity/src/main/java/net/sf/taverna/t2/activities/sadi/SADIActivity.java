/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sadi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.client.Registry;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.common.SADIException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A SADI Activity.
 * 
 * @author David Withers
 */
public class SADIActivity extends AbstractAsynchronousActivity<SADIActivityConfigurationBean> {

	private static final Logger logger = Logger.getLogger(SADIActivity.class);

	private SADIActivityConfigurationBean configurationBean;

	/**
	 * The SADI registry that the service was found in. Required for checking
	 * the service status.
	 */
	private Registry registry;

	/**
	 * The SADI service that the activity invokes.
	 */
	private Service service;

	/**
	 * Tree representations of the service input/output OWL classes.
	 */
	private RestrictionNode inputRestrictionTree, outputRestrictionTree;

	private Map<OntClass, Set<SADIActivityOutputPort>> outputPortClassMapping;
	private Map<OntClass, Set<SADIActivityInputPort>> inputPortClassMapping;

	private Map<String, SADIActivityOutputPort> outputPortMapping;
	private Map<String, SADIActivityInputPort> inputPortMapping;

	/**
	 * Constructs a new SADIActivity.
	 */
	public SADIActivity() {
		OntDocumentManager.getInstance().setCacheModels(false);
		inputPortMapping = new HashMap<String, SADIActivityInputPort>();
		outputPortMapping = new HashMap<String, SADIActivityOutputPort>();
		inputPortClassMapping = new HashMap<OntClass, Set<SADIActivityInputPort>>();
		outputPortClassMapping = new HashMap<OntClass, Set<SADIActivityOutputPort>>();
	}

	@Override
	public void configure(SADIActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		try {
			configurePorts();
		} catch (IOException e) {
			throw new ActivityConfigurationException(e);
		} catch (SADIException e) {
			throw new ActivityConfigurationException(e);
		}
	}

	@Override
	public SADIActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> inputData,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			@SuppressWarnings("unchecked")
			public void run() {
				String inputUri = configurationBean.getServiceURI() + UUID.randomUUID();
				InvocationContext context = callback.getContext();
				ReferenceService referenceService = context.getReferenceService();

				try {
					// resolve inputs
					for (Entry<String, T2Reference> entry : inputData.entrySet()) {
						String portName = entry.getKey();
						T2Reference inputReference = entry.getValue();
						try {
							// try to render as RDF first
							List<?> inputNodes = (List<RDFNode>) referenceService.renderIdentifier(
									inputReference, RDFNode.class, context);
							inputPortMapping.get(portName).setValues(inputUri, inputNodes);
						} catch (ReferenceServiceException e) {
							// not RDF, fall back to string
							List<?> inputValues = (List<String>) referenceService.renderIdentifier(
									inputReference, String.class, context);
							inputPortMapping.get(portName).setValues(inputUri, inputValues);
						}
					}

					if (logger.isDebugEnabled()) {
						logger.debug(SADIUtils.printTree(getInputRestrictionTree()));
					}
					
					List<Resource> inputResources = SADIUtils.getInputResources(
							getInputRestrictionTree(), inputUri);
					
					if (logger.isDebugEnabled()) {
						logger.debug("Input resources:");
						for (Resource resource : inputResources) {
							logger.debug(resource);
						}
					}
					
					// run the activity
					Collection<Triple> outputTriples = service.invokeService(inputResources);

					if (logger.isDebugEnabled()) {
						logger.debug("Output triples:");
						for (Triple triple : outputTriples) {
							logger.debug(triple);
						}
					}

					if (logger.isDebugEnabled()) {
						logger.debug(SADIUtils.printTree(getOutputRestrictionTree()));
					}

					SADIUtils.putOutputResources(getOutputRestrictionTree(), outputTriples,
							inputResources, inputUri);

					// register outputs
					Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

					Collection<SADIActivityOutputPort> outputPorts = outputPortMapping.values();
					for (SADIActivityOutputPort outputPort : outputPorts) {
						if (outputPort.getValues(inputUri) != null) {
                            outputData.put(outputPort.getName(), referenceService.register(outputPort
                                    .getValues(inputUri), outputPort.getDepth(), true, context));
                        } else {
                            // FIXME populate with an empty list?
                            System.out.println(String.format("Empty output detected for '%s'!", outputPort.getName()));
                            outputData.put(outputPort.getName(), referenceService.register(new ArrayList(),
                                    outputPort.getDepth(), true, context));
                        }
						outputPort.clearValues(inputUri);
					}

					// send result to the callback
					callback.receiveResult(outputData, new int[0]);
				} catch (ReferenceServiceException e) {
					logger.warn("ReferenceService error while executing activity", e);
					callback.fail("ReferenceService error while executing activity", e);
				} catch (IOException e) {
					logger.warn("IO error while executing activity", e);
					callback.fail("IO error while executing activity", e);
				} catch (SADIException e) {
					logger.warn("Error while executing activity", e);
					callback.fail("Error while executing activity", e);
				}
			}

		});
	}

	private void configurePorts() throws SADIException, IOException {
		removeInputs();
		RestrictionNode inputRestrictionTree = getInputRestrictionTree();
		inputRestrictionTree.clearSelected();
		List<List<String>> inputRestrictionPaths = configurationBean.getInputRestrictionPaths();
		if (inputRestrictionPaths.size() == 0) {
			inputRestrictionPaths = SADIUtils.getDefaultRestrictionPaths(inputRestrictionTree);
			configurationBean.setInputRestrictionPaths(inputRestrictionPaths);
		}
		int inputDepth = SADIUtils.getMinimumDepth(inputRestrictionPaths);
		for (List<String> path : inputRestrictionPaths) {
			RestrictionNode restriction = SADIUtils.getRestriction(inputRestrictionTree, path);
			restriction.setSelected();
			addInput(restriction, inputDepth > 0 ? inputDepth : 1);
		}

		removeOutputs();
		RestrictionNode outputRestrictionTree = getOutputRestrictionTree();
		outputRestrictionTree.clearSelected();
		List<List<String>> outputRestrictionPaths = configurationBean.getOutputRestrictionPaths();
		if (outputRestrictionPaths.size() == 0) {
			outputRestrictionPaths = SADIUtils.getDefaultRestrictionPaths(outputRestrictionTree);
			configurationBean.setOutputRestrictionPaths(outputRestrictionPaths);
		}
		int outputDepth = SADIUtils.getMinimumDepth(outputRestrictionPaths) + 1;
		for (List<String> path : outputRestrictionPaths) {
			RestrictionNode restriction = SADIUtils.getRestriction(outputRestrictionTree, path);
			restriction.setSelected();
			addOutput(restriction, outputDepth > 0 ? outputDepth : 1);
		}
	}

	/**
	 * Returns the SADI registry that the activity is configured to use.
	 * 
	 * @return the SADI registry that the activity is configured to use
	 * @throws IOException
	 *             if the SADI registry cannot be accessed
	 */
	public Registry getRegistry() throws IOException {
		if (registry == null) {
			registry = SADIRegistries.getRegistry(configurationBean.getSparqlEndpoint(),
					configurationBean.getGraphName());
		}
		return registry;
	}

	/**
	 * Returns the SADI service that this activity invokes.
	 * 
	 * @return the SADI service that this activity invokes
	 * @throws IOException
	 * @throws SADIException
	 */
	public Service getService() throws IOException, SADIException {
		if (service == null) {
			service = getRegistry().getService(configurationBean.getServiceURI());
		}
		return service;
	}

	protected void addInput(RestrictionNode restrictedProperty, int portDepth) {
		String portName = Tools.uniquePortName(restrictedProperty.toString(), inputPorts);
		SADIActivityInputPort inputPort = new SADIActivityInputPort(this, restrictedProperty,
				portName, portDepth);
		addInputPortClassMapping(restrictedProperty.getOntClass(), inputPort);
		inputPortMapping.put(portName, inputPort);
		inputPorts.add(inputPort);
	}

	protected void addOutput(RestrictionNode restrictedProperty, int portDepth) {
		String portName = Tools.uniquePortName(restrictedProperty.toString(), outputPorts);
		SADIActivityOutputPort outputPort = new SADIActivityOutputPort(this, restrictedProperty,
				portName, portDepth);
		addOutputPortClassMapping(restrictedProperty.getOntClass(), outputPort);
		outputPortMapping.put(portName, outputPort);
		outputPorts.add(outputPort);
	}

	protected void addInputPortClassMapping(OntClass ontClass, SADIActivityInputPort inputPort) {
		Set<SADIActivityInputPort> inputs = inputPortClassMapping.get(ontClass);
		if (inputs == null) {
			inputs = new HashSet<SADIActivityInputPort>();
			inputPortClassMapping.put(ontClass, inputs);
		}
		inputs.add(inputPort);
	}

	protected void addOutputPortClassMapping(OntClass ontClass, SADIActivityOutputPort outputPort) {
		Set<SADIActivityOutputPort> outputs = outputPortClassMapping.get(ontClass);
		if (outputs == null) {
			outputs = new HashSet<SADIActivityOutputPort>();
			outputPortClassMapping.put(ontClass, outputs);
		}
		outputs.add(outputPort);
	}

	@Override
	public void removeInputs() {
		super.removeInputs();
		inputPortClassMapping.clear();
		inputPortMapping.clear();
	}

	@Override
	public void removeOutputs() {
		super.removeOutputs();
		outputPortClassMapping.clear();
		outputPortMapping.clear();
	}

	/**
	 * Returns the input ports that consume instances of the specified OWL class.
	 * 
	 * @param ontClass the OWL class
	 * @return the import ports that consume instances of the specified OWL class
	 */
	public Set<SADIActivityInputPort> getInputPortsForClass(OntClass ontClass) {
		Set<SADIActivityInputPort> inputs = inputPortClassMapping.get(ontClass);
		if (inputs == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(inputs);
	}

	/**
	 * Returns the output ports that produce instances of the specified OWL class.
	 * 
	 * @param ontClass the OWL class
	 * @return the output ports that produce instances of the specified OWL class
	 */
	public Set<SADIActivityOutputPort> getOutputPortsForClass(OntClass ontClass) {
		Set<SADIActivityOutputPort> outputs = outputPortClassMapping.get(ontClass);
		if (outputs == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(outputs);
	}

	/**
	 * Returns a tree representations of the service input class.
	 * 
	 * @return a tree representations of the service input class
	 * @throws IOException
	 * @throws SADIException
	 */
	public RestrictionNode getInputRestrictionTree() throws SADIException, IOException {
		if (inputRestrictionTree == null) {
			inputRestrictionTree = SADIUtils.buildRestrictionTree(
				getService().getInputClass(), null, configurationBean.getInputRestrictionPaths()
			);
		}
		return inputRestrictionTree;
	}

	/**
	 * Returns a tree representations of the service output class.
	 * 
	 * @return a tree representations of the service output class
	 * @throws IOException
	 * @throws SADIException
	 */
	public RestrictionNode getOutputRestrictionTree() throws SADIException, IOException {
		if (outputRestrictionTree == null) {
			outputRestrictionTree = SADIUtils.buildRestrictionTree(
				getService().getOutputClass(), getService().getInputClass(), configurationBean.getOutputRestrictionPaths()
			);
		}
		return outputRestrictionTree;
	}

}
