package net.sf.taverna.t2.activities.localworker.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.serialization.xml.ActivityXMLDeserializer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Creates all the {@link LocalworkerActivityItem}s by de-serializing them from
 * a {@link BeanshellActivity} stored on disk, creating a new
 * {@link LocalworkerActivityItem} and populating their
 * {@link BeanshellActivityConfigurationBean} with the appropriate
 * {@link ActivityInputPortDefinitionBean},
 * {@link ActivityOutputPortDefinitionBean} and depths for each port
 * 
 * @author Ian Dunlop
 * 
 */
public class LocalworkerQuery extends ActivityQuery {

	private static Logger logger = Logger.getLogger(Logger.class);

	/** Used to deserialize the Activities stored on disk */
	private ActivityXMLDeserializer deserializer;

	public LocalworkerQuery(String property) {
		super(property);
		deserializer = ActivityXMLDeserializer.getInstance();
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
	 * workers in to the correct place
	 */
	@Override
	public void doQuery() {

		InputStream inputStream = getClass().getResourceAsStream(
				"/localworker_names");
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = "";
		String category = null;
		try {
			while ((line = inputReader.readLine()) != null) {
				if (line.startsWith("category")) {
					String[] split = line.split(":");
					category = split[1];
				} else {
					LocalworkerActivityItem createItem;
					try {
						createItem = createItem(line);
					} catch (ItemCreationException e) {
						logger.warn("Could not create item for: " + line, e);
						continue;
					}
					createItem.setCategory("localworker_" + category);
					add(createItem);
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private class ItemCreationException extends Exception {

		public ItemCreationException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public ItemCreationException(String message, Throwable cause) {
			super(message, cause);
			// TODO Auto-generated constructor stub
		}

		public ItemCreationException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}

		public ItemCreationException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * Loads the deserialized local worker from disk and creates a
	 * {@link LocalworkerActivityItem} with the correct ports and script from it
	 * 
	 * @param line
	 * @return a LocalWorker with the appropriate Input/Output ports and script
	 * @throws ItemCreationException
	 */
	private LocalworkerActivityItem createItem(String line)
			throws ItemCreationException {
		String[] split = line.split("[.]");
		// get the file from disk
		InputStream resourceAsStream = getClass().getResourceAsStream(
				"/" + line);

		SAXBuilder builder = new SAXBuilder();
		Element detachRootElement = null;
		try {
			detachRootElement = builder.build(resourceAsStream)
					.detachRootElement();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Activity<?> activity = null;
		try {
			activity = deserializer.deserializeActivity(detachRootElement,
					new HashMap<String, Element>());
		} catch (Exception e) {
			throw new ItemCreationException(e);
		}
		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		List<ActivityInputPortDefinitionBean> inputPortBeans = new ArrayList<ActivityInputPortDefinitionBean>();
		for (ActivityInputPort port : inputPorts) {
			ActivityInputPortDefinitionBean bean = new ActivityInputPortDefinitionBean();
			bean.setDepth(port.getDepth());
			bean.setHandledReferenceSchemes(port.getHandledReferenceSchemes());
			// FIXME bean.setMimeTypes(port.get) needs mime types from
			// somewhere??
			bean.setName(port.getName());
			bean.setTranslatedElementType(port.getTranslatedElementClass());
			inputPortBeans.add(bean);
		}
		Set<OutputPort> outputPorts = activity.getOutputPorts();
		List<ActivityOutputPortDefinitionBean> outputPortBeans = new ArrayList<ActivityOutputPortDefinitionBean>();
		for (OutputPort port : outputPorts) {
			ActivityOutputPortDefinitionBean bean = new ActivityOutputPortDefinitionBean();
			bean.setDepth(port.getDepth());
			bean.setGranularDepth(port.getGranularDepth());
			bean.setName(port.getName());
			// FIXME bean.setMimeTypes(port.) mime types needed from
			// annotations
		}

		String script = ((BeanshellActivity) activity).getConfiguration()
				.getScript();

		LocalworkerActivityItem item = new LocalworkerActivityItem();
		item.setScript(script);
		item.setOutputPorts(outputPortBeans);
		item.setInputPorts(inputPortBeans);
		// name is last part of the class name that was split
		item.setOperation(split[split.length - 1]);
		return item;

	}

}
