/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biomoby.shared.MobyException;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A task to parse a Moby Datatype
 * 
 * @author Eddie Kawas
 */
public class MobyParseDatatypeTask implements ProcessorTaskWorker {
	private static Logger logger = Logger.getLogger(MobyParseDatatypeTask.class);

	private MobyParseDatatypeProcessor proc;

	/**
	 * 
	 * @param p
	 *            the processor that this parser is based upon
	 */
	public MobyParseDatatypeTask(Processor p) {
		this.proc = (MobyParseDatatypeProcessor) p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker#execute(java.util.Map,
	 *      uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask)
	 */
	@SuppressWarnings("unchecked")
	public Map execute(java.util.Map workflowInputMap, IProcessorTask parentTask)
			throws TaskExecutionException {

		HashMap output = new HashMap();
		try {

			String inputMapKey = proc.getInputPorts()[0].getName();
			// inputMap wasnt as expected
			if (!workflowInputMap.containsKey(inputMapKey))
				return new HashMap();

			DataThing input = (DataThing) workflowInputMap.get(inputMapKey);
			
			if (input.getDataObject() instanceof String) {
				String inputXML = (String) input.getDataObject();
				if (XMLUtilities.isMultipleInvocationMessage(inputXML)) {
					String[] simples = XMLUtilities.getAllSimplesByArticleName(proc
							.getArticleNameUsedByService(), inputXML);
					for (int x = 0; x < proc.getBoundOutputPorts().length; x++) {
						OutputPort outPort = proc.getBoundOutputPorts()[x];
						if (outPort.getName().equals("namespace")) {
							ArrayList inputs = new ArrayList();
							try {
								processNamespaceFromSimples(simples, inputs);
							} catch (MobyException me) {
								// swallow it. most likely the message was
								// empty
							}
							output.put("namespace", new DataThing(inputs));

						} else if (outPort.getName().equals("id")) {
							ArrayList inputs = new ArrayList();
							try {
								processIdFromSimples(simples, inputs);
							} catch (MobyException me) {
								// swallow it. most likely the message was
								// empty
							}
							output.put("id", new DataThing(inputs));
						} else if (outPort.getName().endsWith("_ns")) {
							ArrayList inputs = new ArrayList();
							try {
								processChildNsFromSimples(simples, inputs, outPort.getName());
							} catch (MobyException me) {
								// swallow it. most likely the message was
								// empty
							}
							output.put(outPort.getName(), new DataThing(inputs));
						} else if (outPort.getName().endsWith("_id")) {
							ArrayList inputs = new ArrayList();
							try {
								processChildIdFromSimples(simples, inputs, outPort.getName());
							} catch (MobyException me) {
								// swallow it. most likely the message was
								// empty
							}
							output.put(outPort.getName(), new DataThing(inputs));
						} else {
							ArrayList inputs = new ArrayList();
							String outputName = outPort.getName();
							try {
								processPortsFromSimples(simples, outPort, inputs);
							} catch (MobyException me) {
								// swallow it. most likely the message was
								// empty
							}
							output.put(outputName, new DataThing(inputs));
						}
					}
					
				} else {
					if (XMLUtilities.isCollection(inputXML)) {
						String[] simples = XMLUtilities.getSimplesFromCollection(proc
								.getArticleNameUsedByService(), inputXML);
						for (int x = 0; x < proc.getBoundOutputPorts().length; x++) {
							OutputPort outPort = proc.getBoundOutputPorts()[x];
							if (outPort.getName().equals("namespace")) {
								ArrayList inputs = new ArrayList();
								try {
									processNamespaceFromSimples(simples, inputs);
								} catch (MobyException me) {
									// swallow it. most likely the message was
									// empty
								}
								output.put("namespace", new DataThing(inputs));

							} else if (outPort.getName().equals("id")) {
								ArrayList inputs = new ArrayList();
								try {
									processIdFromSimples(simples, inputs);
								} catch (MobyException me) {
									// swallow it. most likely the message was
									// empty
								}
								output.put("id", new DataThing(inputs));
							} else if (outPort.getName().endsWith("_ns")) {
								ArrayList inputs = new ArrayList();
								try {
									processChildNsFromSimples(simples, inputs, outPort.getName());
								} catch (MobyException me) {
									// swallow it. most likely the message was
									// empty
								}
								output.put(outPort.getName(), new DataThing(inputs));
							} else if (outPort.getName().endsWith("_id")) {
								ArrayList inputs = new ArrayList();
								try {
									processChildIdFromSimples(simples, inputs, outPort.getName());
								} catch (MobyException me) {
									// swallow it. most likely the message was
									// empty
								}
								output.put(outPort.getName(), new DataThing(inputs));
							} else {
								ArrayList inputs = new ArrayList();
								String outputName = outPort.getName();
								try {
									processPortsFromSimples(simples, outPort, inputs);
								} catch (MobyException me) {
									// swallow it. most likely the message was
									// empty
								}
								output.put(outputName, new DataThing(inputs));
							}
						}

					} else {
						for (int x = 0; x < proc.getBoundOutputPorts().length; x++) {
							OutputPort outPort = proc.getBoundOutputPorts()[x];
							if (outPort.getName().equals("namespace")) {
								try {
									processNamespaceFromSimple(output, inputXML);
								} catch (MobyException me) {
									output.put(outPort.getName(), new DataThing(new ArrayList()));
								}

							} else if (outPort.getName().endsWith("_ns")) {
								try {
									processChildNsFromSimple(output, inputXML, outPort.getName());
								} catch (MobyException me) {
									output.put(outPort.getName(), new DataThing(new ArrayList()));
								}
							} else if (outPort.getName().endsWith("_id")) {
								try {
									processChildIdFromSimple(output, inputXML, outPort.getName());
								} catch (MobyException me) {
									output.put(outPort.getName(), new DataThing(new ArrayList()));
								}
							} else if (outPort.getName().equals("id")) {
								try {
									processIdFromSimple(output, inputXML);
								} catch (MobyException me) {
									output.put(outPort.getName(), new DataThing(new ArrayList()));
								}
							} else {
								try {
									processPortsFromSimple(output, inputXML, outPort);
								} catch (MobyException me) {
									output.put(outPort.getName(), new DataThing(new ArrayList()));
								}
							}
						}
					}
				}

			} else if (input.getDataObject() instanceof List) {
				// logger.debug("Parse a list");
				List list = (List) input.getDataObject();
				// holder contains a list of strings indexed by output port name
				HashMap holder = new HashMap();
				for (Iterator it = list.iterator(); it.hasNext();) {
					String inputXML = (String) it.next();
					if (XMLUtilities.isMultipleInvocationMessage(inputXML)) {
						// this should never happen because outputs from
						// services
						// with multiple invocations are broken down into a list
						// of
						// single invocations.
						logger.warn("2 MIM not implemented yet (or should it be).\n" +
								"If you are seeing this message, please contact the\n" +
								"BioMOBY team and pass along the input that caused\n" +
								"this message, as well as the workflow.");

					} else {
						if (XMLUtilities.isCollection(inputXML)) {
							String[] simples = XMLUtilities.getSimplesFromCollection(proc
									.getArticleNameUsedByService(), inputXML);
							for (int x = 0; x < proc.getBoundOutputPorts().length; x++) {
								OutputPort outPort = proc.getBoundOutputPorts()[x];
								if (outPort.getName().equals("namespace")) {
									ArrayList inputs = new ArrayList();
									try {
										processNamespaceFromSimples(simples, inputs);
									} catch (MobyException me) {
										// swallow it. most likely the message
										// was
										// empty
									}
									if (holder.containsKey("namespace"))
										((ArrayList) holder.get("namespace")).addAll(inputs);
									else
										holder.put("namespace", inputs);

								} else if (outPort.getName().endsWith("_ns")) {
									ArrayList inputs = new ArrayList();
									try {
										processChildNsFromSimples(simples, inputs, outPort
												.getName());
									} catch (MobyException me) {

									}
									if (holder.containsKey(outPort.getName()))
										((ArrayList) holder.get(outPort.getName())).addAll(inputs);
									else
										holder.put(outPort.getName(), inputs);
								} else if (outPort.getName().endsWith("_id")) {
									ArrayList inputs = new ArrayList();
									try {
										processChildIdFromSimples(simples, inputs, outPort
												.getName());
									} catch (MobyException me) {

									}
									if (holder.containsKey(outPort.getName()))
										((ArrayList) holder.get(outPort.getName())).addAll(inputs);
									else
										holder.put(outPort.getName(), inputs);
								} else if (outPort.getName().equals("id")) {
									ArrayList inputs = new ArrayList();
									try {
										processIdFromSimples(simples, inputs);
									} catch (MobyException me) {
										// swallow it. most likely the message
										// was
										// empty
									}
									if (holder.containsKey("id"))
										((ArrayList) holder.get("id")).addAll(inputs);
									else
										holder.put("id", inputs);
								} else {
									ArrayList inputs = new ArrayList();
									String outputName = outPort.getName();
									try {
										processPortsFromSimples(simples, outPort, inputs);
									} catch (MobyException me) {
										// swallow it. most likely the message
										// was
										// empty
									}
									if (holder.containsKey(outputName))
										((ArrayList) holder.get(outputName)).addAll(inputs);
									else
										holder.put(outputName, inputs);
								}
							}

						} else {
							for (int x = 0; x < proc.getBoundOutputPorts().length; x++) {
								OutputPort outPort = proc.getBoundOutputPorts()[x];
								HashMap tMap = new HashMap();
								if (outPort.getName().equals("namespace")) {
									try {
										processNamespaceFromSimple(tMap, inputXML);
									} catch (MobyException me) {
										// swallow it. most likely the message
										// was
										// empty
									}

								} else if (outPort.getName().equals("id")) {
									try {
										processIdFromSimple(tMap, inputXML);
									} catch (MobyException me) {
										// swallow it. most likely the message
										// was
										// empty
									}
								} else if (outPort.getName().endsWith("_ns")) {
									try {
										processChildNsFromSimple(tMap, inputXML, outPort.getName());
									} catch (MobyException me) {
										// swallow it. most likely the message
										// was
										// empty
									}
								} else if (outPort.getName().endsWith("_id")) {
									try {
										processChildIdFromSimple(tMap, inputXML, outPort.getName());
									} catch (MobyException me) {
										// swallow it. most likely the message
										// was
										// empty
									}
								} else {
									try {
										processPortsFromSimple(tMap, inputXML, outPort);
									} catch (MobyException me) {
										//TODO should this be here? should i worry about a list of multiple simples
										tMap.put(outPort.getName(), new DataThing(new ArrayList()));
									}
								}
								if (tMap.size() > 0) {
									for (Iterator tmIt = tMap.keySet().iterator(); tmIt.hasNext();) {
										String outName = (String) tmIt.next();
										DataThing dt = (DataThing) tMap.get(outName);
										if (holder.containsKey(outName)) {
											((ArrayList) holder.get(outName)).add(dt
													.getDataObject());
										} else {
											ArrayList aList = new ArrayList();
											aList.add(dt.getDataObject());
											holder.put(outName, aList);
										}
									}
								}
							}
						}
					}
				}
				for (Iterator tmIt = holder.keySet().iterator(); tmIt.hasNext();) {
					String outName = (String) tmIt.next();
					output.put(outName, new DataThing(holder.get(outName)));
				}
			}
			return output;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new TaskExecutionException("Error parsing moby data: " + ex.getMessage());
		}
	}

