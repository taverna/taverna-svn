/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyDataType;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyRelationship;
import org.biomoby.shared.NoSuccessException;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
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
import org.embl.ebi.escience.scufl.UnknownPortException;

public class BiomobyObjectProcessor extends Processor implements Serializable {

    private static final long serialVersionUID = 3545795464290122041L;

    private URL endpoint;

    private String mobyEndpoint = null;

    private Central worker = null;

    private MobyDataType mobyObject = null;

    private String serviceName = null;

    private String authorityName = null;

    /**
     * Construct a new processor with the given model and name, delegates to the
     * superclass.
     */
    public BiomobyObjectProcessor(ScuflModel model, String processorName,
            String authorityName, String serviceName, String mobyEndpoint)
            throws ProcessorCreationException, DuplicateProcessorNameException {
        super(model, processorName);
        this.mobyEndpoint = mobyEndpoint;
        this.serviceName = serviceName;
        this.authorityName = authorityName;
        if (!this.isOffline()) {
            init();
        } else {
            try {
                this.endpoint = new URL("http://unknown.host.org/UnknownHost");
            } catch (MalformedURLException mue) {
                //
            }
        }
        if (model != null)
            if (this.serviceName.equalsIgnoreCase("Object")
                    || this.serviceName.equalsIgnoreCase("String")
                    || this.serviceName.equalsIgnoreCase("Integer")
                    || this.serviceName.equalsIgnoreCase("DateTime")) {
                return;
            } else {
                createDataLinks(this, true);
            }
    }

    public BiomobyObjectProcessor(ScuflModel model, String processorName,
            String authorityName, String serviceName, String mobyEndpoint,
            boolean prompt) throws ProcessorCreationException,
            DuplicateProcessorNameException {
        super(model, processorName);
        this.mobyEndpoint = mobyEndpoint;
        this.serviceName = serviceName;
        this.authorityName = authorityName;
        if (!this.isOffline()) {
            init();
        } else {
            try {
                this.endpoint = new URL("http://unknown.host.org/UnknownHost");
            } catch (MalformedURLException mue) {
                //
            }
        }
        if (model != null) {
        	//TODO - replace with -> StackTraceElement ste = new Throwable().getStackTrace()[ 1 ] .getClassName();
            Throwable stack = new Throwable();
            stack.fillInStackTrace();
            String callingClassName = "";
            if (stack.getStackTrace().length >=4)
                callingClassName = stack.getStackTrace()[3].getClassName();
                //sun.reflect.Reflection.getCallerClass(4).getName();
            if (!callingClassName.equals("org.embl.ebi.escience.scufl.parser.ProcessorLoaderThread"))
                createDataLinks(this, prompt);            
        }
    }

    /**
     * Construct a new processor with the given model and name, delegates to the
     * superclass.
     */
    public BiomobyObjectProcessor(ScuflModel model, String processorName,
            MobyDataType service, String mobyEndpoint)
            throws ProcessorCreationException, DuplicateProcessorNameException {
        super(model, processorName);
        this.mobyEndpoint = mobyEndpoint;
        this.serviceName = service.getName();
        this.authorityName = service.getAuthority();
        this.mobyObject = service;
        if (!this.isOffline()) {
            init();
        }
    }

