/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby
 * Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.mobyxml.jdom.MobyObjectClassNSImpl;
import org.biomoby.shared.mobyxml.jdom.jDomUtilities;
import org.embl.ebi.escience.baclava.DataThing;
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
import org.xml.sax.InputSource;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class BiomobyTask implements ProcessorTaskWorker {

	private static Logger logger = Logger.getLogger(BiomobyTask.class);

	private boolean DEBUG = false;

	// private static final int INVOCATION_TIMEOUT = 0;

	private Processor proc;

	Namespace mobyNS = MobyObjectClassNSImpl.MOBYNS;

	public BiomobyTask(Processor p) {
		this.proc = p;
	}

	public Map execute(Map inputMap, ProcessorTask parentTask) throws TaskExecutionException {

		if (DEBUG)
			try {
				System.setOut(new PrintStream(new FileOutputStream(new File("biomobyDebug.rtf"), true)));
			} catch (FileNotFoundException e) {
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
					data.setAttribute("queryID", "a1", mobyNS);
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
			// input port takes precedence over other ports
			try {
				String inputXML = null;
				InputPort[] inputPorts = proc.getBoundInputPorts();
				// create the main xml element that we will add
				// simples/collections too
				Document doc = XMLUtilities.createDomDocument();
				Element root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
				Element content = new Element("mobyContent", MobyObjectClassNSImpl.MOBYNS);
				root.addContent(content);
				doc.addContent(root);

				int totalMobyData = 1;
				// list of mobyData jDom elements
				Vector mobyDatas = new Vector();
				// get the mobyData blocks
				for (int i = 0; i < inputPorts.length; i++) {
					String name = inputPorts[i].getName();
					// only process 'named' ports
					if (name.equalsIgnoreCase("input"))
						continue;

					DataThing inputThing = (DataThing) inputMap.get(name);
					// extract the article name of the input
					String art_name = "";
					if (name.indexOf("(") >= 0 && name.indexOf(")") >= 0) {
						art_name = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
					}

					if (!inputThing.getSyntacticType().startsWith("l(")) {
						// simple data type
						inputXML = (String) inputThing.getDataObject();
						Document inputDocument = XMLUtilities.getDOMDocument(inputXML);

						List simpleCollection = XMLUtilities.createMobySimpleListFromCollection(
								inputXML, ((BiomobyProcessor) proc).getMobyEndpoint(), art_name);

						if (simpleCollection.isEmpty()) {
							// pure 'simples' were passed in as input
							if (XMLUtilities.isMultipleInvokationMessage(inputDocument
									.getRootElement())) {
								// multiple invokation message
								List list = XMLUtilities
										.getSingleInvokationsFromMultipleInvokationMessage(inputDocument
												.getRootElement());
								if (list == null || list.isEmpty()) {
									continue;
								}
								totalMobyData *= list.size();
								// go through and rename the article name
								for (Iterator it = list.iterator(); it.hasNext();) {
									Element md = (Element) it.next();
									boolean useNS = true;
									Element sim = md.getChild("Simple", mobyNS);
									if (sim == null) {
										useNS = false;
										sim = md.getChild("Simple");
									}
									if (sim == null)
										continue;
									if (useNS)
										sim.setAttribute("articleName", art_name, mobyNS);
									else
										sim.setAttribute("articleName", art_name);
								}
								mobyDatas.add(list);
							} else {
								// single invokation
								Element oldMOBY = inputDocument.getRootElement();
								Element oldContent = oldMOBY.getChild("mobyContent", mobyNS);
								if (oldContent == null)
									oldContent = oldMOBY.getChild("mobyContent");
								Element mobyData = oldContent.getChild("mobyData", mobyNS);
								if (mobyData == null)
									mobyData = oldContent.getChild("mobyData");

								// if mobyData is null, we cant go on
								if (mobyData == null)
									throw new TaskExecutionException("The input " + name
											+ " was not a well formed moby message.");
								// rename the article name
								Element child = mobyData.getChild("Simple", mobyNS);
								boolean useNS = true;
								if (child == null) {
									child = mobyData.getChild("Simple");
									useNS = false;
								}
								if (child == null) {
									child = mobyData.getChild("Collection", mobyNS);
									useNS = true;
								}
								if (child == null) {
									child = mobyData.getChild("Collection");
									useNS = false;
								}
								if (child != null) {
									if (useNS)
										child.setAttribute("articleName", art_name, mobyNS);
									else
										child.setAttribute("articleName", art_name);
								}
								ArrayList list = new ArrayList();
								list.add(mobyData);
								mobyDatas.add(list);
							}
						} else {
							Element MOBY = XMLUtilities
									.createMultipleInvocationMessageFromList(simpleCollection);
							// MOBY is a full message with multiple invokations
							List list = XMLUtilities
									.getSingleInvokationsFromMultipleInvokationMessage(MOBY);
							if (list == null || list.isEmpty()) {

								continue;
							}
							totalMobyData *= list.size();
							// go through and rename the article name
							for (Iterator it = list.iterator(); it.hasNext();) {
								Element md = (Element) it.next();
								Element child = md.getChild("Simple", mobyNS);
								boolean useNS = true;
								if (child == null) {
									child = md.getChild("Simple");
									useNS = false;
								}
								if (child == null) {
									child = md.getChild("Collection", mobyNS);
									useNS = true;
								}
								if (child == null) {
									child = md.getChild("Collection");
									useNS = false;
								}
								if (child != null) {
									if (useNS)
										child.setAttribute("articleName", art_name, mobyNS);
									else
										child.setAttribute("articleName", art_name);
								}
							}
							mobyDatas.add(list);
						}
					} else {
						// collection

						// set up the collection article name
						art_name = "";
						if (name.indexOf("'") >= 0 && name.lastIndexOf("'") >= 0) {
							art_name = name.substring(name.indexOf("'") + 1, name.lastIndexOf("'"));
						}
						List listCol = (List) inputThing.getDataObject();
						if (!listCol.isEmpty()) {
							inputXML = (String) listCol.get(0);
							if (DEBUG)
								System.out.println("305:" + listCol.size() + "\n" + inputXML + " "
										+ proc.getName());
						} else {
							if (DEBUG)
								System.out.println("307: empty input list obtained." + " "
										+ proc.getName());
						}
						Document inputDocument = XMLUtilities.getDOMDocument(inputXML);
						if (XMLUtilities
								.isMultipleInvokationMessage(inputDocument.getRootElement())) {
							// multiple invocation
							List list = XMLUtilities
									.getSingleInvokationsFromMultipleInvokationMessage(inputDocument
											.getRootElement());
							if (list == null || list.isEmpty()) {
								continue;
							}

							boolean areAllSimples = XMLUtilities.areAllSimples(list);
							if (areAllSimples) {
								// create a single collection containing all the
								// simples in the mobyData block
								Element newMobyData = new Element("mobyData", mobyNS);
								newMobyData.setAttribute("queryID", "amalgamated", mobyNS);
								Element collectionE = new Element("Collection", mobyNS);
								collectionE.setAttribute("articleName", art_name, mobyNS);
								newMobyData.addContent(collectionE);

								for (Iterator it = list.iterator(); it.hasNext();) {
									Element md = (Element) it.next();

									if (md != null) {
										if (!md.getChildren("Simple").isEmpty()
												|| !md.getChildren("Simple", mobyNS).isEmpty()) {

											collectionE.addContent(md.cloneContent());
										}
									}
								}
								ArrayList al = new ArrayList();
								al.add(newMobyData);
								mobyDatas.add(al);
							} else {
								// create multiple invocations for the
								// collections
								totalMobyData *= list.size();
								for (Iterator it = list.iterator(); it.hasNext();) {
									Element md = (Element) it.next();
									Element child = md.getChild("Collection", mobyNS);
									boolean useNS = true;
									if (child == null) {
										child = md.getChild("Collection");
										useNS = false;
									}
									if (child != null) {
										if (useNS)
											child.setAttribute("articleName", art_name, mobyNS);
										else
											child.setAttribute("articleName", art_name);
									}
								}
								mobyDatas.add(list);
							}

						} else {
							List list = XMLUtilities
									.getSingleInvokationsFromMultipleInvokationMessage(inputDocument
											.getRootElement());
							if (list == null || list.isEmpty()) {
								continue;
							}

							boolean areAllSimples = XMLUtilities.areAllSimples(list);

							// single invocation
							if (DEBUG)
								System.out.println("Collection SIM:\n" + inputXML + " "
										+ proc.getName() + " " + areAllSimples);

							if (areAllSimples) {
								Element md = inputDocument.getRootElement().getChild("mobyContent",
										mobyNS);
								if (md == null)
									md = inputDocument.getRootElement().getChild("mobyContent");

								if (md.getChild("mobyData", mobyNS) != null)
									md = md.getChild("mobyData", mobyNS);
								else
									md = md.getChild("mobyData");

								Element newMd = new Element("mobyData", mobyNS);
								Element newCol = new Element("Collection", mobyNS);
								newMd.addContent(newCol);
								newCol.setAttribute("articleName", art_name, mobyNS);
								newCol.addContent(md.cloneContent());
								if (DEBUG)
									System.out.println("Collection SIM FINISHED:\n"
											+ new XMLOutputter().outputString(newMd) + " 396 "
											+ proc.getName());
								ArrayList al = new ArrayList();
								al.add(newMd);
								mobyDatas.add(al);
							} else {

								Element md = inputDocument.getRootElement().getChild("mobyContent",
										mobyNS);
								if (md == null)
									md = inputDocument.getRootElement().getChild("mobyContent");

								if (md.getChild("mobyData", mobyNS) != null)
									md = md.getChild("mobyData", mobyNS);
								else
									md = md.getChild("mobyData");

								// set the article name
								if (md.getChild("Collection", mobyNS) != null) {
									if (md.getChild("Collection", mobyNS).getAttribute(
											"articleName") != null)
										md.getChild("Collection", mobyNS).setAttribute(
												"articleName", art_name);
									else
										md.getChild("Collection", mobyNS).setAttribute(
												"articleName", art_name, mobyNS);
								} else {
									if (md.getChild("Collection").getAttribute("articleName") != null)
										md.getChild("Collection").setAttribute("articleName",
												art_name);
									else
										md.getChild("Collection", mobyNS).setAttribute(
												"articleName", art_name, mobyNS);
								}
								if (DEBUG)
									System.out.println("Collection SIM FINISHED:\n"
											+ new XMLOutputter().outputString(md) + " "
											+ proc.getName());
								ArrayList al = new ArrayList();
								al.add(md);
								mobyDatas.add(al);
							}
						}

					}
				} // end for loop
				/*
				 * ports have been processed -> vector contains a list of all
				 * the different types of inputs with their article names set
				 * correctly. The elements are from mobyData down. Moreover,
				 * there are totalMobyData number of invocations in the output
				 * moby message
				 */
				Element[] mds = new Element[totalMobyData];
				// initialize the mobydata blocks
				for (int x = 0; x < mds.length; x++) {
					mds[x] = new Element("mobyData", MobyObjectClassNSImpl.MOBYNS);
					mds[x].setAttribute("queryID", "a" + x, MobyObjectClassNSImpl.MOBYNS);
					// add the content
					for (Iterator iter = mobyDatas.iterator(); iter.hasNext();) {
						ArrayList list = (ArrayList) iter.next();
						int index = x % list.size();
						mds[x].addContent(((Element) list.get(index)).cloneContent());
					}
					content.addContent(mds[x].detach());
				}

				// do the task and populate outputXML
				String methodName = ((BiomobyProcessor) proc).getServiceName();
				String serviceEndpoint = ((BiomobyProcessor) proc).getEndpoint().toExternalForm();
				System.out.println("Mobycentral soap call\n"
						+ new MobyObjectClassNSImpl(((BiomobyProcessor) proc).getMobyEndpoint())
								.toString(root) + " " + proc.getName());

				String outputXML = new CentralImpl(serviceEndpoint, "http://biomoby.org/").call(
						methodName, new MobyObjectClassNSImpl(((BiomobyProcessor) proc)
								.getMobyEndpoint()).toString(root));
				if (DEBUG)
					System.out.println("Mobycentral soap call returned\n" + " " + proc.getName()
							+ outputXML);
				Map outputMap = new HashMap();
				// goes through and creates the port 'output'
				processOutputPort(outputXML, outputMap);
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
		// System.out.println(outputXML);
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

			Element collectionElement = mobyDataElement.getChild("Collection", mobyNS);
			if (collectionElement != null) {
				List simpleElements = new ArrayList(collectionElement.getChildren());
				for (Iterator i = simpleElements.iterator(); i.hasNext();) {
					Element simpleElement = (Element) i.next();

					Element newRoot = new Element("MOBY", mobyNS);
					Element newMobyContent = new Element("mobyContent", mobyNS);
					newRoot.addContent(newMobyContent);
					Element newMobyData = new Element("mobyData", mobyNS);
					newMobyData.setAttribute("queryID", "a1", mobyNS);
					newMobyContent.addContent(newMobyData);
					newMobyData.addContent(simpleElement.detach());
					XMLOutputter xo = new XMLOutputter();
					String outputItemString = xo.outputString(new Document(newRoot));
					outputList.add(outputItemString);
				}
			}

			// Return the list (may be empty)
			outputMap.put("output", new DataThing(outputList));
			// TODO think of how to output a list (collection)

		}
	}

	private void processOutputPorts(String outputXML, Map outputMap) throws MobyException {
		if (DEBUG)
			System.out.println("Process outputs:\n" + outputXML);
		// fill in the supplementary moby object ports
		OutputPort[] outputPorts = proc.getBoundOutputPorts();
		Document doc = XMLUtilities.getDOMDocument(outputXML);
		for (int x = 0; x < outputPorts.length; x++) {
			String name = outputPorts[x].getName();
			if (!name.equalsIgnoreCase("output")) {
				// join the data to the output port by parsing outputXML
				// 'empty' ports will have empty skeleton
				Element documentElement = doc.getRootElement();
				if (name.indexOf("(Collection - '") > 0) {
					boolean isMIM = (XMLUtilities.isMultipleInvokationMessage(documentElement));

					// process a collection
					if (name.indexOf("MobyCollection") > 0) {
						// parse out the MyCollection because this object doesnt
						// have
						// a name
						String objectType = name.substring(0, name.indexOf("("));

						if (!isMIM) {
							String mobyCollection = XMLUtilities.getMobyCollection(documentElement,
									objectType, "", ((BiomobyProcessor) proc).getMobyEndpoint());
							if (mobyCollection != null)
								outputMap.put(name, new DataThing(mobyCollection));
						} else {
							// multiple invokation message
							List list = XMLUtilities
									.getSingleInvokationsFromMultipleInvokationMessage(documentElement);
							int queryIDCount = 0;
							// create the main MOBY element
							Element newRoot = new Element("MOBY", mobyNS);
							Element newMobyContent = new Element("mobyContent", mobyNS);
							newRoot.addContent(newMobyContent);
							for (Iterator listIterator = list.iterator(); listIterator.hasNext();) {
								Element md = (Element) listIterator.next();
								Element MOBYwrap = new Element("MOBY");
								Element contentWrap = new Element("mobyContent");
								contentWrap.addContent(md.detach());
								MOBYwrap.addContent(contentWrap);
								String mobyCollection = XMLUtilities
										.getMobyCollection(MOBYwrap, objectType, "",
												((BiomobyProcessor) proc).getMobyEndpoint());
								Document collectionDOM = XMLUtilities
										.getDOMDocument(mobyCollection);
								Element oldMOBY = collectionDOM.getRootElement();
								if (oldMOBY == null)
									continue;
								Element oldContent = oldMOBY.getChild("mobyContent", mobyNS);
								if (oldContent == null)
									oldContent = oldMOBY.getChild("mobyContent");
								if (oldContent == null)
									continue;
								Element oldMD = oldContent.getChild("mobyData");
								if (oldMD == null)
									oldMD = oldContent.getChild("mobyData", mobyNS);
								if (oldMD == null)
									continue;

								Element newMobyData = new Element("mobyData", mobyNS);
								newMobyData.setAttribute("queryID", "a" + queryIDCount++, mobyNS);
								newMobyContent.addContent(newMobyData);
								// remove everythnig below the mobyContent
								newMobyData.addContent(oldMD.cloneContent());
							}
							XMLOutputter xo = new XMLOutputter();
							String outputItemString = xo.outputString(new Document(newRoot));
							outputMap.put(name, new DataThing(outputItemString));
						}
					} else {
						// we have an article name, so extract it and do the
						// same as above
						String objectType = name.substring(0, name.indexOf("("));
						String artName = name.substring(name.indexOf("'") + 1, name
								.lastIndexOf("'")); // modified

						if (!isMIM) {

							String mobyCollection = XMLUtilities.getMobyCollection(documentElement,
									objectType, artName, ((BiomobyProcessor) proc)
											.getMobyEndpoint());
							if (mobyCollection != null)
								outputMap.put(name, new DataThing(mobyCollection));
						} else {
							// multiple invokation message
							List list = XMLUtilities
									.getSingleInvokationsFromMultipleInvokationMessage(documentElement);
							int queryIDCount = 0;
							// create the main MOBY element
							Element newRoot = new Element("MOBY", mobyNS);
							Element newMobyContent = new Element("mobyContent", mobyNS);
							newRoot.addContent(newMobyContent);
							for (Iterator listIterator = list.iterator(); listIterator.hasNext();) {
								Element md = (Element) listIterator.next();
								// create a collectionELement from the dom and
								// add it to md
								Element MOBYwrap = new Element("MOBY");
								Element contentWrap = new Element("mobyContent");
								contentWrap.addContent(md.detach());
								MOBYwrap.addContent(contentWrap);
								String mobyCollection = XMLUtilities.getMobyCollection(MOBYwrap,
										objectType, artName, ((BiomobyProcessor) proc)
												.getMobyEndpoint());
								if (mobyCollection == null)
									continue;
								Document collectionDOM = XMLUtilities
										.getDOMDocument(mobyCollection);
								Element oldMOBY = collectionDOM.getRootElement();
								if (oldMOBY == null)
									continue;
								Element oldContent = oldMOBY.getChild("mobyContent", mobyNS);
								if (oldContent == null)
									oldContent = oldMOBY.getChild("mobyContent");
								if (oldContent == null)
									continue;
								Element oldMD = oldContent.getChild("mobyData");
								if (oldMD == null)
									oldMD = oldContent.getChild("mobyData", mobyNS);
								if (oldMD == null)
									continue;

								Element newMobyData = new Element("mobyData", mobyNS);
								newMobyData.setAttribute("queryID", "a" + queryIDCount++, mobyNS);
								newMobyContent.addContent(newMobyData);
								// remove everythnig below the mobyContent
								newMobyData.addContent(oldMD.cloneContent());
							}
							XMLOutputter xo = new XMLOutputter();
							String outputItemString = xo.outputString(new Document(newRoot));
							if (DEBUG)
								System.out.println("processOUTPUTS() MIM of Collection:\n"
										+ outputItemString);
							outputMap.put(name, new DataThing(outputItemString));
						}
					}
				} else {
					// process simples
					// process multiple invokations

					boolean isMIM = (XMLUtilities.isMultipleInvokationMessage(documentElement));

					if (name.indexOf("(_ANON_)") > 0) {
						// extract the datatype
						String objectType = name.substring(0, name.indexOf("("));
						if (!isMIM) {
							String mobySimple = XMLUtilities.getMobyElement(documentElement,
									objectType, "", null, ((BiomobyProcessor) proc)
											.getMobyEndpoint());
							if (mobySimple != null)
								outputMap.put(name, new DataThing(mobySimple));
						} else {
							// we have a multiple invokation message
							// list of mobyData elements
							List list = XMLUtilities
									.getSingleInvokationsFromMultipleInvokationMessage(documentElement);
							int queryIDCount = 0;
							// create the main MOBY element
							Element newRoot = new Element("MOBY", mobyNS);
							Element newMobyContent = new Element("mobyContent", mobyNS);
							newRoot.addContent(newMobyContent);
							for (Iterator listIterator = list.iterator(); listIterator.hasNext();) {
								Element md = (Element) listIterator.next();
								Element simpleElement = jDomUtilities.getElement("Simple", md,
										new String[] { "articleName=" });
								if (simpleElement == null)
									continue;

								if (simpleElement.getChild(objectType, mobyNS) == null)
									if (simpleElement.getChild(objectType) == null)
										continue;
								Element newMobyData = new Element("mobyData", mobyNS);
								newMobyData.setAttribute("queryID", "a" + queryIDCount++, mobyNS);
								newMobyContent.addContent(newMobyData);
								newMobyData.addContent(simpleElement.detach());
							}
							XMLOutputter xo = new XMLOutputter();
							String outputItemString = xo.outputString(new Document(newRoot));
							outputMap.put(name, new DataThing(outputItemString));

						}
					} else {
						// we have an article name, so extract it and do the
						// same as above
						String objectType = name.substring(0, name.indexOf("("));
						String artName = name.substring(name.indexOf("(") + 1, name.indexOf(")"));

						if (!isMIM) {

							String mobySimple = XMLUtilities.getMobyElement(documentElement,
									objectType, artName, null, ((BiomobyProcessor) proc)
											.getMobyEndpoint());
							if (mobySimple != null)
								outputMap.put(name, new DataThing(mobySimple));
						} else {
							// we have a multiple invokation message
							// list of mobyData elements
							List list = XMLUtilities
									.getSingleInvokationsFromMultipleInvokationMessage(documentElement);
							int queryIDCount = 0;
							// create the main MOBY element
							Element newRoot = new Element("MOBY", mobyNS);
							Element newMobyContent = new Element("mobyContent", mobyNS);
							newRoot.addContent(newMobyContent);
							for (Iterator listIterator = list.iterator(); listIterator.hasNext();) {
								Element md = (Element) listIterator.next();
								Element simpleElement = jDomUtilities.getElement("Simple", md,
										new String[] { "articleName=" + artName });
								if (simpleElement == null)
									continue;

								if (simpleElement.getChild(objectType, mobyNS) == null)
									if (simpleElement.getChild(objectType) == null)
										continue;
								Element newMobyData = new Element("mobyData", mobyNS);
								newMobyData.setAttribute("queryID", "a" + queryIDCount++, mobyNS);
								newMobyContent.addContent(newMobyData);
								newMobyData.addContent(simpleElement.detach());
							}
							if (newMobyContent.getChildren().isEmpty()) {
								Element e = new Element("mobyData", mobyNS);
								e.setAttribute("queryID", "e1", mobyNS);
								newMobyContent.addContent(e.detach());
							}
							XMLOutputter xo = new XMLOutputter();
							String outputItemString = xo.outputString(new Document(newRoot));
							System.out.println("processOUTPUTS() MIM of simple:\n"
									+ outputItemString);
							outputMap.put(name, new DataThing(outputItemString));
						}
					}
				}
			}
		}
		System.out.println(outputMap);
	}

}