	/**
	 * @param output
	 * @param inputXML
	 * @param outPort
	 * @throws MobyException
	 */
	@SuppressWarnings("unchecked")
	private void processPortsFromSimple(HashMap output, String inputXML, OutputPort outPort)
			throws MobyException {
		String parseString = outPort.getName();
		String[] articles = null;
		String nextArtName = "";
		String outputName = outPort.getName();
		String simple = XMLUtilities.getSimple(proc.getArticleNameUsedByService(), inputXML);
		Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
		simElement = getChildOfSimple(simElement);
		if (simElement == null) {
			output.put(outputName, new DataThing(new ArrayList()));
		} else {
			parseString = parseString.substring(proc.getArticleNameUsedByService().length() + 1);
			articles = parseString.split("_'");
			if (!isPrimitive(simElement.getName()))
				for (int i = 0; i < articles.length; i++) {
					nextArtName = articles[i];
					if (nextArtName.startsWith("'"))
						nextArtName = nextArtName.substring(1);
					if (nextArtName.endsWith("'"))
						nextArtName = nextArtName.substring(0, nextArtName.length() - 1);
					simElement = getElementWithArticleName(simElement, nextArtName);
					if (simElement == null) {
						output.put(outputName, new DataThing(new ArrayList()));
						break;
					}
				}
			String string = "";
			if (simElement != null)
				string = simElement.getTextTrim();
			ArrayList theList = new ArrayList();
			theList.add(string);
			output.put(outputName, new DataThing(theList));
		}
	}

