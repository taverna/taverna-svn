/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI
 */

package org.embl.ebi.escience.scuflworkers.biomoby;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyPrimaryDataSet;
import org.biomoby.shared.MobyService;
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
 * A processor based on the Biomoby compliant web services. This processor
 * implementation will contact Biomoby registry in order to find the list of
 * extant ports at creation time.
 * <p>
 * 
 * @version $Id: BiomobyProcessor.java,v 1.2 2006-07-10 14:08:14 sowen70 Exp $
 * @author Martin Senger
 */
public class BiomobyProcessor extends Processor implements java.io.Serializable, HTMLSummarisableProcessor {

	private URL endpoint;

	private String mobyEndpoint = null;

	private Central worker = null;

	private MobyService mobyService = null;

	private String serviceName = null;

	private String authorityName = null;

	/**
	 * Construct a new processor with the given model and name, delegates to the
	 * superclass.
	 */
	public BiomobyProcessor(ScuflModel model, String processorName, String authorityName, String serviceName,
			String mobyEndpoint) throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, processorName);
		this.mobyEndpoint = mobyEndpoint;
		this.serviceName = serviceName;
		this.authorityName = authorityName;
		if (this.isOffline() == false) {
			init();
		} else {
			try {
				this.endpoint = new URL("http://unknown.host.org/UnknownHost");
			} catch (MalformedURLException mue) {
				//
			}
		}
	}

	/**
	 * Construct a new processor with the given model and name, delegates to the
	 * superclass.
	 */
	public BiomobyProcessor(ScuflModel model, String processorName, MobyService service, String mobyEndpoint)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, processorName);
		this.mobyEndpoint = mobyEndpoint;
		this.serviceName = service.getName();
		this.authorityName = service.getAuthority();
		this.mobyService = service;
		if (this.isOffline() == false) {
			init();
		} else {
			try {
				this.endpoint = new URL(service.getURL());
			} catch (MalformedURLException mue) {
				//
			}
		}
	}

	void init() throws ProcessorCreationException {
		// Find the service endpoint (by calling Moby registry)
		try {
			if (mobyService == null) {
				worker = new CentralImpl(mobyEndpoint);

				MobyService pattern = new MobyService(serviceName);
				pattern.setAuthority(authorityName);
				MobyService[] services = worker.findService(pattern);
				if (services == null || services.length == 0)
					throw new ProcessorCreationException(formatError("I cannot find the service."));
				mobyService = services[0];
			}
			String serviceEndpoint = mobyService.getURL();
			if (serviceEndpoint == null || serviceEndpoint.equals(""))
				throw new ProcessorCreationException(formatError("Service has an empty endpoint."));
			try {
				setEndpoint(serviceEndpoint);
			} catch (MalformedURLException e2) {
				throw new ProcessorCreationException(formatError("Service has malformed endpoint: '" + serviceEndpoint
						+ "'."));
			}

		} catch (Exception e) {
			if (e instanceof ProcessorCreationException) {
				throw (ProcessorCreationException) e;
			}
			throw new ProcessorCreationException(formatError(e.toString()));
		}
	}

	/**
	 * Get the host for this service
	 */
	public String getResourceHost() {
		return this.authorityName;
	}

	/**
	 * Get the properties for this processor for display purposes
	 */
	public Properties getProperties() {

		Properties props = new Properties();
		props.put("Biomoby service URL", getEndpoint().toString());
		return props;
	}

	/**
	 * Get the moby central endpoint used to locate this processor
	 */
	public String getMobyEndpoint() {
		return this.mobyEndpoint;
	}

	/**
	 * Set the endpoint for this biomoby processor
	 */
	void setEndpoint(String specifier) throws MalformedURLException, ProcessorCreationException {

		URL new_endpoint = new URL(specifier);
		if (this.endpoint != null) {
			if (!this.endpoint.equals(new_endpoint)) {
				fireModelEvent(new ScuflModelEvent(this, "Service endpoint changed to '" + specifier + "'"));

			} else {
				// Do nothing if the endpoint was the same as before
				return;
			}
		} else {
			fireModelEvent(new ScuflModelEvent(this, "Service endpoint set to '" + specifier + "'"));
		}

		this.endpoint = new_endpoint;

		try {
			generatePorts();
			getDescriptionText();
		} catch (PortCreationException e) {
			throw new ProcessorCreationException(formatError("When trying to create ports: " + e.getMessage()));
		} catch (DuplicatePortNameException e) {
			throw new ProcessorCreationException(formatError("When trying to create ports: " + e.getMessage()));
		}
	}

	/**
	 * Set the description field
	 */
	public void getDescriptionText() throws ProcessorCreationException {

		if (mobyService.getDescription() != null)
			setDescription(mobyService.getDescription());
	}

	/**
	 * Use the endpoint data to create new ports and attach them to the
	 * processor.
	 * 
	 * TBD better - for now just take that every service eats a string and
	 * produces a string... (MS)
	 */
	public void generatePorts() throws ProcessorCreationException, PortCreationException, DuplicatePortNameException {

		// Wipe the existing port declarations
		ports = new ArrayList();

		// inputs
		Port input_port = new InputPort(this, "input");

		boolean isInputCollection = false;
		MobyData[] inputs = mobyService.getPrimaryInputs();
		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i] instanceof MobyPrimaryDataSet) {
				isInputCollection = true;
				break;
			}
		}
		input_port.setSyntacticType(isInputCollection ? "l('text/xml')" : "'text/xml'");
		this.addPort(input_port);

		// outputs
		Port output_port = new OutputPort(this, "output");

		boolean isOutputCollection = false;
		MobyData[] outputs = mobyService.getPrimaryOutputs();
		for (int i = 0; i < outputs.length; i++) {
			if (outputs[i] instanceof MobyPrimaryDataSet) {
				isOutputCollection = true;
				break;
			}
		}

		output_port.setSyntacticType(isOutputCollection ? "l('text/xml')" : "'text/xml'");
		this.addPort(output_port);
	}

	/**
	 * Get the URL for this endpoint. This is the service endpoint NOT the
	 * BioMoby registry one!
	 */
	public URL getEndpoint() {
		return this.endpoint;
	}

	/**
	 * Get the name of this Moby-compliant service
	 */
	public String getServiceName() {
		return this.serviceName;
	}

	/**
	 * Get the authority of this Moby-compliant service
	 */
	public String getAuthorityName() {
		return this.authorityName;
	}

	//
	protected String formatError(String msg) {
		// Removed references to the authority, some errors
		// were causing it to be null which in turn threw
		// a NPE from here, breaking Taverna's error handlers
		return ("Problems with service '" + serviceName + "' provided by authority '" + authorityName
				+ "'\nfrom Moby registry at " + mobyEndpoint + ":\n\n" + msg);
	}

	public String getHTMLSummary(List<HTMLSummarisableProcessor> processors, Map<String, Processor> names) {
		StringBuffer sb = new StringBuffer();
		for (HTMLSummarisableProcessor proc : processors) {
			BiomobyProcessor bp = (BiomobyProcessor) proc;
			sb.append("<tr>");
			sb.append("<td bgcolor=\"#ffd200\">Biomoby</td>");
			sb.append("<td><font color=\"purple\">" + bp.getServiceName() + "</font>&nbsp;in&nbsp;"
					+ bp.getEndpoint().getFile() + "</td>");
			sb.append("<td>" + WorkflowSummaryAsHTML.nameFor(names, bp) + "</td></tr>");
		}
		return sb.toString();
	}

	public int htmlTablePlacement() {
		return 4;
	}

}
