package net.sf.taverna.t2.webdav.ui.serviceprovider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivityConfigurationBean;
import net.sf.taverna.t2.activities.localworker.servicedescriptions.LocalworkerActivityIcon;
import net.sf.taverna.t2.activities.localworker.servicedescriptions.LocalworkerServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.serialization.xml.ActivityXMLDeserializer;

public class WebDAVServiceProvider implements ServiceDescriptionProvider {
	
	private static final String LOCALWORKER_NAMES = "/webdav_worker_names";
	
	private static final String LOCALWORKER_SERVICE = "WebDAV local services";

	private static Logger logger = Logger.getLogger(Logger.class);

	private static final URI providerId = URI
	.create("http://taverna.sf.net/2010/service-provider/webdav");
	
	/** Used to deserialize the Activities stored on disk */
	private ActivityXMLDeserializer deserializer = ActivityXMLDeserializer.getInstance();;

	private static Map<String, String> localWorkerToScript = new HashMap<String, String>();

	static {
		localWorkerToScript.put(
				"net.sf.taverna.t2.webdav.sardine.List",
				"List");
		localWorkerToScript.put(
				"net.sf.taverna.t2.webdav.sardine.CreateDirectory",
				"CreateDirectory");
		localWorkerToScript.put(
				"net.sf.taverna.t2.webdav.sardine.Move",
				"Move");
		localWorkerToScript.put(
				"net.sf.taverna.t2.webdav.sardine.Copy",
				"Copy");
		localWorkerToScript.put(
				"net.sf.taverna.t2.webdav.sardine.Exists",
				"Exists");
		localWorkerToScript.put(
				"net.sf.taverna.t2.webdav.sardine.Lock",
				"Lock");
		localWorkerToScript.put(
				"net.sf.taverna.t2.webdav.sardine.Unlock",
				"Unlock");
	}

	public String getName() {
		return LOCALWORKER_SERVICE;
	}

	/**
	 * Use the
	 * {@link net.sf.taverna.t2.activities.localworker.translator.LocalworkerTranslator}
	 * to get a {@link Map} of all the local workers. Use the keys in this map
	 * to load all the serialized activities from disk by using
	 * <code> getClass().getResourceAsStream("/" + className) </code> to get
	 * them and then the {@link ActivityXMLDeserializer} to get the actual
	 * {@link Activity}. Create the {@link LocalworkerActivityItem} by
	 * populating them with the correct ports and depths. Sets the category to
	 * match the T1 version so that a query by category will split the local
	 * workers in to the correct place.
	 */
	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {

		List<ServiceDescription> items = new ArrayList <ServiceDescription>();
		
		InputStream inputStream = getClass().getResourceAsStream(
				LOCALWORKER_NAMES);
		if (inputStream == null) {
			logger.error("Could not find resource " + LOCALWORKER_NAMES);
			return;
		}
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = "";
		String category = null;
		try {
			while ((line = inputReader.readLine()) != null) {
				if (!line.startsWith("#")) {
				if (line.startsWith("category")) {
					String[] split = line.split(":");
					category = split[1];
				} else {
					LocalworkerServiceDescription createItem;
					try {
						createItem = createItem(line);
					} catch (ItemCreationException e) {
						logger.warn("Could not create item for: " + line, e);
						continue;
					}
					createItem.setCategory(category);
					createItem.setProvider("myGrid");
					items.add(createItem);
				}
				}
			}
		} catch (IOException e1) {
			logger.warn("Could not read local worker definitions from "
					+ LOCALWORKER_NAMES);
		}
		callBack.partialResults(items);
		callBack.finished();

	}

	@SuppressWarnings("serial")
	private class ItemCreationException extends Exception {

		public ItemCreationException() {
			super();
		}

		public ItemCreationException(String message, Throwable cause) {
			super(message, cause);
		}

		public ItemCreationException(String message) {
			super(message);
		}

