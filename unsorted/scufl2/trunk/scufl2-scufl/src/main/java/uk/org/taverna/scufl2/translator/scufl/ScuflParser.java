/**
 * 
 */
package uk.org.taverna.scufl2.translator.scufl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;
import uk.org.taverna.scufl2.api.profiles.Profile;
import uk.org.taverna.scufl2.xml.scufl.jaxb.CoordinationType;
import uk.org.taverna.scufl2.xml.scufl.jaxb.DefaultType;
import uk.org.taverna.scufl2.xml.scufl.jaxb.DefaultsType;
import uk.org.taverna.scufl2.xml.scufl.jaxb.LinkType;
import uk.org.taverna.scufl2.xml.scufl.jaxb.ObjectFactory;
import uk.org.taverna.scufl2.xml.scufl.jaxb.ProcessorType;
import uk.org.taverna.scufl2.xml.scufl.jaxb.ScuflType;
import uk.org.taverna.scufl2.xml.scufl.jaxb.SinkType;
import uk.org.taverna.scufl2.xml.scufl.jaxb.SourceType;
import uk.org.taverna.scufl2.xml.scufl.jaxb.WorkflowDescriptionType;

/**
 * @author alanrw
 *
 */
public class ScuflParser {
	
	private static final Logger logger = Logger.getLogger(ScuflParser.class
			.getCanonicalName());

	private static final String SCUFL_XSD = "xsd/scufl.xsd";
	private static final String LOCAL_XSD = "xsd/scufl-local.xsd";
	
	
	private static final String SCUFL = "SCUFL";

	protected Set<ScuflExtensionParser> scuflExtensionParsers = null;
	protected final JAXBContext jaxbContext;
	private boolean strict = false;
	private boolean validating = false;

	protected ThreadLocal<ParserState> parserState = new ThreadLocal<ParserState>() {
		@Override
		protected ParserState initialValue() {
			return new ParserState();
		};
	};

	private static Scufl2Tools scufl2Tools = new Scufl2Tools();
	protected ServiceLoader<ScuflExtensionParser> discoveredScuflExtensionParsers;
	protected final ThreadLocal<Unmarshaller> unmarshaller;
	
