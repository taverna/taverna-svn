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
import java.util.Map.Entry;
import java.util.Set;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.SADIException;
import ca.wilkinsonlab.sadi.client.Registry;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.rdfpath.RDFPath;
import ca.wilkinsonlab.sadi.utils.LSRNUtils;
import ca.wilkinsonlab.sadi.utils.RdfUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * A SADI Activity.
 * 
 * @author David Withers
 */
public class SADIActivity extends AbstractAsynchronousActivity<SADIActivityConfigurationBean> {
	private static final Logger logger = Logger.getLogger(SADIActivity.class);

	static final int INPUT_DEPTH = 1;
	static final int OUTPUT_DEPTH = 2;
	
	private SADIActivityConfigurationBean configurationBean;

	/**
	 * The SADI registry that the service was found in. 
	 * TODO this should no longer be required; in an ideal world, Taverna
	 * would be using the SADI configuration scheme to manage registries,
	 * in which case you'd just have to call Config.getMasterRegistry().getService()
	 * to find the originating registry if you needed it for some reason.
	 */
	private Registry registry;

	/**
	 * The SADI service that the activity invokes.
	 */
	private Service service;

	private Map<String, SADIActivityOutputPort> outputPortMap;
	private Map<String, SADIActivityInputPort> inputPortMap;

	/**
	 * Constructs a new SADIActivity.
	 */
	public SADIActivity() {
//		OntDocumentManager.getInstance().setCacheModels(false);
		inputPortMap = new HashMap<String, SADIActivityInputPort>();
		outputPortMap = new HashMap<String, SADIActivityOutputPort>();
	}

	@Override
	public void configure(SADIActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		
		try {
			service = getRegistry().getService(configurationBean.getServiceURI());
		} catch (IOException e) {
			throw new ActivityConfigurationException(String.format("invalid registry URL: %s", e.getMessage()));
		} catch (SADIException e) {
			logger.error("error loading service definition", e);
			throw new ActivityConfigurationException(String.format("error loading service definition: %s", e.getMessage()));
		}
		
		/* TODO this only needs to happen when the new configurationBean
		 * has been loaded from a saved workflow; given that, is there a
		 * better place to put this?
		 */
		try {
			SADIActivityConfigurationMigration.updateConfiguration(this, service);
		} catch (SADIException e) {
			throw new ActivityConfigurationException("error updating configuration", e);
		}
		
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
		callback.requestRun(new ServiceCall(inputData, callback));
	}

	private void configurePorts() throws SADIException, IOException
	{
		removeInputs();
		Map<String, RDFPath> inputPortMap;
		if (configurationBean.getInputPortMap().isEmpty()) {
			inputPortMap = SADIUtils.getDefaultInputPorts(getService());
			// also update the configuration bean...
			SADIUtils.addPaths(configurationBean.getInputPortMap(), inputPortMap);
		} else {
			inputPortMap = SADIUtils.convertPathMap(configurationBean.getInputPortMap(), getService().getInputClass().getOntModel());
		}
		for (Map.Entry<String, RDFPath> entry: inputPortMap.entrySet())
			addInputPort(entry.getKey(), entry.getValue());

		removeOutputs();
		Map<String, RDFPath> outputPortMap;
		if (configurationBean.getOutputPortMap().isEmpty()) {
			outputPortMap = SADIUtils.getDefaultOutputPorts(getService());
			// also update the configuration bean...
			SADIUtils.addPaths(configurationBean.getOutputPortMap(), outputPortMap);
		} else {
			outputPortMap = SADIUtils.convertPathMap(configurationBean.getOutputPortMap(), getService().getOutputClass().getOntModel());
		}
		for (Map.Entry<String, RDFPath> entry: outputPortMap.entrySet())
			addOutputPort(entry.getKey(), entry.getValue());
	}

	/**
	 * Returns the SADI registry that the activity is configured to use.
	 * 
	 * @return the SADI registry that the activity is configured to use
	 * @throws IOException if the URL of the registry is invalid
	 */
	public Registry getRegistry() throws IOException {
		/* we can't just instantiate in configure() because this is also
		 * used by the HealthChecker...
		 */
		if (registry == null) {
			registry = SADIRegistries.getRegistry(configurationBean.getSparqlEndpoint(),
					configurationBean.getGraphName());
		}
		return registry;
	}

