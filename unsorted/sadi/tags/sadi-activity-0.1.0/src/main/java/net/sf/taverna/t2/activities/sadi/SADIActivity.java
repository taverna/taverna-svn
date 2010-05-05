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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.common.SADIException;
import ca.wilkinsonlab.sadi.rdf.RdfRegistry;
import ca.wilkinsonlab.sadi.rdf.RdfService;
import ca.wilkinsonlab.sadi.utils.OwlUtils;
import ca.wilkinsonlab.sadi.utils.PatternSubstitution;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A SADI Activity.
 * 
 * @author David Withers
 */
public class SADIActivity extends AbstractAsynchronousActivity<SADIActivityConfigurationBean> {

	private static final Logger logger = Logger.getLogger(SADIActivity.class);

	private static final PatternSubstitution pattern = new PatternSubstitution(
			"http://purl.oclc.org/SADI/LSRN/(.+)_Record", "http://lsrn.org/$1");

	public static final String RDF_INPUT_PORT = "rdfInput";

	public static final String RDF_OUTPUT_PORT = "rdfOutput";

	private SADIActivityConfigurationBean configurationBean;

	private RdfRegistry registry;

	private RdfService service;

	private Map<OntClass, Set<SADIActivityOutputPort>> outputPortClassMapping;
	private Map<OntClass, Set<SADIActivityInputPort>> inputPortClassMapping;

