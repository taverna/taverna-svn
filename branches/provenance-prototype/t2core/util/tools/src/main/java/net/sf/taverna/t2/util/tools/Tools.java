package net.sf.taverna.t2.util.tools;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import net.sf.taverna.raven.Raven;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
//import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.annotation.Annotated;
//import net.sf.taverna.t2.workflowmodel.Dataflow;
//import net.sf.taverna.t2.workflowmodel.EditException;
//import net.sf.taverna.t2.workflowmodel.InputPort;
//import net.sf.taverna.t2.workflowmodel.OutputPort;
//import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
//import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
//import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
//import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.AddDispatchLayerEdit;
//import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
//import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
//import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
//import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
//import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 
 * Contains static methods concerned with legacy Processor construction and XML
 * handling for the various configurable types such as Activity and
 * DispatchLayer.
 * 
 * @author Tom Oinn
 * 
 */
public class Tools {

	// XML element names
	private static final String LAYER = "layer";
	private static final String ACTIVITY = "activity";
	private static final String JAVA = "java";
	private static final String OUTPUT_MAP = "outputMap";
	private static final String TO = "to";
	private static final String FROM = "from";
	private static final String MAP = "map";
	private static final String INPUT_MAP = "inputMap";
	private static final String CLASS = "class";
	private static final String VERSION = "version";
	private static final String ARTIFACT = "artifact";
	private static final String GROUP = "group";
	private static final String RAVEN = "raven";
	private static final String ANNOTATIONS = "annotations";
	@SuppressWarnings("unused")
	private static final String ANNOTATION = "annotation";

	// Default values for buildFromActivity()
	private static final int MAX_JOBS = 5;
	private static final long BACKOFF_FACTOR = (long) 1.1;
	private static final int MAX_DELAY = 5000;
	private static final int INITIAL_DELAY = 1000;
	private static final int MAX_RETRIES = 3;

	/**
	 * Build a JDOM &lt;activity&gt; Element corresponding to the given
	 * {@link Activity} implementation. Relies on the {@link XMLEncoder} based
	 * serialisation of the configuration bean to store configuration data.
	 * 
	 * @param activity
	 *            {@link Activity} to serialise
	 * @return JDOM &lt;activity&gt; Element
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Element activityAsXML(Activity<?> activity)
			throws JDOMException, IOException {
		Element activityElem = new Element(ACTIVITY);

		ClassLoader cl = activity.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			activityElem
					.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS);
		classNameElement.setText(activity.getClass().getName());
		
		//need some sort of nested workflow stuff here some time
//		Element nestedActivity = new Element ("nestedActivity");
//		if (activity instanceof DataflowActivity) {
//			DataflowSerialiser dataflowSerialiser = new DataflowSerialiser(((DataflowActivity)activity).getConfiguration().getDataflow());
//			String serialiseWorkflow = dataflowSerialiser.serialiseWorkflow();
//			Document build = new SAXBuilder().build((new StringReader(serialiseWorkflow)));
//			nestedActivity.addContent(build.getRootElement());
//		}
		
		
		activityElem.addContent(classNameElement);

		// Write out the mappings (processor input -> activity input, activity
		// output -> processor output)
		Element ipElement = new Element(INPUT_MAP);
		for (String processorInputName : activity.getInputPortMapping()
				.keySet()) {
			Element mapElement = new Element(MAP);
			mapElement.setAttribute(FROM, processorInputName);
			mapElement.setAttribute(TO, activity.getInputPortMapping().get(
					processorInputName));
			ipElement.addContent(mapElement);
		}
		activityElem.addContent(ipElement);

		Element opElement = new Element(OUTPUT_MAP);
		for (String activityOutputName : activity.getOutputPortMapping()
				.keySet()) {
			Element mapElement = new Element(MAP);
			mapElement.setAttribute(FROM, activityOutputName);
			mapElement.setAttribute(TO, activity.getOutputPortMapping().get(
					activityOutputName));
			opElement.addContent(mapElement);
		}
		activityElem.addContent(opElement);

		// Get element for configuration
		Object o = activity.getConfiguration();
		Element configElement = beanAsElement(o);
		activityElem.addContent(configElement);

		return activityElem;

	}

	/**
	 * Add the annotations contained in the specified &lt;annotations&gt;
	 * element to the specified instance of a MutableAnnotated object.
	 * 
	 * @param annotations
	 *            {@link Element} to extract 'annotation' elements from
	 * @param annotated
	 *            {@link MutableAnnotated} to be annotated
	 */
	@SuppressWarnings("unchecked")
	public static void annotateObject(Element annotations, Annotated annotated) {
		// TODO - implement for new annotation chain framework
		/**
		for (Element e : (List<Element>) annotations.getChildren(ANNOTATION)) {
			ClassLoader cl = Tools.class.getClassLoader();
			Element ravenElement = e.getChild(RAVEN);
			if (ravenElement != null) {
				try {
					cl = getRavenLoader(ravenElement);
				} catch (Exception ex) {
					System.out.println("Exception loading raven "
							+ "classloader for Activity instance");
					ex.printStackTrace();
					// TODO - handle this properly, either by logging correctly
					// or by going back to the repository and attempting to
					// fetch the offending missing artifacts
				}
			}
			Object annotationBean = createBean(e.getChild(JAVA), cl);
			if (annotationBean instanceof WorkflowAnnotation) {
				WorkflowAnnotation newAnnotation = (WorkflowAnnotation) annotationBean;
				try {
					annotated.getAddAnnotationEdit(newAnnotation).doEdit();
				} catch (EditException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				System.out.println("Found non annotation bean inside an"
						+ " annotation element, something's not right here");
			}
		}
		*/

	}