    /*
     *  
     */
    private void init() throws ProcessorCreationException {
        // Find the service endpoint (by calling Moby registry)
        try {
            if (mobyObject == null) {
                worker = new CentralImpl(mobyEndpoint);
                mobyObject = worker.getDataType(this.serviceName);
                setEndpoint(mobyEndpoint);
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
        props.put("Biomoby Object", mobyObject.getName());
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
    void setEndpoint(String specifier) throws MalformedURLException,
            ProcessorCreationException {
        URL new_endpoint = new URL(specifier);
        if (this.endpoint != null) {
            if (!this.endpoint.equals(new_endpoint)) {
                fireModelEvent(new ScuflModelEvent(this,
                        "Object endpoint changed to '" + specifier + "'"));

            } else {
                // Do nothing if the endpoint was the same as before
                return;
            }
        } else {
            fireModelEvent(new ScuflModelEvent(this, "Object endpoint set to '"
                    + specifier + "'"));
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
        if (mobyObject.getDescription() != null)
            setDescription(mobyObject.getDescription());
        else {
            try {
                // worker = new CentralImpl(this.mobyEndpoint);
                this.mobyObject = worker.getDataType(this.serviceName);
            } catch (MobyException e) {
                e.printStackTrace();
                throw new ProcessorCreationException(
                        "Error creating new CentralImpl in getDescriptionText. Processor not created");
            } catch (NoSuccessException e) {
                e.printStackTrace();
                throw new ProcessorCreationException(
                        "Error getting data type in getDescriptionText. Processor not created");
            }

        }
    }

    public void generatePorts() throws ProcessorCreationException,
            PortCreationException, DuplicatePortNameException {

        // Wipe the existing port declarations
        ports = new ArrayList();
        // inputs
        Port input_port = new InputPort(this, "namespace");
        input_port.setSyntacticType("'text/plain'");
        this.addPort(input_port);

        input_port = new InputPort(this, "id");
        input_port.setSyntacticType("'text/plain'");
        this.addPort(input_port);

        input_port = new InputPort(this, "article name");
        input_port.setSyntacticType("'text/plain'");
        this.addPort(input_port);

        try {
            MobyDataType datatype = worker.getDataType(serviceName);
            MobyRelationship[] relations = datatype.getChildren();
            processRelationships(relations);
            String parent = "Object";
            try {
                parent = datatype.getParentNames()[0];
                if (parent.indexOf(":") > 0) {
                    parent = parent.substring(parent.lastIndexOf(":") + 1);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // parent is then by default object
            }
            if (parent.equalsIgnoreCase("String")
                    || parent.equalsIgnoreCase("Integer")
                    || parent.equalsIgnoreCase("float")
                    || parent.equalsIgnoreCase("DateTime")
                    || parent.equalsIgnoreCase("Boolean")
                    || serviceName.equalsIgnoreCase("String")
                    || serviceName.equalsIgnoreCase("Boolean")
                    || serviceName.equalsIgnoreCase("Integer")
                    || serviceName.equalsIgnoreCase("float")
                    || serviceName.equalsIgnoreCase("DateTime")) {
                input_port = new InputPort(this, "value");
                input_port.setSyntacticType("'text/plain'");
                this.addPort(input_port);
            } else {
                if (!parent.equalsIgnoreCase("Object"))
                    extractParentContainerRelationships(parent);
            }
        } catch (MobyException e) {
        } catch (NoSuccessException e) {
        }

        // outputs
        Port output_port = new OutputPort(this, "mobyData");
        output_port.setSyntacticType("'text/xml'");
        this.addPort(output_port);
    }

    private void extractParentContainerRelationships(String string) {
        try {
            Port input_port;
            MobyDataType datatype = worker.getDataType(string);
            // need to propagate the isa up to Object to get all of the has/hasa
            MobyRelationship[] relations = datatype.getChildren();
            processRelationships(relations);
            String parent = "Object";
            try {
                parent = datatype.getParentNames()[0];
                if (parent.indexOf(":") > 0) {
                    parent = parent.substring(parent.lastIndexOf(":") + 1);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // parent is then by default object
            }
            if (parent.equalsIgnoreCase("String")
                    || parent.equalsIgnoreCase("Integer")
                    || parent.equalsIgnoreCase("float")
                    || serviceName.equalsIgnoreCase("String")
                     || parent.equalsIgnoreCase("Boolean")
                    || serviceName.equalsIgnoreCase("Boolean")
                    || serviceName.equalsIgnoreCase("Integer")
                    || serviceName.equalsIgnoreCase("float")) {
                input_port = new InputPort(this, "value");
                input_port.setSyntacticType("'text/plain'");
                this.addPort(input_port);
            } else {
                if (!parent.equalsIgnoreCase("Object"))
                    extractParentContainerRelationships(parent);
            }
        } catch (MobyException e) {
        } catch (NoSuccessException e) {
        } catch (DuplicatePortNameException e) {
            e.printStackTrace();
        } catch (PortCreationException e) {
            e.printStackTrace();
        }
    }

    private void processRelationships(MobyRelationship[] relations)
            throws DuplicatePortNameException, PortCreationException {
        Port input_port;
        for (int x = 0; x < relations.length; x++) {
            MobyRelationship relationship = relations[x];

            // strip urn:lsid:...
            String name = relationship.getDataTypeName();
            if (name.indexOf(":") > 0) {
                name = name.substring(name.lastIndexOf(":") + 1);
            }
            // port name == DataType(articleName)
            name = name + "(" + relationship.getName() + ")";
            switch (relationship.getRelationshipType()) {
            case (Central.iHAS): {
                // TODO - not really supported
                input_port = new InputPort(this, name);
                input_port.setSyntacticType("'text/xml)");
                this.addPort(input_port);
                break;
            }
            case (Central.iHASA): {
                input_port = new InputPort(this, name);
                input_port.setSyntacticType("'text/xml'");
                this.addPort(input_port);
                break;
            }
            default:
                break;
            }
        }
    }

    /**
     * Get the URL for this endpoint. This is the service endpoint NOT the
     * BioMoby registry one!
     */
    public URL getEndpoint() {
        return this.endpoint;
    }

    /**
     * Get the name of this Moby-compliant Object
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

    public MobyDataType getMobyObject() {
        return mobyObject;
    }

    //
    protected String formatError(String msg) {
        // Removed references to the authority, some errors
        // were causing it to be null which in turn threw
        // a NPE from here, breaking Taverna's error handlers
        return ("Problems with object '" + serviceName
                + "' provided by authority '" + authorityName
                + "'\nfrom Moby registry at " + mobyEndpoint + ":\n\n" + msg);
    }

    /**
     * TODO - place brief description here.
     * <p>
     * <b>PRE: </b>
     * <p>
     * <b>POST: </b>
     * 
     * @return
     */
    public Central getCentral() {
        if (worker != null)
            return worker;
        else
            try {
                return new CentralImpl(this.mobyEndpoint);
            } catch (MobyException e) {
                return null;
            }
    }

    /*
     * method that given a BiomobyObjectProcessor, iterates throught the input
     * ports and if a 'complex' processor exists at the port, adds the processor
     * (and its more complex sub components as well) to the workflow. This
     * method is useful because moby datatype processors are sometimes composed
     * of many sub components and adding them automatically to the workflow can
     * be time saving and very helpful.
     */
    private void createDataLinks(Processor bop, boolean prompt) {
        if (prompt) {
            int answer = JOptionPane
                    .showConfirmDialog(
                            null,
                            "Would you like to add all of the subcomponents for the processor '"
                                    + bop.getName()
                                    + "' that was just added to the workflow? Data links would be added.");
            if (answer == JOptionPane.NO_OPTION
                    || answer == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        if (bop instanceof BiomobyObjectProcessor) {
            BiomobyObjectProcessor processor = (BiomobyObjectProcessor) bop;
            ScuflModel scuflModel = processor.getModel();
            if (scuflModel == null)
                return;
            InputPort[] ports = processor.getInputPorts();
            for (int i = 0; i < ports.length; i++) {
                InputPort p = ports[i];
                // ignore article name, id, namespace, value
                if (p.getName().equals("namespace") || p.getName().equals("id")
                        || p.getName().equals("article name")
                        || p.getName().equals("value")) {
                    continue;
                }
                String portName = p.getName();
                String datatype = portName.split("\\(")[0];
                Processor subComponentProcessor;
                try {
                    subComponentProcessor = new BiomobyObjectProcessor(
                            scuflModel, scuflModel
                                    .getValidProcessorName(datatype), "",
                            datatype, processor.getMobyEndpoint(), false);
                    scuflModel.addProcessor(subComponentProcessor);
                    scuflModel.addDataConstraint(new DataConstraint(scuflModel,
                            subComponentProcessor.locatePort("mobyData"), p));
                } catch (ProcessorCreationException pce) {
                    JOptionPane.showMessageDialog(null,
                            "Processor creation exception : \n"
                                    + pce.getMessage(), "Exception!",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (DuplicateProcessorNameException dpne) {
                    JOptionPane.showMessageDialog(null, "Duplicate name : \n"
                            + dpne.getMessage(), "Exception!",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (DataConstraintCreationException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Data Link creation exception : \n"
                                    + ex.getMessage(), "Exception!",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (UnknownPortException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Unknown Port Exception : \n" + ex.getMessage(),
                            "Exception!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                //createDataLinks(subComponentProcessor, false);
            }
        }
    }
}