	public ScuflParser() throws JAXBException {
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

	
	@SuppressWarnings("unchecked")
	public WorkflowBundle parseScufl(File scuflFile) throws IOException,
			ReaderException, JAXBException {
		JAXBElement<ScuflType> root = (JAXBElement<ScuflType>) getUnmarshaller()
				.unmarshal(scuflFile);
		return parseScufl(root.getValue());
	}

	@SuppressWarnings("unchecked")
	public WorkflowBundle parseScufl(InputStream scuflFile) throws IOException,
			JAXBException, ReaderException {
		JAXBElement<ScuflType> root = (JAXBElement<ScuflType>) getUnmarshaller()
				.unmarshal(scuflFile);
		return parseScufl(root.getValue());
	}

	public WorkflowBundle parseScufl(
			ScuflType wf)
			throws ReaderException, JAXBException {
		try {
			parserState.get().setCurrentParser(this);
			WorkflowBundle wfBundle = new WorkflowBundle();
			parserState.get().setCurrentWorkflowBundle(wfBundle);
			makeProfile(wf);
			Workflow w = parseWorkflow(wf);
			wfBundle.setMainWorkflow(w);

			scufl2Tools.setParents(wfBundle);

			return wfBundle;
		} finally {
			parserState.remove();
		}
	}

	private Workflow parseWorkflow(ScuflType wf) {
		Workflow oldCurrentWorkflow = parserState.get().getCurrentWorkflow();
		Workflow workflow = new Workflow();
		workflow.setParent(parserState.get().getCurrentWorkflowBundle());
		parserState.get().addMapping(wf, workflow);
		parserState.get().setCurrentWorkflow(workflow);
		WorkflowDescriptionType description = wf.getWorkflowdescription();
		workflow.setName(sanitiseName(description.getTitle()));
		
		parseWorkflowInputs(wf);
		parseWorkflowOutputs(wf);
		parseProcessors(wf);
		parseLinks(wf);
		parseCoordinations(wf);
		parseAnnotations(wf);
		
		replaceDefaultsWithStringConstants(wf); // To be done
		
		parserState.get().setCurrentWorkflow(oldCurrentWorkflow);
		return workflow;
	}




	private void parseAnnotations(ScuflType wf) {
		// TODO Auto-generated method stub
		
	}


	private void parseCoordinations(ScuflType wf) {
		for (CoordinationType c : wf.getCoordination()) {
			parseCoordination(c);
		}
		
	}


	private void parseCoordination(CoordinationType c) {
		// TODO Auto-generated method stub
		
	}


	private void parseLinks(ScuflType wf) {
		for (LinkType dl : wf.getLink()) {
			parseLink(dl);
		}
	}


	private void parseLink(LinkType dl) {
		// TODO Auto-generated method stub
		
	}


	private void parseWorkflowInputs(ScuflType wf) {
		for (SourceType st : wf.getSource()) {
			parseWorkflowInput(st);
		}
	}
	
	private void parseWorkflowInput(SourceType st) {
		Workflow currentWorkflow = parserState.get().getCurrentWorkflow();
		InputWorkflowPort iwp = new InputWorkflowPort(currentWorkflow, sanitiseName(st.getName()));
		parserState.get().addMapping(st, iwp);
		// Cannot do anything about the depths
	}

	private void parseWorkflowOutputs(ScuflType wf) {
		for (SinkType st : wf.getSink()) {
			parseWorkflowOutput(st);
		}
	}

	private void parseWorkflowOutput(SinkType st) {
		Workflow currentWorkflow = parserState.get().getCurrentWorkflow();
		OutputWorkflowPort owp = new OutputWorkflowPort(currentWorkflow, sanitiseName(st.getName()));
		parserState.get().addMapping(st, owp);
		// Cannot do anything about the depths
	}

	private void parseProcessors(ScuflType wf) {
		for (ProcessorType pt : wf.getProcessor()) {
			parseProcessor(pt);
		}
	}

	private void parseProcessor(ProcessorType pt) {
		Workflow currentWorkflow = parserState.get().getCurrentWorkflow();
		Processor p = new Processor(currentWorkflow, sanitiseName(pt.getName()));
		parserState.get().setCurrentProcessor(p);
		parseDispatchStack(pt);
		parseProcessorElement(pt.getProcessorElement());
		Activity activity = parserState.get().getCurrentActivity();
		if (activity != null) {
			createDefaultProcessorBinding();
		}
		parserState.get().setCurrentActivity(null);

		parseAlternates(pt);
		parseIterationStrategy(pt);
		parserState.get().setCurrentProcessor(null);
		parserState.get().addMapping(pt, p);
		// Cannot do anything about the ports
	}




	private void createDefaultProcessorBinding() {
		Processor p = parserState.get().getCurrentProcessor();
		Activity a = parserState.get().getCurrentActivity();
		
		ProcessorBinding pb = new ProcessorBinding();
		pb.setParent(parserState.get().getCurrentProfile());
		pb.setActivityPosition(0);
		pb.setBoundProcessor(p);
		pb.setBoundActivity(a);
		for (InputActivityPort iap : a.getInputPorts()) {
			InputProcessorPort ipp = findOrCreateProcessorInputPort(p, iap.getName(), iap.getDepth());
			ProcessorInputPortBinding portBinding = new ProcessorInputPortBinding();
			portBinding.setParent(pb);
			portBinding.setBoundActivityPort(iap);
			portBinding.setBoundProcessorPort(ipp);
		}
		for (OutputActivityPort oap : a.getOutputPorts()) {
			OutputProcessorPort opp = findOrCreateProcessorOutputPort(p, oap.getName(), oap.getDepth(), oap.getGranularDepth());
			ProcessorOutputPortBinding portBinding = new ProcessorOutputPortBinding();
			portBinding.setParent(pb);
			portBinding.setBoundActivityPort(oap);
			portBinding.setBoundProcessorPort(opp);
		}
	}


	private OutputProcessorPort findOrCreateProcessorOutputPort(Processor p,
			String name, Integer depth, Integer granularDepth) {
		OutputProcessorPort port = p.getOutputPorts().getByName(name);
		if (port == null) {
			port = new OutputProcessorPort();
			port.setParent(p);
			port.setName(name);
			port.setDepth(depth);
			port.setGranularDepth(granularDepth);
		}
		return port;
	}


	private InputProcessorPort findOrCreateProcessorInputPort(Processor p,
			String name, Integer depth) {
		InputProcessorPort port = p.getInputPorts().getByName(name);
		if (port == null) {
			port = new InputProcessorPort();
			port.setParent(p);
			port.setName(name);
			port.setDepth(depth);
		}
		return port;
	}


	private void parseAlternates(ProcessorType pt) {
		// TODO Auto-generated method stub
	}


	private void parseProcessorElement(JAXBElement<?> processorElement) {
		Object processorElementValue = processorElement.getValue();
		parseExtensionObject(processorElementValue);
	}

	private void parseExtensionObject(Object o) {
		findExtensionParser(o.getClass());
		if (parserState.get().getCurrentExtensionParser() != null) {
			parserState.get().getCurrentExtensionParser().setParserState(parserState.get());
			parserState.get().getCurrentExtensionParser().parseScuflObject(o);
			parserState.get().setCurrentExtensionParser(null);
		}
		else {
			System.err.println("Unrecognized extension " + o.getClass());
		}		
	}

	private void findExtensionParser(Class c) {
		parserState.get().setCurrentExtensionParser(null);
		for (ScuflExtensionParser extensionParser : getScuflExtensionParsers()) {
			if (extensionParser.canHandle(c)) {
				parserState.get().setCurrentExtensionParser(extensionParser);
				break;
			}
		}
	}
	
	private void parseDispatchStack(ProcessorType pt) {
		// TODO
	}

	private void parseIterationStrategy(ProcessorType pt) {
		// TODO
		
	}


	/**
	 * Crawls the scuflModel processors and checks their input ports for unbound
	 * default values. If one is found then a StringConstantProcessor is
	 * inserted upstream.
	 * 
	 * @param scuflModel
	 * @throws WorkflowTranslationException
	 */
	private void replaceDefaultsWithStringConstants(ScuflType scuflModel) {
		for (ProcessorType t1Processor : scuflModel
				.getProcessor()) {
			DefaultsType defaults = t1Processor.getDefaults();
			if (defaults != null) {
				for (DefaultType d : defaults.getDefault()) {
					String portName = d.getName();
					String constantValue = d.getValue();
					// TOBEDONE
				}
			}
		}

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
				
				URL scuflXSD = getClass().getResource(
						SCUFL_XSD);
				schemas.add(scuflXSD.toURI());
				
				List<Source> schemaSources = new ArrayList<Source>();
				for (URI schemaUri : schemas) {
					schemaSources.add(new StreamSource(schemaUri.toASCIIString()));
				}						
				Source[] sources = schemaSources.toArray(new Source[schemaSources.size()]);
				schema = schemaFactory.newSchema(sources);
				
			} catch (SAXException e) {
				throw new RuntimeException("Can't load schema "
						+ SCUFL_XSD, e);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Can't find schema "
						+ SCUFL_XSD, e);
			} catch (NullPointerException e) {
				throw new RuntimeException("Can't find schema "
						+ SCUFL_XSD, e);
			}
			u.setSchema(schema);


		}
		return u;
	}

	private void makeProfile(ScuflType wf) {
		Profile profile = new Profile(SCUFL + "-" + wf.getVersion());
		profile.setParent(parserState.get().getCurrentWorkflowBundle());
		parserState.get().getCurrentWorkflowBundle().setMainProfile(profile);
		parserState.get().setCurrentProfile(profile);
	}
	
	protected List<URI> getAdditionalSchemas() {
		List<URI> uris = new ArrayList<URI>();
		for (ScuflExtensionParser parser : getScuflExtensionParsers()) {
			List<URI> schemas = parser.getAdditionalSchemas();
			if (schemas != null) {
				uris.addAll(schemas);
			}
		}
		return uris;
	}

	public synchronized Set<ScuflExtensionParser> getScuflExtensionParsers() {
		Set<ScuflExtensionParser> parsers = scuflExtensionParsers;
		if (parsers != null) {
			return parsers;
		}
		parsers = new HashSet<ScuflExtensionParser>();
		// TODO: Do we need to cache this, or is the cache in ServiceLoader
		// fast enough?
		if (discoveredScuflExtensionParsers == null) {
			discoveredScuflExtensionParsers = ServiceLoader.load(ScuflExtensionParser.class);
		}
		for (ScuflExtensionParser parser : discoveredScuflExtensionParsers) {
			parsers.add(parser);
		}
		return parsers;
	}


	/**
	 * @return the strict
	 */
	public boolean isStrict() {
		return strict;
	}


	/**
	 * @param strict the strict to set
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}


	/**
	 * @return the validating
	 */
	public boolean isValidating() {
		return validating;
	}


	/**
	 * @param validating the validating to set
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Checks that the name does not have any characters that are invalid for a
	 * processor name.
	 * 
	 * The name must contain only the chars[A-Za-z_0-9].
	 * 
	 * @param name
	 *            the original name
	 * @return the sanitised name
	 */
	private static String sanitiseName(String name) {
		String result = name;
		if (Pattern.matches("\\w++", name) == false) {
			result = "";
			for (char c : name.toCharArray()) {
				if (Character.isLetterOrDigit(c) || c == '_') {
					result += c;
				}
			}
		}
		return result;
	}	

}
