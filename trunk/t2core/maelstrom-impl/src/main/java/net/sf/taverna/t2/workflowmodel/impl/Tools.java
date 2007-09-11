package net.sf.taverna.t2.workflowmodel.impl;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.annotation.impl.MutableAnnotated;
import net.sf.taverna.t2.annotation.impl.ServiceAnnotationContainerImpl;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.AddDispatchLayerEdit;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;

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

	/**
	 * Construct a new Processor with a single Activity and overall processor
	 * inputs and outputs mapped to the activity inputs and outputs. This is
	 * intended to be equivalent to the processor creation in Taverna1 where the
	 * concepts of Processor and Activity were somewhat confused; it also inserts
	 * retry, parallelize and failover layers configured as a Taverna1 process
	 * would be.
	 * <p>
	 * Modifies the given acivity object, adding the mappings for input and
	 * output port names (these will all be fooport->fooport but they're still
	 * needed)
	 * 
	 * @param activity the Activity to use to build the new processor around
	 * @return
	 */
	public static ProcessorImpl buildFromActivity(Activity<?> activity)
			throws EditException {
		ProcessorImpl result = new ProcessorImpl();
		// Add the Service to the processor
		result.serviceList.add(new ServiceAnnotationContainerImpl(activity));
		// Create processor inputs and outputs corresponding to service inputs
		// and outputs and set the mappings in the Service object.
		activity.getInputPortMapping().clear();
		activity.getOutputPortMapping().clear();
		for (InputPort ip : activity.getInputPorts()) {
			new CreateProcessorInputPortEdit(result, ip.getName(), ip
					.getDepth()).doEdit();
			activity.getInputPortMapping().put(ip.getName(), ip.getName());
		}
		for (OutputPort op : activity.getOutputPorts()) {
			new CreateProcessorOutputPortEdit(result, op.getName(), op
					.getDepth(), op.getGranularDepth()).doEdit();
			activity.getOutputPortMapping().put(op.getName(), op.getName());
		}
		DispatchStackImpl stack = result.dispatchStack;
		// Top level parallelize layer
		new AddDispatchLayerEdit(stack, new Parallelize(5), 0).doEdit();
		new AddDispatchLayerEdit(stack, new Failover(), 1).doEdit();
		new AddDispatchLayerEdit(stack, new Retry(3, 1000, 5000, (long) 1.1), 2)
				.doEdit();
		new AddDispatchLayerEdit(stack, new Invoke(), 3).doEdit();
		return result;
	}

	/**
	 * Get the &lt;java&gt; element from the XMLEncoder for the given bean as a
	 * JDOM Element
	 * 
	 * @param o
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Element beanAsElement(Object o) throws JDOMException,
			IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder xenc = new XMLEncoder(bos);
		xenc.writeObject(o);
		xenc.close();
		byte[] bytes = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		Element configElement = new SAXBuilder().build(bis).getRootElement();
		configElement.getParent().removeContent(configElement);
		return configElement;
	}

	/**
	 * Return the annotation element for a specified annotated entity
	 * 
	 * @param a
	 *            the workflow entity to serialize annotations for
	 * @return a JDOM Element object containing the annotations
	 */
	public static Element getAnnotationsElement(Annotated a) {
		Element result = new Element("annotations");
		for (WorkflowAnnotation annotation : a.getAnnotations()) {
			Element annotationElement = new Element("annotation");
			// If this was loaded by raven then store the artifact details
			if (annotation.getClass().getClassLoader() instanceof LocalArtifactClassLoader) {
				LocalArtifactClassLoader lacl = (LocalArtifactClassLoader) annotation
						.getClass().getClassLoader();
				annotationElement.addContent(ravenElement(lacl));
			}
			try {
				annotationElement.addContent(beanAsElement(annotation));
			} catch (JDOMException e) {
				// Auto-generated catch block but should never see this
				e.printStackTrace();
			} catch (IOException e) {
				// Auto-generated catch block but should never see this
				e.printStackTrace();
			}
			result.addContent(annotationElement);
		}
		return result;
	}

	/**
	 * If the annotations set for the annotated object is non-empty then this
	 * will insert the element produce by the getAnnotationElement method into
	 * the specified element. If the set is empty this does nothing - this is to
	 * prevent copy and paste code of the style 'if there are annotations
	 * add...'
	 * 
	 * @param e
	 * @param a
	 */
	public static void injectAnnotations(Element e, Annotated a) {
		if (a.getAnnotations().isEmpty() == false) {
			e.addContent(getAnnotationsElement(a));
		}
	}

	/**
	 * Similarly to the inject annotations method, this looks for a child
	 * 'annotations' element in the supplied element and, if present, uses it to
	 * annotate the supplied mutable annotated object. If the annotations
	 * element is not present this method does nothing - this allows us to make
	 * the annotations element optional in all places it can occur without
	 * having to write a whole pile of ugly cut and paste style code to detect
	 * these cases.
	 * 
	 * @param e
	 * @param a
	 */
	public static void populateAnnotationsFromParent(Element e,
			MutableAnnotated a) {
		Element annotationsElement = e.getChild("annotations");
		if (annotationsElement != null) {
			annotateObject(annotationsElement, a);
		}
	}

	/**
	 * Add the annotations contained in the specified &lt;annotations&gt;
	 * element to the specified instance of a MutableAnnotated object
	 */
	@SuppressWarnings("unchecked")
	public static void annotateObject(Element annotationsElement,
			MutableAnnotated annotateMe) {
		for (Element e : (List<Element>) annotationsElement
				.getChildren("annotation")) {
			ClassLoader cl = Tools.class.getClassLoader();
			Element ravenElement = e.getChild("raven");
			if (ravenElement != null) {
				try {
					cl = getRavenLoader(ravenElement);
				} catch (Exception ex) {
					System.out
							.println("Exception loading raven classloader for Service instance");
					ex.printStackTrace();
					// TODO - handle this properly, either by logging correctly
					// or
					// by going back to the repository and attempting to fetch
					// the
					// offending missing artifacts
				}
			}
			Object annotationBean = createBean(e.getChild("java"), cl);
			if (annotationBean instanceof WorkflowAnnotation) {
				WorkflowAnnotation newAnnotation = (WorkflowAnnotation) annotationBean;
				annotateMe.addAnnotation(newAnnotation);
			} else {
				System.out
						.println("Found non annotation bean inside an annotation element, something's not right here");
			}
		}

	}

	/**
	 * Build a Service instance from the specified &lt;service&gt; JDOM Element
	 * using reflection to assemble the configuration bean and configure the new
	 * Service object. If the &lt;service&gt; has a &lt;raven&gt; child element
	 * the metadata in that element will be used to locate an appropriate
	 * ArtifactClassLoader, if absent the ClassLoader used will be the one used
	 * to load this utility class.
	 * 
	 * @param e
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ActivityConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public static Activity buildActivity(Element e)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ActivityConfigurationException {
		Element ravenElement = e.getChild("raven");
		ClassLoader cl = Tools.class.getClassLoader();
		if (ravenElement != null) {
			try {
				cl = getRavenLoader(ravenElement);
			} catch (Exception ex) {
				System.out
						.println("Exception loading raven classloader for Service instance");
				ex.printStackTrace();
				// TODO - handle this properly, either by logging correctly or
				// by going back to the repository and attempting to fetch the
				// offending missing artifacts
			}
		}
		String className = e.getChild("class").getTextTrim();
		Class<? extends Activity> c = (Class<? extends Activity>) cl
				.loadClass(className);
		Activity<Object> activity = c.newInstance();

		Element ipElement = e.getChild("inputMap");
		for (Element mapElement : (List<Element>) (ipElement.getChildren("map"))) {
			String processorInputName = mapElement.getAttributeValue("from");
			String activityInputName = mapElement.getAttributeValue("to");
			activity.getInputPortMapping().put(processorInputName,
					activityInputName);
		}

		Element opElement = e.getChild("outputMap");
		for (Element mapElement : (List<Element>) (opElement.getChildren("map"))) {
			String activityOutputName = mapElement.getAttributeValue("from");
			String processorOutputName = mapElement.getAttributeValue("to");
			activity.getOutputPortMapping().put(activityOutputName,
					processorOutputName);
		}

		// Handle the configuration of the service
		Element configElement = e.getChild("java");
		Object configObject = createBean(configElement, cl);
		activity.configure(configObject);
		return activity;
	}

	/**
	 * Use the XMLDecoder to build an arbitrary java bean from the &lt;java&gt;
	 * JDOM Element object. Uses the supplied ClassLoader to accomodate systems
	 * such as Raven
	 * 
	 * @param e
	 * @param cl
	 * @return
	 */
	public static Object createBean(Element e, ClassLoader cl) {
		String configAsString = new XMLOutputter(Format.getRawFormat())
				.outputString(e);
		XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(
				configAsString.getBytes()), null, null, cl);
		Object configObject = decoder.readObject();
		return configObject;
	}

	/**
	 * Create the &lt;raven&gt; element for a given local artifact classloader
	 * 
	 * @param loader
	 * @return
	 */
	public static Element ravenElement(LocalArtifactClassLoader loader) {
		Element result = new Element("raven");
		Artifact a = loader.getArtifact();
		// Group
		Element groupIdElement = new Element("group");
		groupIdElement.setText(a.getGroupId());
		result.addContent(groupIdElement);
		// Artifact ID
		Element artifactIdElement = new Element("artifact");
		artifactIdElement.setText(a.getArtifactId());
		result.addContent(artifactIdElement);
		// Version
		Element versionElement = new Element("version");
		versionElement.setText(a.getVersion());
		result.addContent(versionElement);
		// Return assembled raven element
		return result;
	}

	/**
	 * Assuming this class is itself loaded by raven this method returns the
	 * classloader to be used to load classes from the artifact specified by the
	 * &lt;raven&gt; element passed in here.
	 * <p>
	 * If this class wasn't loaded by Raven then this ignores the element
	 * entirely and defaults to using the same classloader as this class (Tools)
	 * was loaded by. This is probably not what you want but it's a sensible
	 * enough fallback position
	 * 
	 * @param ravenElement
	 * @return
	 * @throws ArtifactNotFoundException
	 * @throws ArtifactStateException
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
		String groupId = ravenElement.getChildTextTrim("group");
		String artifactId = ravenElement.getChildTextTrim("artifact");
		String version = ravenElement.getChildTextTrim("version");
		Artifact a = new BasicArtifact(groupId, artifactId, version);
		return repository.getLoader(a, null);
	}

	/**
	 * Return a JDOM &lt;layer&gt; Element corresponding to the given
	 * DispatchLayer
	 */
	public static Element dispatchLayerAsXML(DispatchLayer<?> l)
			throws JDOMException, IOException {
		Element e = new Element("layer");

		ClassLoader cl = l.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			e.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element("class");
		classNameElement.setText(l.getClass().getName());
		e.addContent(classNameElement);

		// Get element for configuration
		Object o = l.getConfiguration();
		Element configElement = beanAsElement(o);
		e.addContent(configElement);
		return e;
	}

	/**
	 * Build a DispatchLayer object from the specified JDOM &lt;layer&gt;
	 * Element
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	public static DispatchLayer buildDispatchLayer(Element e)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		Element ravenElement = e.getChild("raven");
		ClassLoader cl = Tools.class.getClassLoader();
		if (ravenElement != null) {
			try {
				cl = getRavenLoader(ravenElement);
			} catch (Exception ex) {
				System.out
						.println("Exception loading raven classloader for Service instance");
				ex.printStackTrace();
				// TODO - handle this properly, either by logging correctly or
				// by going back to the repository and attempting to fetch the
				// offending missing artifacts
			}
		}
		String className = e.getChild("class").getTextTrim();
		Class<? extends DispatchLayer> c = (Class<? extends DispatchLayer>) cl
				.loadClass(className);
		DispatchLayer<Object> layer = c.newInstance();

		// Handle the configuration of the dispatch layer
		Element configElement = e.getChild("java");
		Object configObject = createBean(configElement, cl);
		layer.configure(configObject);

		return layer;
	}

	/**
	 * Return a JDOM &lt;service&gt; Element corresponding to the given Service
	 * implementation. Relies on the XMLEncoder based serialization of the
	 * configuration bean to store config data.
	 * 
	 * @param a
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Element activityAsXML(Activity<?> a) throws JDOMException,
			IOException {
		Element e = new Element("service");

		ClassLoader cl = a.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			e.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element("class");
		classNameElement.setText(a.getClass().getName());
		e.addContent(classNameElement);

		// Write out the mappings (processor input -> service input, service
		// output -> processor output)
		Element ipElement = new Element("inputMap");
		for (String processorInputName : a.getInputPortMapping().keySet()) {
			Element mapElement = new Element("map");
			mapElement.setAttribute("from", processorInputName);
			mapElement.setAttribute("to", a.getInputPortMapping().get(
					processorInputName));
			ipElement.addContent(mapElement);
		}
		e.addContent(ipElement);

		Element opElement = new Element("outputMap");
		for (String activityOutputName : a.getOutputPortMapping().keySet()) {
			Element mapElement = new Element("map");
			mapElement.setAttribute("from", activityOutputName);
			mapElement.setAttribute("to", a.getOutputPortMapping().get(
					activityOutputName));
			opElement.addContent(mapElement);
		}
		e.addContent(opElement);

		// Get element for configuration
		Object o = a.getConfiguration();
		Element configElement = beanAsElement(o);
		e.addContent(configElement);

		return e;

	}

}