	/**
	 * Returns the SADI service that this activity invokes.
	 * This will return null if the activity has not been configured.
	 * @return the SADI service that this activity invokes
	 * @throws IOException
	 * @throws SADIException
	 */
	public Service getService() {
		return service;
	}
	
	protected void addInputPort(String name, RDFPath path) {
		SADIActivityInputPort inputPort = new SADIActivityInputPort(this, 
				path, name, INPUT_DEPTH);
//		addInputPortClassMapping(path.getLastPathElement().getType(), inputPort);
		inputPortMap.put(name, inputPort);
		inputPorts.add(inputPort);
	}
	
	protected void addOutputPort(String name, RDFPath path) {
		SADIActivityOutputPort outputPort = new SADIActivityOutputPort(this, 
				path, name, OUTPUT_DEPTH);
//		addOutputPortClassMapping(path.getLastPathElement().getType(), outputPort);
		outputPortMap.put(name, outputPort);
		outputPorts.add(outputPort);
	}

	@Override
	public void removeInputs() {
		super.removeInputs();
		inputPortMap.clear();
	}

	@Override
	public void removeOutputs() {
		super.removeOutputs();
		outputPortMap.clear();
	}

	/**
	 * Returns the input ports that consume instances of the specified OWL class.
	 * 
	 * @param ontClass the OWL class
	 * @return the import ports that consume instances of the specified OWL class
	 */
	public Set<SADIActivityInputPort> getInputPortsForClass(String classURI) {
		Set<SADIActivityInputPort> inputs = new HashSet<SADIActivityInputPort>();
		for (SADIActivityInputPort input: inputPortMap.values()) {
			if (input.getValuesFromURI().equals(classURI))
				inputs.add(input);
		}
		return Collections.unmodifiableSet(inputs);
	}

	/**
	 * Returns the output ports that produce instances of the specified OWL class.
	 * 
	 * @param ontClass the OWL class
	 * @return the output ports that produce instances of the specified OWL class
	 */
	public Set<SADIActivityOutputPort> getOutputPortsForClass(String classURI) {
		// TODO should match subclasses too
		Set<SADIActivityOutputPort> outputs = new HashSet<SADIActivityOutputPort>();
		for (SADIActivityOutputPort output: outputPortMap.values()) {
			if (output.getValuesFromURI().equals(classURI))
				outputs.add(output);
		}
		return Collections.unmodifiableSet(outputs);
	}
	
	private class ServiceCall implements Runnable
	{
		private final Map<String, T2Reference> inputData;
		private final AsynchronousActivityCallback callback;
		private final InvocationContext context;
		private final ReferenceService referenceService;
		private Model model;
		private List<Resource> inputs;
		
		public ServiceCall(Map<String, T2Reference> inputData, 
				 AsynchronousActivityCallback callback)
		{
			this.inputData = inputData;
			this.callback = callback;
			context = callback.getContext();
			referenceService = context.getReferenceService();
			model = ModelFactory.createDefaultModel();
			inputs = new ArrayList<Resource>();
		}
		
		private Resource getInputResource(int index)
		{
			while (inputs.size() <= index)
				inputs.add(model.createResource(RdfUtils.createUniqueURI()));

			return inputs.get(index);
		}
		
		private List<?> getInputValues(T2Reference inputReference)
		{
			try {
				// try to render as RDF first
				return (List<?>) referenceService.renderIdentifier(
						inputReference, RDFNode.class, context);
			} catch (ReferenceServiceException e) {
				// not RDF, fall back to string
				return (List<?>) referenceService.renderIdentifier(
						inputReference, String.class, context);
			}
		}
		
		private List<RDFNode> getInputNodes(List<?> inputValues)
		{
			Model model = ModelFactory.createDefaultModel();
			List<RDFNode> inputNodes = new ArrayList<RDFNode>();
			for (Object inputValue: inputValues) {
				if (inputValue instanceof RDFNode) {
					inputNodes.add((RDFNode)inputValue);
				} else if (inputValue instanceof String) {
					String inputString = (String)inputValue;
					if (inputString.startsWith("<") &&
						inputString.endsWith(">")) {
						Resource resource = model.createResource(StringUtils.substring(inputString, 1, -1));
						// TODO try to resolve resource?
//						try {
//							model.read(resource.getURI());
//						} catch (Exception e) {
//							logger.error(e.getMessage(), e);
//						}
						inputNodes.add(resource);
					} else {
						inputNodes.add(RdfUtils.createTypedLiteral((String)inputValue));
					}
				} else {
					throw new RuntimeException(String.format("input was an unexpected instance of %s", inputValue.getClass()));
				}
			}
			return inputNodes;
		}
		