	public SADIActivity() {
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
				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

				InvocationContext context = callback.getContext();
				ReferenceService referenceService = context.getReferenceService();

				try {
					// resolve inputs
					Model inputModel = ModelFactory.createDefaultModel();
					Map<String, List<String>> inputs = new HashMap<String, List<String>>();
					if (inputData.containsKey(RDF_INPUT_PORT)) {
						// if rdf input is available read it into the model
						String rdfInput = (String) referenceService.renderIdentifier(inputData
								.get(RDF_INPUT_PORT), String.class, context);
						inputModel.read(new StringReader(rdfInput), null);
					} else {
						// else add a resource to the model for each input
						OntClass inputClass = getService().getInputClass();

						for (Entry<String, T2Reference> entry : inputData.entrySet()) {
							T2Reference inputReference = entry.getValue();
							List<String> inputValues = (List<String>) referenceService
									.renderIdentifier(inputReference, String.class, context);

							inputs.put(entry.getKey(), inputValues);

							for (String input : inputValues) {
								addResource(inputModel, inputClass, input);
							}
						}
					}

					List<Statement> inputStatements = inputModel.listStatements(null, RDF.type,
							service.getInputClass()).toList();

					// run the activity
					Model outputModel = service.invokeServiceUnparsed(inputModel);

					// register outputs
//					String inputName = getService().getInputClass().getLabel(null);
					List<String> input = new ArrayList<String>();
					for (Statement statement : inputStatements) {
						input.add(SADIUtils.uriToId(statement.getSubject().getLocalName()));
					}

//					outputData.put(inputName, referenceService.register(input, 1, true, context));

					Map<String, List<Object>> outputs = new HashMap<String, List<Object>>();

					Set<OutputPort> outputPorts = getOutputPorts();
					for (OutputPort outputPort : outputPorts) {
//						if (!outputPort.getName().equals(inputName)) {
							outputs.put(outputPort.getName(), new ArrayList<Object>(input.size()));
//						}
					}

//					for (Statement statement : outputModel.listStatements().toList()) {
//						String subject = statement.getSubject().getLocalName();
//						String predicate = statement.getPredicate().getLocalName();
//						if (outputs.containsKey(predicate)) {
//							int index = input.indexOf(subject);
//							Node node = statement.getObject().asNode();
//							if (node.isLiteral()) {
//								outputs.get(predicate).get(index).add(node.getLiteralValue());
//							} else if (node.isURI()) {
//								outputs.get(predicate).get(index).add(SADIUtils.uriToId(node.getURI()));
//							}
//						}
//					}
//
//					for (Entry<String, List<List<Object>>> output : outputs.entrySet()) {
//						outputData.put(output.getKey(), referenceService.register(
//								output.getValue(), 2, true, context));
//					}

					for (Statement statement : outputModel.listStatements().toList()) {
						String subject = statement.getSubject().getLocalName();
						String predicate = statement.getPredicate().getLocalName();
						if (outputs.containsKey(predicate)) {
							int index = input.indexOf(subject);
//							outputs.get(predicate).add(index, statement);
							outputs.get(predicate).add(statement);
							
//							Node node = statement.getObject().asNode();
//							if (node.isLiteral()) {
//								outputs.get(predicate).add(index, node.getLiteralValue());
//							} else if (node.isURI()) {
//								outputs.get(predicate).add(index, SADIUtils.uriToId(node.getURI()));
//							}
						}
					}

					for (Entry<String, List<Object>> output : outputs.entrySet()) {
						outputData.put(output.getKey(), referenceService.register(
								output.getValue(), 1, true, context));
					}

//					StringWriter stringWriter = new StringWriter();
//					outputModel.write(stringWriter);
//					outputData.put(RDF_OUTPUT_PORT, referenceService.register(stringWriter
//							.toString(), 0, true, context));

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
					callback.fail("Eerror while executing activity", e);
				}
			}

		});
	}

	private void configurePorts() throws SADIException, IOException {
		removeInputs();
//		addInput(RDF_INPUT_PORT, 0, false, null, null);
		addInput(getService().getInputClass(), 1);

		removeOutputs();
//		addOutput(RDF_OUTPUT_PORT, 0);
//		addOutput(getService().getInputClass(), 1);
		Collection<Restriction> restrictions = getService().getRestrictions();
		for (Restriction restriction : restrictions) {
			addOutput(restriction, 1);
		}
	}

	private String mapResourceURI(String uri) throws IOException {
		String resourceURI = null;
		if (pattern.matches(uri)) {
			resourceURI = pattern.execute(uri);
		}
		return resourceURI;
	}

	private void addResource(Model model, OntClass ontClass, String input) throws IOException {
		Resource type = model.createResource(ontClass.getURI());
		model.createResource(mapResourceURI(ontClass.getURI()) + ":" + input, type);
	}

	/**
	 * Returns the registry.
	 * 
	 * @return the registry
	 * @throws IOException
	 */
	public RdfRegistry getRegistry() throws IOException {
		if (registry == null) {
			registry = SADIRegistries.getRegistry(configurationBean.getSparqlEndpoint(), configurationBean
					.getGraphName());
		}
		return registry;
	}

	/**
	 * Returns the service.
	 * 
	 * @return the service
	 * @throws IOException
	 */
	public RdfService getService() throws IOException {
		if (service == null) {
			service = getRegistry().getService(configurationBean.getServiceURI());
		}
		return service;
	}

	protected void addInput(OntClass ontClass, int portDepth) {
		String portName = Tools.uniquePortName(ontClass.getLocalName(), inputPorts);
		SADIActivityInputPort inputPort = new SADIActivityInputPort(this, ontClass, portName, portDepth);
		addInputPortClassMapping(ontClass, inputPort);
		inputPorts.add(inputPort);
	}

	protected void addOutput(Restriction restriction, int portDepth) {
		OntClass ontClass = OwlUtils.getValuesFromAsClass(restriction);
		if (ontClass != null) {
			String portName = Tools.uniquePortName(restriction.getOnProperty().getLocalName(), outputPorts);
			SADIActivityOutputPort outputPort = new SADIActivityOutputPort(this, ontClass,
					portName, portDepth);
			addOutputPortClassMapping(ontClass, outputPort);
			outputPorts.add(outputPort);
		}
	}

	protected void addInputPortClassMapping(OntClass ontClass, SADIActivityInputPort inputPort) {
		if (inputPortClassMapping == null) {
			 inputPortClassMapping = new HashMap<OntClass, Set<SADIActivityInputPort>>();
		}
		Set<SADIActivityInputPort> inputs = inputPortClassMapping.get(ontClass);
		if (inputs == null) {
			inputs = new HashSet<SADIActivityInputPort>();
			inputPortClassMapping.put(ontClass, inputs);
		}
		inputs.add(inputPort);
	}
	
	protected void removeInputPortClassMapping(OntClass ontClass) {
		if (inputPortClassMapping == null) {
			 return;
		}
		Set<SADIActivityInputPort> inputs = inputPortClassMapping.get(ontClass);
		if (inputs == null) {
			return;
		}
		inputs.remove(ontClass);
	}
	
	protected void addOutputPortClassMapping(OntClass ontClass, SADIActivityOutputPort outputPort) {
		if (outputPortClassMapping == null) {
			outputPortClassMapping = new HashMap<OntClass, Set<SADIActivityOutputPort>>();
		}		
		Set<SADIActivityOutputPort> outputs = outputPortClassMapping.get(ontClass);
		if (outputs == null) {
			outputs = new HashSet<SADIActivityOutputPort>();
			outputPortClassMapping.put(ontClass, outputs);
		}
		outputs.add(outputPort);
	}
	
	protected void removeOutputPortClassMapping(OntClass ontClass) {
		if (outputPortClassMapping == null) {
			return;
		}		
		Set<SADIActivityOutputPort> outputs = outputPortClassMapping.get(ontClass);
		if (outputs == null) {
			return;
		}
		outputs.remove(ontClass);
	}
	
	@Override
	public void removeInputs() {
		super.removeInputs();
		if (inputPortClassMapping != null) {
			inputPortClassMapping.clear();
		}
	}
	
	@Override
	public void removeOutputs() {
		super.removeOutputs();
		if (outputPortClassMapping != null) {
			outputPortClassMapping.clear();
		}
	}
	
	public Set<SADIActivityInputPort> getInputPorts(OntClass ontClass) {
		Set<SADIActivityInputPort> inputs = inputPortClassMapping.get(ontClass);
		if (inputs == null ) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(inputs);
	}
	
	public Set<SADIActivityOutputPort> getOutputPorts(OntClass ontClass) {
		Set<SADIActivityOutputPort> outputs = outputPortClassMapping.get(ontClass);
		if (outputs == null ) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(outputs);
	}
	
}