	/**
	 * @param output
	 * @param inputXML
	 * @throws MobyException
	 */
	@SuppressWarnings("unchecked")
	private void processIdFromSimple(HashMap output, String inputXML) throws MobyException {
		String simple = XMLUtilities.getSimple(proc.getArticleNameUsedByService(), inputXML);
		Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
		ArrayList theList = new ArrayList();
		theList.add(getElementsMobyID((simElement)));
		output.put("id", new DataThing(theList));
	}

	/**
	 * @param output
	 * @param inputXML
	 * @throws MobyException
	 */
	@SuppressWarnings("unchecked")
	private void processNamespaceFromSimple(HashMap output, String inputXML) throws MobyException {
		String simple = XMLUtilities.getSimple(proc.getArticleNameUsedByService(), inputXML);
		Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
		ArrayList theList = new ArrayList();
		theList.add(getElementsMobyNamespace((simElement)));
		output.put("namespace", new DataThing(theList));
	}

	/**
	 * @param simples
	 * @param outPort
	 * @param inputs
	 * @throws MobyException
	 */
	@SuppressWarnings("unchecked")
	private void processPortsFromSimples(String[] simples, OutputPort outPort, ArrayList inputs)
			throws MobyException {
		for (int i = 0; i < simples.length; i++) {
			String parseString = outPort.getName();
			String[] articles = null;
			String nextArtName = "";
			String simple = simples[i];
			Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
			simElement = getChildOfSimple(simElement);
			if (simElement != null) {
				parseString = parseString
						.substring(proc.getArticleNameUsedByService().length() + 1);
				articles = parseString.split("_'");
				if (!isPrimitive(simElement.getName()))
					for (int j = 0; j < articles.length; j++) {
						nextArtName = articles[j];
						if (nextArtName.startsWith("'"))
							nextArtName = nextArtName.substring(1);
						if (nextArtName.endsWith("'"))
							nextArtName = nextArtName.substring(0, nextArtName.length() - 1);
						simElement = getElementWithArticleName(simElement, nextArtName);
						if (simElement == null) {
							// output.put(outputName, new
							// DataThing(new ArrayList()));
							break;
						}
					}
				if (simElement != null)
					inputs.add(simElement.getTextTrim());
			}
		}
	}

