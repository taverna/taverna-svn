package idaservicetype.idaservicetype.ui.converter;

import idaservicetype.idaservicetype.ui.converter.PortMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.IOInputPort;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.IOOutputPort;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerIOODescription;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerParameterDescription;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidAnalyticsPreferences;

import ch.uzh.ifi.ddis.ida.api.IOObjectDescription;
import ch.uzh.ifi.ddis.ida.api.OperatorApplication;
import ch.uzh.ifi.ddis.ida.api.Parameter;
import ch.uzh.ifi.ddis.ida.api.Plan;
import ch.uzh.ifi.ddis.ida.api.SimpleParameter;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class UpdatedDataFlowGenerator {
	
	private Dataflow dataFlow;
   
	private Edits edits;
    
	private Map<String, RapidMinerExampleActivity> rmActivity = new HashMap<String, RapidMinerExampleActivity>();
    
	private Map<RapidMinerExampleActivity, Set<RapidMinerExampleActivity>> producerToUser = new HashMap<RapidMinerExampleActivity, Set<RapidMinerExampleActivity>>();
    
	private Map<String,Connection> connectionsByIOObjectId = new HashMap<String, Connection>();

    private Set<RapidMinerExampleActivity> terminatingOperators = new HashSet<RapidMinerExampleActivity>();

    private Map<String, Set<PortMapper>> portMapping = new HashMap<String, Set<PortMapper>>();
    
    private Map<String, Processor> processors = new HashMap<String, Processor>();
    
	private String workingLocation = "/demo/tempfiles/";

    private static final Logger logger = LoggerFactory.getLogger(DataFlowGenerator.class);

    public UpdatedDataFlowGenerator() {
    	
        this.edits = new EditsImpl();
        this.dataFlow = edits.createDataflow();
    	
    }
    
    public Dataflow getDataflow (Plan p) {
    	
    	 // get the basic types from plan (OperatorApplication)
        List<OperatorApplication> basicOpApps = new ArrayList<OperatorApplication>();
        logger.info("\n\nGot plan rank: " + p.getRank());
        
        for (OperatorApplication opAp : p.getOperatorApplications()) {
            basicOpApps.addAll(processOperatorApplication(opAp));
        }
    	
        // create the workflow
        for (OperatorApplication opApp : basicOpApps) {

            try {
                processOperator(opApp);
            } catch (ActivityConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        
        for (String IOObjectID : connectionsByIOObjectId.keySet()) {

            logger.info("processing IOObject: " + IOObjectID);
            Connection con = connectionsByIOObjectId.get(IOObjectID);

            String fileLocation = con.getLocation();

            if (fileLocation == null) {

                fileLocation = workingLocation + con.getIoObjectId();
            }
            
            logger.info("file location: " + fileLocation);

            // if nothing produces it then it is the input file
            // set it as the input location on the activity
            if (con.getProducer() == null) {
                logger.info("No producer for: " + IOObjectID + " loc:" + fileLocation);
                System.err.println("No producer for: " + IOObjectID + " loc:" + fileLocation);

                for (Connection.User activityUser : con.getUsers()) {
                    RapidMinerExampleActivity activity = rmActivity.get(activityUser.getOperator().getConfiguration().getOperatorName());

                    Connection.DMWFProperty dmwfProperty = activityUser.getUsesProperty();

                    String dmwfPropertyName = dmwfProperty.getPropertyName();
                    String dmwfPropertyType = dmwfProperty.getPropertyType();

                    logger.info("dmwfPRopertyname: " + dmwfPropertyName );
                    logger.info("dmwfPRopertype: " + dmwfPropertyType );
                    
                    
                    // get the activity ports on the input ports of the incoming activity
                    LinkedHashMap<String, IOInputPort> ioObjectDescription = activity.getConfiguration().getInputPorts();

                    boolean seenFirstModel = false;
                    for (String key : ioObjectDescription.keySet()) {

                        String portName = ioObjectDescription.get(key).getPortName();
                        String portClass = ioObjectDescription.get(key).getPortClass();

                        if (dmwfPropertyName.equals("uses")) {
                            // we know it just a plain uses, so set it!
                            ioObjectDescription.get(key).setFileLocationAt(0, fileLocation);
                            logger.info("Setting input location:" + fileLocation + " uses by " + activity.getConfiguration().getOperatorName() +
                            " on port " + key);

                        }
                        else if (dmwfPropertyName.equals("usesAW")) {
                            if (portName.contains("weights")) {
                                ioObjectDescription.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting input location:" + fileLocation + " usesAw " + activity.getConfiguration().getOperatorName() +
                                " on port " + key);
                            }
                        }
                        else if (dmwfPropertyName.equals("usesModel")) {
                            if (portName.equals("model")) {
                                ioObjectDescription.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting input location:" + fileLocation + " usesModel " + activity.getConfiguration().getOperatorName() +
                                " on port " + key);
                            }
                        }
                        else if (dmwfPropertyName.equals("usesFirstModel")) {
                            // if filelocation is not set on the bean, then it must be the first model
                            if (portName.contains("model") && !seenFirstModel) {
                                ioObjectDescription.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting input location:" + fileLocation + " usesFirstModel " + activity.getConfiguration().getOperatorName() +
                                " on port " + key);

                                seenFirstModel = true;
                            }
                        }
                        else if (dmwfPropertyName.equals("usesSecondModel")) {
                            // if filelocation is not set on the bean, then it must be the first model
                            if (seenFirstModel) {
                                ioObjectDescription.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting input location:" + fileLocation + " usesSecondModel " + activity.getConfiguration().getOperatorName() +
                                " on port " + key);
                            }
                        }
                        else if (dmwfPropertyName.equals("usesData")) {
                            // else it is an example set
                            if (portClass.equals("ExampleSet")) {
                                ioObjectDescription.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting input location:" + fileLocation + " usesData " + activity.getConfiguration().getOperatorName() +
                                " on port " + key);
                            }
                        }

                    }

                    // update the configuration
                    activity.getConfiguration().setInputPorts(ioObjectDescription);
                }
            }
            else {

                // it does have a produces, set the file location on the producer

                RapidMinerExampleActivity activity = rmActivity.get(con.getProducer().getConfiguration().getOperatorName());

                LinkedHashMap<String, IOOutputPort> ioOutputPorts = activity.getConfiguration().getOutputPorts();

                for (String key : ioOutputPorts.keySet()) {
                    // go the producer output ports and set the file location
                    String portName = ioOutputPorts.get(key).getPortName();
                    String portClass = ioOutputPorts.get(key).getPortClass();

                    Connection.DMWFProperty dmwfProperty = con.getProducesProperty();

                    logger.debug("looping through port: " + key + " (" + portName + ":" + portClass +")");
                    logger.debug("dmwfProperty: " + dmwfProperty.getPropertyName() + " (" + dmwfProperty.getPropertyType() + ")");
                    if (!key.equals("original")) {
                        // not all operators output the original file it seems...
                        if (dmwfProperty.getPropertyName().equals("producesData")) {
                            if (portClass.equals("ExampleSet") && dmwfProperty.getPropertyType().equals("DataTable")) {
                                ioOutputPorts.get(key).getFileLocations().add(fileLocation);

                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " producesData " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);
                            }
                        }
                        else if (dmwfProperty.getPropertyName().equals("producesPrePropModel")) {
                            if (portName.contains("preprocessing_model")) {
                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " producesPrePropModel " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);

                            }
                        }
                        else if (dmwfProperty.getPropertyName().equals("produces")) {
                            if (portClass.equals("ExampleSet") && dmwfProperty.getPropertyType().equals("DataTable")) {
                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " produces " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);
                            }
                            else if (portClass.equals("Model") && dmwfProperty.getPropertyType().equals("Model")) {
                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " produces " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);
                            }
                            else if (portName.contains("model") && dmwfProperty.getPropertyType().equals("Model")) {
                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " produces " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);
                            }
                            else if (portName.contains("model") && dmwfProperty.getPropertyType().endsWith("Model")) {
                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " produces " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);
                            }
                            else if (portClass.contains("Performance")) {
                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " produces " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);
                            }
                            else if (portClass.equals("AttributeWeights") && dmwfProperty.getPropertyType().equals("AttributeWeights")) {
                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " produces " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);

                            }

                        }
                    }
                    activity.getConfiguration().setOutputPorts(ioOutputPorts);

                }

                logger.info("Setting output location on producer " + activity.getConfiguration().getOperatorName() + " to " + fileLocation );

                // need to connect producer to all the users
                if (producerToUser.get(activity) == null) {
                    producerToUser.put(activity, new HashSet<RapidMinerExampleActivity>());
                }

                if (con.getUsers().isEmpty()) {
                    // connect the output port to the final activity output
                    logger.info("this IO never has a user:" + IOObjectID + " produced by : " + activity.toString());
                    terminatingOperators.add(activity);
                }

                // create port mappings for connections
                for (Connection.User activityUser : con.getUsers()) {

                    PortMapper pM = new PortMapper(activity.getConfiguration().getOperatorName(),
                            con.getProducesProperty().getPropertyType(),
                            activityUser.getUsesProperty().getPropertyType(),
                            con.getProducesProperty().getPropertyName(),
                            fileLocation,
                            activityUser.getOperator().getConfiguration().getOperatorName(),
                            activityUser.getUsesProperty().getPropertyName());

                    if (portMapping.containsKey(activity.getConfiguration().getOperatorName())) {
                        portMapping.get(activity.getConfiguration().getOperatorName()).add(pM);
                    }
                    else {
                        portMapping.put(activity.getConfiguration().getOperatorName(), new HashSet<PortMapper>());
                        portMapping.get(activity.getConfiguration().getOperatorName()).add(pM);
                    }

                    logger.info("Setting producer to user on : " + fileLocation +
                            " to " + activity.getConfiguration().getOperatorName() + " --> "
                            + activityUser.getOperator().getConfiguration().getOperatorName());

                    producerToUser.get(activity).add(activityUser.getOperator());
                }

            }

        }
        
        // now go through the operators, create them and link them up.
        logger.info("Starting to build T2Flow");
        for (String opId : rmActivity.keySet()) {

            RapidMinerExampleActivity activity = rmActivity.get(opId);
            logger.info("Processing: OPid=" + opId  + " op name=" + activity.getConfiguration().getOperatorName());
            if (processors.get(opId) == null) {

                Processor processor = createProcessor(activity);
                processors.put(opId, processor);

            }

            Processor producer = processors.get(opId);

            for (RapidMinerExampleActivity userActivity : producerToUser.get(activity)) {

                logger.info(activity.getConfiguration().getOperatorName() + " uses " + userActivity.getConfiguration().getOperatorName());

                String userId = userActivity.getConfiguration().getOperatorName();
                if (!processors.containsKey(userId)) {
                    Processor user = createProcessor(userActivity);
                    processors.put(userId, user);


                }


                LinkedHashMap<String, IOOutputPort> outputPortsMap = activity.getConfiguration().getOutputPorts();
                LinkedHashMap<String, IOInputPort> inputPortsMap = userActivity.getConfiguration().getInputPorts();

                try {
                    connectProcessors (producer, outputPortsMap, processors.get(userId), inputPortsMap);
                } catch (EditException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        }        
        
        return null; // CHANGE
    }

    private List<OperatorApplication> processOperatorApplication(OperatorApplication opAp) {
        
    	List<OperatorApplication> basicOpApps = new ArrayList<OperatorApplication>();
       
    	if (opAp.getOpType().name().equals("BASIC")) {
            basicOpApps.add(opAp);

            logger.info("Type:" + opAp.getOpType().name());
            logger.info("Operator Execution Name: " + opAp.getAnnOperatorName());

            logger.info("id:" + opAp.getOperatorID());
            logger.info("name:" + opAp.getOperatorName());
            logger.info("Type name " + opAp.getOperatorTypeName());

            for (SimpleParameter sparams : opAp.getSimpleParameters()) {
                logger.info("simple:" + sparams.getDataPropertyID());
                logger.info("simple:" + sparams.getDataPropertyName());
                logger.info("simple:" + sparams.getDataType());
                logger.info("simple:" + sparams.getValue());
            }

            for (IOObjectDescription desc: opAp.getProducedObject()) {
                logger.info("Produces id: " + desc.getIOObjectID());
                logger.info("Produces type id: " + desc.getIOObjectTypeID());
                logger.info("Produces name: " + desc.getIOObjectName());
                logger.info("Produces location: " + desc.getIOOLocation());
                logger.info("Produces type: " + desc.getIOObjectType());
                logger.info("Produces role: " + desc.getRoleName());
            
            }

            for (IOObjectDescription desc: opAp.getUsedObject()) {
                logger.info("Uses id: " + desc.getIOObjectID());
                logger.info("Uses type id: " + desc.getIOObjectTypeID());
                logger.info("Uses name: " + desc.getIOObjectName());
                logger.info("Uses location: " + desc.getIOOLocation());
                logger.info("Uses type: " + desc.getIOObjectType());
                logger.info("Uses role: " + desc.getRoleName());
            }

        }
        else {
            for (OperatorApplication steps : opAp.getSteps()) {
                basicOpApps.addAll(processOperatorApplication(steps));
            }
        }
        return basicOpApps;
    }

    public void processOperator (OperatorApplication opApp) throws ActivityConfigurationException {

    	// Set activity bean configurations
        RapidMinerActivityConfigurationBean bean = new RapidMinerActivityConfigurationBean();

        bean.setIsParametersConfigured(true);
        bean.setOperatorName(opApp.getOperatorName());
        bean.setCallName(opApp.getAnnOperatorName());
        bean.setHasDescriptions(false);
   
        List<RapidMinerParameterDescription> paramDescription = new ArrayList<RapidMinerParameterDescription>();

        // Set parameters
        for (SimpleParameter sparams : opApp.getSimpleParameters()) {

            RapidMinerParameterDescription p = new RapidMinerParameterDescription();

            String name = sparams.getDataPropertyName();

            logger.info("parameter: " + name + " -> " + sparams.getValue());

            if (name.startsWith("simpleParameter_")) {
                name = name.substring("simpleParameter_".length());
                p.setParameterName(name);
                p.setExecutionValue(sparams.getValue());
                p.setUseParameter(true);
            }

            paramDescription.add(p);
        }


        bean.setParameterDescriptions(paramDescription);

        // todo sort this out for main taverna execution CHANGE
	        RapidAnalyticsPreferences prefs = new RapidAnalyticsPreferences();
	        prefs.setRepositoryLocation("http://rpc295.cs.man.ac.uk:8081");
	        prefs.setUsername("jupp");
	        prefs.setPassword("jupppwd");
                
        RapidMinerExampleActivity activity = new RapidMinerExampleActivity(prefs);
        RapidMinerIOODescription portDescription = new RapidMinerIOODescription(prefs, opApp.getAnnOperatorName());

        bean.setInputPorts(portDescription.getInputPort());
        bean.setOutputPorts(portDescription.getOutputPort());

        activity.configure(bean);
        
        // find uses IOobject, this needs to be turned into a location if it doesn't have one
        // store what it uses as the operator that produces what it uses will be connected
        // to this operator from it's outputLocation to the inputLocation

        for (IOObjectDescription ioobject : opApp.getUsedObject()) {

        	//log("Used: "+ioobject.getRoleName()+", "+ioobject.getIOObjectID());
            Connection connection = getConnectionByIOObjectId(ioobject);

            connection.addUser(activity, ioobject);
            connection.setLocation(ioobject.getIOOLocation());

        }

        for (IOObjectDescription ioobject : opApp.getProducedObject()) {

        	//log("Used: "+ioobject.getRoleName()+", "+ioobject.getIOObjectID());
            Connection connection = getConnectionByIOObjectId(ioobject);
            connection.setProducer(activity, ioobject);

        }

        rmActivity.put(opApp.getOperatorName(), activity);

    }

    private Connection getConnectionByIOObjectId(IOObjectDescription ioobject) {
		
    	final String id = ioobject.getIOObjectID();
		Connection result = connectionsByIOObjectId.get(id);
		if (result == null) {
			result = new Connection(id);
			connectionsByIOObjectId.put(id, result);
		}
		return result;
	}
    
    public Processor createProcessor (RapidMinerExampleActivity activity) {

        Processor opProcessor = edits.createProcessor(activity.getConfiguration().getOperatorName());
        try {
            Edit e1 = edits.getAddProcessorEdit(dataFlow, opProcessor);
            e1.doEdit();

            Edit e2 = edits.getAddActivityEdit(opProcessor, activity);
            e2.doEdit();

            Edit e3 = edits.getDefaultDispatchStackEdit(opProcessor);
            e3.doEdit();

        } catch (EditException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        logger.info(opProcessor.getLocalName() + " processor created");

        return opProcessor;

    }    

    public void connectProcessors(Processor producer, LinkedHashMap<String, IOOutputPort> outputPortsMap, Processor user, LinkedHashMap<String, IOInputPort> inputPortsMap) throws EditException {

        logger.info("Connecting Processors: " + producer.getLocalName() + " --> " + user.getLocalName());

        for (PortMapper p  : portMapping.get(producer.getLocalName())) {

            if (p.getUserName().equals(user.getLocalName())) {


                for (String producer_outputPort : outputPortsMap.keySet()) {

                    if (outputPortsMap.get(producer_outputPort).getFileLocationAt(0) == null ||
                            outputPortsMap.get(producer_outputPort).getFileLocationAt(0).trim().isEmpty()) {
                        continue;

                    }
                    ActivityOutputPort out = getOutputPort(producer, producer_outputPort);

                    
                    // get the port class
                    String outportClass = outputPortsMap.get(producer_outputPort).getPortClass();

                    for (String inputKey : inputPortsMap.keySet()) {

                        ActivityInputPort ip = getInputPort(user, inputKey);
                        String inportClass  = inputPortsMap.get(inputKey).getPortClass();

                        logger.info("PORT CLASS : OUTPORT : " + outportClass + " INPORT : " + inportClass);
                        
                        if (!out.getName().equals("original")) {

                            if (p.getProducerProperty().equals("produces") && p.getUserProperty().equals("uses")) {
                                if (outportClass.equals(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("produces") && p.getUserProperty().equals("usesData")) {
                                if (outportClass.equals(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("produces") && p.getUserProperty().equals("usesAW")) {
                                if (outportClass.equals(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (outportClass.endsWith(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("produces") && p.getUserProperty().equals("usesModel")) {
                                if (outportClass.equals(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (producer_outputPort.startsWith("model") && inputKey.contains("model")) {
                                   // logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());

                                	Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("produces") && p.getUserProperty().equals("usesFirstModel")) {
                                if (outportClass.equals(inportClass)  && ip.getName().endsWith("1")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (producer_outputPort.startsWith("model") && ip.getName().endsWith("1")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("produces") && p.getUserProperty().equals("usesSecondModel")) {
                                if (outportClass.equals(inportClass) && ip.getName().endsWith("2")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (producer_outputPort.startsWith("model") && ip.getName().endsWith("2")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("producesData") && p.getUserProperty().equals("uses")) {
                                if (outportClass.equals(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("producesData") && p.getUserProperty().equals("usesData")) {
                                if (outportClass.equals(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("producesPrePropModel") && p.getUserProperty().equals("uses")) {
                                if (outportClass.equals(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("producesPrePropModel") && p.getUserProperty().equals("usesModel")) {
                                if (outportClass.equals(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (outportClass.endsWith(inportClass)) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("producesPrePropModel") && p.getUserProperty().equals("usesFirstModel")) {
                                if (outportClass.equals(inportClass) && ip.getName().endsWith("1")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (outportClass.endsWith(inportClass) && ip.getName().endsWith("1")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (outportClass.contains(inportClass) && ip.getName().endsWith("1")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }
                            else if (p.getProducerProperty().equals("producesPrePropModel") && p.getUserProperty().equals("usesSecondModel")) {
                                if (outportClass.equals(inportClass) && ip.getName().endsWith("2")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (outportClass.endsWith(inportClass) && ip.getName().endsWith("2")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }
                                else if (outportClass.contains(inportClass) && ip.getName().endsWith("2")) {
                                    logger.info("Creating data link between: " + out.getName() + " --> " + ip.getName());
                                    Edit e4 = Tools.getCreateAndConnectDatalinkEdit(dataFlow, out, ip);
                                    e4.doEdit();
                                }

                            }

                        }

                    }
                }
            }
        }
    }

    public ActivityOutputPort getOutputPort (Processor p, String name) {

    	name += 0;
    	
        for (Activity a : p.getActivityList()) {
        	System.out.println(" activity -> " + a.toString());
            if (a instanceof RapidMinerExampleActivity) {

                for (Object outPort : a.getOutputPorts()) {
                	System.out.println(" 		outPort -> " + outPort.toString() + " , " + name);

                    ActivityOutputPort x = (ActivityOutputPort) outPort;
                    if (x.getName().equals(name)) {
                        return x;
                    }
                }

            }
        }
        return null;
    }

    public ActivityInputPort getInputPort (Processor p, String name) {
    	
    	name += 0;
    	
        for (Activity a : p.getActivityList()) {
            for (Object inPort : a.getInputPorts()) {
                ActivityInputPort x = (ActivityInputPort) inPort;
                if (x.getName().equals(name)) {
                    return x;
                }
            }
        }
        return null;
    }
}
