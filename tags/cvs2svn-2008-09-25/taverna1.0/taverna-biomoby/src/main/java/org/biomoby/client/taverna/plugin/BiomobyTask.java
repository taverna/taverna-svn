/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby
 * Project
 */

/* 
 * Methods for asynchronous MOBY calls have been developed by
 * José María Fernández, INB.
 */
package org.biomoby.client.taverna.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.service.dashboard.data.ParametersTable;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyPrefixResolver;
import org.biomoby.shared.MobyService;
import org.biomoby.shared.Utils;
import org.biomoby.shared.parser.MobyTags;
import org.biomoby.w3c.addressing.EndpointReference;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.omg.lsae.notifications.AnalysisEvent;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class BiomobyTask implements ProcessorTaskWorker {

	private static final String GET_MULTIPLE_RESOURCE_PROPERTIES_ACTION="http://docs.oasis-open.org/wsrf/rpw-2/GetMultipleResourceProperties/GetMultipleResourcePropertiesRequest";
	private static final String DESTROY_RESOURCE_ACTION="http://docs.oasis-open.org/wsrf/rlw-2/ImmediateResourceTermination/DestroyRequest";

	private static final String RESOURCE_PROPERTIES_NS="http://docs.oasis-open.org/wsrf/rp-2";
	private static final String RESOURCE_LIFETIME_NS="http://docs.oasis-open.org/wsrf/rl-2";

	private static final String XMLNS_NS="http://www.w3.org/2000/xmlns/";
	private static final String XSD_NS="http://www.w3.org/2001/XMLSchema";
	private static final String WSA_NS="http://www.w3.org/2005/08/addressing";
	private static final String WSU_NS="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	private static final String ANON_URI=WSA_NS+"/anonymous";
	private static final String RESULT_PREFIX = "result_";
	private static final String STATUS_PREFIX = "status_";

	private static Logger logger = Logger.getLogger(BiomobyTask.class);

	private static int qCounter = 0;

	// private static final int INVOCATION_TIMEOUT = 0;

	private BiomobyProcessor proc;

	private Namespace mobyNS = XMLUtilities.MOBY_NS;

	private boolean isDebug = logger.isDebugEnabled(); 
	
	public BiomobyTask(Processor p) throws TaskExecutionException {
	    // always should be the case
	    if (p instanceof BiomobyProcessor)
		this.proc = (BiomobyProcessor)p;
	    else
		throw new TaskExecutionException("'" + p.getClass() + "' is not an instanceof BiomobyProcessor!");
	    	
	}

	@SuppressWarnings("unchecked")
	public Map execute(Map inputMap, IProcessorTask parentTask) throws TaskExecutionException {
		
		// execute the service if the service has no inputs (by registry
		// definition)
		if (proc.getMobyService().getPrimaryInputs().length == 0) {
		    try {
			String methodName = proc.getServiceName();
			String serviceEndpoint = proc.getEndpoint().toExternalForm();
			String serviceInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			    + "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">"
			    + "  <moby:mobyContent>"
			    + "    <moby:mobyData moby:queryID=\"sip_1_\" />"
			    + "  </moby:mobyContent>"
			    + "</moby:MOBY>";
			String[] invocations = new String[]{serviceInput};
			// add secondaries
			if (this.proc.containsSecondaries()) {
				@SuppressWarnings("unused")
				ParametersTable pt = this.proc.getParameterTable();
				Element[] parameters = null;
				parameters =  this.proc.getParameterTable().toXML();
				serviceInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				    + "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">"
				    + "  <moby:mobyContent>"
				    + "    <moby:mobyData moby:queryID=\"sip_1_\">";
				XMLOutputter out = new XMLOutputter(Format.getCompactFormat());
				Format format = out.getFormat();
				format.setOmitDeclaration(true);
				out.setFormat(format);
				for (int i = 0; i < parameters.length; i++) {
				    serviceInput += out.outputString(parameters[i]);
				}
				serviceInput += "    </moby:mobyData>" 
				    + "  </moby:mobyContent>"
				    + "</moby:MOBY>";
				format = Format.getPrettyFormat();
				format.setOmitDeclaration(false);
				format.setIndent("   ");
				serviceInput = new XMLOutputter(format).outputString(XMLUtilities.getDOMDocument(serviceInput));
				invocations = new String[]{serviceInput};
			}

			// execute the service that takes no Biomoby datatypes as input
			for (int inCount = 0; inCount < invocations.length; inCount++) {
			    if (isDebug)
				logger.debug("input(" + inCount + "):\n" + invocations[inCount]);
			    // execute a 'moby' service
			    invocations[inCount] = 
				executeService(serviceEndpoint,methodName,invocations[inCount]);
			    if (isDebug)
				logger.debug("output(" + inCount + "):\n" + invocations[inCount]);
			}
			String outputXML = XMLUtilities.createMultipleInvokations(invocations);
			Map outputMap = new HashMap();
			// goes through and creates the port 'output'
			processOutputPort(outputXML, outputMap);
			// create the other ports
			processOutputPorts(outputXML, outputMap);
			return outputMap;
		    } catch (MobyException ex) {
			logger.error(
				"Error invoking biomoby service for biomoby. A MobyException caught",
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
		
		// Legacy 'input' port takes precedence over other ports
		if (inputMap.containsKey("input")) {
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
				String methodName = proc.getServiceName();
				String serviceEndpoint = proc.getEndpoint().toExternalForm();
				String outputXML = executeService(serviceEndpoint, methodName, inputXML);
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

					
					// String inputType = myInput.getSyntacticType();
					DataThing inputThing = (DataThing) inputMap.get(portName);
					if (!inputThing.getSyntacticType().startsWith("l(")) {
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
										wrappedSimple = XMLUtilities.renameSimple(articleName, type, wrappedSimple);
										wrappedSimple = XMLUtilities.setQueryID(wrappedSimple, queryID /*
																										 * +
																										 * "_+_" +
																										 * XMLUtilities.getQueryID(wrappedSimple)
																										 */);
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
									wrappedSimple = XMLUtilities.renameSimple(articleName, type, wrappedSimple);
									wrappedSimple = XMLUtilities.setQueryID(wrappedSimple, queryID /*
																									 * +
																									 * "_+_" +
																									 * XMLUtilities.getQueryID(wrappedSimple)
																									 */);
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
						/*
						 * need this map in cases where simples are passed into
						 * a service that wants a collection. each simple is
						 * then added into the same collection
						 */
						Map collectionMap = new HashMap();
						for (Iterator it = list.iterator(); it.hasNext();) {
							Element inputElement = null;
							String next = (String) it.next();
							try {
								inputElement = XMLUtilities.getDOMDocument(next)
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
								// this is here for mim messages of simples
								Element mimCollection = null;
								String mimQueryID = "";
								for (int j = 0; j < invocations.length; j++) {
									Element[] elements = XMLUtilities
											.getListOfCollections(invocations[j]);
									mimQueryID = XMLUtilities.getQueryID(invocations[j]);
									if (elements.length == 0) {
										if (mimCollection == null)
											mimCollection = new Element("Collection",
													XMLUtilities.MOBY_NS);

										Element theSimple = XMLUtilities
												.extractMobyData(invocations[j]);
										if (theSimple.getChild("Simple") != null)
											theSimple = theSimple.getChild("Simple");
										else if (theSimple.getChild("Simple", XMLUtilities.MOBY_NS) != null)
											theSimple = theSimple.getChild("Simple",
													XMLUtilities.MOBY_NS);
										mimCollection.addContent(theSimple.detach());
										// mimQueryID = mimQueryID + "_" +
										// XMLUtilities.getQueryID(invocations[j]);
									} else {
										// collection passed in (always 1 passed
										// in)
										Element collection = invocations[j];
										collection = XMLUtilities.renameCollection(articleName,
												collection);
										collection = XMLUtilities.createMobyDataElementWrapper(
												collection,
												XMLUtilities.getQueryID(invocations[j]), null);
										mdList.add(XMLUtilities.extractMobyData(collection));
									}
								}
								if (mimCollection != null) {
									mimCollection = XMLUtilities.createMobyDataElementWrapper(
											mimCollection, mimQueryID,
											null);
									mimCollection = XMLUtilities.renameCollection(articleName,
											mimCollection);
									mimCollection = XMLUtilities.createMobyDataElementWrapper(
											mimCollection, mimQueryID,
											null);
									mdList.add(XMLUtilities.extractMobyData(mimCollection));
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
									collection = XMLUtilities
											.createMobyDataElementWrapper(collection, XMLUtilities
													.getQueryID(inputElement), null);
									collection = XMLUtilities.renameCollection(articleName,
											collection);
									collection = XMLUtilities
											.createMobyDataElementWrapper(collection, XMLUtilities
													.getQueryID(inputElement), null);
									if (collectionMap.containsKey(articleName)) {
										// add the simple to a pre-existing
										// collection
										ArrayList mdList = (ArrayList) collectionMap
												.remove(articleName);
										mdList.add(XMLUtilities.extractMobyData(collection));
										collectionMap.put(articleName, mdList);
									} else {
										// new collection - add element and
										// increment count
										ArrayList mdList = new ArrayList();
										mdList.add(XMLUtilities.extractMobyData(collection));
										collectionMap.put(articleName, mdList);
										// totalMobyDatas++;
										if (totalMobyDatas < 1)
											totalMobyDatas = 1;
									}
								} else {
									// we have a collection
									Element collection = inputElement;
									collection = XMLUtilities.renameCollection(articleName,
											collection);
									ArrayList mdList = new ArrayList();
									collection = XMLUtilities
											.createMobyDataElementWrapper(collection, XMLUtilities
													.getQueryID(inputElement), null);
									mdList.add(XMLUtilities.extractMobyData(collection));
									mobyDatas.add(mdList);
									if (totalMobyDatas < 1)
										totalMobyDatas = 1;

								}
							} // end if SIM
						} // end iteration over inputThing list
						Iterator collectionIterator = collectionMap.keySet().iterator();
						while (collectionIterator.hasNext()) {
							String key = (String) collectionIterator.next();
							List theList = (List) collectionMap.get(key);
							theList = XMLUtilities.mergeCollections(theList, key);
							List unwrappedList = new ArrayList();
							for (Iterator it = theList.iterator(); it.hasNext(); ) {
								Element e = (Element)it.next();
								if (XMLUtilities.isWrapped(e))
									unwrappedList.add(XMLUtilities.extractMobyData(e));
								else
									unwrappedList.add(e);
							}
							mobyDatas.add(unwrappedList);
						}
					}
				}

				if (isDebug) {
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
				if (isDebug) {
					logger.debug("TotalMobyDatas: " + totalMobyDatas);
				}
				Element[] mds = new Element[totalMobyDatas];
				// initialize the mobydata blocks
				for (int x = 0; x < mds.length; x++) {
					mds[x] = new Element("mobyData", XMLUtilities.MOBY_NS);
					String queryID = "_";
					// add the content
					for (Iterator iter = mobyDatas.iterator(); iter.hasNext();) {
					    ArrayList list = (ArrayList) iter.next();
					    int index = x % list.size();
					    Element next = ((Element) list.get(index));
					    if (isDebug)
						logger.debug(new XMLOutputter(Format.getPrettyFormat()).outputString(next));
					    // queryID += "_" + XMLUtilities.getQueryID(next);
					    queryID = XMLUtilities.getQueryID(next);
					    mds[x].addContent(next.cloneContent());
					}
					mds[x].setAttribute("queryID", queryID, XMLUtilities.MOBY_NS);
					// if secondarys exist add them here
					if (this.proc.containsSecondaries()) {
						@SuppressWarnings("unused") ParametersTable pt = this.proc.getParameterTable();
						Element[] parameters = null;
						parameters = this.proc.getParameterTable().toXML();
						for (int i = 0; i < parameters.length; i++) {
							mds[x].addContent((parameters[i]).detach());
						}
					}
					content.addContent(mds[x].detach());
				}

				if (isDebug) {
					logger.debug("After MobyData aggregation");
					logger.debug(new XMLOutputter(Format.getPrettyFormat()).outputString(root));
					logger.debug("******* End ******");
				}
				
				String methodName = proc.getServiceName();
				String serviceEndpoint = proc.getEndpoint().toExternalForm();
				String serviceInput = new XMLOutputter(Format.getPrettyFormat()).outputString(root);
				String[] invocations = XMLUtilities
						.getSingleInvokationsFromMultipleInvokations(serviceInput);
				// logger.debug(serviceInput);
				// going to iterate over all invocations so that messages with
				// many mobyData blocks dont timeout.
				if (isDebug)
				    logger.debug("Total invocations " + invocations.length);
				
				// do the task and populate outputXML
				for (int inCount = 0; inCount < invocations.length; inCount++) {
				    if (isDebug)
					logger.debug("input("+inCount+"):\n" + invocations[inCount]);
				    // call the service iff the input is not empty
				    if (!XMLUtilities.isEmpty(invocations[inCount])) {
					invocations[inCount] = 
						executeService(serviceEndpoint,methodName,invocations[inCount]);
				    }
				    if (isDebug)
					logger.debug("output("+inCount+"):\n" + invocations[inCount]);
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
	
	private String executeCgiService(String serviceEndpoint, String xml) throws MobyException {
            try {
        	// Construct data
        	String data = "query=" + 
        	 	URLEncoder.encode(xml, "UTF-8");
        
        	// Send data
        	URL url = new URL(serviceEndpoint);
        	URLConnection conn = url.openConnection();
        	conn.setDoOutput(true);
        	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        	wr.write(data);
        	wr.flush();
        	// Get the response
        	BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        	String line;
        	StringBuffer sb = new StringBuffer();
        	while ((line = rd.readLine()) != null) {
        	    sb.append(line);
        	}
        	wr.close();
        	rd.close();
        	return sb.toString();
            } catch (Exception e) {
        	throw new MobyException(e.getMessage());
            }
	}
	
	private String executeMobyService(String endpoint, String service, String xml) throws MobyException {
	    return new CentralImpl(endpoint,"http://biomoby.org/").call(service,xml);
	}
	
	/**
	 * This method is used to launch an asynchronous MOBY job.
	 * @param endpoint
	 * The endpoint of the service.
	 * @param msName
	 * The MOBY service name.
	 * @param mobyXML
	 * The MOBY payload to be sent to the service.
	 * @return
	 * The EndpointReference object which helds the details of the MOBY asynchronous job.
	 * @throws MobyException
	 */
	private EndpointReference launchMobyAsyncService(String endpoint, String msName, String mobyXML)
		throws MobyException
	{
		try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            Service service=Service.create(new QName(MobyService.BIOMOBY_SERVICE_URI,msName + "Service"));
            QName mQName=new QName(MobyService.BIOMOBY_SERVICE_URI,msName + "Port");
            
            service.addPort(mQName,SOAPBinding.SOAP11HTTP_BINDING,endpoint);
            Dispatch<Source> dispatch = service.createDispatch(mQName,Source.class,Service.Mode.PAYLOAD);
            Map<String,Object> rc = dispatch.getRequestContext();
            rc.put(BindingProvider.SOAPACTION_USE_PROPERTY, new Boolean(true));
            rc.put(BindingProvider.SOAPACTION_URI_PROPERTY, MobyService.BIOMOBY_SERVICE_URI+"#" + msName+"_submit");
            
            org.w3c.dom.Document mobyMessage = db.newDocument();
            org.w3c.dom.Element rootMessage = mobyMessage.createElementNS(MobyService.BIOMOBY_SERVICE_URI, msName + "_submit");
            mobyMessage.appendChild(rootMessage);
            org.w3c.dom.Element data=mobyMessage.createElementNS(MobyService.BIOMOBY_SERVICE_URI, "data");
            rootMessage.appendChild(data);
            data.setAttributeNS(XMLNS_NS,"xmlns:xsi",MobyPrefixResolver.XSI_NAMESPACE2001);
            data.setAttributeNS(XMLNS_NS,"xmlns:xsd",XSD_NS);
            // data.setAttributeNS("http://www.w3.org/2000/xmlns/","xsd","http://www.w3.org/2001/XMLSchema");
            data.setAttributeNS(MobyPrefixResolver.XSI_NAMESPACE2001, "type", "xsd:string");
            data.appendChild(mobyMessage.createTextNode(mobyXML));
            
            Source input=new DOMSource(mobyMessage);
            Source output = dispatch.invoke(input);
            
    		StringWriter sw=new StringWriter();
    		Transformer tr = TransformerFactory.newInstance().newTransformer();
    		tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			tr.transform(output, new StreamResult(sw));
    		
			String response=sw.toString();
            return EndpointReference.createFromXML(response);
		} catch(ParserConfigurationException pce) {
			throw new MobyException("Unable to create document builder for MOBY asynchronous call submission",pce);
		} catch(TransformerConfigurationException tce) {
			throw new MobyException("Unable to create transformer factory for MOBY asynchronous call response",tce);
		} catch(TransformerException te) {
			throw new MobyException("Unable to create transformer for MOBY asynchronous call response",te);
		}
	}
	
	/**
	 * This method issues WSRF getMultipleResourceProperties calls. As this call is used in
	 * BioMOBY for polling and for result fetching, it has an additional parameter which
	 * handles the call mode.
	 * @param msName
	 * The MOBY service name
	 * @param queryIds
	 * The array with the queryIds to use. It may contain null strings
	 * @param epr
	 * The EndpointReference object which helds the MOBY asynchronous job information
	 * @param asResult
	 * If this parameter is true, then this call fetches the results associated to the input queryIds.
	 * If it is false, then this call only asks for the job status.
	 * @return
	 * When at least one of the strings from queryIds array was not null, a SOAPPart object is returned
	 * with the answer for the request issued to the MOBY service.
	 * Otherwise, it returns null. 
	 * @throws SOAPException
	 */
	private SOAPPart getMultipleResourceProperties(String msName,String[] queryIds,EndpointReference epr,boolean asResult)
		throws SOAPException
	{
		String op=asResult?RESULT_PREFIX:STATUS_PREFIX;
		
	    Service service=Service.create(new QName(MobyService.BIOMOBY_SERVICE_URI,msName + "Service"));
	    QName mQName=new QName(MobyService.BIOMOBY_SERVICE_URI,"WSRF_Operations_Port");
	    service.addPort(mQName,SOAPBinding.SOAP11HTTP_BINDING,epr.getAddress());
	    
	    Dispatch<SOAPMessage> dispatch = service.createDispatch(mQName,SOAPMessage.class,Service.Mode.MESSAGE);
	    Map<String,Object> rc = dispatch.getRequestContext();
	    rc.put(BindingProvider.SOAPACTION_USE_PROPERTY, new Boolean(true));
	    rc.put(BindingProvider.SOAPACTION_URI_PROPERTY, GET_MULTIPLE_RESOURCE_PROPERTIES_ACTION);
	    
	    MessageFactory mf = MessageFactory.newInstance();
	    SOAPMessage request = mf.createMessage();
	    SOAPPart part = request.getSOAPPart();
	    
	    String mobyPrefix="mobyws";
	    String wsaPrefix="wsa";
	    String wsuPrefix="wsu";
	    // Obtain the SOAPEnvelope and header and body elements.
	    SOAPEnvelope env = part.getEnvelope();
	    SOAPHeader header = env.getHeader();
	    SOAPBody body = env.getBody();
	
	    header.addNamespaceDeclaration(mobyPrefix, MobyService.BIOMOBY_SERVICE_URI);
	    header.addNamespaceDeclaration(wsaPrefix, WSA_NS);
	    header.addNamespaceDeclaration(wsuPrefix, WSU_NS);
	    // This is for the action
	    SOAPElement actionRoot=header.addChildElement("Action", wsaPrefix, WSA_NS);
	    actionRoot.addAttribute(env.createName("Id", wsuPrefix, WSU_NS),"Action");
	    actionRoot.addTextNode(GET_MULTIPLE_RESOURCE_PROPERTIES_ACTION);
	    
	    // This is for the To
	    SOAPElement toRoot=header.addChildElement("To", wsaPrefix, WSA_NS);
	    toRoot.addAttribute(env.createName("Id", wsuPrefix, WSU_NS),"To");
	    toRoot.addTextNode(epr.getAddress());
	    
	    // And this is for the mobyws
	    SOAPElement mobywsRoot=header.addChildElement("ServiceInvocationId",mobyPrefix,MobyService.BIOMOBY_SERVICE_URI);
	    mobywsRoot.addNamespaceDeclaration(wsaPrefix, WSA_NS);
	    mobywsRoot.addAttribute(env.createName("isReferenceParameter",wsaPrefix,WSA_NS),"true");
	    mobywsRoot.addTextNode(epr.getServiceInvocationId());
	    
	    // At last, the replyto
	    SOAPElement replyRoot=header.addChildElement("ReplyTo", wsaPrefix, WSA_NS);
	    replyRoot.addAttribute(env.createName("Id", wsuPrefix, WSU_NS),"ReplyTo");
	    SOAPElement addr = replyRoot.addChildElement("Address",wsaPrefix,WSA_NS);
	    addr.addTextNode(ANON_URI);
	    
	    // Let's disable the headers
	    // ((WSBindingProvider)dispatch).setOutboundHeaders(headers);
	    
	    // Now the SOAP body
	    SOAPElement smrp=body.addChildElement("GetMultipleResourceProperties","rp",RESOURCE_PROPERTIES_NS);
	    boolean doSubmit=false;
	    for(String queryId: queryIds) {
	    	if(queryId!=null) {
	    		doSubmit=true;
			    SOAPElement sii = smrp.addChildElement("ResourceProperty","rp",RESOURCE_PROPERTIES_NS);
			    sii.addNamespaceDeclaration(mobyPrefix, MobyService.BIOMOBY_SERVICE_URI);
			    sii.addTextNode(mobyPrefix+":"+op+queryId);
	    	}
	    }
	    
	    if(doSubmit) {
		    request.saveChanges();
		    SOAPMessage output = dispatch.invoke(request);
		    
			return output.getSOAPPart();
	    } else {
	    	return null;
	    }
	}
	
	/**
	 * This method free the asynchronous MOBY resources associated to the job identifier
	 * tied to the EndpointReference object passed as input.
	 * @param msName
	 * The MOBY service name
	 * @param epr
	 * The EndpointReference object which holds the MOBY asynchronous job information
	 * @throws SOAPException
	 */
	private void freeAsyncResources(String msName,EndpointReference epr)
		throws SOAPException
	{
	    Service service=Service.create(new QName(MobyService.BIOMOBY_SERVICE_URI,msName + "Service"));
	    QName mQName=new QName(MobyService.BIOMOBY_SERVICE_URI,"WSRF_Operations_Port");
	    service.addPort(mQName,SOAPBinding.SOAP11HTTP_BINDING,epr.getAddress());
	    
	    Dispatch<SOAPMessage> dispatch = service.createDispatch(mQName,SOAPMessage.class,Service.Mode.MESSAGE);
	    Map<String,Object> rc = dispatch.getRequestContext();
	    rc.put(BindingProvider.SOAPACTION_USE_PROPERTY, new Boolean(true));
	    rc.put(BindingProvider.SOAPACTION_URI_PROPERTY, GET_MULTIPLE_RESOURCE_PROPERTIES_ACTION);
	    
	    MessageFactory mf = MessageFactory.newInstance();
	    SOAPMessage request = mf.createMessage();
	    SOAPPart part = request.getSOAPPart();
	    
	    String mobyPrefix="mobyws";
	    String wsaPrefix="wsa";
	    String wsuPrefix="wsu";
	    // Obtain the SOAPEnvelope and header and body elements.
	    SOAPEnvelope env = part.getEnvelope();
	    SOAPHeader header = env.getHeader();
	    SOAPBody body = env.getBody();
	
	    header.addNamespaceDeclaration(mobyPrefix, MobyService.BIOMOBY_SERVICE_URI);
	    header.addNamespaceDeclaration(wsaPrefix, WSA_NS);
	    header.addNamespaceDeclaration(wsuPrefix, WSU_NS);
	    // This is for the action
	    SOAPElement actionRoot=header.addChildElement("Action", wsaPrefix, WSA_NS);
	    actionRoot.addAttribute(env.createName("Id", wsuPrefix, WSU_NS),"Action");
	    actionRoot.addTextNode(DESTROY_RESOURCE_ACTION);
	    
	    // This is for the To
	    SOAPElement toRoot=header.addChildElement("To", wsaPrefix, WSA_NS);
	    toRoot.addAttribute(env.createName("Id", wsuPrefix, WSU_NS),"To");
	    toRoot.addTextNode(epr.getAddress());
	    
	    // And this is for the mobyws
	    SOAPElement mobywsRoot=header.addChildElement("ServiceInvocationId",mobyPrefix,MobyService.BIOMOBY_SERVICE_URI);
	    mobywsRoot.addNamespaceDeclaration(wsaPrefix, WSA_NS);
	    mobywsRoot.addAttribute(env.createName("isReferenceParameter",wsaPrefix,WSA_NS),"true");
	    mobywsRoot.addTextNode(epr.getServiceInvocationId());
	    
	    // At last, the replyto
	    SOAPElement replyRoot=header.addChildElement("ReplyTo", wsaPrefix, WSA_NS);
	    replyRoot.addAttribute(env.createName("Id", wsuPrefix, WSU_NS),"ReplyTo");
	    SOAPElement addr = replyRoot.addChildElement("Address",wsaPrefix,WSA_NS);
	    addr.addTextNode(ANON_URI);
	    
	    // Let's disable the headers
	    // ((WSBindingProvider)dispatch).setOutboundHeaders(headers);
	    
	    // Now the SOAP body
	    body.addChildElement("Destroy","rl",RESOURCE_LIFETIME_NS);
	    
	    request.saveChanges();
	    // We don't mind what it is returned
		dispatch.invoke(request);
	}
	
	/**
	 * This method does the same as getMultipleResourceProperties, with the difference that
	 * it returns an String instead of a SOAPPart object. The result is the serialization of
	 * the SOAPPart output obtained from getMultipleResourceProperties. 
	 * @param msName
	 * The MOBY service name
	 * @param queryIds
	 * The array with the queryIds to use. It may contain null strings
	 * @param epr
	 * The EndpointReference object which helds the MOBY asynchronous job information
	 * @param asResult
	 * If this parameter is true, then this call fetches the results associated to the input queryIds.
	 * If it is false, then this call only asks for the job status.
	 * @return
	 * When at least one of the strings from queryIds array was not null, an String with the serialized
	 * answer from the service.
	 * Otherwise, it returns null. 
	 * @throws SOAPException
	 */
	private String getMultipleResourcePropertiesAsString(String msName,String[] queryIds,EndpointReference epr,boolean asResult)
		throws TransformerConfigurationException, SOAPException, TransformerException 
	{
		SOAPPart result = getMultipleResourceProperties(msName,queryIds,epr,asResult);
		if(result==null)   return null;
		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		
		DOMSource dombody=new DOMSource(result);
	    
		StringWriter sw=new StringWriter();
		tr.transform(dombody, new StreamResult(sw));
		
		return sw.toString();
	}
	
	/**
	 * This method does the check and fetch work related to asynchronous services.
	 * When all the results are fetched, it returns false.
	 * When some recheck must be issued, it returns true.
	 * @param msName
	 * The MOBY service name
	 * @param epr
	 * The EndpointReference, used for the queries
	 * @param queryIds
	 * The array which holds the queryIds to ask for.
	 * It can contain null strings.
	 * @param result
	 * The array which will hold the mobyData results.
	 * This one must have the same size as queryIds array. 
	 * @return
	 * true, if we need more checking iterations.
	 * Otherwise, false
	 * @throws MobyException
	 */
	private boolean checkMobyAsyncJobsStatus(String msName,EndpointReference epr,String[] queryIds,org.w3c.dom.Element[] result)
		throws MobyException
	{
		// Needed to remap results
		HashMap<String, Integer> queryMap = new HashMap<String, Integer>();
		for(int qi=0;qi<queryIds.length;qi++) {
			String queryId=queryIds[qi];
			if(queryId!=null)
				queryMap.put(queryId, new Integer(qi));
		}
		
		if(queryMap.size()==0)
			return false;
		
		try {
			AnalysisEvent[] l_ae=null;
			// First, status from queries
			String response=getMultipleResourcePropertiesAsString(msName,queryIds,epr,false);
			if(response!=null) {
				l_ae=AnalysisEvent.createFromXML(response);
			}
			
			if(l_ae==null || l_ae.length==0) {
				new MobyException("Troubles while checking asynchronous MOBY job status from service "+msName);
			}
			
			ArrayList<String> finishedQueries=new ArrayList<String>();
			// Second, gather those finished queries
			for(int iae=0;iae<l_ae.length ;iae++) {
				AnalysisEvent ae = l_ae[iae];
				if(ae.isCompleted()) {
					String queryId=ae.getQueryId();
					if(!queryMap.containsKey(queryId)) {
						throw new MobyException("Invalid result queryId on asynchronous MOBY job status fetched from "+msName);
					}
					finishedQueries.add(queryId);
				}
			}
			
			// Third, let's fetch the results from the finished queries
			if(finishedQueries.size()>0) {
				String[] resQueryIds=finishedQueries.toArray(new String[0]);
				SOAPPart soapDOM = getMultipleResourceProperties(msName,resQueryIds,epr,true);
				NodeList l_mul = soapDOM.getElementsByTagNameNS(RESOURCE_PROPERTIES_NS, "GetMultipleResourcePropertiesResponse");
				if(l_mul==null || l_mul.getLength()==0) {
					throw new MobyException("Error while fetching asynchronous MOBY results from "+msName);
				}
				
				org.w3c.dom.Element mul=(org.w3c.dom.Element)l_mul.item(0);
				for(org.w3c.dom.Node child=mul.getFirstChild(); child!=null;child=child.getNextSibling()) {
					if(child.getNodeType()==Node.ELEMENT_NODE && MobyService.BIOMOBY_SERVICE_URI.equals(child.getNamespaceURI())) {
						String preQueryId=child.getLocalName();
						int subpos = preQueryId.indexOf(RESULT_PREFIX);
						if(subpos!=0) {
							throw new MobyException("Invalid result prefix on asynchronous MOBY job results fetched from "+msName);
						}
						String queryId=preQueryId.substring(RESULT_PREFIX.length());
						if(!queryMap.containsKey(queryId)) {
								throw new MobyException("Invalid result queryId on asynchronous MOBY job results fetched from "+msName);
						}
						
						org.w3c.dom.Element elchild=(org.w3c.dom.Element) child;
						NodeList l_moby = elchild.getElementsByTagNameNS(MobyPrefixResolver.MOBY_XML_NAMESPACE, MobyTags.MOBYDATA);
						if(l_moby==null || l_moby.getLength()==0)
							l_moby = elchild.getElementsByTagNameNS(MobyPrefixResolver.MOBY_XML_NAMESPACE_INVALID, MobyTags.MOBYDATA);
						
						if(l_moby==null || l_moby.getLength()==0) {
							throw new MobyException("Recovered empty payload from asynchronous MOBY service "+msName);
						}
						Integer queryPos=queryMap.get(queryId);
						result[queryPos]=(org.w3c.dom.Element)l_moby.item(0);
						// Marking as null
						queryIds[queryPos]=null;
					}
				}
			}
			
			return finishedQueries.size()!=queryMap.size();
		} catch(SOAPException se) {
			throw new MobyException("Error while querying MOBY job status",se);
		} catch(TransformerConfigurationException tce) {
			throw new MobyException("Error while preparing to parse MOBY job status",tce);
		} catch(TransformerException te) {
			throw new MobyException("Error while parsing MOBY job status",te);
		}
	}
	
	/**
	 * This method calls the input MOBY service using the asynchronous protocol.
	 * @param endpoint
	 * The endpoint of the service.
	 * @param msName
	 * The MOBY service name.
	 * @param mobyXML
	 * The MOBY payload to be sent to the service.
	 * @return
	 * The MOBY payload with the results from the service.
	 * @throws MobyException
	 */
	private String executeMobyAsyncService(String endpoint, String msName, String mobyXML) throws MobyException {
		// First, let's get the queryIds
		org.w3c.dom.Document message = null;
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        dbf.setNamespaceAware(true);
	        dbf.setValidating(false);
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        
			message = db.parse(new InputSource(new StringReader(mobyXML)));
		} catch(Throwable t) {
			throw new MobyException("Error while parsing input query",t);
		}
		
		NodeList l_data = message.getElementsByTagNameNS(MobyPrefixResolver.MOBY_XML_NAMESPACE, MobyTags.MOBYDATA);
		if(l_data==null || l_data.getLength()==0) {
			l_data = message.getElementsByTagNameNS(MobyPrefixResolver.MOBY_XML_NAMESPACE_INVALID, MobyTags.MOBYDATA);
		}
		
		// Freeing resources
		message=null;
		
		if(l_data==null || l_data.getLength()==0) {
			throw new MobyException("Empty asynchronous MOBY query!");
		}
		
		int nnode=l_data.getLength();
		String[] queryIds = new String[nnode];
		String[] tmpQueryIds = new String[nnode];
		org.w3c.dom.Element[] results= new org.w3c.dom.Element[nnode];
		for(int inode=0;inode<nnode;inode++) {
			String queryId=null; 
			
			org.w3c.dom.Element mdata=(org.w3c.dom.Element)l_data.item(inode);
			
			queryId=mdata.getAttribute(MobyTags.QUERYID);
			if(queryId==null || queryId.length()==0)
				queryId=mdata.getAttributeNS(MobyPrefixResolver.MOBY_XML_NAMESPACE,MobyTags.QUERYID);
			if(queryId==null || queryId.length()==0)
				queryId=mdata.getAttributeNS(MobyPrefixResolver.MOBY_XML_NAMESPACE_INVALID,MobyTags.QUERYID);
			
			if(queryId==null || queryId.length()==0) {
				throw new MobyException("Unable to extract queryId for outgoing MOBY message");
			}
			
			tmpQueryIds[inode] = queryIds[inode] = queryId;
			results[inode]=null;
		}
		
		// Freeing resources
		l_data=null;
		
		// Second, let's launch
		EndpointReference epr = launchMobyAsyncService(endpoint, msName, mobyXML);
		
		// Third, waiting for the results
		try {
			long pollingInterval=proc.getRetryDelay();
			double backoff = proc.getBackoff();
	
			// Max: one minute pollings
			long maxPollingInterval=60000L;
			
			// Min: one second
			if(pollingInterval<=0L)
				pollingInterval=1000L;
			
			// Backoff: must be bigger than 1.0
			if(backoff<=1.0)
				backoff=1.5;
			
			do {
				try {
					Thread.sleep(pollingInterval);
				} catch(InterruptedException ie) {
					// DoNothing(R)
				}
				
				if(pollingInterval!=maxPollingInterval) {
					pollingInterval = (long)((double)pollingInterval*proc.getBackoff());
					if(pollingInterval>maxPollingInterval) {
						pollingInterval=maxPollingInterval;
					}
				}
			} while(checkMobyAsyncJobsStatus(msName, epr, tmpQueryIds, results));
		} finally {
			try {
				freeAsyncResources(msName, epr);
			} catch(SOAPException se) {
				logger.info("An error was fired while freeing MOBY asynchronous resources from "+msName, se);
			}
		}
		
		// Fourth, assembling back the results
		org.w3c.dom.Document resdoc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        dbf.setNamespaceAware(true);
	        dbf.setValidating(false);
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        resdoc = db.newDocument();
	        
	        org.w3c.dom.Element mobyroot = resdoc.createElementNS(MobyPrefixResolver.MOBY_XML_NAMESPACE,MobyTags.MOBY);
	        resdoc.appendChild(mobyroot);
	        org.w3c.dom.Element mobycontent = resdoc.createElementNS(MobyPrefixResolver.MOBY_XML_NAMESPACE,MobyTags.MOBYCONTENT);
	        mobyroot.appendChild(mobycontent);
	        
	        // Results array already contains mobyData
	        
	        for(org.w3c.dom.Element result: results) {
	        	mobycontent.appendChild(resdoc.importNode(result,true));
	        }
		} catch(Throwable t) {
			throw new MobyException("Error while assembling output",t);
		}
		
		// Fifth, returning results
		try {
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			
			DOMSource dombody=new DOMSource(resdoc);
		    
			StringWriter sw=new StringWriter();
			tr.transform(dombody, new StreamResult(sw));
			
			return sw.toString();
		} catch(Throwable t) {
			throw new MobyException("Error while assembling output",t);
		}
	}
	
	private String executeService(String url, String serviceName, String xml) throws MobyException {
	    String serviceCategory = this.proc.getMobyService().getCategory();
	    if (serviceCategory.equals(MobyService.CATEGORY_MOBY)) {
	    	return executeMobyService(url, serviceName, xml);
		} else if (serviceCategory.equals("cgi")) {
			return executeCgiService(url, xml);
		} else if (serviceCategory.equals(MobyService.CATEGORY_MOBY_ASYNC)) {
			return executeMobyAsyncService(url, serviceName, xml);
		}
	    return "";
	}

@SuppressWarnings("unchecked")
	private void processOutputPort(String outputXML, Map outputMap) throws TaskExecutionException,
			JDOMException, IOException {
		OutputPort myOutput = null;
		OutputPort[] myOutputs = proc.getBoundOutputPorts();
		for (int i = 0; i < myOutputs.length; i++) {
			if (myOutputs[i].getName().equalsIgnoreCase("output"))
				myOutput = myOutputs[i];
		}
		if (myOutput == null) {
		    // throw new TaskExecutionException("output port is invalid.");
		    return;
		}
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
				// logger.warn("There was a problem processing the output
				// port.\n" + outputXML);
				try {
					outputList.add(new XMLOutputter(Format.getPrettyFormat())
							.outputString(XMLUtilities.createMobyDataWrapper(XMLUtilities
									.getQueryID(outputXML), XMLUtilities
									.getServiceNotesAsElement(outputXML))));
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

	@SuppressWarnings("unchecked")
	private void processOutputPorts(String outputXML, Map outputMap) throws MobyException {
		OutputPort[] outputPorts = proc.getBoundOutputPorts(); // used to be
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
								Element serviceNotesElement = XMLUtilities
										.getServiceNotesAsElement(outputXML);
								for (int i = 0; i < invocations.length; i++) {
									try {
										String collection = XMLUtilities.getWrappedCollection(
												articleName, invocations[i]);
										String[] simples = XMLUtilities.getSimplesFromCollection(
												articleName, collection);
										String query_id = XMLUtilities.getQueryID(collection);
										for (int j = 0; j < simples.length; j++) {
											innerList.add(XMLUtilities
													.createMobyDataElementWrapper(simples[j],
																	query_id
																	/*
																	 * + "_+_s" +
																	 * qCounter++
																	 */,
															serviceNotesElement));
										}
									} catch (MobyException e) {
										// collection didnt exist, so put an
										// empty
										// mobyData
										// TODO keep the original wrapper
										/*
										 * String qID =
										 * XMLUtilities.getQueryID(invocations[i]);
										 * Element empty =
										 * XMLUtilities.createMobyDataWrapper(qID,
										 * XMLUtilities.getServiceNotesAsElement(outputXML));
										 * XMLOutputter output = new
										 * XMLOutputter(Format
										 * .getPrettyFormat());
										 * innerList.add(output.outputString(empty));
										 */
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

									String[] simples = XMLUtilities.getSimplesFromCollection(
											articleName, collection);
									Element serviceNotesElement = XMLUtilities
											.getServiceNotesAsElement(outputXML);
									String query_id = XMLUtilities.getQueryID(collection);
									for (int i = 0; i < simples.length; i++) {
										innerList
												.add(XMLUtilities.createMobyDataElementWrapper(
														simples[i], query_id
																/*
																 * + "_+_s" +
																 * qCounter++
																 */,
														serviceNotesElement));
									}

									outputMap.put(name, new DataThing(innerList));
								} catch (MobyException e) {
									// TODO keep the original wrapper
									List innerList = new ArrayList();
									/*
									 * // simple didnt exist, so put an empty //
									 * mobyData String qID =
									 * XMLUtilities.getQueryID(outputXML);
									 * Element empty =
									 * XMLUtilities.createMobyDataWrapper(qID,
									 * XMLUtilities.getServiceNotesAsElement(outputXML));
									 * XMLOutputter output = new
									 * XMLOutputter(Format.getPrettyFormat());
									 * innerList.add(output.outputString(empty));
									 */
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
										/*
										 * // collection didnt exist, so put an //
										 * empty // mobyData // TODO keep the
										 * original wrapper String qID =
										 * XMLUtilities.getQueryID(invocations[i]);
										 * Element empty =
										 * XMLUtilities.createMobyDataWrapper(qID,
										 * XMLUtilities.getServiceNotesAsElement(outputXML));
										 * XMLOutputter output = new
										 * XMLOutputter(Format
										 * .getPrettyFormat());
										 * innerList.add(output.outputString(empty));
										 */
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
									/*
									 * String qID =
									 * XMLUtilities.getQueryID(outputXML);
									 * Element empty =
									 * XMLUtilities.createMobyDataWrapper(qID,
									 * XMLUtilities.getServiceNotesAsElement(outputXML));
									 * XMLOutputter output = new
									 * XMLOutputter(Format.getPrettyFormat());
									 * innerList.add(output.outputString(empty));
									 */
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
								.createMobyDataWrapper(XMLUtilities.getQueryID(outputXML),
										XMLUtilities.getServiceNotesAsElement(outputXML)));
						List innerList = new ArrayList();
						innerList.add(empty);
						outputMap.put(name, new DataThing(empty));
						// FIXME outputMap.put(name, new
						// DataThing((Object)null));
						continue;
					} else {
						// TODO what if you make mim messages a single string
						// and then simples always output 'text/xml'?
						String articleName = name.substring(name.indexOf("(") + 1, name
								.indexOf(")"));
						if (isMIM) {

							String[] invocations = XMLUtilities
									.getSingleInvokationsFromMultipleInvokations(outputXML);

							ArrayList innerList = new ArrayList();

							for (int i = 0; i < invocations.length; i++) {
								try {
									String simple = XMLUtilities.getWrappedSimple(articleName,
											invocations[i]);
									innerList.add(simple);
								} catch (MobyException e) {
									// simple didnt exist, so put an empty
									// mobyData
									// TODO keep the original wrapper
									String qID = XMLUtilities.getQueryID(invocations[i]);

									Element empty = XMLUtilities.createMobyDataWrapper(qID,
											XMLUtilities.getServiceNotesAsElement(outputXML));
									XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
									// invocations[i] =
									// output.outputString(empty);
									innerList.add(output.outputString(empty));
									// FIXME outputMap.put(name, new
									// DataThing(""));
								}
							}
							String[] s = new String[innerList.size()];
							s = (String[]) innerList.toArray(s);
							try {
								outputMap.put(name, new DataThing(XMLUtilities
										.createMultipleInvokations(s)));
							} catch (MobyException e) {
								logger.error("Error creating output for service: " + proc.getName()
										+ "." + System.getProperty("line.separator")
										+ e.getMessage());
								outputMap.put(name, new DataThing(""));
							}
						} else {
							// process the single invocation and put into a
							// string
							try {
								String simple = XMLUtilities.getWrappedSimple(articleName,outputXML);
								ArrayList innerList = new ArrayList();
								innerList.add(simple);
								outputMap.put(name, new DataThing(simple));
							} catch (MobyException e) {
								// simple didnt exist, so put an empty mobyData
								// TODO keep the original wrapper
								String qID = XMLUtilities.getQueryID(outputXML);
								Element empty = XMLUtilities.createMobyDataWrapper(qID,
										XMLUtilities.getServiceNotesAsElement(outputXML));
								XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
								ArrayList innerList = new ArrayList();
								innerList.add(output.outputString(empty));
								outputMap.put(name, new DataThing(output.outputString(empty)));
								// FIXME outputMap.put(name, new DataThing(""));
								
							}
						}
					}
				}
			}
		}
	}
}