	/**
	 * @param simples
	 * @param inputs
	 * @throws MobyException
	 */
	@SuppressWarnings("unchecked")
	private void processIdFromSimples(String[] simples, ArrayList inputs) throws MobyException {
		for (int i = 0; i < simples.length; i++) {
			String simple = simples[i];
			Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
			if (!simElement.getChildren().isEmpty())
				simElement = (Element)simElement.getChildren().get(0);
			inputs.add(getElementsMobyID(simElement));
		}
	}

	/**
	 * @param simples
	 * @param inputs
	 * @throws MobyException
	 */
	@SuppressWarnings("unchecked")
	private void processNamespaceFromSimples(String[] simples, ArrayList inputs)
			throws MobyException {
		for (int i = 0; i < simples.length; i++) {
			String simple = simples[i];
			Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
			if (!simElement.getChildren().isEmpty())
				simElement = (Element)simElement.getChildren().get(0);
			inputs.add(getElementsMobyNamespace(simElement));
		}
	}

	@SuppressWarnings("unchecked")
	private Element getElementWithArticleName(Element e, String nextArtName) {
		List list = e.getChildren();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof Element) {
				Element element = (Element) o;
				if (element.getAttribute("articleName") != null) {
					if (element.getAttributeValue("articleName").equals(nextArtName))
						return element;
				} else if (element.getAttribute("articleName", XMLUtilities.MOBY_NS) != null) {
					if (element.getAttributeValue("articleName", XMLUtilities.MOBY_NS).equals(
							nextArtName))
						return element;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private String getElementsMobyNamespace(Element e) {
		String ns = "";
		List list = e.getChildren();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof Element) {
				Element element = (Element) o;
				if (element.getAttribute("namespace") != null) {
					return element.getAttributeValue("namespace");
				} else if (element.getAttribute("namespace", XMLUtilities.MOBY_NS) != null) {
					return element.getAttributeValue("namespace", XMLUtilities.MOBY_NS);
				}
			}
		}
		return ns;
	}

