/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby
 * Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.service.dashboard.data.ParametersTable;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.Utils;
import org.biomoby.shared.mobyxml.jdom.MobyObjectClassNSImpl;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class BiomobyTask implements ProcessorTaskWorker {

	private static final boolean DEBUG = false;

	private static Logger logger = Logger.getLogger(BiomobyTask.class);

	private static int qCounter = 0;

	// private static final int INVOCATION_TIMEOUT = 0;

	private Processor proc;

	Namespace mobyNS = MobyObjectClassNSImpl.MOBYNS;

	public BiomobyTask(Processor p) {
		this.proc = p;
	}

	public Map execute(Map inputMap, IProcessorTask parentTask) throws TaskExecutionException {
		if (DEBUG) {
			logger.debug("Service " + proc.getName());
			for (Iterator it = inputMap.keySet().iterator(); it.hasNext();) {
				String key = (String) it.next();
				if (((DataThing) inputMap.get(key)).getDataObject() instanceof String) {
					logger.debug("key " + key + "has value of\n"
							+ ((DataThing) inputMap.get(key)).getDataObject());
					continue;
				} else if (((DataThing) inputMap.get(key)).getDataObject() instanceof List) {
					List list = (List) ((DataThing) inputMap.get(key)).getDataObject();
					for (Iterator it2 = list.iterator(); it2.hasNext();) {
						logger.debug("List key " + key + "has value of\n" + it2.next());
					}
				}
			}
			logger.debug("Printing of ports complete.");
		}

		if (inputMap.containsKey("input")) {
			// input port takes precedence over other ports
			try {
				DataThing inputThing = (DataThing) inputMap.get("input");

				InputPort myInput = null;
				InputPort[] myInputs = proc.getInputPorts();
				for (int i = 0; i < myInputs.length; i++) {
					if (myInputs[i].getName().equalsIgnoreCase("input")) {
						myInput = myInputs[i];
						break;
					}
				}
				if (myInput == null)
					throw new TaskExecutionException(
							"The port 'input' was not specified correctly.");
				String inputType = myInput.getSyntacticType();
				// If this is 'text/xml' then the biomoby service consumes
				// a simple and there is no processing to do as that's what we
				// have
				// If this is l('text/xml') biomoby expects a collection but
				// we have a java List of simples - need to convert this into
				// a biomoby collection document
				String inputXML = null;

				if (inputType.equals("'text/xml'")) {
					inputXML = (String) inputThing.getDataObject();
				} else {
					// List of strings containing simple biomoby objects
					List simpleInputs = (List) inputThing.getDataObject();
					// Create the empty collection document
					Element root = new Element("MOBY", mobyNS);
					Element content = new Element("mobyContent", mobyNS);
					root.addContent(content);
					Element data = new Element("mobyData", mobyNS);
					data.setAttribute("queryID", "d" + qCounter++, mobyNS);
					content.addContent(data);
					Element collectionElement = new Element("Collection", mobyNS);
					collectionElement.setAttribute("articleName", "", mobyNS);
					// It is this collection element that's going to acquire the
					// simples
					for (Iterator i = simpleInputs.iterator(); i.hasNext();) {
						String s = (String) i.next();
						Element el = XMLUtilities.getDOMDocument(s).getRootElement();
						Element mobyDataElement = el.getChild("mobyContent", mobyNS).getChild(
								"mobyData", mobyNS);
						// Remove the single 'Simple' child from this...
						Element simpleElement = (Element) mobyDataElement.getChildren().get(0);
						// Tag the simple element onto the collection.
						collectionElement.addContent(simpleElement.detach());
					}
					XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
					inputXML = xo.outputString(new Document(root));
					// Iterate and create the collection,
					// ....inputXML = collectionThing
				}

				// do the task and populate outputXML
				String methodName = ((BiomobyProcessor) proc).getServiceName();
				String serviceEndpoint = ((BiomobyProcessor) proc).getEndpoint().toExternalForm();
				String outputXML = new CentralImpl(serviceEndpoint, "http://biomoby.org/").call(
						methodName, inputXML);
				Map outputMap = new HashMap();
				// goes through and creates the port 'output'
				processOutputPort(outputXML, outputMap);
				// create the other ports
				processOutputPorts(outputXML, outputMap);
				return outputMap;

			} catch (MobyException ex) {
				// a MobyException should be already reasonably formatted
				logger.error("Error invoking biomoby service for biomoby. A MobyException caught",
						ex);
				TaskExecutionException tee = new TaskExecutionException(
						"Task failed due to problem invoking biomoby service.\n" + ex.getMessage());
				tee.initCause(ex);
				throw tee;

			} catch (Exception ex) {
				// details of other exceptions will appear only in a log
				ex.printStackTrace();
				logger.error("Error invoking biomoby service for biomoby", ex);
				TaskExecutionException tee = new TaskExecutionException(
						"Task failed due to problem invoking biomoby service (see details in log)");
				tee.initCause(ex);
				throw tee;
			}
		} else {
			// now try other named ports
			try {

				InputPort myInput = null;
				InputPort[] myInputs = proc.getBoundInputPorts();
				String inputXML = null;
				Element root = new Element("MOBY", XMLUtilities.MOBY_NS);
				Element content = new Element("mobyContent", XMLUtilities.MOBY_NS);
				root.addContent(content);
				int totalMobyDatas = 0;
				Vector mobyDatas = new Vector(); // list of mobyData element
				for (int i = 0; i < myInputs.length; i++) {
					if (myInputs[i].getName().equalsIgnoreCase("input")) {
						continue;
					}
					myInput = myInputs[i];
					if (myInput == null)
						throw new TaskExecutionException("The port '" + myInputs[i].getName()
								+ "' was not specified correctly.");
					// the port name
					String portName = myInput.getName();
					// the article name
					String articleName = "";
					String type = portName;
					if (portName.indexOf("(") >= 0 && portName.indexOf(")") > 0) {
						articleName = portName.substring(portName.indexOf("(") + 1, portName
								.indexOf(")"));

						if (articleName.indexOf("'") >= 0 && articleName.lastIndexOf("'") > 0)
							articleName = articleName.substring(articleName.indexOf("'") + 1,
									articleName.lastIndexOf("'"));

						type = portName.substring(0, portName.indexOf("("));
					}

					String inputType = myInput.getSyntacticType();
					DataThing inputThing = (DataThing) inputMap.get(portName);
					if (!inputType.startsWith("l(")) {
						inputXML = (String) inputThing.getDataObject();
						Element inputElement = null;
						try {
							inputElement = XMLUtilities.getDOMDocument(inputXML).getRootElement();

						} catch (MobyException e) {
							throw new TaskExecutionException(XMLUtilities.newline
									+ "There was an error parsing the input XML:"
									+ XMLUtilities.newline + Utils.format(inputXML, 3)
									+ XMLUtilities.newline + e.getLocalizedMessage());
						}
						// determine whether we have a multiple invocation
						// message
						if (XMLUtilities.isMultipleInvocationMessage(inputElement)) {
							// multiple invocations
							Element[] invocations = XMLUtilities
									.getSingleInvokationsFromMultipleInvokations(inputElement);
							ArrayList list = new ArrayList();
							for (int j = 0; j < invocations.length; j++) {
								Element[] elements = XMLUtilities
										.getListOfCollections(invocations[j]);
								if (elements.length == 0) {
									// single simple
									inputElement = XMLUtilities.renameSimple(articleName, type,
											invocations[j]);
									Element md = XMLUtilities.extractMobyData(inputElement);
									list.add(md);
								} else {
									// collection of simples => create multiple
									// invocation message
									String queryID = XMLUtilities.getQueryID(invocations[j]);
									Element[] simples = XMLUtilities
											.getSimplesFromCollection(invocations[j]);
									for (int k = 0; k < simples.length; k++) {
										Element wrappedSimple = XMLUtilities
												.createMobyDataElementWrapper(simples[k]);
										XMLUtilities.renameSimple(articleName, type, wrappedSimple);
										XMLUtilities.setQueryID(wrappedSimple, queryID + "_+_"
												+ XMLUtilities.getQueryID(wrappedSimple));
										list.add(XMLUtilities.extractMobyData(wrappedSimple));
									}
								}
							}
							if (list.isEmpty())
								continue;
							if (totalMobyDatas < 1)
								totalMobyDatas = 1;
							totalMobyDatas *= list.size();
							mobyDatas.add(list);
						} else {
							// single invocation
							// is this a collection
							Element[] elements = XMLUtilities.getListOfCollections(inputElement);
							if (elements.length == 0) {
								// single simple
								inputElement = XMLUtilities.renameSimple(articleName, type,
										inputElement);
								ArrayList list = new ArrayList();
								Element md = XMLUtilities.extractMobyData(inputElement);
								list.add(md);
								mobyDatas.add(list);
								if (totalMobyDatas < 1)
									totalMobyDatas = 1;
							} else {
								// collection of simples => create multiple
								// invocation message
								String queryID = XMLUtilities.getQueryID(inputElement);
								Element[] simples = XMLUtilities
										.getSimplesFromCollection(inputElement);

								ArrayList list = new ArrayList();
								for (int j = 0; j < simples.length; j++) {
									Element wrappedSimple = XMLUtilities
											.createMobyDataElementWrapper(simples[j]);
									XMLUtilities.renameSimple(articleName, type, wrappedSimple);
									XMLUtilities.setQueryID(wrappedSimple, queryID + "_+_"
											+ XMLUtilities.getQueryID(wrappedSimple));
									list.add(XMLUtilities.extractMobyData(wrappedSimple));
								}
								if (list.isEmpty())
									continue;
								mobyDatas.add(list);
								if (totalMobyDatas < 1)
									totalMobyDatas = 1 * list.size();
								else {
									totalMobyDatas *= list.size();
								}
							}

						}
					} else {
						// we have a collection!

						// inputThing is a list of Strings
						List list = (List) inputThing.getDataObject();
						/* need this map in cases where simples are passed into a service
						 * that wants a collection. each simple is then added into the same collection
						 */
						Map collectionMap = new HashMap();
						for (Iterator it = list.iterator(); it.hasNext();) {
							Element inputElement = null;
							try {
								inputElement = XMLUtilities.getDOMDocument((String) it.next())
										.getRootElement();

							} catch (MobyException e) {
								throw new TaskExecutionException(XMLUtilities.newline
										+ "There was an error parsing the input XML:"
										+ XMLUtilities.newline + Utils.format(inputXML, 3)
										+ XMLUtilities.newline + e.getLocalizedMessage());
							}
							// determine whether we have a multiple invocation
							// message
							if (XMLUtilities.isMultipleInvocationMessage(inputElement)) {
								// multiple invocations (update totalMobyDatas)
								Element[] invocations = XMLUtilities
										.getSingleInvokationsFromMultipleInvokations(inputElement);
								ArrayList mdList = new ArrayList();
								for (int j = 0; j < invocations.length; j++) {
									Element[] elements = XMLUtilities
											.getListOfCollections(invocations[j]);
									if (elements.length == 0) {
										// simple was passed in - wrap it
										Element collection = XMLUtilities
												.extractMobyData(invocations[j]);
										collection = XMLUtilities
												.createMobyDataElementWrapper(collection,
														XMLUtilities.getQueryID(invocations[j]), null);
										collection = XMLUtilities.renameCollection(articleName,
												collection);
										collection = XMLUtilities
												.createMobyDataElementWrapper(collection,
														XMLUtilities.getQueryID(invocations[j]), null);
										mdList.add(XMLUtilities.extractMobyData(collection));
									} else {
										// collection passed in (always 1 passed
										// in)
										Element collection = invocations[j];
										collection = XMLUtilities.renameCollection(articleName,
												collection);
										collection = XMLUtilities
												.createMobyDataElementWrapper(collection,
														XMLUtilities.getQueryID(invocations[j]), null);
										mdList.add(XMLUtilities.extractMobyData(collection));
									}
								}
								if (mdList.isEmpty())
									continue;

								mobyDatas.add(mdList);
								if (totalMobyDatas < 1)
									totalMobyDatas = 1;
								totalMobyDatas *= mdList.size();
							} else {
								// single invocation
								Element[] elements = XMLUtilities
										.getListOfCollections(inputElement);
								if (elements.length == 0) {
									// simple was passed in so wrap it
									Element collection = new Element("Collection",
											XMLUtilities.MOBY_NS);
									collection.addContent(XMLUtilities
											.extractMobyData(inputElement).cloneContent());
									collection = XMLUtilities.createMobyDataElementWrapper(
											collection, XMLUtilities.getQueryID(inputElement), null);
									collection = XMLUtilities.renameCollection(articleName,
											collection);
									collection = XMLUtilities.createMobyDataElementWrapper(
											collection, XMLUtilities.getQueryID(inputElement), null);
									if (collectionMap.containsKey(articleName)) {
										//add the simple to a pre-existing collection
										ArrayList mdList = (ArrayList)collectionMap.remove(articleName);
										mdList.add(XMLUtilities.extractMobyData(collection));
										collectionMap.put(articleName, mdList);
									} else {
										// new collection - add element and increment count
										ArrayList mdList = new ArrayList();
										mdList.add(XMLUtilities.extractMobyData(collection));
										collectionMap.put(articleName, mdList);
										//totalMobyDatas++;
										if (totalMobyDatas < 1)
											totalMobyDatas = 1;
									}
									//mobyDatas.add(mdList);
									//totalMobyDatas++;
								} else {
									// we have a collection
									Element collection = inputElement;
									collection = XMLUtilities.renameCollection(articleName,
											collection);
									ArrayList mdList = new ArrayList();
									collection = XMLUtilities.createMobyDataElementWrapper(
											collection, XMLUtilities.getQueryID(inputElement), null);
									mdList.add(XMLUtilities.extractMobyData(collection));
									if (DEBUG) {
										logger.debug("***********SIM_COLLECTION_IN****************");
										logger.debug(new XMLOutputter(Format
												.getPrettyFormat()).outputString(collection));
										logger.debug("***********SIM_COLLECTION_IN****************");
									}
									mobyDatas.add(mdList);
									if (totalMobyDatas < 1)
										totalMobyDatas = 1;
									
								}
							} // end if MIM
						} // end iteration over inputThing list
						Iterator collectionIterator = collectionMap.keySet().iterator();
						while (collectionIterator.hasNext()) {
							String key = (String)collectionIterator.next();
							List theList = (List)collectionMap.get(key);
							theList = XMLUtilities.mergeCollections(theList, key);
							mobyDatas.add(theList);
						}
					}					
				}

				if (DEBUG) {
					logger.debug("Before MobyData aggregation");
					for (Iterator itr = mobyDatas.iterator(); itr.hasNext();) {
						List eList = (List) itr.next();
						for (int x = 0; x < eList.size(); x++) {
							logger.debug(new XMLOutputter(Format.getPrettyFormat())
									.outputString((Element) eList.get(x)));
						}
					}
					logger.debug("******* End ******");
				}
				/*
				 * ports have been processed -> vector contains a list of all
				 * the different types of inputs with their article names set
				 * correctly. The elements are from mobyData down. Moreover,
				 * there are totalMobyData number of invocations in the output
				 * moby message
				 */
				if (DEBUG) {
					logger.debug("TotalMobyDatas: " + totalMobyDatas);
				}
				Element[] mds = new Element[totalMobyDatas];
				// initialize the mobydata blocks
				for (int x = 0; x < mds.length; x++) {
					mds[x] = new Element("mobyData", XMLUtilities.MOBY_NS);
					String queryID = "";
					// add the content
					for (Iterator iter = mobyDatas.iterator(); iter.hasNext();) {
						ArrayList list = (ArrayList) iter.next();
						int index = x % list.size();
						Element next = ((Element) list.get(index));
						queryID += "_" + XMLUtilities.getQueryID(next);
						mds[x].addContent(next.cloneContent());

					}
					// remove the first _
					if (queryID != null && queryID.length() > 1)
						queryID = queryID.substring(1);
					mds[x].setAttribute("queryID", queryID, XMLUtilities.MOBY_NS);
					// if secondarys exist add them here
					if (((BiomobyProcessor)this.proc).containsSecondaries()) {
						ParametersTable pt = ((BiomobyProcessor)this.proc).getParameterTable();
						Element[] parameters = null;
						parameters = ((BiomobyProcessor)this.proc).getParameterTable().toXML();
						for (int i = 0; i < parameters.length; i++) {
							mds[x].addContent((parameters[i]).detach());
						}
					}
					content.addContent(mds[x].detach());
				}
				
				if (DEBUG) {
					logger.debug("After MobyData aggregation");
					logger.debug(new XMLOutputter(Format.getPrettyFormat())
							.outputString(root));
					logger.debug("******* End ******");
				}
				// do the task and populate outputXML

				String methodName = ((BiomobyProcessor) proc).getServiceName();
				String serviceEndpoint = ((BiomobyProcessor) proc).getEndpoint().toExternalForm();

				String serviceInput = new XMLOutputter(Format.getPrettyFormat()).outputString(root);
				String[] invocations = XMLUtilities
						.getSingleInvokationsFromMultipleInvokations(serviceInput);
				//logger.debug(serviceInput);
				// going to iterate over all invocations so that messages with
				// many mobyData blocks dont timeout.
				logger.debug("Total invocations " + invocations.length);
				if (invocations.length > 0)
					logger.debug("invocation 00");
				for (int inCount = 0; inCount < invocations.length; inCount++) {

					// logger.debug("invocation " + (inCount + 1) + " of " +
					// invocations.length);
					logger.debug(((inCount + 1) % 10 == 0 ? "\b\b" + (inCount + 1) : "\b"
							+ (inCount + 1) % 10));
					if (DEBUG)
						logger.debug("input:\n" + invocations[inCount]);
					if (!XMLUtilities.isEmpty(invocations[inCount]))
						invocations[inCount] = new CentralImpl(serviceEndpoint,
								"http://biomoby.org/").call(methodName, invocations[inCount]);
					if (DEBUG)
						logger.debug("output:\n" + invocations[inCount]);
				}
				String outputXML = XMLUtilities.createMultipleInvokations(invocations);

				Map outputMap = new HashMap();
				// goes through and creates the port 'output'
				processOutputPort(outputXML, outputMap);
				// create the other ports
				processOutputPorts(outputXML, outputMap);
				return outputMap;
			} catch (MobyException ex) {
				// a MobyException should be already reasonably formatted
				logger.error("Error invoking biomoby service for biomoby. A MobyException caught",
						ex);
				TaskExecutionException tee = new TaskExecutionException(
						"Task failed due to problem invoking biomoby service.\n" + ex.getMessage());
				tee.initCause(ex);
				throw tee;

			} catch (Exception ex) {
				// details of other exceptions will appear only in a log
				ex.printStackTrace();
				logger.error("Error invoking biomoby service for biomoby", ex);
				TaskExecutionException tee = new TaskExecutionException(
						"Task failed due to problem invoking biomoby service (see details in log)");
				tee.initCause(ex);
				throw tee;
			}
		}
	}

	private void processOutputPort(String outputXML, Map outputMap) throws TaskExecutionException,
			JDOMException, IOException {
		OutputPort myOutput = null;
		OutputPort[] myOutputs = proc.getOutputPorts();
		for (int i = 0; i < myOutputs.length; i++) {
			if (myOutputs[i].getName().equalsIgnoreCase("output"))
				myOutput = myOutputs[i];
		}
		if (myOutput == null)
			throw new TaskExecutionException("output port is invalid.");
		String outputType = myOutput.getSyntacticType();
		// logger.debug(outputXML);
		// Will be either 'text/xml' or l('text/xml')

		if (outputType.equals("'text/xml'")) {
			outputMap.put("output", new DataThing(outputXML));
		} else {
			List outputList = new ArrayList();
			// Drill into the output xml document creating
			// a list of strings containing simple types
			// add them to the outputList

			// This is in the 'outputXML'
			// --------------------------
			// <?xml version="1.0" encoding="UTF-8"?>
			// <moby:MOBY xmlns:moby="http://www.biomoby.org/moby">
			// <moby:mobyContent>
			// <moby:mobyData queryID='b1'>
			// <Collection articleName="mySequenceCollection">
			// <Simple>
			// <Object namespace="Genbank/gi" id="163483"/>
			// </Simple>
			// <Simple>
			// <Object namespace="Genbank/gi" id="244355"/>
			// </Simple>
			// <Simple>
			// <Object namespace="Genbank/gi" id="533253"/>
			// </Simple>
			// <Simple>
			// <Object namespace="Genbank/gi" id="745290"/>
			// </Simple>
			// </Collection>
			// </moby:mobyData>
			// </moby:mobyContent>
			// </moby:MOBY>

			// And this is what I want to create - several times:
			// --------------------------------------------------
			// <?xml version="1.0" encoding="UTF-8"?>
			// <moby:MOBY xmlns:moby="http://www.biomoby.org/moby">
			// <moby:mobyContent>
			// <moby:mobyData queryID='a1'>
			// <Simple articleName=''>
			// <Object namespace="Genbank/gi" id="163483"/>
			// </Simple>
			// </moby:mobyData>
			// </moby:mobyContent>
			// </moby:MOBY>

			// Create a DOM document from the resulting XML
			SAXBuilder saxBuilder = new SAXBuilder();
			Document doc = saxBuilder.build(new InputSource(new StringReader(outputXML)));
			Element mobyElement = doc.getRootElement();
			Element mobyDataElement = mobyElement.getChild("mobyContent", mobyNS).getChild(
					"mobyData", mobyNS);

			Element collectionElement = null;
			try {
				collectionElement = mobyDataElement.getChild("Collection", mobyNS);
			} catch (Exception e) {
				logger.warn("There was a problem processing the output port.\n" + outputXML);
				try {
					outputList.add(new XMLOutputter(Format.getPrettyFormat())
							.outputString(XMLUtilities.createMobyDataWrapper(XMLUtilities
									.getQueryID(outputXML), XMLUtilities.getServiceNotesAsElement(outputXML))));
				} catch (MobyException me) {
					logger.debug(me);
				}
			}
			if (collectionElement != null) {
				List simpleElements = new ArrayList(collectionElement.getChildren());
				for (Iterator i = simpleElements.iterator(); i.hasNext();) {
					Element simpleElement = (Element) i.next();

					Element newRoot = new Element("MOBY", mobyNS);
					Element newMobyContent = new Element("mobyContent", mobyNS);
					Element serviceNotes = XMLUtilities.getServiceNotesAsElement(outputXML);
					if (serviceNotes != null)
						newMobyContent.addContent(serviceNotes.detach());
					newRoot.addContent(newMobyContent);
					Element newMobyData = new Element("mobyData", mobyNS);
					newMobyContent.addContent(newMobyData);
					newMobyData.addContent(simpleElement.detach());
					try {
						XMLUtilities.setQueryID(newRoot, XMLUtilities.getQueryID(outputXML) + "_+_"
								+ XMLUtilities.getQueryID(newRoot));
					} catch (MobyException e) {
						newMobyData.setAttribute("queryID", "a1", mobyNS);
					}
					XMLOutputter xo = new XMLOutputter();
					String outputItemString = xo.outputString(new Document(newRoot));
					outputList.add(outputItemString);
				}
			}

			// Return the list (may be empty)
			outputMap.put("output", new DataThing(outputList));
		}
	}

	private void processOutputPorts(String outputXML, Map outputMap) throws MobyException {
		OutputPort[] outputPorts = proc.getOutputPorts(); // used to be
		// boundOutputPorts
		boolean isMIM = XMLUtilities.isMultipleInvocationMessage(outputXML);
		for (int x = 0; x < outputPorts.length; x++) {
			String name = outputPorts[x].getName();
			if (!name.equalsIgnoreCase("output")) {
				if (outputPorts[x].getSyntacticType().startsWith("l(")) {
					// collection - list of strings
					String articleName = "";
					if (name.indexOf("MobyCollection") > 0) {
						// un-named collection -> ignore it as it is illegal
						// in the api
						// TODO could throw exception
						
						List innerList = new ArrayList();
						outputMap.put(name, new DataThing(innerList));
						continue;
					} else {
						articleName = name.substring(name.indexOf("'") + 1, name.lastIndexOf("'"));
						if (name.indexOf("' As Simples)") > 0) {
							// list of simples wanted
							if (isMIM) {
								String[] invocations = XMLUtilities
										.getSingleInvokationsFromMultipleInvokations(outputXML);
								
								List innerList = new ArrayList();
								for (int i = 0; i < invocations.length; i++) {
									try {
										String collection = XMLUtilities.getWrappedCollection(
												articleName, invocations[i]);
										String[] simples = XMLUtilities.getSimplesFromCollection(articleName, collection);
										for (int j = 0; j < simples.length; j++) {
											innerList.add(XMLUtilities.createMobyDataElementWrapper(simples[j], XMLUtilities.getQueryID(collection)+"_+_s"+qCounter++));
										}
									} catch (MobyException e) {
										// collection didnt exist, so put an
										// empty
										// mobyData
										// TODO keep the original wrapper
										String qID = XMLUtilities.getQueryID(invocations[i]);
										Element empty = XMLUtilities.createMobyDataWrapper(qID, XMLUtilities.getServiceNotesAsElement(outputXML));
										XMLOutputter output = new XMLOutputter(Format
												.getPrettyFormat());
										innerList.add(output.outputString(empty));
									}
								}
								outputMap.put(name, new DataThing(innerList));
							} else {
								// process the single invocation and put string
								// into
								// a
								// list
								try {
									
									List innerList = new ArrayList();
									String collection = XMLUtilities.getWrappedCollection(
											articleName, outputXML);
									
									String[] simples = XMLUtilities.getSimplesFromCollection(articleName, collection);
									for (int i = 0; i < simples.length; i++) {
										innerList.add(XMLUtilities.createMobyDataElementWrapper(simples[i], XMLUtilities.getQueryID(collection)+"_+_s"+qCounter++));
									}
									
									outputMap.put(name, new DataThing(innerList));
								} catch (MobyException e) {
									// TODO keep the original wrapper
									List innerList = new ArrayList();
									// simple didnt exist, so put an empty
									// mobyData
									String qID = XMLUtilities.getQueryID(outputXML);
									Element empty = XMLUtilities.createMobyDataWrapper(qID, XMLUtilities.getServiceNotesAsElement(outputXML));
									XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
									innerList.add(output.outputString(empty));
									outputMap.put(name, new DataThing(innerList));
								}
							}
						} else {
							if (isMIM) {
								// process each invocation and then merge them
								// into
								// a
								// single string
								String[] invocations = XMLUtilities
										.getSingleInvokationsFromMultipleInvokations(outputXML);
								
								List innerList = new ArrayList();
								for (int i = 0; i < invocations.length; i++) {
									try {
										String collection = XMLUtilities.getWrappedCollection(
												articleName, invocations[i]);
										innerList.add(collection);
									} catch (MobyException e) {
										// collection didnt exist, so put an
										// empty
										// mobyData
										// TODO keep the original wrapper
										String qID = XMLUtilities.getQueryID(invocations[i]);
										Element empty = XMLUtilities.createMobyDataWrapper(qID, XMLUtilities.getServiceNotesAsElement(outputXML));
										XMLOutputter output = new XMLOutputter(Format
												.getPrettyFormat());
										innerList.add(output.outputString(empty));
									}
								}
								
								outputMap.put(name, new DataThing(innerList));
							} else {

								try {
									
									List innerList = new ArrayList();
									String collection = XMLUtilities.getWrappedCollection(
											articleName, outputXML);
									innerList.add(collection);
									outputMap.put(name, new DataThing(innerList));
								} catch (MobyException e) {
									// TODO keep the original wrapper
									List innerList = new ArrayList();
									// simple didnt exist, so put an empty
									// mobyData
									String qID = XMLUtilities.getQueryID(outputXML);
									Element empty = XMLUtilities.createMobyDataWrapper(qID, XMLUtilities.getServiceNotesAsElement(outputXML));
									XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
									innerList.add(output.outputString(empty));
									outputMap.put(name, new DataThing(innerList));
								}
							}
						}
					}
				} else {
					// simple - single string
					if (name.indexOf("_ANON_") > 0) {
						// un-named simple -> ignore it as it is illegal in the
						// api
						// TODO could throw exception

						String empty = new XMLOutputter().outputString(XMLUtilities
								.createMobyDataWrapper(XMLUtilities.getQueryID(outputXML), XMLUtilities.getServiceNotesAsElement(outputXML)));
						List innerList = new ArrayList();
						innerList.add(empty);
						outputMap.put(name, new DataThing(innerList));
						continue;
					} else {
						String articleName = name.substring(name.indexOf("(") + 1, name
								.indexOf(")"));
						String objectType = name.substring(0, name.indexOf("("));
						if (isMIM) {

							String[] invocations = XMLUtilities
									.getSingleInvokationsFromMultipleInvokations(outputXML);
							
							ArrayList innerList = new ArrayList();

							for (int i = 0; i < invocations.length; i++) {
								try {
									String simple = XMLUtilities.getWrappedSimple(articleName,
											objectType, invocations[i], ((BiomobyProcessor) proc)
													.getMobyEndpoint());
									innerList.add(simple);
								} catch (MobyException e) {
									// simple didnt exist, so put an empty
									// mobyData
									// TODO keep the original wrapper
									String qID = XMLUtilities.getQueryID(invocations[i]);
									
									Element empty = XMLUtilities.createMobyDataWrapper(qID, XMLUtilities.getServiceNotesAsElement(outputXML));
									XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
									// invocations[i] =
									// output.outputString(empty);
									innerList.add(output.outputString(empty));
								}
							}
							outputMap.put(name, new DataThing(innerList));
						} else {
							// process the single invocation and put into a
							// string
							try {
								String simple = XMLUtilities.getWrappedSimple(articleName,
										objectType, outputXML, ((BiomobyProcessor) proc)
												.getMobyEndpoint());
								ArrayList innerList = new ArrayList();
								innerList.add(simple);
								outputMap.put(name, new DataThing(innerList));
							} catch (MobyException e) {
								// simple didnt exist, so put an empty mobyData
								// TODO keep the original wrapper
								String qID = XMLUtilities.getQueryID(outputXML);
								Element empty = XMLUtilities.createMobyDataWrapper(qID, XMLUtilities.getServiceNotesAsElement(outputXML));
								XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
								ArrayList innerList = new ArrayList();
								innerList.add(output.outputString(empty));
								outputMap.put(name, new DataThing(innerList));
							}
						}
					}
				}
			}
		}
	}

}