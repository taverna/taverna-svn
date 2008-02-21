package net.sf.taverna.t2.activities.biomoby;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManagerException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.client.taverna.plugin.XMLUtilities;
import org.biomoby.service.dashboard.data.ParametersTable;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyPrimaryDataSet;
import org.biomoby.shared.MobyPrimaryDataSimple;
import org.biomoby.shared.MobySecondaryData;
import org.biomoby.shared.MobyService;
import org.biomoby.shared.Utils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

/**
 * An Activity based on the Biomoby compliant web services. This activity
 * implementation will contact Biomoby registry in order to find the list of
 * extant ports at creation time.
 * 
 * Copied from org.biomoby.client.taverna.plugin.BiomobyProcessor and
 * org.biomoby.client.taverna.plugin.BiomobyTask and converted to a Taverna 2
 * Activity.
 * 
 * @author Martin Senger
 * @author Edward Kawas
 * @author David Withers
 */
public class BiomobyActivity extends
		AbstractAsynchronousActivity<BiomobyActivityConfigurationBean> {

	private static Logger logger = Logger.getLogger(BiomobyActivity.class);

	private static final Namespace mobyNS = Namespace.getNamespace("moby",
			"http://www.biomoby.org/moby");

	private static int qCounter = 0;

	private BiomobyActivityConfigurationBean configurationBean;

	private URL endpoint;

	private Central worker = null;

	private MobyService mobyService = null;

	private boolean containSecondary = false;

	private ParametersTable parameterTable = null;

	@Override
	public void configure(BiomobyActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		init();
		generatePorts();
	}

	@Override
	public BiomobyActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> inputMap,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				DataFacade dataFacade = new DataFacade(callback.getContext()
						.getDataManager());

				Map<String, EntityIdentifier> outputMap = new HashMap<String, EntityIdentifier>();

				if (logger.isDebugEnabled()) {
					logger.debug("Service " /* + proc.getName() */);
					for (Iterator it = inputMap.keySet().iterator(); it.hasNext();) {
						String key = (String) it.next();
						try {
							Object input = dataFacade.resolve(inputMap.get(key),
									String.class);
							if (input instanceof String) {
								logger.debug("key " + key + "has value of\n"
										+ input);
								continue;
							} else if (input instanceof List) {
								List list = (List) input;
								for (Iterator it2 = list.iterator(); it2
										.hasNext();) {
									logger.debug("List key " + key
											+ "has value of\n" + it2.next());
								}
							}
						} catch (RetrievalException e) {
							logger.debug(
									"Error resolving data for port " + key, e);
						} catch (NotFoundException e) {
							logger.debug(
									"Error resolving data for port " + key, e);
						}
					}
					logger.debug("Printing of ports complete.");
				}

				if (inputMap.containsKey("input")) {
					// input port takes precedence over other ports
					try {

						Object input = dataFacade.resolve(inputMap.get("input"), String.class);

						ActivityInputPort myInput = null;
						for (ActivityInputPort inputPort : getInputPorts()) {
							if (inputPort.getName().equalsIgnoreCase("input")) {
								myInput = inputPort;
								break;
							}
						}
						if (myInput == null) {
							callback.fail("The port 'input' was not specified correctly.");
							return;
						}

						// If port depth is 0 then the biomoby service consumes
						// a simple and there is no processing to do as that's
						// what we
						// have
						// If port depth is 1 biomoby expects a collection but
						// we have a java List of simples - need to convert this
						// into
						// a biomoby collection document
						String inputXML = null;

						if (myInput.getDepth() == 0) {
							inputXML = (String) input;
						} else {
							// List of strings containing simple biomoby objects
							List simpleInputs = (List) input;
							// Create the empty collection document
							Element root = new Element("MOBY", mobyNS);
							Element content = new Element("mobyContent", mobyNS);
							root.addContent(content);
							Element data = new Element("mobyData", mobyNS);
							data.setAttribute("queryID", "d" + qCounter++,
									mobyNS);
							content.addContent(data);
							Element collectionElement = new Element(
									"Collection", mobyNS);
							collectionElement.setAttribute("articleName", "",
									mobyNS);
							// It is this collection element that's going to
							// acquire the simples
							for (Iterator i = simpleInputs.iterator(); i
									.hasNext();) {
								String s = (String) i.next();
								Element el = XMLUtilities.getDOMDocument(s)
										.getRootElement();
								Element mobyDataElement = el.getChild(
										"mobyContent", mobyNS).getChild(
										"mobyData", mobyNS);
								// Remove the single 'Simple' child from this...
								Element simpleElement = (Element) mobyDataElement
										.getChildren().get(0);
								// Tag the simple element onto the collection.
								collectionElement.addContent(simpleElement
										.detach());
							}
							XMLOutputter xo = new XMLOutputter(Format
									.getPrettyFormat());
							inputXML = xo.outputString(new Document(root));
							// Iterate and create the collection,
							// ....inputXML = collectionThing
						}

						// do the task and populate outputXML
						String methodName = configurationBean.getServiceName();
						String serviceEndpoint = endpoint.toExternalForm();
						String outputXML = new CentralImpl(serviceEndpoint,
								"http://biomoby.org/").call(methodName,
								inputXML);
						// goes through and creates the port 'output'
						processOutputPort(outputXML, outputMap, dataFacade);
						// create the other ports
						processOutputPorts(outputXML, outputMap, dataFacade);

						callback.receiveResult(outputMap, new int[0]);

					} catch (DataManagerException e) {
						callback.fail("Error accessing input/output data", e);
					} catch (NotFoundException e) {
						callback.fail("Error accessing input/output data", e);
					} catch (MobyException ex) {
						// a MobyException should be already reasonably
						// formatted
						logger.error("Error invoking biomoby service for biomoby. A MobyException caught",
								ex);
						callback.fail("Activity failed due to problem invoking biomoby service.\n"
								+ ex.getMessage(), ex);
					} catch (Exception ex) {
						// details of other exceptions will appear only in a log
						ex.printStackTrace();
						logger.error("Error invoking biomoby service for biomoby",
								ex);
						callback.fail("Activity failed due to problem invoking biomoby service (see details in log)",
								ex);
					}
				} else {
					// now try other named ports
					try {
						String inputXML = null;
						Element root = new Element("MOBY", XMLUtilities.MOBY_NS);
						Element content = new Element("mobyContent", XMLUtilities.MOBY_NS);
						root.addContent(content);
						int totalMobyDatas = 0;
						Vector mobyDatas = new Vector(); // list of mobyData element
						for (ActivityInputPort myInput : getInputPorts()) {
							if (myInput.getName().equalsIgnoreCase("input")) {
								continue;
							}
							// the port name
							String portName = myInput.getName();
							// the article name
							String articleName = "";
							String type = portName;
							if (portName.indexOf("(") >= 0
									&& portName.indexOf(")") > 0) {
								articleName = portName.substring(portName
										.indexOf("(") + 1, portName
										.indexOf(")"));

								if (articleName.indexOf("'") >= 0
										&& articleName.lastIndexOf("'") > 0)
									articleName = articleName.substring(
											articleName.indexOf("'") + 1,
											articleName.lastIndexOf("'"));

								type = portName.substring(0, portName
										.indexOf("("));
							}

							// String inputType = myInput.getSyntacticType();
							Object input = dataFacade.resolve(inputMap
									.get(portName), myInput
									.getTranslatedElementClass());
							if (myInput.getDepth() == 0) {
								inputXML = (String) input;
								Element inputElement = null;
								try {
									inputElement = XMLUtilities.getDOMDocument(
											inputXML).getRootElement();

								} catch (MobyException e) {
									callback.fail(XMLUtilities.newline
													+ "There was an error parsing the input XML:"
													+ XMLUtilities.newline
													+ Utils.format(inputXML, 3)
													+ XMLUtilities.newline
													+ e.getLocalizedMessage());
									return;
								}
								// determine whether we have a multiple
								// invocation message
								if (XMLUtilities
										.isMultipleInvocationMessage(inputElement)) {
									// multiple invocations
									Element[] invocations = XMLUtilities
											.getSingleInvokationsFromMultipleInvokations(inputElement);
									ArrayList list = new ArrayList();
									for (int j = 0; j < invocations.length; j++) {
										Element[] elements = XMLUtilities
												.getListOfCollections(invocations[j]);
										if (elements.length == 0) {
											// single simple
											inputElement = XMLUtilities
													.renameSimple(articleName,
															type,
															invocations[j]);
											Element md = XMLUtilities
													.extractMobyData(inputElement);
											list.add(md);
										} else {
											// collection of simples => create
											// multiple
											// invocation message
											String queryID = XMLUtilities
													.getQueryID(invocations[j]);
											Element[] simples = XMLUtilities
													.getSimplesFromCollection(invocations[j]);
											for (int k = 0; k < simples.length; k++) {
												Element wrappedSimple = XMLUtilities
														.createMobyDataElementWrapper(simples[k]);
												wrappedSimple = XMLUtilities.renameSimple(
																articleName,
																type,
																wrappedSimple);
												wrappedSimple = XMLUtilities.setQueryID(
																wrappedSimple,
																queryID /*+ "_+_" +
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
									Element[] elements = XMLUtilities
											.getListOfCollections(inputElement);
									if (elements.length == 0) {
										// single simple
										inputElement = XMLUtilities
												.renameSimple(articleName,
														type, inputElement);
										ArrayList list = new ArrayList();
										Element md = XMLUtilities
												.extractMobyData(inputElement);
										list.add(md);
										mobyDatas.add(list);
										if (totalMobyDatas < 1)
											totalMobyDatas = 1;
									} else {
										// collection of simples => create
										// multiple
										// invocation message
										String queryID = XMLUtilities
												.getQueryID(inputElement);
										Element[] simples = XMLUtilities
												.getSimplesFromCollection(inputElement);

										ArrayList list = new ArrayList();
										for (int j = 0; j < simples.length; j++) {
											Element wrappedSimple = XMLUtilities
													.createMobyDataElementWrapper(simples[j]);
											wrappedSimple = XMLUtilities
													.renameSimple(articleName,
															type, wrappedSimple);
											wrappedSimple = XMLUtilities
													.setQueryID(wrappedSimple,
															queryID /*
																	 * + "_+_" +
																	 * XMLUtilities.getQueryID(wrappedSimple)
																	 */);
											list
													.add(XMLUtilities
															.extractMobyData(wrappedSimple));
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
								List list = (List) input;
								/*
								 * need this map in cases where simples are
								 * passed into a service that wants a
								 * collection. each simple is then added into
								 * the same collection
								 */
								Map collectionMap = new HashMap();
								for (Iterator it = list.iterator(); it
										.hasNext();) {
									Element inputElement = null;
									String next = (String) it.next();
									try {
										inputElement = XMLUtilities.getDOMDocument(next)
												.getRootElement();

									} catch (MobyException e) {
										callback.fail(XMLUtilities.newline
														+ "There was an error parsing the input XML:"
														+ XMLUtilities.newline
														+ Utils.format(inputXML, 3)
														+ XMLUtilities.newline
														+ e.getLocalizedMessage());
										return;
									}
									// determine whether we have a multiple
									// invocation message
									if (XMLUtilities
											.isMultipleInvocationMessage(inputElement)) {
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
											mimQueryID = XMLUtilities
													.getQueryID(invocations[j]);
											if (elements.length == 0) {
												if (mimCollection == null)
													mimCollection = new Element(
															"Collection",
															XMLUtilities.MOBY_NS);

												Element theSimple = XMLUtilities
														.extractMobyData(invocations[j]);
												if (theSimple
														.getChild("Simple") != null)
													theSimple = theSimple
															.getChild("Simple");
												else if (theSimple.getChild(
														"Simple",
														XMLUtilities.MOBY_NS) != null)
													theSimple = theSimple
															.getChild(
																	"Simple",
																	XMLUtilities.MOBY_NS);
												mimCollection
														.addContent(theSimple
																.detach());
												// mimQueryID = mimQueryID + "_" +
												// XMLUtilities.getQueryID(invocations[j]);
											} else {
												// collection passed in (always
												// 1 passed in)
												Element collection = invocations[j];
												collection = XMLUtilities
														.renameCollection(
																articleName,
																collection);
												collection = XMLUtilities
														.createMobyDataElementWrapper(
																collection,
																XMLUtilities
																		.getQueryID(invocations[j]),
																null);
												mdList.add(XMLUtilities
																.extractMobyData(collection));
											}
										}
										if (mimCollection != null) {
											mimCollection = XMLUtilities
													.createMobyDataElementWrapper(
															mimCollection,
															mimQueryID, null);
											mimCollection = XMLUtilities
													.renameCollection(
															articleName,
															mimCollection);
											mimCollection = XMLUtilities
													.createMobyDataElementWrapper(
															mimCollection,
															mimQueryID, null);
											mdList.add(XMLUtilities
															.extractMobyData(mimCollection));
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
											Element collection = new Element(
													"Collection",
													XMLUtilities.MOBY_NS);
											collection.addContent(XMLUtilities
													.extractMobyData(
															inputElement)
													.cloneContent());
											collection = XMLUtilities
													.createMobyDataElementWrapper(
															collection,
															XMLUtilities
																	.getQueryID(inputElement),
															null);
											collection = XMLUtilities
													.renameCollection(
															articleName,
															collection);
											collection = XMLUtilities
													.createMobyDataElementWrapper(
															collection,
															XMLUtilities
																	.getQueryID(inputElement),
															null);
											if (collectionMap
													.containsKey(articleName)) {
												// add the simple to a
												// pre-existing
												// collection
												ArrayList mdList = (ArrayList) collectionMap
														.remove(articleName);
												mdList.add(XMLUtilities
																.extractMobyData(collection));
												collectionMap.put(articleName,
														mdList);
											} else {
												// new collection - add element
												// and
												// increment count
												ArrayList mdList = new ArrayList();
												mdList.add(XMLUtilities
																.extractMobyData(collection));
												collectionMap.put(articleName,
														mdList);
												// totalMobyDatas++;
												if (totalMobyDatas < 1)
													totalMobyDatas = 1;
											}
										} else {
											// we have a collection
											Element collection = inputElement;
											collection = XMLUtilities
													.renameCollection(
															articleName,
															collection);
											ArrayList mdList = new ArrayList();
											collection = XMLUtilities
													.createMobyDataElementWrapper(
															collection,
															XMLUtilities
																	.getQueryID(inputElement),
															null);
											mdList
													.add(XMLUtilities
															.extractMobyData(collection));
											mobyDatas.add(mdList);
											if (totalMobyDatas < 1)
												totalMobyDatas = 1;

										}
									} // end if SIM
								} // end iteration over inputThing list
								Iterator collectionIterator = collectionMap
										.keySet().iterator();
								while (collectionIterator.hasNext()) {
									String key = (String) collectionIterator
											.next();
									List theList = (List) collectionMap
											.get(key);
									theList = XMLUtilities.mergeCollections(
											theList, key);
									List unwrappedList = new ArrayList();
									for (Iterator it = theList.iterator(); it
											.hasNext();) {
										Element e = (Element) it.next();
										if (XMLUtilities.isWrapped(e))
											unwrappedList.add(XMLUtilities
													.extractMobyData(e));
										else
											unwrappedList.add(e);
									}
									mobyDatas.add(unwrappedList);
								}
							}
						}

						if (logger.isDebugEnabled()) {
							logger.debug("Before MobyData aggregation");
							for (Iterator itr = mobyDatas.iterator(); itr
									.hasNext();) {
								List eList = (List) itr.next();
								for (int x = 0; x < eList.size(); x++) {
									logger.debug(new XMLOutputter(Format
											.getPrettyFormat())
											.outputString((Element) eList
													.get(x)));
								}
							}
							logger.debug("******* End ******");
						}
						/*
						 * ports have been processed -> vector contains a list
						 * of all the different types of inputs with their
						 * article names set correctly. The elements are from
						 * mobyData down. Moreover, there are totalMobyData
						 * number of invocations in the output moby message
						 */
						if (logger.isDebugEnabled()) {
							logger.debug("TotalMobyDatas: " + totalMobyDatas);
						}
						Element[] mds = new Element[totalMobyDatas];
						// initialize the mobydata blocks
						for (int x = 0; x < mds.length; x++) {
							mds[x] = new Element("mobyData",
									XMLUtilities.MOBY_NS);
							String queryID = "_";
							// add the content
							for (Iterator iter = mobyDatas.iterator(); iter
									.hasNext();) {
								ArrayList list = (ArrayList) iter.next();
								int index = x % list.size();
								Element next = ((Element) list.get(index));
								logger.debug(new XMLOutputter(Format
										.getPrettyFormat()).outputString(next));
								// queryID += "_" +
								// XMLUtilities.getQueryID(next);
								queryID = XMLUtilities.getQueryID(next);
								mds[x].addContent(next.cloneContent());

							}
							// remove the first _
							// if (queryID != null && queryID.length() > 1)
							// queryID = queryID.substring(1);
							mds[x].setAttribute("queryID", queryID,
									XMLUtilities.MOBY_NS);
							// if secondarys exist add them here
							if (containSecondary) {
								@SuppressWarnings("unused")
								ParametersTable pt = parameterTable;
								Element[] parameters = null;
								parameters = parameterTable.toXML();
								for (int i = 0; i < parameters.length; i++) {
									mds[x].addContent((parameters[i]).detach());
								}
							}
							content.addContent(mds[x].detach());
						}

						if (logger.isDebugEnabled()) {
							logger.debug("After MobyData aggregation");
							logger.debug(new XMLOutputter(Format
									.getPrettyFormat()).outputString(root));
							logger.debug("******* End ******");
						}
						// do the task and populate outputXML

						String methodName = configurationBean.getServiceName();
						String serviceEndpoint = endpoint.toExternalForm();

						String serviceInput = new XMLOutputter(Format
								.getPrettyFormat()).outputString(root);
						String[] invocations = XMLUtilities
								.getSingleInvokationsFromMultipleInvokations(serviceInput);
						// logger.debug(serviceInput);
						// going to iterate over all invocations so that
						// messages with
						// many mobyData blocks dont timeout.
						logger.debug("Total invocations " + invocations.length);
						if (invocations.length > 0)
							logger.debug("invocation 00");
						for (int inCount = 0; inCount < invocations.length; inCount++) {

							if (logger.isDebugEnabled())
								logger.info("input(" + inCount + "):\n"
										+ invocations[inCount]);
							if (!XMLUtilities.isEmpty(invocations[inCount]))
								invocations[inCount] = new CentralImpl(
										serviceEndpoint, "http://biomoby.org/")
										.call(methodName, invocations[inCount]);
							if (logger.isDebugEnabled())
								logger.info("output(" + inCount + "):\n"
										+ invocations[inCount]);
						}

						String outputXML = XMLUtilities
								.createMultipleInvokations(invocations);
						// goes through and creates the port 'output'
						processOutputPort(outputXML, outputMap, dataFacade);
						// create the other ports
						processOutputPorts(outputXML, outputMap, dataFacade);

						callback.receiveResult(outputMap, new int[0]);

					} catch (DataManagerException e) {
						callback.fail("Error accessing input/output data", e);
					} catch (NotFoundException e) {
						callback.fail("Error accessing input/output data", e);
					} catch (MobyException ex) {
						// a MobyException should be already reasonably
						// formatted
						logger.error("Error invoking biomoby service for biomoby. A MobyException caught",
								ex);
						callback.fail("Activity failed due to problem invoking biomoby service.\n"
										+ ex.getMessage(), ex);

					} catch (Exception ex) {
						// details of other exceptions will appear only in a log
						ex.printStackTrace();
						logger.error("Error invoking biomoby service for biomoby",
								ex);
						callback.fail("Task failed due to problem invoking biomoby service (see details in log)",
								ex);
					}

				}
			}

		});

	}

	private void init() throws ActivityConfigurationException {
		// Find the service endpoint (by calling Moby registry)
		try {
			if (mobyService == null) {
				worker = new CentralImpl(configurationBean.getMobyEndpoint());

				MobyService pattern = new MobyService(configurationBean.getServiceName());
				pattern.setAuthority(configurationBean.getAuthorityName());
				pattern.setCategory("");
				MobyService[] services = worker.findService(pattern);
				if (services == null || services.length == 0)
					throw new ActivityConfigurationException(
							formatError("I cannot find the service."));
				mobyService = services[0];
			}
			String serviceEndpoint = mobyService.getURL();
			if (serviceEndpoint == null || serviceEndpoint.equals(""))
				throw new ActivityConfigurationException(
						formatError("Service has an empty endpoint."));
			try {
				endpoint = new URL(serviceEndpoint);
			} catch (MalformedURLException e2) {
				throw new ActivityConfigurationException(
						formatError("Service has malformed endpoint: '"
								+ serviceEndpoint + "'."));
			}

		} catch (Exception e) {
			if (e instanceof ActivityConfigurationException) {
				throw (ActivityConfigurationException) e;
			}
			throw new ActivityConfigurationException(formatError(e.toString()));
		}
		// here we get the wsdl, as the biomoby api assumes ...
		try {
			 new RetrieveWsdlThread(worker, mobyService).start();
		} catch (Exception e) {
			/* don't care if an exception occurs here ... */
		}

	}

	/**
	 * Use the endpoint data to create new ports and attach them to the
	 * processor.
	 */
	private void generatePorts() {

		// inputs TODO - find a better way to deal with collections
		MobyData[] serviceInputs = this.mobyService.getPrimaryInputs();
		int inputDepth = 0;
		for (int x = 0; x < serviceInputs.length; x++) {
			if (serviceInputs[x] instanceof MobyPrimaryDataSimple) {
				MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) serviceInputs[x];

				// retrieve the simple article name
				String simpleName = simple.getName();
				if (simpleName.equals("")) {
					simpleName = "_ANON_";
				}
				simpleName = "(" + simpleName + ")";

				String portName = simple.getDataType().getName() + simpleName;
				addInput(portName, inputDepth, true,
						new ArrayList<Class<? extends ReferenceScheme<?>>>(),
						String.class);
			} else {
				// collection of items
				inputDepth = 1;
				MobyPrimaryDataSet collection = (MobyPrimaryDataSet) serviceInputs[x];
				String collectionName = collection.getName();
				if (collectionName.equals(""))
					collectionName = "MobyCollection";
				MobyPrimaryDataSimple[] simples = collection.getElements();
				for (int y = 0; y < simples.length; y++) {
					// collection port
					String portName = simples[y].getDataType().getName()
							+ "(Collection - '" + collectionName + "')";
					addInput(
							portName,
							inputDepth,
							true,
							new ArrayList<Class<? extends ReferenceScheme<?>>>(),
							String.class);

				}
			}
		}
		addInput("input", inputDepth, true,
				new ArrayList<Class<? extends ReferenceScheme<?>>>(),
				String.class);

		MobyData[] secondaries = this.mobyService.getSecondaryInputs();

		if (secondaries.length > 0) {
			MobySecondaryData[] msd = new MobySecondaryData[secondaries.length];
			for (int i = 0; i < secondaries.length; i++) {
				msd[i] = (MobySecondaryData) secondaries[i];
			}
			containSecondary = true;
			this.parameterTable = new org.biomoby.service.dashboard.data.ParametersTable(
					msd);
		}

		// outputs
		MobyData[] serviceOutputs = this.mobyService.getPrimaryOutputs();
		int outputDepth = 0;
		for (int x = 0; x < serviceOutputs.length; x++) {
			if (serviceOutputs[x] instanceof MobyPrimaryDataSimple) {
				MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) serviceOutputs[x];

				// retrieve the simple article name
				String simpleName = simple.getName();
				if (simpleName.equals("")) {
					simpleName = "_ANON_";
				}
				simpleName = "(" + simpleName + ")";

				String outputName = simple.getDataType().getName() + simpleName;
				addOutput(outputName, outputDepth);
			} else {
				outputDepth = 1;
				// collection of items
				MobyPrimaryDataSet collection = (MobyPrimaryDataSet) serviceOutputs[x];
				String collectionName = collection.getName();
				if (collectionName.equals(""))
					collectionName = "MobyCollection";
				MobyPrimaryDataSimple[] simples = collection.getElements();
				for (int y = 0; y < simples.length; y++) {
					String outputName = simples[y].getDataType().getName()
							+ "(Collection - '" + collectionName + "')";
					addOutput(outputName, outputDepth);

					outputName = simples[y].getDataType().getName()
							+ "(Collection - '" + collectionName
							+ "' As Simples)";
					addOutput(outputName, outputDepth);
				}
			}
		}

		addOutput("output", outputDepth);

	}

	private void processOutputPort(String outputXML, Map outputMap,
			DataFacade dataFacade) throws ActivityConfigurationException,
			JDOMException, IOException, DataManagerException {
		OutputPort myOutput = null;
		for (OutputPort outputPort : getOutputPorts()) {
			if (outputPort.getName().equalsIgnoreCase("output"))
				myOutput = outputPort;
		}
		if (myOutput == null)
			throw new ActivityConfigurationException("output port is invalid.");

		if (myOutput.getDepth() == 0) {
			outputMap.put("output", dataFacade.register(outputXML, myOutput
					.getDepth()));
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
			Document doc = saxBuilder.build(new InputSource(new StringReader(
					outputXML)));
			Element mobyElement = doc.getRootElement();
			Element mobyDataElement = mobyElement.getChild("mobyContent",
					mobyNS).getChild("mobyData", mobyNS);

			Element collectionElement = null;
			try {
				collectionElement = mobyDataElement.getChild("Collection",
						mobyNS);
			} catch (Exception e) {
				// logger.warn("There was a problem processing the output
				// port.\n" + outputXML);
				try {
					outputList
							.add(new XMLOutputter(Format.getPrettyFormat())
									.outputString(XMLUtilities
											.createMobyDataWrapper(
													XMLUtilities
															.getQueryID(outputXML),
													XMLUtilities
															.getServiceNotesAsElement(outputXML))));
				} catch (MobyException me) {
					logger.debug(me);
				}
			}
			if (collectionElement != null) {
				List simpleElements = new ArrayList(collectionElement
						.getChildren());
				for (Iterator i = simpleElements.iterator(); i.hasNext();) {
					Element simpleElement = (Element) i.next();

					Element newRoot = new Element("MOBY", mobyNS);
					Element newMobyContent = new Element("mobyContent", mobyNS);
					Element serviceNotes = XMLUtilities
							.getServiceNotesAsElement(outputXML);
					if (serviceNotes != null)
						newMobyContent.addContent(serviceNotes.detach());
					newRoot.addContent(newMobyContent);
					Element newMobyData = new Element("mobyData", mobyNS);
					newMobyContent.addContent(newMobyData);
					newMobyData.addContent(simpleElement.detach());
					try {
						XMLUtilities.setQueryID(newRoot, XMLUtilities
								.getQueryID(outputXML)
								+ "_+_" + XMLUtilities.getQueryID(newRoot));
					} catch (MobyException e) {
						newMobyData.setAttribute("queryID", "a1", mobyNS);
					}
					XMLOutputter xo = new XMLOutputter();
					String outputItemString = xo.outputString(new Document(
							newRoot));
					outputList.add(outputItemString);
				}
			}

			// Return the list (may be empty)
			outputMap.put("output", dataFacade.register(outputList, myOutput
					.getDepth()));
		}
	}

	private void processOutputPorts(String outputXML, Map outputMap,
			DataFacade dataFacade) throws MobyException, DataManagerException {
		boolean isMIM = XMLUtilities.isMultipleInvocationMessage(outputXML);
		for (OutputPort outputPort : getOutputPorts()) {
			String name = outputPort.getName();
			if (!name.equalsIgnoreCase("output")) {
				if (outputPort.getDepth() == 1) {
					// collection - list of strings
					String articleName = "";
					if (name.indexOf("MobyCollection") > 0) {
						// un-named collection -> ignore it as it is illegal
						// in the api
						// TODO could throw exception

						List innerList = new ArrayList();
						outputMap.put(name, dataFacade.register(innerList,
								outputPort.getDepth()));
						continue;
					} else {
						articleName = name.substring(name.indexOf("'") + 1,
								name.lastIndexOf("'"));
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
										String collection = XMLUtilities
												.getWrappedCollection(
														articleName,
														invocations[i]);
										String[] simples = XMLUtilities
												.getSimplesFromCollection(
														articleName, collection);
										for (int j = 0; j < simples.length; j++) {
											innerList
													.add(XMLUtilities
															.createMobyDataElementWrapper(
																	simples[j],
																	XMLUtilities
																			.getQueryID(collection)
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
								outputMap.put(name, dataFacade.register(
										innerList, outputPort.getDepth()));
							} else {
								// process the single invocation and put string
								// into
								// a
								// list
								try {

									List innerList = new ArrayList();
									String collection = XMLUtilities
											.getWrappedCollection(articleName,
													outputXML);

									String[] simples = XMLUtilities
											.getSimplesFromCollection(
													articleName, collection);
									Element serviceNotesElement = XMLUtilities
											.getServiceNotesAsElement(outputXML);
									for (int i = 0; i < simples.length; i++) {
										innerList
												.add(XMLUtilities
														.createMobyDataElementWrapper(
																simples[i],
																XMLUtilities
																		.getQueryID(collection)
																/*
																 * + "_+_s" +
																 * qCounter++
																 */,
																serviceNotesElement));
									}

									outputMap.put(name, dataFacade.register(
											innerList, outputPort.getDepth()));
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
									outputMap.put(name, dataFacade.register(
											innerList, outputPort.getDepth()));
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
										String collection = XMLUtilities
												.getWrappedCollection(
														articleName,
														invocations[i]);
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

								outputMap.put(name, dataFacade.register(
										innerList, outputPort.getDepth()));
							} else {

								try {

									List innerList = new ArrayList();
									String collection = XMLUtilities
											.getWrappedCollection(articleName,
													outputXML);
									innerList.add(collection);
									outputMap.put(name, dataFacade.register(
											innerList, outputPort.getDepth()));
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
									outputMap.put(name, dataFacade.register(
											innerList, outputPort.getDepth()));
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

						String empty = new XMLOutputter()
								.outputString(XMLUtilities
										.createMobyDataWrapper(
												XMLUtilities
														.getQueryID(outputXML),
												XMLUtilities
														.getServiceNotesAsElement(outputXML)));
						List innerList = new ArrayList();
						innerList.add(empty);
						outputMap.put(name, dataFacade.register(empty,
								outputPort.getDepth()));
						// FIXME outputMap.put(name, new
						// DataThing((Object)null));
						continue;
					} else {
						// TODO what if you make mim messages a single string
						// and then simples always output 'text/xml'?
						String articleName = name.substring(
								name.indexOf("(") + 1, name.indexOf(")"));
						if (isMIM) {

							String[] invocations = XMLUtilities
									.getSingleInvokationsFromMultipleInvokations(outputXML);

							ArrayList innerList = new ArrayList();

							for (int i = 0; i < invocations.length; i++) {
								try {
									String simple = XMLUtilities
											.getWrappedSimple(articleName,
													invocations[i]);
									innerList.add(simple);
								} catch (MobyException e) {
									// simple didnt exist, so put an empty
									// mobyData
									// TODO keep the original wrapper
									String qID = XMLUtilities
											.getQueryID(invocations[i]);

									Element empty = XMLUtilities
											.createMobyDataWrapper(
													qID,
													XMLUtilities
															.getServiceNotesAsElement(outputXML));
									XMLOutputter output = new XMLOutputter(
											Format.getPrettyFormat());
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
								outputMap.put(name, dataFacade.register(
										XMLUtilities
												.createMultipleInvokations(s),
										outputPort.getDepth()));
							} catch (MobyException e) {
								logger.error("Error creating output for service: "
												+ "."
												+ System.getProperty("line.separator")
												+ e.getMessage());
								outputMap.put(name, dataFacade.register("",
										outputPort.getDepth()));
							}
						} else {
							// process the single invocation and put into a
							// string
							try {
								String simple = XMLUtilities.getWrappedSimple(
										articleName, outputXML);
								ArrayList innerList = new ArrayList();
								innerList.add(simple);
								outputMap.put(name, dataFacade.register(simple,
										outputPort.getDepth()));
							} catch (MobyException e) {
								// simple didnt exist, so put an empty mobyData
								// TODO keep the original wrapper
								String qID = XMLUtilities.getQueryID(outputXML);
								Element empty = XMLUtilities
										.createMobyDataWrapper(
												qID,
												XMLUtilities
														.getServiceNotesAsElement(outputXML));
								XMLOutputter output = new XMLOutputter(Format
										.getPrettyFormat());
								ArrayList innerList = new ArrayList();
								innerList.add(output.outputString(empty));
								outputMap.put(name, dataFacade.register(output
										.outputString(empty), outputPort
										.getDepth()));
								// FIXME outputMap.put(name, new DataThing(""));

							}
						}
					}
				}
			}
		}
	}

	protected String formatError(String msg) {
		// Removed references to the authority, some errors
		// were causing it to be null which in turn threw
		// a NPE from here, breaking Taverna's error handlers
		return ("Problems with service '" + configurationBean.getServiceName()
				+ "' provided by authority '"
				+ configurationBean.getAuthorityName()
				+ "'\nfrom Moby registry at "
				+ configurationBean.getMobyEndpoint() + ":\n\n" + msg);
	}

	protected ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	protected OutputPort getOutputPort(String name) {
		for (OutputPort port : getOutputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

}
