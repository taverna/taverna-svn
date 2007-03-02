/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */

package org.embl.ebi.escience.scuflworkers.soaplab;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.view.WorkflowSummaryAsHTML;
import org.embl.ebi.escience.scuflworkers.HTMLSummarisableProcessor;

/**
 * A processor based on the Soaplab web service around the EMBOSS tools. This
 * processor implementation will contact Soaplab in order to find the list of
 * extant ports at creation time. It is therefore important when creating an
 * instance of this class that the creating thread should be able to make an
 * HTTP connection to the supplied endpoint.
 * 
 * @author Tom Oinn
 */
public class SoaplabProcessor extends Processor implements Serializable, HTMLSummarisableProcessor {

	private URL endpoint = null;

	private int pollingInterval = 0;

	private double pollingBackoff = 1.0;

	private int pollingIntervalMax = 0;

	public void setPolling(int interval, double backoff, int maxInterval) {
		if (maxInterval < interval) {
			maxInterval = interval;
			backoff = 1.0;
		}
		this.pollingInterval = interval;
		this.pollingBackoff = backoff;
		this.pollingIntervalMax = maxInterval;
	}

	public boolean isPollingDefined() {
		return (pollingInterval != 0 || pollingBackoff != 1.0 || pollingIntervalMax != 0);
	}

	public int getPollingInterval() {
		return this.pollingInterval;
	}

	public double getPollingBackoff() {
		return this.pollingBackoff;
	}

	public int getPollingIntervalMax() {
		return this.pollingIntervalMax;
	}

	public int getMaximumWorkers() {
		return 10;
	}

	// Get the host part of the endpoint
	public String getResourceHost() {
		return endpoint.getHost();
	}

	// Get the category of the application
	public String getCategory() {
		String[] app = endpoint.getPath().split("::");
		if (app.length == 1) {
			// Probably new style URL of form http://host.com/root/category.name
			String[] s = endpoint.getPath().split("\\.");
			return s[s.length - 2];
		}
		String[] pathbits = app[0].split("/");
		return pathbits[pathbits.length - 1];
	}