	@SuppressWarnings("unchecked")
	private String getElementsMobyID(Element e) {
		String id = "";
		List list = e.getChildren();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof Element) {
				Element element = (Element) o;
				//System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(element));
				if (element.getAttribute("id") != null) {
					return element.getAttributeValue("id");
				} else if (element.getAttribute("id", XMLUtilities.MOBY_NS) != null) {
					return element.getAttributeValue("id", XMLUtilities.MOBY_NS);
				}
			}
		}
		return id;
	}

	@SuppressWarnings("unchecked")
	private Element getChildOfSimple(Element e) {
		List list = e.getChildren();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof Element)
				return (Element) o;
		}
		return e;
	}

	@SuppressWarnings("unchecked")
	private boolean isPrimitive(String name) {
		if (name.equals("Integer") || name.equals("String") || name.equals("Float")
				|| name.equals("DateTime") || name.equals("Boolean"))
			return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	private void processChildNsFromSimple(HashMap map, String inputXML, String portName)
			throws MobyException {
		String simple = XMLUtilities.getSimple(proc.getArticleNameUsedByService(), inputXML);
		Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
		Element child = null;
		String[] path = portName.split("_");
		// expect it to be at least 3 => simple articlename, child relation
		// name, and id | ns
		if (path.length < 3) {
			// put empty string
			map.put(portName, new DataThing(new ArrayList()));
			return;
		}
		if (simElement.getChildren().size() == 0) {
			// put empty string
			map.put(portName, new DataThing(new ArrayList()));
			return;
		}

		for (int x = 0; x < simElement.getChildren().size(); x++) {
			Object o = simElement.getChildren().get(x);
			if (o instanceof Element) {
				child = (Element) o;
				break;
			}
		}
		if (child == null) {
			map.put(portName, new DataThing(new ArrayList()));
			return;
		}

		// now extract the element we want
		String match = new XMLOutputter(Format.getPrettyFormat()).outputString(child);
		for (int x = 1; x < path.length - 1; x++) {
			String currentArtName = path[x];
			if (currentArtName.startsWith("'"))
				currentArtName = currentArtName.substring(1);
			if (currentArtName.endsWith("'"))
				currentArtName = currentArtName.substring(0, currentArtName.length() - 1);
			match = XMLUtilities.getDirectChildByArticleName(match, currentArtName);
			if (match == null) {
				map.put(portName, new DataThing(new ArrayList()));
				return;
			}
		}
		// now match contains the xml that has the element that we want
		child = XMLUtilities.getDOMDocument(match).getRootElement();
		String ns = "";
		ns = child.getAttributeValue("namespace");
		if (ns == null)
			ns = child.getAttributeValue("namespace", XMLUtilities.MOBY_NS);
		ArrayList theList = new ArrayList();
		if (ns != null)
			theList.add(ns);
		map.put(portName, new DataThing(theList));
	}

	@SuppressWarnings("unchecked")
	private void processChildNsFromSimples(String[] simples, ArrayList list, String portName)
			throws MobyException {
		for (int i = 0; i < simples.length; i++) {
			String simple = simples[i];
			Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
			Element child = null;
			String[] path = portName.split("_");
			// expect it to be at least 3 => simple articlename, child relation
			// name, and id | ns
			if (path.length < 3) {
				// put empty string
				list.add("");
				continue;
			}
			if (simElement.getChildren().size() == 0) {
				// put empty string
				list.add("");
				continue;
			}

			for (int x = 0; x < simElement.getChildren().size(); x++) {
				Object o = simElement.getChildren().get(x);
				if (o instanceof Element) {
					child = (Element) o;
					break;
				}
			}
			if (child == null) {
				list.add("");
				continue;
			}

			// now extract the element we want
			String match = new XMLOutputter(Format.getPrettyFormat()).outputString(child);
			for (int x = 1; x < path.length - 1; x++) {
				String currentArtName = path[x];
				if (currentArtName.startsWith("'"))
					currentArtName = currentArtName.substring(1);
				if (currentArtName.endsWith("'"))
					currentArtName = currentArtName.substring(0, currentArtName.length() - 1);
				match = XMLUtilities.getDirectChildByArticleName(match, currentArtName);
				if (match == null) {
					list.add("");
					continue;
				}
			}
			// now match contains the xml that has the element that we want
			child = XMLUtilities.getDOMDocument(match).getRootElement();
			String ns = "";
			ns = child.getAttributeValue("namespace");
			if (ns == null)
				ns = child.getAttributeValue("namespace", XMLUtilities.MOBY_NS);
			list.add(ns == null ? "" : ns);
		}
	}

	/*
	 * extracts the id from a child element
	 */
	@SuppressWarnings("unchecked")
	private void processChildIdFromSimple(HashMap map, String inputXML, final String portName)
			throws MobyException {
		String simple = XMLUtilities.getSimple(proc.getArticleNameUsedByService(), inputXML);
		Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
		Element child = null;
		String[] path = portName.split("_");
		// expect it to be at least 3 => simple articlename, child relation
		// name, and id | ns
		if (path.length < 3) {
			// put empty string
			map.put(portName, new DataThing(new ArrayList()));
			return;
		}
		if (simElement.getChildren().size() == 0) {
			// put empty string
			map.put(portName, new DataThing(new ArrayList()));
			return;
		}

		for (int x = 0; x < simElement.getChildren().size(); x++) {
			Object o = simElement.getChildren().get(x);
			if (o instanceof Element) {
				child = (Element) o;
				break;
			}
		}
		if (child == null) {
			map.put(portName, new DataThing(new ArrayList()));
			return;
		}

		// now extract the element we want
		String match = new XMLOutputter(Format.getPrettyFormat()).outputString(child);
		for (int x = 1; x < path.length - 1; x++) {
			String currentArtName = path[x];
			if (currentArtName.startsWith("'"))
				currentArtName = currentArtName.substring(1);
			if (currentArtName.endsWith("'"))
				currentArtName = currentArtName.substring(0, currentArtName.length() - 1);
			match = XMLUtilities.getDirectChildByArticleName(match, currentArtName);
			if (match == null) {
				map.put(portName, new DataThing(new ArrayList()));
				return;
			}
		}
		// now match contains the xml that has the element that we want
		child = XMLUtilities.getDOMDocument(match).getRootElement();
		String id = "";
		id = child.getAttributeValue("id");
		if (id == null)
			id = child.getAttributeValue("id", XMLUtilities.MOBY_NS);
		ArrayList theList = new ArrayList();
		if (id != null)
			theList.add(id);
		map.put(portName, new DataThing(theList));
	}

	/*
	 * extracts the id from a child element
	 */
	@SuppressWarnings("unchecked")
	private void processChildIdFromSimples(String[] simples, ArrayList list, String portName)
			throws MobyException {
		for (int i = 0; i < simples.length; i++) {
			String simple = simples[i];
			Element simElement = XMLUtilities.getDOMDocument(simple).getRootElement();
			Element child = null;
			String[] path = portName.split("_");
			// expect it to be at least 3 => simple articlename, child relation
			// name, and id | ns
			if (path.length < 3) {
				// put empty string
				list.add("");
				continue;
			}
			if (simElement.getChildren().size() == 0) {
				// put empty string
				list.add("");
				continue;
			}

			for (int x = 0; x < simElement.getChildren().size(); x++) {
				Object o = simElement.getChildren().get(x);
				if (o instanceof Element) {
					child = (Element) o;
					break;
				}
			}
			if (child == null) {
				list.add("");
				continue;
			}

			// now extract the element we want
			String match = new XMLOutputter(Format.getPrettyFormat()).outputString(child);
			for (int x = 1; x < path.length - 1; x++) {
				String currentArtName = path[x];
				if (currentArtName.startsWith("'"))
					currentArtName = currentArtName.substring(1);
				if (currentArtName.endsWith("'"))
					currentArtName = currentArtName.substring(0, currentArtName.length() - 1);
				match = XMLUtilities.getDirectChildByArticleName(match, currentArtName);
				if (match == null) {
					list.add("");
					continue;
				}
			}
			// now match contains the xml that has the element that we want
			child = XMLUtilities.getDOMDocument(match).getRootElement();
			String id = "";
			id = child.getAttributeValue("id");
			if (id == null)
				id = child.getAttributeValue("id", XMLUtilities.MOBY_NS);
			list.add(id == null ? "" : id);
		}
	}
}
