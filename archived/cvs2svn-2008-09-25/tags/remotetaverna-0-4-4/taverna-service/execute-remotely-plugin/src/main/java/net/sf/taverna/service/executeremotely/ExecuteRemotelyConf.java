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

/**
 * Configuration for execute remotely panel. Saved in {@value #CONFFILE} inside
 * the configuration directory of the Taverna home directory.
 * <p>
 * What is stored is the list of services with their URL, username and password.
 * 
 * @author Stian Soiland
 * 
 */
public class ExecuteRemotelyConf {

	private static Logger logger = Logger.getLogger(ExecuteRemotelyConf.class);

	private static final String CONFDIR = "conf";

	private static final String CONFFILE =
		"net.sf.taverna.service.executeremotely.xml";

	private final File confFile = findConfFile();

	private final List<RESTContext> services = new ArrayList<RESTContext>();
	
	private RESTContext selected;

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
			if ("selected".equals(service.getAttribute("selected"))) {
				selected = context;
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
			Element elem = context.toXML();
			/* Don't use getSelected() as it would pick the first one */
			if (context.equals(selected)) {
				elem.setAttribute("selected", "selected");
			}
			servicesElem.addContent(elem);
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
		if (service == null) { 
			throw new NullPointerException("Service can't be null");
		}
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
		if (getSelected().equals(service)) {
			setSelected(service);
		}
		save();
		logger.info("Removed service " + service);
	}

	/**
	 * Get a copy of the list of services
	 * 
	 * @return An RESTContext[] array of the services
	 */
	public RESTContext[] getServices() {
		return services.toArray(new RESTContext[0]);
	}

	/**
	 * Get the currently active service, or the first available service. If no
	 * services are available, <code>null</code> is returned.
	 * 
	 * @return The currently selected service.
	 */
	public RESTContext getSelected() {
		if (selected == null && !services.isEmpty()) {
			// Just choose the first one instead
			return services.get(0);
		}
		return selected;
	}

	/**
	 * Set the currently active service. If this service has not yet been
	 * registered with {@link #addService(RESTContext)} it will be added. The
	 * currently active service is stored and is available as
	 * {@link #getSelected()}.
	 * 
	 * @param service
	 *            The service to set selected, or <code>null</code> if the
	 *            current service is to be reset.
	 */
	public void setSelected(RESTContext service) {
		if (! services.contains(service) && service != null) {
			addService(service);
		}
		this.selected = service;
		logger.debug("Selected service " + this.selected);
		save();
	}

}