		public void run()
		{
			try {
				Service service = getService();
				
				// accumulate inputs...
				for (Entry<String, T2Reference> entry : inputData.entrySet()) {
					T2Reference inputReference = entry.getValue();
					List<?> inputValues = getInputValues(inputReference);
					List<RDFNode> inputNodes = getInputNodes(inputValues);
					
					String portName = entry.getKey();
					SADIActivityInputPort inputPort = inputPortMap.get(portName);
					if (inputPort == null) {
						logger.warn(String.format("ignoring data received on unknown port %s", portName));
						continue;
					}
					RDFPath path = inputPort.getRDFPath();
					
					if (path.isEmpty()) {
						/* if the path is empty, that means that each input node is an
						 * instance of the input class, so we can use them directly, 
						 * preserving their URIs, if any...
						 */
						for (RDFNode inputNode: inputNodes) {
							if (inputNode.isResource()) {
								Resource inputResource = inputNode.as(Resource.class);
								if (inputResource.isURIResource()) {
									inputs.add(inputResource);
									// TODO might have to copy contents to our model?
								} else {
									/* inputs to SADI service must have URIs, so
									 * create a named clone of the anonymous resource
									 * and use that instead...
									 */
									Resource anonResource = inputNode.as(Resource.class);
									Resource namedResource = ResourceUtils.renameResource(anonResource, RdfUtils.createUniqueURI());
									inputs.add(namedResource);
									// TODO might have to copy contents to our model?
								}
							} else {
								/* hope the literal is an ID and try to do something with it...
								 */
								if (LSRNUtils.isLSRNType(service.getInputClass())) {
									inputs.add(LSRNUtils.createInstance(model, service.getInputClass(), RdfUtils.getPlainString(inputNode)));
								} else {
									logger.warn(String.format("ignoring literal value '%s' where a resource was expected", inputNode));
								}
							}
						}
					} else {
						/* if the path is not empty, add these values to the model we're
						 * accumulating... 
						 */
						for (int i=0; i<inputNodes.size(); ++i) {
							RDFNode node = (RDFNode)inputNodes.get(i);
							path.addValueRootedAt(getInputResource(i), node);
							if (node.isResource())
								model.add(ResourceUtils.reachableClosure(node.asResource()));
						}
					}
				}
				
				// call the service...
				Model outputTriples = service.invokeService(inputs);
				model.add(outputTriples);
				outputTriples.close();
				
				// TODO do this earlier so we benefit when assembling input data?
				OntModel ontModel = service.getOutputClass().getOntModel();
				ontModel.addSubModel(model);
				ontModel.rebind(); // may not be necessary...

				// register outputs...
				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();
				Collection<SADIActivityOutputPort> outputPorts = outputPortMap.values();
				for (SADIActivityOutputPort outputPort : outputPorts) {
					RDFPath path = outputPort.getRDFPath();
					List<List<RDFNode>> outputLists = new ArrayList<List<RDFNode>>();
					for (Resource input: inputs) {
						List<RDFNode> outputs = new ArrayList<RDFNode>();
						Resource root = input.inModel(ontModel);
						Collection<RDFNode> values = path.getValuesRootedAt(root);
						for (RDFNode value: values) {
							outputs.add(value.inModel(model));
						}
						outputLists.add(outputs);
					}
					outputData.put(outputPort.getName(), referenceService.register(outputLists,
							OUTPUT_DEPTH, true, context));
				}
				
				ontModel.removeSubModel(model);
				ontModel.rebind(); // may not be necessary...

				// send result to the callback
				callback.receiveResult(outputData, new int[0]);
			} catch (ReferenceServiceException e) {
				logger.warn("ReferenceService error while executing activity", e);
				callback.fail("ReferenceService error while executing activity", e);
			} catch (Exception e) {
				logger.warn("Error while executing activity", e);
				callback.fail("Error while executing activity", e);
			}
		}	
	}
}