		public ItemCreationException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * Loads the deserialised local worker from disk and creates a
	 * {@link LocalworkerActivityItem} with the correct ports and script from it
	 * 
	 * @param line
	 * @return a LocalWorker with the appropriate Input/Output ports and script
	 * @throws ItemCreationException
	 */
	private LocalworkerServiceDescription createItem(String line)
			throws ItemCreationException {
		//String[] split = line.split("[.]");
		// get the file from disk
		String resource = "/" + line;
		InputStream resourceAsStream = getClass().getResourceAsStream(resource);
		if (resourceAsStream == null) {
			throw new ItemCreationException("Could not find resource "
					+ resource);
		}

		SAXBuilder builder = new SAXBuilder();
		Element detachRootElement = null;
		try {
			detachRootElement = builder.build(resourceAsStream)
					.detachRootElement();
		} catch (JDOMException e) {
			throw new ItemCreationException("Could not parse resource "
					+ resource, e);
		} catch (IOException e) {
			throw new ItemCreationException("Could not read resource "
					+ resource, e);
		}
		LocalworkerActivity activity = null;
		try {
			activity = (LocalworkerActivity) deserializer
					.deserializeActivity(detachRootElement,
							new HashMap<String, Element>(), LocalworkerActivity.class
									.getClassLoader());
		} catch (Exception e) {
			logger.error("Could not create LocalWorkerServiceDescription", e);
			throw new ItemCreationException(e);
		}
		List<ActivityInputPortDefinitionBean> inputPortBeans = new ArrayList<ActivityInputPortDefinitionBean>();
		LocalworkerActivityConfigurationBean configuration = (LocalworkerActivityConfigurationBean) activity
				.getConfiguration();
		
		// Translate the old dependencies field into artifactDependencies field
		// The local worker definition xml files still have the old dependencies field
		LinkedHashSet<BasicArtifact> artifactDependencies = new LinkedHashSet<BasicArtifact>();
		for (String dep : configuration.getDependencies()){
			String[] artifactParts = dep.split(":");
			if (artifactParts.length == 3) {
				artifactDependencies.add(new BasicArtifact(artifactParts[0], artifactParts[1],
						artifactParts[2]));
			}
		}
		configuration.setArtifactDependencies(artifactDependencies);

		for (ActivityInputPortDefinitionBean bean : configuration
				.getInputPortDefinitions()) {
			bean.setDepth(bean.getDepth());
			bean.setName(bean.getName());
			bean.setHandledReferenceSchemes(bean.getHandledReferenceSchemes());
			bean.setTranslatedElementType(bean.getTranslatedElementType());
			// bean.setMimeTypes(bean.getMimeTypes());
			inputPortBeans.add(bean);
		}
		List<ActivityOutputPortDefinitionBean> outputPortBeans = new ArrayList<ActivityOutputPortDefinitionBean>();
		for (ActivityOutputPortDefinitionBean bean : configuration
				.getOutputPortDefinitions()) {
			bean.setDepth(bean.getDepth());
			bean.setGranularDepth(bean.getGranularDepth());
			bean.setName(bean.getName());
			bean.setMimeTypes(bean.getMimeTypes());
			outputPortBeans.add(bean);
		}

		String script = ((BeanshellActivity) activity).getConfiguration()
				.getScript();

		LocalworkerServiceDescription item = new LocalworkerServiceDescription();
		item.setScript(script);
		item.setOutputPorts(outputPortBeans);
		item.setInputPorts(inputPortBeans);
		item.setLocalworkerName(line);
		// name is last part of the class name that was split
		//String operation = split[split.length - 1];
		String operationName = localWorkerToScript.get(line);
		item.setOperation(operationName);
		item.setDependencies(((BeanshellActivity) activity).getConfiguration()
				.getDependencies()); // this property is not in use any more
		item.setArtifactDependencies(configuration.getArtifactDependencies());
//		item.setOperation(operation);
		return item;

	}

	public Icon getIcon() {
		return LocalworkerActivityIcon.getLocalworkerIcon();
	}
	
	@Override
	public String toString() {
		return "WebDAV local services provider";
	}
	
	public static String getServiceNameFromClassname(String classname) {
		return (localWorkerToScript.get(classname));
	}

	public String getId() {
		return providerId.toString();
	}

}
