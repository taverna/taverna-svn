package net.sf.taverna.service.executeremotely;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.service.rest.client.RESTContext;
import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ExecuteRemotelyConf {

	private static Logger logger = Logger.getLogger(ExecuteRemotelyConf.class);

	private static final String CONFDIR = "conf";

	private static final String CONFFILE =
		"net.sf.taverna.service.executeremotely.xml";

	private File confFile = findConfFile();

	private List<RESTContext> services = new ArrayList<RESTContext>();

	private static ExecuteRemotelyConf instance;

	/**
	 * Retrieve the singleton
	 * 
	 * @return
	 */
	public static synchronized ExecuteRemotelyConf getInstance() {
		if (instance == null) {
			instance = new ExecuteRemotelyConf();
		}
		return instance;
	}

	/**
	 * Use singleton method {@link #getInstance()} instead.
	 */
	private ExecuteRemotelyConf() {
		load(confFile);
		// TODO: Also load from taverna.home ?
	}

	private File findConfFile() {
		File confDir = MyGridConfiguration.getUserDir(CONFDIR);
		return new File(confDir, CONFFILE);
	}

	/**
	 * Load the list of services from an XML file.
	 */
	@SuppressWarnings("unchecked")
	private void load(File file) {
		if (!file.isFile()) {
			logger.info("Ignoring non-existing " + file);
			return;
		}
		Document document;
		try {
			document = new SAXBuilder().build(file);
		} catch (JDOMException e) {
			logger.warn("Could not parse " + file, e);
			return;
		} catch (IOException e) {
			logger.warn("Could not read " + file, e);
			return;
		}
		Element root = document.getRootElement();
		Element servicesElem = root.getChild("services");
		for (Element service : (List<Element>) servicesElem.getChildren()) {
			RESTContext context;
			try {
				context = RESTContext.fromXML(service);
			} catch (NullPointerException ex) {
				logger.warn("Invalid service element: " + service);
				continue;
			}
			services.add(context);
		}
		logger.debug("Loaded services from " + file);
	}

	/**
	 * Save the list of services to an XML file.
	 */
	private void save() {
		Element execElem = new Element("executeremotely");
		Element servicesElem = new Element("services");
		execElem.addContent(servicesElem);
		for (RESTContext context : services) {
			servicesElem.addContent(context.toXML());
		}
		Document doc = new Document(execElem);
		try {
			Writer writer = new FileWriter(confFile);
			new XMLOutputter(Format.getPrettyFormat()).output(doc, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.warn("Could not write configuration to " + confFile, e);
		}
		logger.debug("Saved services to " + confFile);
	}

	/**
	 * Add or replace service. A service is identified by it's URI and username.
	 */
	public void addService(RESTContext service) {
		if (services.contains(service)) {
			// Replace it
			services.remove(service);
		}
		services.add(service);
		save();
		logger.info("Added service " + service);
	}

	/**
	 * Remove a service.
	 * 
	 * @param service
	 *            Service to remove
	 */
	public void removeService(RESTContext service) {
		services.remove(service);
		save();
		logger.info("Removed service " + service);
	}

	public RESTContext[] getServices() {
		return services.toArray(new RESTContext[0]);
	}

}
