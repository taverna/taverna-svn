package uk.org.taverna.scufl2.translator.t2flow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import uk.org.taverna.scufl2.api.common.Named;
import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.common.URITools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;
import uk.org.taverna.scufl2.api.core.ControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.dispatchstack.DispatchStackLayer;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyNode;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import uk.org.taverna.scufl2.api.iterationstrategy.PortNode;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.port.ReceiverPort;
import uk.org.taverna.scufl2.api.port.SenderPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;
import uk.org.taverna.scufl2.api.profiles.Profile;
import uk.org.taverna.scufl2.api.property.PropertyLiteral;
import uk.org.taverna.scufl2.api.property.PropertyObject;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Activity;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.AnnotatedGranularDepthPort;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.AnnotatedGranularDepthPorts;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.AnnotatedPorts;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Condition;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Conditions;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ConfigBean;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.CrossProduct;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Dataflow;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Datalinks;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.DepthPort;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.DepthPorts;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.DispatchLayer;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.DispatchStack;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.DotProduct;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.GranularDepthPort;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.GranularDepthPorts;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.IterationNode;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.IterationNodeParent;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Link;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.LinkType;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Mapping;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ObjectFactory;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Port;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.PortProduct;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Processors;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Raven;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.Role;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.TopIterationNode;

public class T2FlowParser {

	private static final String T2FLOW_EXTENDED_XSD = "xsd/t2flow-extended.xsd";
	@SuppressWarnings("unused")
	private static final String T2FLOW_XSD = "xsd/t2flow.xsd";

	private static final Logger logger = Logger.getLogger(T2FlowParser.class
			.getCanonicalName());

	public static final URI ravenURI = URI
			.create("http://ns.taverna.org.uk/2010/xml/t2flow/raven/");

	public static final URI configBeanURI = URI
			.create("http://ns.taverna.org.uk/2010/xml/t2flow/configbean/");

	public static <T extends Named> T findNamed(Collection<T> namedObjects,
			String name) {
		for (T named : namedObjects) {
			if (named.getName().equals(name)) {
				return named;
			}
		}
		return null;
	}

	protected ThreadLocal<ParserState> parserState = new ThreadLocal<ParserState>() {
		@Override
		protected ParserState initialValue() {
			return new ParserState();
		};
	};

	private static Scufl2Tools scufl2Tools = new Scufl2Tools();

	private static URITools uriTools = new URITools();

	protected Set<T2Parser> t2Parsers = null;
	protected final JAXBContext jaxbContext;
	private boolean strict = false;
	private boolean validating = false;

	public final boolean isValidating() {
		return validating;
	}

	public final void setValidating(boolean validating) {
		this.validating = validating;
	}

	protected ServiceLoader<T2Parser> discoveredT2Parsers;
	protected final ThreadLocal<Unmarshaller> unmarshaller;

