package net.sf.taverna.t2.provenance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom.output.XMLOutputter;

/**
 * Generate a web page from some provenance.
 * 
 * @author Ian Dunlop
 * 
 */
public class HtmlProvenanceConnector implements ProvenanceConnector {

	private String dataflowName = null;

	private ArrayList<ProvenanceItem> provenanceCollection;

	private String html;

	private String location;

	private DataFacade dataFacade;

	/**
	 * Create a provenance connector and tell it where you want any html to be
	 * stored
	 * 
	 * @param location -
	 *            where the root for the page should be . Will store resolved
	 *            entities in root/data
	 */
	public HtmlProvenanceConnector(String location) {
		this.location = location;
		provenanceCollection = new ArrayList<ProvenanceItem>();
		html = "<html><body>";
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		return provenanceCollection;
	}

	public void store(DataFacade dataFacade) {
		this.dataFacade = dataFacade;
		int size = provenanceCollection.size();
		if (size > 0) {
			// retrieve last event stored and send to the service for storage
			ProvenanceItem provItem = provenanceCollection.get(size - 1);
			addHtml(provItem);
			String asString = provItem.getAsString();
			// get type of provItem and send this info as well
			// figure out the type of event and strip out the correct items for
			// the html
			if (asString != null) {
				// html = html + asString + "\n";
				// System.out.println(asString);
				if (asString.equalsIgnoreCase("<DataflowRunComplete/>")) {
					writeHtml();
				}
			} else {
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(provItem
						.getAsXML(dataFacade));
				// System.out.println(outputString);
				// html = html + outputString + "\n";
				if (outputString.equalsIgnoreCase("<DataflowRunComplete/>")) {
					writeHtml();
				}
			}
		}
	}

	/**
	 * Figure out the type of {@link ProvenanceItem} and format the html
	 * accordingly
	 * 
	 * @param provItem
	 */
	private void addHtml(ProvenanceItem provItem) {
		// TODO Auto-generated method stub
		if (provItem instanceof ProcessProvenanceItem) {
			if (dataflowName == null) {
				dataflowName = ((ProcessProvenanceItem) provItem)
						.getDataflowID();
			}
			ProcessorProvenanceItem processorProvenanceItem = ((ProcessProvenanceItem) provItem)
					.getProcessorProvenanceItem();
			ActivityProvenanceItem activityProvenanceItem = processorProvenanceItem
					.getActivityProvenanceItem();
			IterationProvenanceItem iterationProvenanceItem = activityProvenanceItem
					.getIterationProvenanceItem();
			InputDataProvenanceItem inputDataItem = iterationProvenanceItem
					.getInputDataItem();
			OutputDataProvenanceItem outputDataItem = iterationProvenanceItem
					.getOutputDataItem();
			int[] iteration = iterationProvenanceItem.getIteration();

			String inputTable = "<table border=1><tr><th>Inputs</th><th>Identifier</th></tr>";
			Map<String, EntityIdentifier> inputDataMap = inputDataItem
					.getDataMap();
			for (String string : inputDataMap.keySet()) {
				EntityIdentifier entityIdentifier = inputDataMap.get(string);
				inputTable = inputTable + "<tr><td>" + string + "</td><td>"
						+ resolve(entityIdentifier) + "</td><td></td></tr>";

			}
			inputTable = inputTable + "</table>";

			String outputTable = "<table border=1><tr><th>Outputs</th><th>Identifier</th></tr>";
			Map<String, EntityIdentifier> outputDataMap = inputDataItem
					.getDataMap();
			for (String string : outputDataMap.keySet()) {
				EntityIdentifier entityIdentifier = outputDataMap.get(string);
				outputTable = outputTable + "<tr><td>" + string + "</td><td>"
						+ resolve(entityIdentifier) + "</td><td></td></tr>";

			}
			outputTable = outputTable + "</table>";
			html = html + "Processor: "
					+ processorProvenanceItem.getProcessorID() + " Iteration: "
					+ iteration.length + "<br>" + inputTable + "<br>"
					+ outputTable + "<br><br>";

			System.out.println("ProcessProvenanceItem");
		}

	}

	private String resolve(EntityIdentifier entityIdentifier) {

		String resolvedString = "unresolved: " + entityIdentifier;
		Object resolve = null;
		try {
			resolve = dataFacade.resolve(entityIdentifier);
		} catch (RetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (resolve instanceof EntityList) {
			System.out.println("Entity List");
		} else if (resolve instanceof InputStream) {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			try {
				IOUtils.copy((InputStream) resolve, byteStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] byteArray = byteStream.toByteArray();
			try {
				UUID uuid = UUID.randomUUID();
				MagicMatch magicMatch = Magic.getMagicMatch(byteArray);
				System.out.println("It's a input stream");
				System.out.println(magicMatch.getMimeType());

				String string = new String(byteArray);
				File file = new File(location, "workflows/data/");

				File file2 = new File(file, uuid + ".txt");
				FileUtils.touch(file2);
				FileOutputStream fileOutputStream = new FileOutputStream(file2);
				IOUtils.write(byteArray, fileOutputStream);
				System.out.println(string);
				resolvedString = "<A HREF=\"data/" + uuid + ".txt\">"
						+ entityIdentifier + "</A>";
				System.out.println(resolvedString);
			} catch (MagicParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MagicMatchNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MagicException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (resolve instanceof String) {
			System.out.println("It's a string " + resolve);
		} else if (resolve instanceof Byte[]) {
			System.out.println("Literal");
		} else if (resolve instanceof Literal) {

		}

		return resolvedString;
	}

	/**
	 * Get the location for the web server and write the html out to that place.
	 */
	private void writeHtml() {
		// TODO Get the name of the workflow and the unique identifier and write
		// the files to that directory
		// ie workflowname/uuid - hack at the moment just uses the strange
		// dataflow name assigned during provenance run
		html = html + "</body></html>";

		File workflowName = new File(location + "/workflows", dataflowName
				+ ".html");
		FileOutputStream output;
		try {
			FileUtils.touch(workflowName);
			output = new FileOutputStream(workflowName);
			IOUtils.write(html, output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