	/**
	 * Get the &lt;java&gt; element from the {@link XMLEncoder} for the given
	 * bean as a JDOM {@link Element}.
	 * 
	 * @see net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser
	 * @param obj
	 *            Object to serialise
	 * @return &lt;java&gt; element for serialised bean
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Element beanAsElement(Object obj) throws JDOMException,
			IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder xenc = new XMLEncoder(bos);
		xenc.writeObject(obj);
		xenc.close();
		byte[] bytes = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		Element configElement = new SAXBuilder().build(bis).getRootElement();
		configElement.getParent().removeContent(configElement);
		return configElement;
	}

	/**
	 * Build an Activity instance from the specified &lt;activity&gt; JDOM
	 * Element using reflection to assemble the configuration bean and configure
	 * the new Activity object. If the &lt;activity&gt; has a &lt;raven&gt;
	 * child element the metadata in that element will be used to locate an
	 * appropriate ArtifactClassLoader, if absent the ClassLoader used will be
	 * the one used to load this utility class.
	 * 
	 * @param element
	 *            &lt;activity&gt; JDOM from where to build the Activity
	 * @return Built {@link Activity} instance
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ActivityConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public static Activity buildActivity(Element element)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ActivityConfigurationException {
		Element ravenElement = element.getChild(RAVEN);
		ClassLoader cl = Tools.class.getClassLoader();
		if (ravenElement != null) {
			try {
				cl = getRavenLoader(ravenElement);
			} catch (Exception ex) {
				System.out.println("Exception loading raven classloader "
						+ "for Activity instance");
				ex.printStackTrace();
				// TODO - handle this properly, either by logging correctly or
				// by going back to the repository and attempting to fetch the
				// offending missing artifacts
			}
		}
		String className = element.getChild(CLASS).getTextTrim();
		Class<? extends Activity> c = (Class<? extends Activity>) cl
				.loadClass(className);
		Activity<Object> activity = c.newInstance();

		Element ipElement = element.getChild(INPUT_MAP);
		for (Element mapElement : (List<Element>) (ipElement.getChildren(MAP))) {
			String processorInputName = mapElement.getAttributeValue(FROM);
			String activityInputName = mapElement.getAttributeValue(TO);
			activity.getInputPortMapping().put(processorInputName,
					activityInputName);
		}

		Element opElement = element.getChild(OUTPUT_MAP);
		for (Element mapElement : (List<Element>) (opElement.getChildren(MAP))) {
			String activityOutputName = mapElement.getAttributeValue(FROM);
			String processorOutputName = mapElement.getAttributeValue(TO);
			activity.getOutputPortMapping().put(activityOutputName,
					processorOutputName);
		}

		// Handle the configuration of the activity
		Element configElement = element.getChild(JAVA);
		Object configObject = createBean(configElement, cl);
		activity.configure(configObject);
		return activity;
	}

	/**
	 * Build a {@link DispatchLayer} object from the specified JDOM
	 * &lt;layer&gt; {@link Element}.
	 * 
	 * @param element
	 *            &lt;layer&gt; {@link Element}
	 * @return A {@link DispatchLayer} built from the element
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	public static DispatchLayer buildDispatchLayer(Element element)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		Element ravenElement = element.getChild(RAVEN);
		ClassLoader cl = Tools.class.getClassLoader();
		if (ravenElement != null) {
			try {
				cl = getRavenLoader(ravenElement);
			} catch (Exception ex) {
				System.out.println("Exception loading raven classloader "
						+ "for Activity instance");
				ex.printStackTrace();
				// TODO - handle this properly, either by logging correctly or
				// by going back to the repository and attempting to fetch the
				// offending missing artifacts
			}
		}
		String className = element.getChild(CLASS).getTextTrim();
		Class<? extends DispatchLayer> c = (Class<? extends DispatchLayer>) cl
				.loadClass(className);
		DispatchLayer<Object> layer = c.newInstance();

		// Handle the configuration of the dispatch layer
		Element configElement = element.getChild(JAVA);
		Object configObject = createBean(configElement, cl);
		layer.configure(configObject);

		return layer;
	}

	/**
	 * Construct a new {@link Processor} with a single {@link Activity} and
	 * overall processor inputs and outputs mapped to the activity inputs and
	 * outputs. This is intended to be equivalent to the processor creation in
	 * Taverna1 where the concepts of Processor and Activity were somewhat
	 * confused; it also inserts retry, parallelise and failover layers
	 * configured as a Taverna1 process would be.
	 * <p>
	 * Modifies the given activity object, adding the mappings for input and
	 * output port names (these will all be fooport->fooport but they're still
	 * needed)
	 * 
	 * @param activity
	 *            the {@link Activity} to use to build the new processor around
	 * @return An initialised {@link ProcessorImpl}
	 */


