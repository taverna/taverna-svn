package net.sf.taverna.t2.myexperiment.translator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.cyclone.WorkflowModelTranslator;
import net.sf.taverna.t2.cyclone.WorkflowTranslationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;

import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class RSSFeedWorkflowTranslator {

	private Map<String, String> workflowList;
	private String name;
	private String password;
	private String repository;

	public void init(String name, String password, String repository) {

		this.name = name;
		this.password = password;
		this.repository = repository;
		workflowList = new HashMap<String, String>();
		Repository myRepository = LocalRepository.getRepository(new File(
				this.repository));
		TavernaSPIRegistry.setRepository(myRepository);
	}

	private void getRSS() throws MalformedURLException {
		URL feedUrl = new URL("http://www.myexperiment.org/workflows.rss");

		Document doc = getXMLDocument(feedUrl);
		NodeList nodes = doc.getElementsByTagName("item");
		for (int i = 0; i < nodes.getLength(); i++) {
			nodes.item(i).getTextContent();
			nodes.item(i).getFirstChild().getNodeName();
			NodeList childNodes = nodes.item(i).getChildNodes();
			String version = null;
			String link = null;
			for (int j = 0; j < childNodes.getLength(); j++) {
				if (childNodes.item(j).getNodeName()
						.equalsIgnoreCase("version")) {
					version = childNodes.item(j).getTextContent();
				}
				if (childNodes.item(j).getNodeName().equalsIgnoreCase("link")) {
					link = childNodes.item(j).getTextContent();
				}
			}

			workflowList.put(link, version);

		}

	}

	private Document getXMLDocument(URL feedUrl) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		try {
			URLConnection URLconnection = feedUrl.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;

			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				System.out.println("OK");
				InputStream in = httpConnection.getInputStream();

				try {
					doc = db.parse(in);
				} catch (org.xml.sax.SAXException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("HTTP connection response OK ");

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}

	private void translateRSS() {
		boolean error = false;
		ScuflModel model = new ScuflModel();
		Set<Entry<String, String>> entrySet = workflowList.entrySet();
		Iterator<Entry<String, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<String, String> next = iterator.next();
			String url = null;
			url = next.getKey() + ";download?version=" + next.getValue();

			HttpURLConnection connection;
			try {
				System.out.println("Populating scufl model for "
						+ url.toString());
				connection = (HttpURLConnection) new URL(url).openConnection();
				connection.setRequestProperty("Accept", "text/xml");

				int code = connection.getResponseCode();
				URL url2;
				if (code == 401) {
					// authentication required
					System.out.println("Password needed");
					connection = (HttpURLConnection) new URL(url)
							.openConnection();
					String userPassword = this.name + ":" + this.password;

					String encoding = new sun.misc.BASE64Encoder()
							.encode(userPassword.getBytes());
					connection.setRequestProperty("Authorization", "Basic "
							+ encoding);
					connection.setRequestProperty("Accept", "text/xml");

				}
				XScuflParser.populate(connection.getInputStream(), model, null);
				System.out.println("Now translating");
			} catch (UnknownProcessorException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (UnknownPortException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (ProcessorCreationException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (DataConstraintCreationException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (DuplicateProcessorNameException e) {
				// TODO Auto-generated catch block
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (MalformedNameException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (ConcurrencyConstraintCreationException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (DuplicateConcurrencyConstraintNameException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (XScuflFormatException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			} catch (IOException e) {
				System.out.println("Cannot read " + e.getMessage());
				error = true;
			}
			try {
				if (!error) {
					Dataflow dataflow = WorkflowModelTranslator
							.doTranslation(model);
					System.out
							.println("******Translation success****** See below for ports");
					List<? extends DataflowInputPort> inputPorts = dataflow
							.getInputPorts();
					List<? extends DataflowOutputPort> outputPorts = dataflow
							.getOutputPorts();
					for (DataflowInputPort port : inputPorts) {
						System.out.println("input port: " + port.getName());
					}
					for (DataflowOutputPort port : outputPorts) {
						System.out.println("output port: " + port.getName());
					}

				} else {
					error = false;
				}
			} catch (WorkflowTranslationException e) {
				System.out.println("This workflow cannot be translated: "
						+ model.getDescription().getTitle() +  " " + e.getMessage());
			}
		}
	}

	public static void main(String[] args) throws MalformedURLException {
		RSSFeedWorkflowTranslator trans = new RSSFeedWorkflowTranslator();

		if (args.length<3) {
			System.out.println("Please start with arguments myExpPassword myExpName repositorylocation");
			System.exit(1);
		}
		String password = args[0];
		String name= args[1];
		String repository = args[2];
		trans.init(name, password, repository);
		trans.getRSS();
		trans.translateRSS();

	}

}
