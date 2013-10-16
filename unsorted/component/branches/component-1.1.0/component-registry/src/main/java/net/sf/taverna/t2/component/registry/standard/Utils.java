package net.sf.taverna.t2.component.registry.standard;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBElement;

import uk.org.taverna.component.api.Description;
import net.sf.taverna.t2.annotation.annotationbeans.AbstractTextualValueAssertion;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

class Utils {
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();
	private static final AnnotationTools annotationTools = new AnnotationTools();
	private static final T2DataflowOpener opener = new T2DataflowOpener();

	public static String serializeDataflow(Dataflow dataflow)
			throws RegistryException {
		try {
			// Ugly, but how to do it...
			ByteArrayOutputStream dataflowStream = new ByteArrayOutputStream();
			FileManager.getInstance().saveDataflowSilently(dataflow,
					new T2FlowFileType(), dataflowStream, false);
			return dataflowStream.toString("UTF-8");
		} catch (Exception e) {
			throw new RegistryException(
					"failed to serialize component implementation", e);
		}
	}

	public static Dataflow getDataflowFromUri(String uri) throws OpenException,
			MalformedURLException {
		return opener.openDataflow(T2_FLOW_FILE_TYPE, new URL(uri))
				.getDataflow();
	}

	public static String getAnnotation(Dataflow dataflow,
			Class<? extends AbstractTextualValueAssertion> annotation,
			String defaultValue) {
		return annotationTools.getAnnotationString(dataflow, annotation,
				defaultValue);
	}

	public static JAXBElement<?> getElement(Description d, String name)
			throws RegistryException {
		for (Object o : d.getContent())
			if (o instanceof JAXBElement) {
				JAXBElement<?> el = (JAXBElement<?>) o;
				if (el.getName().getLocalPart().equals(name))
					return el;
			}
		throw new RegistryException("no " + name + " element");
	}

	public static String getElementString(Description d, String name)
			throws RegistryException {
		return getElement(d, name).getValue().toString().trim();
	}

	public static String getValue(Description d) {
		StringBuilder sb = new StringBuilder();
		for (Object o : d.getContent())
			if (!(o instanceof JAXBElement))
				sb.append(o);
		return sb.toString();
	}

}