	/**
	 * Use the XMLDecoder to build an arbitrary java bean from the &lt;java&gt;
	 * JDOM Element object. Uses the supplied {@link ClassLoader} to accommodate
	 * systems such as {@link Raven}.
	 * 
	 * @param element
	 *            &lt;java&gt; JDOM {@link Element} from where to build the bean
	 * @param classLoader
	 *            {@link ClassLoader} from where to find the bean's classes
	 * @return The deserialised bean
	 */
	public static Object createBean(Element element, ClassLoader classLoader) {
		String beanXML = new XMLOutputter(Format.getRawFormat())
				.outputString(element);
		XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(beanXML
				.getBytes()), null, null, classLoader);
		Object bean = decoder.readObject();
		return bean;
	}

	/**
	 * Make a JDOM &lt;layer&gt; {@link Element} serialising the given
	 * {@link DispatchLayer}.
	 * 
	 * @param layer
	 *            layer to serialise
	 * @return &lt:layer&gt: {@link Element} describing the
	 *         {@link DispatchLayer}
	 */
	public static Element dispatchLayerAsXML(DispatchLayer<?> layer)
			throws JDOMException, IOException {
		Element layerElem = new Element(LAYER);

		ClassLoader cl = layer.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			layerElem.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS);
		classNameElement.setText(layer.getClass().getName());
		layerElem.addContent(classNameElement);

		// Get element for configuration
		Object o = layer.getConfiguration();
		Element configElement = beanAsElement(o);
		layerElem.addContent(configElement);
		return layerElem;
	}

	/**
	 * Return the &lt;annotation&gt; element for a specified {@link Annotated}
	 * entity.
	 * 
	 * @see net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser
	 * @param annotated
	 *            the workflow entity to serialise annotations for
	 * @return a JDOM {@link Element} object containing the annotations
	 */
	public static Element getAnnotationsElement(Annotated<?> annotated) {
		Element result = new Element(ANNOTATIONS);
		// TODO - implement for new annotation chain framework
		/**
		 * for (WorkflowAnnotation annotation : annotated.getAnnotations()) {
		 * Element annotationElement = new Element(ANNOTATION); // If this was
		 * loaded by raven then store the artifact details if
		 * (annotation.getClass().getClassLoader() instanceof
		 * LocalArtifactClassLoader) { LocalArtifactClassLoader lacl =
		 * (LocalArtifactClassLoader) annotation .getClass().getClassLoader();
		 * annotationElement.addContent(ravenElement(lacl)); } try {
		 * annotationElement.addContent(beanAsElement(annotation)); } catch
		 * (JDOMException e) { // Auto-generated catch block but should never
		 * see this e.printStackTrace(); } catch (IOException e) { //
		 * Auto-generated catch block but should never see this
		 * e.printStackTrace(); } result.addContent(annotationElement); }
		 */
		return result;
	}

	/**
	 * Get the {@link ClassLoader} for loading classes from the artifact
	 * specified by the &lt;raven&gt; element.
	 * <p>
	 * If this class wasn't loaded by Raven then this ignores the element
	 * entirely and defaults to using the same classloader as
	 * {@link Tools this class} was loaded by. This is probably not what you
	 * want but it's a sensible enough fallback position
	 * 
	 * @param ravenElement
	 *            &lt;raven&gt; element describing artifact
	 * @return Resolved {@link LocalArtifactClassLoader} or current
	 *         {@link ClassLoader}
	 * @throws ArtifactNotFoundException
	 *             If the element (directly or indirectly) specified an unknown
	 *             artifact
	 * @throws ArtifactStateException
	 *             If something went wrong when fetching artifact
	 */
	public static ClassLoader getRavenLoader(Element ravenElement)
			throws ArtifactNotFoundException, ArtifactStateException {
		// Try to get the current Repository object, if there isn't one we can't
		// do this here
		Repository repository = null;
		try {
			LocalArtifactClassLoader lacl = (LocalArtifactClassLoader) (Tools.class
					.getClassLoader());
			repository = lacl.getRepository();

		} catch (ClassCastException cce) {
			return Tools.class.getClassLoader();
			// TODO - should probably warn that this is happening as it's likely
			// to be because of an error in API usage. There are times it won't
			// be though so leave it for now.
		}
		String groupId = ravenElement.getChildTextTrim(GROUP);
		String artifactId = ravenElement.getChildTextTrim(ARTIFACT);
		String version = ravenElement.getChildTextTrim(VERSION);
		Artifact artifact = new BasicArtifact(groupId, artifactId, version);
		return repository.getLoader(artifact, null);
	}

	/**
	 * Insert the element produce by {@link #getAnnotationsElement(Annotated)}
	 * into the specified {@link Element}. If the annotation set is empty this
	 * does nothing - this is to prevent copy and paste code of the style 'if
	 * there are annotations add...'
	 * 
	 * @param element
	 *            {@link Element} where to inject annotations
	 * @param annotated
	 *            {@link Annotated} from where to find annotations
	 */
	public static void injectAnnotations(Element element, Annotated<?> annotated) {
		if (!annotated.getAnnotations().isEmpty()) {
			element.addContent(getAnnotationsElement(annotated));
		}
	}

	/**
	 * Populate annotations of a {@link MutableAnnotated} from an
	 * {@link Element} containing a child 'annotations'. If the annotations
	 * element is not present this method does nothing.
	 * 
	 * @see #injectAnnotations(Element, Annotated)
	 * @param parent
	 *            Element from where to find the 'annotations' child
	 * @param annotated
	 *            {@link MutableAnnotated} to be annotated
	 */
	public static void populateAnnotationsFromParent(Element parent,
			Annotated<?> annotated) {
		Element annotationsElement = parent.getChild(ANNOTATIONS);
		if (annotationsElement != null) {
			annotateObject(annotationsElement, annotated);
		}
	}

	/**
	 * Create the &lt;raven&gt; element for a given local artifact classloader.
	 * 
	 * @param classLoader
	 *            The {@link LocalArtifactClassLoader} for the artifact
	 * @return Populated &lt;raven&gt; element
	 */
	public static Element ravenElement(LocalArtifactClassLoader classLoader) {
		Element element = new Element(RAVEN);
		Artifact artifact = classLoader.getArtifact();
		// Group
		Element groupIdElement = new Element(GROUP);
		groupIdElement.setText(artifact.getGroupId());
		element.addContent(groupIdElement);
		// Artifact ID
		Element artifactIdElement = new Element(ARTIFACT);
		artifactIdElement.setText(artifact.getArtifactId());
		element.addContent(artifactIdElement);
		// Version
		Element versionElement = new Element(VERSION);
		versionElement.setText(artifact.getVersion());
		element.addContent(versionElement);
		// Return assembled raven element
		return element;
	}

	/**
	 * Protected constructor, use static methods only.
	 */
	protected Tools() {
	}

}
