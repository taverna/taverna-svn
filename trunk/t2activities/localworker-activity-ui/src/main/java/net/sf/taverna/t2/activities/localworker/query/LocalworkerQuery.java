package net.sf.taverna.t2.activities.localworker.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
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
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortDefinitionBean;
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

	private static final String LOCALWORKER_NAMES = "/localworker_names";

	private static Logger logger = Logger.getLogger(Logger.class);

	/** Used to deserialize the Activities stored on disk */
	private ActivityXMLDeserializer deserializer;

	private static Map<String, String> localWorkerToScript = new HashMap<String, String>();

	static {
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.ByteArrayToString",
				"Byte Array To String");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.DecodeBase64",
				"Decode Base 64 to byte Array");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.EchoList", "Echo List");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.EmitLotsOfStrings",
				"Create Lots Of Strings");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.EncodeBase64",
				"Encode Byte Array to Base 64");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.ExtractImageLinks",
				"Get image URLs from HTTP document");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.FailIfFalse",
				"Fail If False");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.FailIfTrue",
				"Fail If True");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.FilterStringList",
				"Filter List of Strings by regex");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.FlattenList",
				"Flatten List");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.PadNumber",
				"Pad numeral with leading 0s");
		localWorkerToScript
				.put(
						"org.embl.ebi.escience.scuflworkers.java.RegularExpressionStringList",
						"Filter list of strings extracting match to a regex");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.SendEmail",
				"Send an Email");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.SliceList",
				"Extract Elements from a List");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.SplitByRegex",
				"Split string into string list by regular expression");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.StringConcat",
				"Concatenate two strings");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.StringListMerge",
				"Merge String List to a String");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.StringSetDifference",
				"String List Difference");
		localWorkerToScript
				.put(
						"org.embl.ebi.escience.scuflworkers.java.StringSetIntersection",
						"String List Intersection");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.StringSetUnion",
				"String List Union");
		localWorkerToScript
				.put(
						"org.embl.ebi.escience.scuflworkers.java.StringStripDuplicates",
						"Remove String Duplicates");
		localWorkerToScript
				.put(
						"org.embl.ebi.escience.scuflworkers.java.TestAlwaysFailingProcessor",
						"Test - Always Fails");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.WebImageFetcher",
				"Get Image From URL");
		localWorkerToScript.put(
				"org.embl.ebi.escience.scuflworkers.java.WebPageFetcher",
				"Get Web Page from URL");

		// xml:XPathText
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.xml.XPathTextWorker",
				"XPath From Text");

		// biojava
		localWorkerToScript
				.put(
						"net.sourceforge.taverna.scuflworkers.biojava.GenBankParserWorker",
						"Read Gen Bank File");
		localWorkerToScript
				.put(
						"net.sourceforge.taverna.scuflworkers.biojava.ReverseCompWorker",
						"Reverse Complement DNA");
		localWorkerToScript
				.put(
						"net.sourceforge.taverna.scuflworkers.biojava.SwissProtParserWorker",
						"Read Swiss Prot File");
		localWorkerToScript
				.put(
						"net.sourceforge.taverna.scuflworkers.biojava.TranscribeWorker",
						"Transcribe DNA");

		// io
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.io.TextFileReader",
				"Read Text File");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.io.TextFileWriter",
				"Write Text File");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.io.LocalCommand",
				"Execute Command Line App");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.io.FileListByExtTask",
				"List Files by Extension");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.io.FileListByRegexTask",
				"List Files By Regex");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.io.DataRangeTask",
				"Select Data Range From File");
		localWorkerToScript
				.put(
						"net.sourceforge.taverna.scuflworkers.io.ConcatenateFileListWorker",
						"Concatenate Files");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.io.EnvVariableWorker",
				"Get Environment Variables as XML");

		// ui
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.ui.AskWorker",
				"Ask");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.ui.SelectWorker",
				"Select");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.ui.ChooseWorker",
				"Choose");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.ui.TellWorker",
				"Tell");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.ui.WarnWorker",
				"Warn");
		localWorkerToScript.put(
				"net.sourceforge.taverna.scuflworkers.ui.SelectFileWorker",
				"Select File");
	}

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
					createItem.setCategory(category);
					createItem.setProvider("myGrid");
					add(createItem);
				}
			}
		} catch (IOException e1) {
			logger.warn("Could not read local worker definitions from "
					+ LOCALWORKER_NAMES);
		}

	}

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
	private LocalworkerActivityItem createItem(String line)
			throws ItemCreationException {
		String[] split = line.split("[.]");
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
		Activity<?> activity = null;
		try {
			activity = deserializer
					.deserializeActivity(detachRootElement,
							new HashMap<String, Element>(), getClass()
									.getClassLoader());
		} catch (Exception e) {
			throw new ItemCreationException(e);
		}
		List<ActivityInputPortDefinitionBean> inputPortBeans = new ArrayList<ActivityInputPortDefinitionBean>();
		BeanshellActivityConfigurationBean configuration = (BeanshellActivityConfigurationBean) activity
				.getConfiguration();

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

		LocalworkerActivityItem item = new LocalworkerActivityItem();
		item.setScript(script);
		item.setOutputPorts(outputPortBeans);
		item.setInputPorts(inputPortBeans);
		// name is last part of the class name that was split
		String operation = split[split.length - 1];
		String operationName = localWorkerToScript.get(line);
		item.setOperation(operationName);
//		item.setOperation(operation);
		return item;

	}

}