	// Get the installation path of the soaplab server
	public String getServicePath() {
		String[] pathbits = endpoint.getPath().split("/");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < pathbits.length - 1; i++) {
			if (i > 0) {
				sb.append("/");
			}
			sb.append(pathbits[i]);
		}
		return sb.toString();
	}

	// Get the application name of the application
	public String getAppName() {
		String[] app = endpoint.getPath().split("::");
		if (app.length == 1) {
			// Probably new style URL of form http://host.com/root/category.name
			String[] s = endpoint.getPath().split("\\.");
			return s[s.length - 1];
		}
		return app[1];
	}

	/**
	 * Construct a new processor with the given model and name, delegates to the
	 * superclass.
	 */
	public SoaplabProcessor(ScuflModel model, String name, String endpoint) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, name);

		// If this is an old style endpoint with the '::' rewrite it
		String[] split = endpoint.split("::");
		String theEndpoint = endpoint;
		if (split.length == 2) {
			theEndpoint = split[0] + "." + split[1];
		}

		// Set the endpoint, this then populates the ports appropriately
		// from the returned parameters of the soap call.
		try {
			String firstPart = theEndpoint.split("\\?")[0];
			try {
				setEndpoint(firstPart);
			} catch (Exception e) {
				triedNewForm = true;
				StringBuffer sb = new StringBuffer();
				split = firstPart.split("\\.");
				for (int i = 0; i < split.length - 1; i++) {
					sb.append(split[i]);
					if (i < split.length - 2) {
						sb.append(".");
					}
				}
				sb.append("::" + split[split.length - 1]);
				setEndpoint(sb.toString());
			}
		} catch (MalformedURLException mue) {
			throw new ProcessorCreationException(name
					+ ": The supplied endpoint url was \n   malformed, endpoint was specified as '" + theEndpoint + "'");
		}
	}

	/**
	 * Get the properties for this processor for display purposes
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		props.put("Soaplab URL", getEndpoint().toString());
		return props;
	}

	boolean triedNewForm = false;

	/**
	 * Set the endpoint for this soaplab processor
	 */
	void setEndpoint(String specifier) throws MalformedURLException, ProcessorCreationException {
		URL new_endpoint = new URL(specifier);
		if (endpoint != null) {
			if (endpoint.equals(new_endpoint) == false) {
				fireModelEvent(new ScuflModelEvent(this, "Service endpoint changed to '" + specifier + "'"));
			} else {
				// Do nothing if the endpoint was the same as before
				return;
			}
		} else {
			fireModelEvent(new ScuflModelEvent(this, "Service endpoint set to '" + specifier + "'"));
		}
		endpoint = new_endpoint;
		try {
			if (this.isOffline() == false) {
				generatePorts();
				getDescriptionText();
			}
		} catch (PortCreationException pce) {
			throw new ProcessorCreationException(getName()
					+ ": Exception when trying to create ports\n   from Soaplab endpoint : " + pce.getMessage());
		} catch (DuplicatePortNameException dpne) {
			throw new ProcessorCreationException(getName()
					+ ": Exception when trying to create ports\n   from Soaplab endpoint : " + dpne.getMessage());
		}
	}

	/**
	 * Use the endpoint data to set the description field
	 */
	public void getDescriptionText() throws ProcessorCreationException {
		try {
			Map info = (Map) Soap.callWebService(endpoint.toString(), "getAnalysisType");
			// Get the description element from the map
			String description = (String) info.get("description");
			if (description != null) {
				setDescription(description);
			}
		} catch (ServiceException se) {
			throw new ProcessorCreationException(getName()
					+ ": Unable to create a new call to connect to\n   soaplab, error was : " + se.getMessage());
		} catch (RemoteException re) {
			throw new ProcessorCreationException(getName()
					+ ": Unable to call the get description method\n   for XScufl processor " + getName()
					+ "\nendpoint : " + endpoint.toString() + "\n   Remote exception message " + re.getMessage());
		}
	}

	/**
	 * Use the endpoint data to create new ports and attach them to the
	 * processor. Interogates Soaplab for names of input and output parameters,
	 * and additionally for their syntactic types, as we might as well keep that
	 * information while we have it.
	 */
	public void generatePorts() throws ProcessorCreationException, PortCreationException, DuplicatePortNameException {
		// Wipe the existing port declarations
		ports = new ArrayList();
		try {
			// Do web service type stuff[tm]
			Map[] inputs = (Map[]) Soap.callWebService(endpoint.toString(), "getInputSpec");
			// Iterate over the inputs
			for (int i = 0; i < inputs.length; i++) {
				Map input_spec = inputs[i];
				String input_name = (String) input_spec.get("name");
				String input_type = ((String) (input_spec.get("type"))).toLowerCase();
				// Could get other properties such as defaults here
				// but at the moment we've got nowhere to put them
				// so we don't bother.
				Port new_port = new InputPort(this, input_name);
				if (input_type.equals("string")) {
					new_port.setSyntacticType("'text/plain'");
				} else if (input_type.equals("string[]")) {
					new_port.setSyntacticType("l('text/plain')");
				} else if (input_type.equals("byte[]")) {
					new_port.setSyntacticType("'application/octet-stream'");
				} else if (input_type.equals("byte[][]")) {
					new_port.setSyntacticType("l('application/octet-stream')");
				}
				this.addPort(new_port);
			}

			// Get outputs
			Map[] results = (Map[]) Soap.callWebService(endpoint.toString(), "getResultSpec");
			// Iterate over the outputs
			for (int i = 0; i < results.length; i++) {
				Map output_spec = results[i];
				String output_name = (String) output_spec.get("name");
				String output_type = ((String) (output_spec.get("type"))).toLowerCase();
				// Check to see whether the output is either report or
				// detailed_status, in
				// which cases we ignore it, this is soaplab metadata rather
				// than application
				// data.
				if ((!output_name.equalsIgnoreCase("detailed_status"))) {
					// && (!output_name.equalsIgnoreCase("report"))) {
					Port new_port = new OutputPort(this, output_name);
					if (output_type.equals("string")) {
						new_port.setSyntacticType("'text/plain'");
					} else if (output_type.equals("string[]")) {
						new_port.setSyntacticType("l('text/plain')");
					} else if (output_type.equals("byte[]")) {
						new_port.setSyntacticType("'application/octet-stream'");
					} else if (output_type.equals("byte[][]")) {
						new_port.setSyntacticType("l('application/octet-stream')");
					}
					this.addPort(new_port);
				}
			}

		} catch (ServiceException se) {
			throw new ProcessorCreationException(getName()
					+ ": Unable to create a new call to connect\n   to soaplab, error was : " + se.getMessage());
		} catch (RemoteException re) {
			throw new ProcessorCreationException(getName() + ": Unable to call the get spec method for\n   endpoint : "
					+ endpoint.toString() + "\n   Remote exception message " + re.getMessage());
		} catch (NullPointerException npe) {
			// If we had a null pointer exception, go around again - this is a
			// bug somewhere between axis and soaplab
			// that occasionally causes NPEs to happen in the first call or two
			// to a given soaplab installation. It also
			// manifests in the Talisman soaplab clients.
			generatePorts();
		}
	}

	/**
	 * Get the URL for this endpoint
	 */
	public URL getEndpoint() {
		return endpoint;
	}

	public String getHTMLSummary(List<HTMLSummarisableProcessor> processors, Map<String, Processor> names) {
		StringBuffer sb = new StringBuffer();
		Map<String,Map<String,Set<String>>> soaplabLocations = new HashMap<String,Map<String,Set<String>>>();
		for (HTMLSummarisableProcessor proc : processors) {
			SoaplabProcessor processor = (SoaplabProcessor) proc;
			String soaplabLocation = processor.getServicePath();
			if (soaplabLocations.containsKey(soaplabLocation) == false) {
				soaplabLocations.put(soaplabLocation, new HashMap<String,Set<String>>());
			}
			Map<String,Set<String>> nameToProcessorNames = soaplabLocations.get(soaplabLocation);
			String appName = processor.getCategory() + "::<font color=\"purple\">" + processor.getAppName() + "</font>";
			if (nameToProcessorNames.containsKey(appName) == false) {
				nameToProcessorNames.put(appName, new HashSet<String>());
			}
			Set<String> processorNames = nameToProcessorNames.get(appName);
			processorNames.add(WorkflowSummaryAsHTML.nameFor(names, processor));
		}
		for (Iterator j = soaplabLocations.keySet().iterator(); j.hasNext();) {
			String location = (String) j.next();
			Map<String,Set<String>> nameToProcessorName = soaplabLocations.get(location);
			int rows = 2 + nameToProcessorName.size();
			sb.append("<tr>");
			sb.append("<td width=\"80\" valign=\"top\" rowspan=\"" + rows + "\" bgcolor=\"#faf9d2\">Soaplab</td>");
			sb.append("<td colspan=\"2\" bgcolor=\"faf9d2\">Service rooted at <em>" + location + "</em></td>");
			sb.append("</tr>");
			sb.append("<tr><td bgcolor=\"#eeeedd\">App category and name</td><td bgcolor=\"#eeeedd\">Processors</td></tr>");
			
			for (Iterator k = nameToProcessorName.keySet().iterator(); k.hasNext();) {
				String appName = (String) k.next();
				Set<String> processorNames = nameToProcessorName.get(appName);
				sb.append("<tr>");
				sb.append("<td>" + appName + "</td>");
				sb.append("<td>");
				for (Iterator l = processorNames.iterator(); l.hasNext();) {
					sb.append((String) l.next());
					if (l.hasNext()) {
						sb.append(", ");
					}
				}
				sb.append("</td></tr>");
			}
		}
		return sb.toString();
	}

	public int htmlTablePlacement() {
		return 2;
	}

}
