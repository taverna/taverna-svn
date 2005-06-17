/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */

package org.biomoby.client.taverna.plugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyPrimaryDataSet;
import org.biomoby.shared.MobyPrimaryDataSimple;
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

/**
 * A processor based on the Biomoby compliant web services.  This
 * processor implementation will contact Biomoby registry in order to
 * find the list of extant ports at creation time. <p>
 *
 * @version $Id: BiomobyProcessor.java,v 1.1 2005-06-17 14:17:38 mereden Exp $
 * @author Martin Senger
 */
public class BiomobyProcessor extends Processor implements java.io.Serializable {

    
    private static final long serialVersionUID = 1L;

    private URL endpoint;

    private String mobyEndpoint = null;

    private Central worker = null;

    private MobyService mobyService = null;

    private String serviceName = null;

    private String authorityName = null;

    /**
     * Construct a new processor with the given model and name,
     * delegates to the superclass.
     */
    public BiomobyProcessor(ScuflModel model, String processorName,
            String authorityName, String serviceName, String mobyEndpoint)
            throws ProcessorCreationException, DuplicateProcessorNameException {
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
     * Construct a new processor with the given model and name,
     * delegates to the superclass.
     */
    public BiomobyProcessor(ScuflModel model, String processorName,
            MobyService service, String mobyEndpoint)
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
                    throw new ProcessorCreationException(
                            formatError("I cannot find the service."));
                mobyService = services[0];
            }
            String serviceEndpoint = mobyService.getURL();
            if (serviceEndpoint == null || serviceEndpoint.equals(""))
                throw new ProcessorCreationException(
                        formatError("Service has an empty endpoint."));
            try {
                setEndpoint(serviceEndpoint);
            } catch (MalformedURLException e2) {
                throw new ProcessorCreationException(
                        formatError("Service has malformed endpoint: '"
                                + serviceEndpoint + "'."));
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
     * Get the moby central endpoint used to locate this
     * processor
     */
    public String getMobyEndpoint() {
        return this.mobyEndpoint;
    }

    /**
     * Set the endpoint for this biomoby processor
     */
    void setEndpoint(String specifier) throws MalformedURLException,
            ProcessorCreationException {

        URL new_endpoint = new URL(specifier);
        if (this.endpoint != null) {
            if (!this.endpoint.equals(new_endpoint)) {
                fireModelEvent(new ScuflModelEvent(this,
                        "Service endpoint changed to '" + specifier + "'"));

            } else {
                // Do nothing if the endpoint was the same as before
                return;
            }
        } else {
            fireModelEvent(new ScuflModelEvent(this,
                    "Service endpoint set to '" + specifier + "'"));
        }

        this.endpoint = new_endpoint;

        try {
            generatePorts();
            getDescriptionText();
        } catch (PortCreationException e) {
            throw new ProcessorCreationException(
                    formatError("When trying to create ports: "
                            + e.getMessage()));
        } catch (DuplicatePortNameException e) {
            throw new ProcessorCreationException(
                    formatError("When trying to create ports: "
                            + e.getMessage()));
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
     * Use the endpoint data to create new ports and attach them to
     * the processor.
     *
     * TBD better - for now just take that every service eats a string
     * and produces a string... (MS)
     */
    public void generatePorts() throws ProcessorCreationException,
            PortCreationException, DuplicatePortNameException {

        // Wipe the existing port declarations
        ports = new ArrayList();

        // inputs TODO - find a better way to deal with collections
        MobyData[] serviceInputs = this.mobyService.getPrimaryInputs();
        for (int x = 0; x < serviceInputs.length; x++) {
            if (serviceInputs[x] instanceof MobyPrimaryDataSimple) {
                MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple)serviceInputs[x];
                
                // retrieve the simple article name
                String simpleName = simple.getName();
                if (simpleName.equals("")) {
                    simpleName = "_ANON_";
                }
                simpleName = "(" + simpleName + ")";
                
                Port inPort = new InputPort(this, simple.getDataType().getName() + simpleName);
                inPort.setSyntacticType("'text/xml'");
                this.addPort(inPort);
            } else {
                // collection of items
                MobyPrimaryDataSet collection = (MobyPrimaryDataSet)serviceInputs[x];
                String collectionName = collection.getName();
                if (collectionName.equals(""))
                    collectionName = "MobyCollection";
                MobyPrimaryDataSimple[] simples = collection.getElements();
                for (int y = 0; y < simples.length; y++) {
                    
                    Port inPort = new InputPort(this, simples[y].getDataType().getName() + "(Collection - '" + collectionName + "')");
                    inPort.setSyntacticType("'text/xml'");
                    this.addPort(inPort);
                }
            }
        }
        Port input_port = new InputPort(this, "input");
        input_port.setSyntacticType("'text/xml'");
        this.addPort(input_port);
        // outputs
        MobyData[] serviceOutputs = this.mobyService.getPrimaryOutputs();
        for (int x = 0; x < serviceOutputs.length; x++) {
            if (serviceOutputs[x] instanceof MobyPrimaryDataSimple) {
                MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple)serviceOutputs[x];
                
                // retrieve the simple article name
                String simpleName = simple.getName();
                if (simpleName.equals("")) {
                    simpleName = "_ANON_";
                }
                simpleName = "(" + simpleName + ")";
                
                Port outPort = new OutputPort(this, simple.getDataType().getName() + simpleName);
                outPort.setSyntacticType("'text/xml'");
                this.addPort(outPort);
            } else {
                // collection of items
                MobyPrimaryDataSet collection = (MobyPrimaryDataSet)serviceOutputs[x];
                String collectionName = collection.getName();
                if (collectionName.equals(""))
                    collectionName = "MobyCollection";
                MobyPrimaryDataSimple[] simples = collection.getElements();
                for (int y = 0; y < simples.length; y++) {
                    Port outPort = new OutputPort(this, simples[y].getDataType().getName() + "(Collection - '" + collectionName + "')");
                    outPort.setSyntacticType("'text/xml'");
                    this.addPort(outPort);
                }
            }
        }
        
        Port output_port = new OutputPort(this, "output");
        output_port.setSyntacticType("'text/xml'");
        this.addPort(output_port);
    }

    /**
     * Get the URL for this endpoint. This is the service
     * endpoint NOT the BioMoby registry one!
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

    public MobyService getMobyService() {
        return this.mobyService;
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
        return ("Problems with service '" + serviceName
                + "' provided by authority '" + authorityName
                + "'\nfrom Moby registry at " + mobyEndpoint + ":\n\n" + msg);
    }

    public Central getCentralWorker() {
        return worker;
    }
    
}