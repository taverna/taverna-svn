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
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.ActivityXMLDeserializer;

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

	/** Used to deserialize the Activities stored on disk */
	private ActivityXMLDeserializer deserializer;

	public LocalworkerQuery(String property) {
		super(property);
		deserializer = ActivityXMLDeserializer.getInstance();
	}

	/**
	 * Use the {@link LocalworkerTranslator} to get a {@link Map} of all the
	 * local workers. Use the keys in this map to load all the serialized
	 * activities from disk by using
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
					LocalworkerActivityItem createItem = createItem(line);
					createItem.setCategory("localworker_" + category);
					add(createItem);
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * Loads the deserialized local worker from disk and creates a
	 * {@link LocalworkerActivityItem} with the correct ports and script from it
	 * 
	 * @param line
	 * @return a LocalWorker with the appropriate Input/Output ports and script
	 */
	private LocalworkerActivityItem createItem(String line) {
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
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeserializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