	public T2FlowParser() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
		unmarshaller = new ThreadLocal<Unmarshaller>() {
			@Override
			protected Unmarshaller initialValue() {
				try {
					return jaxbContext.createUnmarshaller();
				} catch (JAXBException e) {
					logger.log(Level.SEVERE, "Could not create unmarshaller", e);
					return null;
				}
			}
		};
	}

	protected ReceiverPort findReceiverPort(Workflow wf, Link sink)
			throws ReaderException {
		String portName = sink.getPort();
		if (portName == null) {
			throw new ReaderException("Port name not specified");
		}
		String processorName = sink.getProcessor();
		if (processorName == null) {
			if (sink.getType().equals(LinkType.PROCESSOR)) {
				throw new ReaderException(
						"Link type was processor, but no processor name found");
			}
			OutputWorkflowPort candidate = wf.getOutputPorts().getByName(
					portName);
			if (candidate == null) {
				throw new ReaderException("Link to unknown workflow port "
						+ portName);
			}
			return candidate;
		} else {
			if (sink.getType().equals(LinkType.DATAFLOW)) {
				throw new ReaderException(
						"Link type was dataflow, but processor name was found");
			}
			Processor processor = wf.getProcessors().getByName(processorName);
			if (processor == null) {
				throw new ReaderException("Link to unknown processor "
						+ processorName);
			}
			InputProcessorPort candidate = processor.getInputPorts().getByName(
					portName);
			if (candidate == null) {
				throw new ReaderException("Link to unknown port " + portName
						+ " in " + processorName);
			}
			return candidate;
		}
	}

	protected SenderPort findSenderPort(Workflow wf, Link source)
			throws ReaderException {
		if (source.getType().equals(LinkType.MERGE)) {
			throw new ReaderException(
					"Link type Merge unexpected for sender ports");
		}
		String portName = source.getPort();
		if (portName == null) {
			throw new ReaderException("Port name not specified");
		}
		String processorName = source.getProcessor();
		if (processorName == null) {
			if (source.getType().equals(LinkType.PROCESSOR)) {
				throw new ReaderException(
						"Link type was processor, but no processor name found");
			}
			InputWorkflowPort candidate = wf.getInputPorts()
					.getByName(portName);
			if (candidate == null) {
				throw new ReaderException("Link from unknown workflow port "
						+ portName);
			}
			return candidate;
		} else {
			if (source.getType().equals(LinkType.DATAFLOW)) {
				throw new ReaderException(
						"Link type was dataflow, but processor name was found");
			}
			Processor processor = wf.getProcessors().getByName(processorName);
			if (processor == null) {
				throw new ReaderException("Link from unknown processor "
						+ processorName);
			}
			OutputProcessorPort candidate = processor.getOutputPorts()
					.getByName(portName);
			if (candidate == null) {
				throw new ReaderException("Link from unknown port " + portName
						+ " in " + processorName);
			}
			return candidate;
		}
	}

	protected T2Parser getT2Parser(URI classURI) {
		for (T2Parser t2Parser : getT2Parsers()) {
			if (t2Parser.canHandlePlugin(classURI)) {
				return t2Parser;
			}
		}
		return null;
	}

	public synchronized Set<T2Parser> getT2Parsers() {
		Set<T2Parser> parsers = t2Parsers;
		if (parsers != null) {
			return parsers;
		}
		parsers = new HashSet<T2Parser>();
		// TODO: Do we need to cache this, or is the cache in ServiceLoader
		// fast enough?
		if (discoveredT2Parsers == null) {
			discoveredT2Parsers = ServiceLoader.load(T2Parser.class);
		}
		for (T2Parser parser : discoveredT2Parsers) {
			parsers.add(parser);
		}
		return parsers;
	}

	public synchronized void setT2Parsers(Set<T2Parser> parsers) {
		this.t2Parsers = parsers;
	}

	public boolean isStrict() {
		return strict;
	}

	protected void makeProfile(uk.org.taverna.scufl2.xml.t2flow.jaxb.Workflow wf) {
		Profile profile = new Profile(wf.getProducedBy());
		profile.setParent(parserState.get().getCurrentWorkflowBundle());
		parserState.get().getCurrentWorkflowBundle().setMainProfile(profile);
		parserState.get().setCurrentProfile(profile);
	}

	private URI makeRavenURI(Raven raven, String className) {
		return ravenURI.resolve(uriTools.validFilename(raven.getGroup()) + "/"
				+ uriTools.validFilename(raven.getArtifact()) + "/"
				+ uriTools.validFilename(raven.getVersion()) + "/"
				+ uriTools.validFilename(className));
	}

	private URI mapTypeFromRaven(Raven raven, String activityClass)
			throws ReaderException {
		URI classURI = makeRavenURI(raven, activityClass);
		parserState.get().setCurrentT2Parser(null);
		T2Parser t2Parser = getT2Parser(classURI);
		if (t2Parser == null) {
			String message = "Unknown T2 activity " + classURI
					+ ", install supporting T2Parser";
			if (isStrict()) {
				throw new ReaderException(message);
			} else {
				logger.warning(message);
				return classURI;
			}
		}
		parserState.get().setCurrentT2Parser(t2Parser);
		return t2Parser.mapT2flowRavenIdToScufl2URI(classURI);
	}

	protected uk.org.taverna.scufl2.api.activity.Activity parseActivity(
			Activity origActivity) throws ReaderException {
		Raven raven = origActivity.getRaven();
		String activityClass = origActivity.getClazz();
		URI activityId = mapTypeFromRaven(raven, activityClass);
		uk.org.taverna.scufl2.api.activity.Activity newActivity = new uk.org.taverna.scufl2.api.activity.Activity();
		newActivity.setConfigurableType(activityId);
		newActivity.setName(parserState.get().getCurrentProcessorBinding()
				.getName());
		parserState.get().getCurrentProfile().getActivities()
				.addWithUniqueName(newActivity);
		newActivity.setParent(parserState.get().getCurrentProfile());
		return newActivity;
	}

	protected void parseActivityBinding(Activity origActivity,
			int activityPosition) throws ReaderException, JAXBException {
		ProcessorBinding processorBinding = new ProcessorBinding();

		processorBinding.setName(parserState.get().getCurrentProcessor()
				.getName());
		parserState.get().getCurrentProfile().getProcessorBindings()
				.addWithUniqueName(processorBinding);

		processorBinding.setBoundProcessor(parserState.get()
				.getCurrentProcessor());
		parserState.get().setCurrentProcessorBinding(processorBinding);
		uk.org.taverna.scufl2.api.activity.Activity newActivity = parseActivity(origActivity);
		parserState.get().setCurrentActivity(newActivity);

		parserState.get().getCurrentProfile().getActivities().add(newActivity);
		processorBinding.setBoundActivity(newActivity);
		processorBinding.setActivityPosition(activityPosition);

		parserState.get().setCurrentConfigurable(newActivity);

		try {
			parseConfiguration(origActivity.getConfigBean(),
					Configures.activity);
		} catch (JAXBException e) {
			if (isStrict()) {
				throw e;
			}
			logger.log(Level.WARNING, "Can't configure activity" + newActivity,
					e);
		}

		parseActivityInputMap(origActivity.getInputMap());
		parseActivityOutputMap(origActivity.getOutputMap());

		parserState.get().setCurrentConfigurable(null);
		parserState.get().setCurrentActivity(null);
		parserState.get().setCurrentProcessorBinding(null);
	}

	enum Configures {
		activity, dispatchLayer
	};

	protected void parseConfiguration(ConfigBean configBean,
			Configures configures) throws JAXBException, ReaderException {

		// Placeholder to check later if no configuration have been provided
		Configuration UNCONFIGURED = new Configuration();
		
		Configuration configuration = UNCONFIGURED;
		if (parserState.get().getCurrentT2Parser() == null) {
			String message = "No config parser for " + configures
					+ parserState.get().getCurrentConfigurable();
			if (isStrict()) {
				throw new ReaderException(message);
			}
			return;
		}

		try {
			configuration = parserState.get().getCurrentT2Parser()
					.parseConfiguration(this, configBean, parserState.get());
		} catch (ReaderException e) {
			if (isStrict()) {
				throw e;
			}
		}
		if (configuration == null) {
			// Perfectly valid - true for say Invoke layer
			return;
		}
		
		if (configuration == UNCONFIGURED) {
			if (isStrict()) {
				throw new ReaderException("No configuration returned from "
						+ parserState.get().getCurrentT2Parser() + " for "
						+ configures
						+ parserState.get().getCurrentConfigurable());
			}
			// We'll have to fake it
			configuration = new Configuration();
			configuration.setConfigurableType(configBeanURI.resolve("Config"));

			URI fallBackURI = configBeanURI.resolve(configBean.getEncoding());

			java.util.Map<URI, SortedSet<PropertyObject>> properties = configuration
					.getPropertyResource().getProperties();
			Object any = configBean.getAny();
			Element element = (Element) configBean.getAny();
			PropertyLiteral literal = new PropertyLiteral(element);
			// literal.setLiteralValue(configBean.getAny().toString());
			// literal.setLiteralType(PropertyLiteral.XML_LITERAL);
			properties.get(fallBackURI).add(literal);

		}
		
		if (configures == Configures.activity) {
			configuration.setName(parserState.get().getCurrentActivity()
					.getName());
		} else {
			DispatchStackLayer layer = (DispatchStackLayer) parserState.get().getCurrentConfigurable();
			configuration.setName(parserState.get().getCurrentProcessor().getName() +
					"-dispatch-" + parserState.get().getCurrentDispatchStack().size());

		}
		parserState.get().getCurrentProfile().getConfigurations()
				.addWithUniqueName(configuration);
		configuration.setConfigures(parserState.get().getCurrentConfigurable());
		parserState.get().getCurrentProfile().getConfigurations().addWithUniqueName(configuration);
	}

	public Unmarshaller getUnmarshaller() {
		Unmarshaller u = unmarshaller.get();
		if (!isValidating() && u.getSchema() != null) {
			u.setSchema(null);
		} else if (isValidating() && u.getSchema() == null) {
			// Load and set schema to validate against
			Schema schema;
			try {
				SchemaFactory schemaFactory = SchemaFactory
						.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
				List<URI> schemas = getAdditionalSchemas();
				URL t2flowExtendedXSD = T2FlowParser.class
						.getResource(T2FLOW_EXTENDED_XSD);
				schemas.add(t2flowExtendedXSD.toURI());

				List<Source> schemaSources = new ArrayList<Source>();
				for (URI schemaUri : schemas) {
					schemaSources.add(new StreamSource(schemaUri
							.toASCIIString()));
				}
				Source[] sources = schemaSources
						.toArray(new Source[schemaSources.size()]);
				schema = schemaFactory.newSchema(sources);
			} catch (SAXException e) {
				throw new RuntimeException("Can't load schemas", e);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Can't find schemas", e);
			} catch (NullPointerException e) {
				throw new RuntimeException("Can't find schemas", e);
			}
			u.setSchema(schema);
		}
		return u;
	}

	protected List<URI> getAdditionalSchemas() {
		List<URI> uris = new ArrayList<URI>();
		for (T2Parser parser : getT2Parsers()) {
			List<URI> schemas = parser.getAdditionalSchemas();
			if (schemas != null) {
				uris.addAll(schemas);
			}
		}
		return uris;
	}

	protected void parseActivityInputMap(
			uk.org.taverna.scufl2.xml.t2flow.jaxb.Map inputMap)
			throws ReaderException {
		for (Mapping mapping : inputMap.getMap()) {
			String fromProcessorOutput = mapping.getFrom();
			String toActivityOutput = mapping.getTo();
			ProcessorInputPortBinding processorInputPortBinding = new ProcessorInputPortBinding();

			InputProcessorPort inputProcessorPort = findNamed(parserState.get()
					.getCurrentProcessor().getInputPorts(), fromProcessorOutput);
			if (inputProcessorPort == null) {
				String message = "Invalid input port binding, "
						+ "unknown processor port: " + fromProcessorOutput
						+ "->" + toActivityOutput + " in "
						+ parserState.get().getCurrentProcessor();
				if (isStrict()) {
					throw new ReaderException(message);
				} else {
					logger.log(Level.WARNING, message);
					continue;
				}
			}

			InputActivityPort inputActivityPort = parserState.get()
					.getCurrentActivity().getInputPorts()
					.getByName(toActivityOutput);
			if (inputActivityPort == null) {
				inputActivityPort = new InputActivityPort();
				inputActivityPort.setName(toActivityOutput);
				inputActivityPort.setParent(parserState.get()
						.getCurrentActivity());
				parserState.get().getCurrentActivity().getInputPorts()
						.add(inputActivityPort);
			}

			if (inputActivityPort.getDepth() == null) {
				inputActivityPort.setDepth(inputProcessorPort.getDepth());
			}

			processorInputPortBinding.setBoundActivityPort(inputActivityPort);
			processorInputPortBinding.setBoundProcessorPort(inputProcessorPort);
			parserState.get().getCurrentProcessorBinding()
					.getInputPortBindings().add(processorInputPortBinding);
		}

	}

	protected void parseActivityOutputMap(
			uk.org.taverna.scufl2.xml.t2flow.jaxb.Map outputMap)
			throws ReaderException {
		for (Mapping mapping : outputMap.getMap()) {
			String fromActivityOutput = mapping.getFrom();
			String toProcessorOutput = mapping.getTo();
			ProcessorOutputPortBinding processorOutputPortBinding = new ProcessorOutputPortBinding();

			OutputProcessorPort outputProcessorPort = findNamed(parserState
					.get().getCurrentProcessor().getOutputPorts(),
					toProcessorOutput);
			if (outputProcessorPort == null) {
				String message = "Invalid output port binding, "
						+ "unknown processor port: " + fromActivityOutput
						+ "->" + toProcessorOutput + " in "
						+ parserState.get().getCurrentProcessor();
				if (isStrict()) {
					throw new ReaderException(message);
				} else {
					logger.log(Level.WARNING, message);
					continue;
				}
			}

			OutputActivityPort outputActivityPort = parserState.get()
					.getCurrentActivity().getOutputPorts()
					.getByName(fromActivityOutput);
			if (outputActivityPort == null) {
				outputActivityPort = new OutputActivityPort();
				outputActivityPort.setName(fromActivityOutput);
				outputActivityPort.setParent(parserState.get()
						.getCurrentActivity());
				parserState.get().getCurrentActivity().getOutputPorts()
						.add(outputActivityPort);
			}

			if (outputActivityPort.getDepth() == null) {
				outputActivityPort.setDepth(outputProcessorPort.getDepth());
			}
			if (outputActivityPort.getGranularDepth() == null) {
				outputActivityPort.setGranularDepth(outputProcessorPort
						.getGranularDepth());
			}

			processorOutputPortBinding.setBoundActivityPort(outputActivityPort);
			processorOutputPortBinding
					.setBoundProcessorPort(outputProcessorPort);
			parserState.get().getCurrentProcessorBinding()
					.getOutputPortBindings().add(processorOutputPortBinding);
		}

	}

	protected Workflow parseDataflow(Dataflow df, Workflow wf)
			throws ReaderException, JAXBException {
		parserState.get().setCurrentWorkflow(wf);
		wf.setInputPorts(parseInputPorts(df.getInputPorts()));
		wf.setOutputPorts(parseOutputPorts(df.getOutputPorts()));
		wf.setProcessors(parseProcessors(df.getProcessors()));
		wf.setDataLinks(parseDatalinks(df.getDatalinks()));
		wf.setControlLinks(parseControlLinks(df.getConditions()));
		// TODO: annotations
		parserState.get().setCurrentWorkflow(null);
		return wf;
	}

	private Set<ControlLink> parseControlLinks(Conditions conditions)
			throws ReaderException {
		Set<ControlLink> links = new HashSet<ControlLink>();
		for (Condition condition : conditions.getCondition()) {
			NamedSet<Processor> processors = parserState.get()
					.getCurrentWorkflow().getProcessors();
			String target = condition.getTarget();
			Processor block = processors.getByName(target);
			if (block == null && isStrict()) {
				throw new ReaderException(
						"Unrecognized blocking processor in control link: "
								+ target);
			}
			String control = condition.getControl();
			Processor untilFinished = processors.getByName(control);
			if (untilFinished == null && isStrict()) {
				throw new ReaderException(
						"Unrecognized untilFinished processor in control link: "
								+ control);
			}

			links.add(new BlockingControlLink(block, untilFinished));
		}
		return links;
	}

	protected Set<DataLink> parseDatalinks(Datalinks origLinks)
			throws ReaderException {
		HashSet<DataLink> newLinks = new HashSet<DataLink>();
		Map<ReceiverPort, AtomicInteger> mergeCounts = new HashMap<ReceiverPort, AtomicInteger>();
		for (uk.org.taverna.scufl2.xml.t2flow.jaxb.DataLink origLink : origLinks
				.getDatalink()) {
			try {
				SenderPort senderPort = findSenderPort(parserState.get()
						.getCurrentWorkflow(), origLink.getSource());
				ReceiverPort receiverPort = findReceiverPort(parserState.get()
						.getCurrentWorkflow(), origLink.getSink());

				DataLink newLink = new DataLink(parserState.get()
						.getCurrentWorkflow(), senderPort, receiverPort);

				AtomicInteger mergeCount = mergeCounts.get(receiverPort);
				if (origLink.getSink().getType().equals(LinkType.MERGE)) {
					if (mergeCount != null && mergeCount.intValue() < 1) {
						throw new ReaderException(
								"Merged and non-merged links to port "
										+ receiverPort);
					}
					if (mergeCount == null) {
						mergeCount = new AtomicInteger(0);
						mergeCounts.put(receiverPort, mergeCount);
					}
					newLink.setMergePosition(mergeCount.getAndIncrement());
				} else {
					if (mergeCount != null) {
						throw new ReaderException(
								"More than one link to non-merged port "
										+ receiverPort);
					}
					mergeCounts.put(receiverPort, new AtomicInteger(-1));
				}
				newLinks.add(newLink);
			} catch (ReaderException ex) {
				logger.log(Level.WARNING, "Could not translate link:\n"
						+ origLink, ex);
				if (isStrict()) {
					throw ex;
				}
				continue;
			}
		}
		return newLinks;
	}

	protected uk.org.taverna.scufl2.api.dispatchstack.DispatchStack parseDispatchStack(
			DispatchStack dispatchStack) throws ReaderException {
		uk.org.taverna.scufl2.api.dispatchstack.DispatchStack newStack = new uk.org.taverna.scufl2.api.dispatchstack.DispatchStack();
		parserState.get().setCurrentDispatchStack(newStack);
		try { 
			for (DispatchLayer dispatchLayer : dispatchStack.getDispatchLayer()) {
				DispatchStackLayer layer = parseDispatchStack(dispatchLayer);
				newStack.add(layer);
			}
		} finally {
			parserState.get().setCurrentDispatchStack(null);
		}
		return newStack;
	}

	protected DispatchStackLayer parseDispatchStack(DispatchLayer dispatchLayer)
			throws ReaderException {
		DispatchStackLayer dispatchStackLayer = new DispatchStackLayer();
		URI typeUri = mapTypeFromRaven(dispatchLayer.getRaven(),
				dispatchLayer.getClazz());
		dispatchStackLayer.setConfigurableType(typeUri);
		parserState.get().setCurrentConfigurable(dispatchStackLayer);
		try {
			parseConfiguration(dispatchLayer.getConfigBean(),
					Configures.dispatchLayer);
		} catch (JAXBException ex) {
			String message = "Can't parse configuration for dispatch layer in "
					+ parserState.get().getCurrentProcessor();
			logger.log(Level.WARNING, message, ex);
			if (isStrict()) {
				throw new ReaderException(message, ex);
			}

		}
		return dispatchStackLayer;
	}

	@SuppressWarnings("boxing")
	protected Set<InputWorkflowPort> parseInputPorts(
			AnnotatedGranularDepthPorts originalPorts) throws ReaderException {
		Set<InputWorkflowPort> createdPorts = new HashSet<InputWorkflowPort>();
		for (AnnotatedGranularDepthPort originalPort : originalPorts.getPort()) {
			InputWorkflowPort newPort = new InputWorkflowPort(parserState.get()
					.getCurrentWorkflow(), originalPort.getName());
			newPort.setDepth(originalPort.getDepth().intValue());
			if (!originalPort.getGranularDepth()
					.equals(originalPort.getDepth())) {
				String message = "Specific input port granular depth not "
						+ "supported in scufl2, port " + originalPort.getName()
						+ " has depth " + originalPort.getDepth()
						+ " and granular depth "
						+ originalPort.getGranularDepth();
				logger.log(Level.WARNING, message);
				if (isStrict()) {
					throw new ReaderException(message);
				}
			}
			createdPorts.add(newPort);
		}
		return createdPorts;
	}

	protected IterationStrategyStack parseIterationStrategyStack(
			uk.org.taverna.scufl2.xml.t2flow.jaxb.IterationStrategyStack originalStack)
			throws ReaderException {
		IterationStrategyStack newStack = new IterationStrategyStack();

		List<TopIterationNode> strategies = originalStack.getIteration()
				.getStrategy();
		for (TopIterationNode strategy : strategies) {
			IterationNode topNode = strategy.getCross();
			if (topNode == null) {
				topNode = strategy.getDot();
			}
			if (topNode == null) {
				continue;
			}
			IterationNodeParent parent = (IterationNodeParent) topNode;
			if (parent.getCrossOrDotOrPort().isEmpty()) {
				continue;
			}
			try {
				newStack.add((IterationStrategyTopNode) parseIterationStrategyNode(topNode));
			} catch (ReaderException e) {
				logger.warning(e.getMessage());
				if (isStrict()) {
					throw e;
				}
			}
		}

		return newStack;
	}

	protected IterationStrategyNode parseIterationStrategyNode(
			IterationNode topNode) throws ReaderException {
		if (topNode instanceof PortProduct) {
			PortProduct portProduct = (PortProduct) topNode;
			PortNode portNode = new PortNode();
			portNode.setDesiredDepth(portProduct.getDepth().intValue());
			String name = portProduct.getName();
			Processor processor = parserState.get().getCurrentProcessor();
			InputProcessorPort inputProcessorPort = processor.getInputPorts()
					.getByName(name);
			portNode.setInputProcessorPort(inputProcessorPort);
			return portNode;
		}

		IterationStrategyNode node;
		if (topNode instanceof DotProduct) {
			node = new uk.org.taverna.scufl2.api.iterationstrategy.DotProduct();
		} else if (topNode instanceof CrossProduct) {
			node = new uk.org.taverna.scufl2.api.iterationstrategy.CrossProduct();
		} else {
			throw new ReaderException("Invalid node " + topNode);
		}
		List<IterationStrategyNode> children = (List<IterationStrategyNode>) node;
		IterationNodeParent parent = (IterationNodeParent) topNode;
		for (IterationNode child : parent.getCrossOrDotOrPort()) {
			children.add(parseIterationStrategyNode(child));
		}
		return node;
	}

	protected Set<OutputWorkflowPort> parseOutputPorts(
			AnnotatedPorts originalPorts) {
		Set<OutputWorkflowPort> createdPorts = new HashSet<OutputWorkflowPort>();
		for (Port originalPort : originalPorts.getPort()) {
			OutputWorkflowPort newPort = new OutputWorkflowPort(parserState
					.get().getCurrentWorkflow(), originalPort.getName());
			createdPorts.add(newPort);
		}
		return createdPorts;

	}

	@SuppressWarnings("boxing")
	protected Set<InputProcessorPort> parseProcessorInputPorts(
			Processor newProc, DepthPorts origPorts) {
		Set<InputProcessorPort> newPorts = new HashSet<InputProcessorPort>();
		for (DepthPort origPort : origPorts.getPort()) {
			InputProcessorPort newPort = new InputProcessorPort(newProc,
					origPort.getName());
			newPort.setDepth(origPort.getDepth().intValue());
			// TODO: What about InputProcessorPort granular depth?
			newPorts.add(newPort);
		}
		return newPorts;
	}

	@SuppressWarnings("boxing")
	protected Set<OutputProcessorPort> parseProcessorOutputPorts(
			Processor newProc, GranularDepthPorts origPorts) {
		Set<OutputProcessorPort> newPorts = new HashSet<OutputProcessorPort>();
		for (GranularDepthPort origPort : origPorts.getPort()) {
			OutputProcessorPort newPort = new OutputProcessorPort(newProc,
					origPort.getName());
			newPort.setDepth(origPort.getDepth().intValue());
			newPort.setGranularDepth(origPort.getGranularDepth().intValue());
			newPorts.add(newPort);
		}
		return newPorts;
	}

	protected Set<Processor> parseProcessors(Processors originalProcessors)
			throws ReaderException, JAXBException {
		HashSet<Processor> newProcessors = new HashSet<Processor>();
		for (uk.org.taverna.scufl2.xml.t2flow.jaxb.Processor origProc : originalProcessors
				.getProcessor()) {
			Processor newProc = new Processor(parserState.get()
					.getCurrentWorkflow(), origProc.getName());
			parserState.get().setCurrentProcessor(newProc);
			newProc.setInputPorts(parseProcessorInputPorts(newProc,
					origProc.getInputPorts()));
			newProc.setOutputPorts(parseProcessorOutputPorts(newProc,
					origProc.getOutputPorts()));
			newProc.setDispatchStack(parseDispatchStack(origProc
					.getDispatchStack()));
			newProc.setIterationStrategyStack(parseIterationStrategyStack(origProc
					.getIterationStrategyStack()));
			newProcessors.add(newProc);
			int i = 0;
			for (Activity origActivity : origProc.getActivities().getActivity()) {
				parseActivityBinding(origActivity, i++);
			}
		}
		parserState.get().setCurrentProcessor(null);
		return newProcessors;
	}

	@SuppressWarnings("unchecked")
	public WorkflowBundle parseT2Flow(File t2File) throws IOException,
			ReaderException, JAXBException {
		JAXBElement<uk.org.taverna.scufl2.xml.t2flow.jaxb.Workflow> root = (JAXBElement<uk.org.taverna.scufl2.xml.t2flow.jaxb.Workflow>) getUnmarshaller()
				.unmarshal(t2File);
		return parseT2Flow(root.getValue());
	}

	@SuppressWarnings("unchecked")
	public WorkflowBundle parseT2Flow(InputStream t2File) throws IOException,
			JAXBException, ReaderException {
		JAXBElement<uk.org.taverna.scufl2.xml.t2flow.jaxb.Workflow> root = (JAXBElement<uk.org.taverna.scufl2.xml.t2flow.jaxb.Workflow>) getUnmarshaller()
				.unmarshal(t2File);
		return parseT2Flow(root.getValue());
	}

	public WorkflowBundle parseT2Flow(
			uk.org.taverna.scufl2.xml.t2flow.jaxb.Workflow wf)
			throws ReaderException, JAXBException {
		try {
			parserState.get().setT2FlowParser(this);
			WorkflowBundle wfBundle = new WorkflowBundle();
			parserState.get().setCurrentWorkflowBundle(wfBundle);
			makeProfile(wf);

			// First a skeleton scan of workflows (for nested workflow configs)
			Map<Dataflow, Workflow> dataflowMap = new HashMap<Dataflow, Workflow>();
			for (Dataflow df : wf.getDataflow()) {
				Workflow workflow = skeletonDataflow(df);
				dataflowMap.put(df, workflow);
				wfBundle.getWorkflows().addWithUniqueName(workflow);
				workflow.setParent(wfBundle);
				if (df.getRole().equals(Role.TOP)) {
					wfBundle.setMainWorkflow(workflow);
					wfBundle.setName(df.getName());
					wfBundle.setGlobalBaseURI(WorkflowBundle.WORKFLOW_BUNDLE_ROOT
							.resolve(df.getId() + "/"));
				}
			}
			// Second stage
			for (Dataflow df : wf.getDataflow()) {
				Workflow workflow = dataflowMap.get(df);
				parseDataflow(df, workflow);
			}
			if (isStrict() && wfBundle.getMainWorkflow() == null) {
				throw new ReaderException("No main workflow");
			}
			scufl2Tools.setParents(wfBundle);

			return wfBundle;
		} finally {
			parserState.remove();
		}
	}

	protected Workflow skeletonDataflow(Dataflow df) {
		Workflow wf = new Workflow();
		parserState.get().setCurrentWorkflow(wf);
		wf.setName(df.getName());
		wf.setWorkflowIdentifier(Workflow.WORKFLOW_ROOT.resolve(df.getId()
				+ "/"));
		return wf;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

}
