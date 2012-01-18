package idaservicetype.idaservicetype.ui.converter;

import idaservicetype.idaservicetype.ui.converter.PortMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.IOInputPort;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.IOOutputPort;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerIOODescription;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerParameterDescription;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidMinerPluginConfiguration;

import ch.uzh.ifi.ddis.ida.api.IOObjectDescription;
import ch.uzh.ifi.ddis.ida.api.OperatorApplication;
import ch.uzh.ifi.ddis.ida.api.Parameter;
import ch.uzh.ifi.ddis.ida.api.Plan;
import ch.uzh.ifi.ddis.ida.api.SimpleParameter;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
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

public class MyDataFlowGenerator {
	
    private Dataflow df;
    
    private Plan plan;
    
    private Edits edits;
    
    private Map<String,Connection> connectionsByIOObjectId = new HashMap<String, Connection>();

    private Map<String, RapidMinerExampleActivity> rmActivities = new HashMap<String, RapidMinerExampleActivity>();

    private String workingLocation = "/demo/tempfiles/test/";

    private Map<RapidMinerExampleActivity, Set<RapidMinerExampleActivity>> producerToUser = new HashMap<RapidMinerExampleActivity, Set<RapidMinerExampleActivity>>();

    private Set<RapidMinerExampleActivity> terminatingOperators = new HashSet<RapidMinerExampleActivity>();
    
    private Map<String, Set<PortMapper>> portMapping = new HashMap<String, Set<PortMapper>>();
    
    private Map<String, Processor> processors = new HashMap<String, Processor>();

	private RapidAnalyticsPreferences preferences;

	private UsernamePassword username_password;
    
    private static final Logger logger = LoggerFactory.getLogger(DataFlowGenerator.class);
    
	public MyDataFlowGenerator(){
        this.edits = new EditsImpl();
        this.df = edits.createDataflow();
	}
	
    public Dataflow getDataFlow (Plan p) {
    	
    	setPlan(p);
    	// Get the basic operatorApps
    	List<OperatorApplication> basicOpApps = new ArrayList<OperatorApplication>();
    	
    	for (OperatorApplication opApp : p.getOperatorApplications()) {
    		basicOpApps.addAll(processOperatorApplication(opApp));
    	}
    	
    	// Create corresponding activities
    	for (OperatorApplication opApp : basicOpApps) {
    		try {
				createActivities(opApp);
			} catch (ActivityConfigurationException e) {
				e.printStackTrace();
			}
    	}
    	
    	// For each connection
    	for (String IOObjectID : connectionsByIOObjectId.keySet()) {
    		
            logger.info("processing IOObject: " + IOObjectID);
            Connection connection = connectionsByIOObjectId.get(IOObjectID);

            String fileLocation = connection.getLocation();

            if (fileLocation == null) {
                fileLocation = workingLocation + connection.getIoObjectId();
            }
            
            logger.info("file location: " + fileLocation);
    		
            // if there is no producer then it is an input file
            if (connection.getProducer() == null) {
            	
            	logger.info("No producer for: " + IOObjectID + " loc:" + fileLocation);
            	            	
            	for (Connection.User activityUser : connection.getUsers()) {
            		
            		// set the location for all the user activities
            		RapidMinerExampleActivity activity = rmActivities.get(activityUser.getOperator().getConfiguration().getOperatorName());

                    Connection.DMWFProperty dmwfProperty = activityUser.getUsesProperty();

                    String dmwfPropertyName = dmwfProperty.getPropertyName();
                    String dmwfPropertyType = dmwfProperty.getPropertyType();

                    // get the activity ports on the input ports of the incoming activity
                    LinkedHashMap<String, IOInputPort> ioObjectDescription = activity.getConfiguration().getInputPorts();
                
                    for (String key : ioObjectDescription.keySet()) {
                    	
                    	String portName = ioObjectDescription.get(key).getPortName();
                        String portClass = ioObjectDescription.get(key).getPortClass();
                    	String portType = ioObjectDescription.get(key).getPortType();
                      
                        logger.info("DMWF property name: " + dmwfPropertyName + " dmwf property type: " + dmwfPropertyType + " updated: " + dmwfPropertyName.replace(" ", "_"));
                        logger.info("IOObjectDescription port name: " + portName + " port class " + portClass + " port type " + portType);
                    
                        
                        String prop = dmwfPropertyName.replace(" ", "_");
                        logger.info(" prop " + prop + " portname " + portName);
                       	String val = prop.substring(1, prop.length() - 1);
                       	
                    	if (val.equals(portName)) {
                    		
                    		ioObjectDescription.get(key).setFileLocationAt(0, fileLocation);
                    		logger.info("Setting input location:" + fileLocation + " uses by " + activity.getConfiguration().getOperatorName() +
                                    " on port " + key);
                    		
                    	}

                    }	// end of looping throught IOObjectDescriptions
                    
                    // update the configuration with the updated file locations to the activities configuration bean
                    activity.getConfiguration().setInputPorts(ioObjectDescription);
                    
            	}	// end of looping through connection Users
            	
            } 	
            else {
                
            	// it does have a produces, set the file location on the producer
            	logger.info("producer found for: " + IOObjectID + " loc:" + fileLocation);

            	RapidMinerExampleActivity activity = rmActivities.get(connection.getProducer().getConfiguration().getOperatorName());

                LinkedHashMap<String, IOOutputPort> ioOutputPorts = activity.getConfiguration().getOutputPorts();
                
                for (String key : ioOutputPorts.keySet()) {
                	
                
                    // go the producer output ports and set the file location
                    String portName = ioOutputPorts.get(key).getPortName();
                    String portClass = ioOutputPorts.get(key).getPortClass();
                   
                    Connection.DMWFProperty dmwfProperty = connection.getProducesProperty();
                                                            
                    logger.debug("looping through port: " + key + " (" + portName + ":" + portClass +")");
                    logger.debug("dmwfProperty: " + dmwfProperty.getPropertyName() + " (" + dmwfProperty.getPropertyType() + ")");
                 
                    if (!key.equals("original")) {		// ignore the original port as it's just the original input (look at RapidMiner)
                    	
                    	// first, find out how many producers are set
                    	
                    	// if there is just one producer then MATCH the class and set it
                    	if (dmwfProperty.getPropertyName() == null) {
        
                    		if (portClass.equals("IOObject")) {
                    			
                    			logger.info(" MATCH MATCH MATCH ");
                    			//ioOutputPorts.get(key).getFileLocations().add(fileLocation);

                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " producesData " + activity.getConfiguration().getOperatorName() +
                                                                        " on port " + key);
                    		}
                    		
                    	} else {
                    		
                        	// if there is more than one producer then it should have the RM role set, just match dmwfpropertyname with the portname
                    		String dmwfPropertyName = dmwfProperty.getPropertyName().replace(" ", "_");
                    		dmwfPropertyName = dmwfPropertyName.substring(1, dmwfPropertyName.length() - 1);
                    		
                    		// match them up
                    		if (dmwfPropertyName.equals(portName)) {
                    			
                    			//ioOutputPorts.get(key).getFileLocations().add(fileLocation);
                    		
                                ioOutputPorts.get(key).setFileLocationAt(0, fileLocation);
                                logger.info("Setting output location:" + fileLocation + " producesData " + activity.getConfiguration().getOperatorName() +
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
                
                if (connection.getUsers().isEmpty()) {
                    // connect the output port to the final activity output
                    logger.info("this IO never has a user:" + IOObjectID + " produced by : " + activity.toString());
                    terminatingOperators.add(activity);
                }   
                
                for (Connection.User activityUser : connection.getUsers()) {
                	
                	// create a port mapping object of all activity users
                	PortMapper pM = new PortMapper(activity.getConfiguration().getOperatorName(),
                			connection.getProducesProperty().getPropertyType(),
                            activityUser.getUsesProperty().getPropertyType(),
                            connection.getProducesProperty().getPropertyName(),
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
              
    	}	// end of looping through connections
    	
    	logger.info("Starting to build T2Flow");
        // now go through the operators, create them and link them up.
    	
        for (String operatorID : rmActivities.keySet()) {
        	
        	RapidMinerExampleActivity activity = rmActivities.get(operatorID);
            logger.info("Processing: OPid = " + operatorID  + " op name= " + activity.getConfiguration().getOperatorName());
                        
            if (processors.get(operatorID) == null) {
            	
            	// if the processor does not currently exist, create it and add it
                Processor processor = createProcessor(activity);
                processors.put(operatorID, processor);

            }
            
            Processor producer = processors.get(operatorID);
        	
            // go through the producerToUser mapping for the activity
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
        
        // connect the terminating activities
        for (RapidMinerExampleActivity terminatingActivities :  terminatingOperators) {
            connectFinalActivity(terminatingActivities);
        }
        
    	return df;
    }

    public List<OperatorApplication> processOperatorApplication(OperatorApplication opApp) {
    	
    	// return a list of OperatorApplication
    	List<OperatorApplication> basicOpApps = new ArrayList<OperatorApplication>();
    	
    	if (opApp.getOpType().name().equals("BASIC")) {
    		basicOpApps.add(opApp);

            logger.info("Type:" + opApp.getOpType().name());
            logger.info("Operator Execution Name: " + opApp.getAnnOperatorName());

            logger.info("id:" + opApp.getOperatorID());
            logger.info("name:" + opApp.getOperatorName());
            logger.info("Type name " + opApp.getOperatorTypeName());

            for (SimpleParameter sparams : opApp.getSimpleParameters()) {
                logger.info("simple:" + sparams.getDataPropertyID());
                logger.info("simple:" + sparams.getDataPropertyName());
                logger.info("simple:" + sparams.getDataType());
                logger.info("simple:" + sparams.getValue());
            }

            for (Parameter params : opApp.getParameters()) {
                logger.info("Param" + params.getInstClassID());
                logger.info("Param" + params.getInstID());
                logger.info("Param" + params.getInstName());
                logger.info("Param" + params.getInstTypeName());
                logger.info("Param" + params.getRoleID());
                logger.info("Param" + params.getRoleName());
            }

            for (IOObjectDescription desc: opApp.getProducedObject()) {
                logger.info("Produces id: " + desc.getIOObjectID());
                logger.info("Produces type id: " + desc.getIOObjectTypeID());
                logger.info("Produces name: " + desc.getIOObjectName());
                logger.info("Produces location: " + desc.getIOOLocation());
                logger.info("Produces type: " + desc.getIOObjectType());
                logger.info("Produces role: " + desc.getRoleName());
                logger.info("Produces RM: " + desc.getRMRoleName());
            }

            for (IOObjectDescription desc: opApp.getUsedObject()) {
                logger.info("Uses id: " + desc.getIOObjectID());
                logger.info("Uses type id: " + desc.getIOObjectTypeID());
                logger.info("Uses name: " + desc.getIOObjectName());
                logger.info("Uses location: " + desc.getIOOLocation());
                logger.info("Uses type: " + desc.getIOObjectType());
                logger.info("Uses role: " + desc.getRoleName());
                logger.info("User RM: " + desc.getRMRoleName());
            }

        }
        else {
            for (OperatorApplication steps : opApp.getSteps()) {
                basicOpApps.addAll(processOperatorApplication(steps));
            }
     	}
    	    	
    	return basicOpApps;
    }

    public void createActivities(OperatorApplication opApp) throws ActivityConfigurationException {
    	
    	// Set activity configuration bean
    	RapidMinerActivityConfigurationBean bean = new RapidMinerActivityConfigurationBean();
        bean.setIsParametersConfigured(true);
        bean.setOperatorName(opApp.getOperatorName());
        bean.setCallName(opApp.getAnnOperatorName());
        bean.setHasDescriptions(false);
    
        // Get and set activities parameter descriptions
        List<RapidMinerParameterDescription> paramDescription = new ArrayList<RapidMinerParameterDescription>();
	   
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

        // todo sort this out for main taverna execution	- get from prefs in plugin
        RapidAnalyticsPreferences prefs = new RapidAnalyticsPreferences();
        //prefs.setRepositoryLocation("http://rpc295.cs.man.ac.uk:8081");
        //prefs.setUsername("");
        //prefs.setPassword("");
        sortPreferences();
		prefs = preferences;
		
		
        // Corresponding activity
        RapidMinerExampleActivity activity = new RapidMinerExampleActivity(prefs);
        
        RapidMinerIOODescription portDescription = new RapidMinerIOODescription(prefs, opApp.getAnnOperatorName());
        bean.setInputPorts(portDescription.getInputPort());
        bean.setOutputPorts(portDescription.getOutputPort());
        
        for (String key : bean.getOutputPorts().keySet()) {
        	
        	logger.info(" ACTIVITY " + bean.getOutputPorts().get(key).getPortClass() + " " + bean.getOutputPorts().get(key).getPortName());
        	bean.getOutputPorts().get(key).setNumberOfPorts(1);//debug
        }
                
      for (String key : bean.getInputPorts().keySet()) {
        	
        	logger.info(" ACTIVITY " + bean.getInputPorts().get(key).getPortClass() + " " + bean.getInputPorts().get(key).getPortName());
        	bean.getInputPorts().get(key).setNumberOfPorts(1);//debug
        }
        
        activity.configure(bean);
        
        // find uses IOobject, this needs to be turned into a location if it doesn't have one
        // store what it uses as the operator that produces what it uses will be connected
        // to this operator from it's outputLocation to the inputLocation
        
        for (IOObjectDescription ioobject : opApp.getUsedObject()) {
        	logger.info("Used: "+ioobject.getRoleName()+", "+ioobject.getIOObjectID() 
        			+ ", "+ioobject.getIOOLocation() + ", " + ioobject.getRMRoleName());
            // Create connection map
        	Connection connection = getConnectionByIOObjectId(ioobject);
            // Add activity and ioobject to User
            connection.addUser(activity, ioobject);
            connection.setLocation(ioobject.getIOOLocation());
        }
        
        for (IOObjectDescription ioobject : opApp.getProducedObject()) {
        	logger.info("Produces: "+ioobject.getRoleName()+", "+ioobject.getIOObjectID() 
        			+ ", "+ioobject.getIOOLocation() + ", " + ioobject.getRMRoleName());
        	Connection connection = getConnectionByIOObjectId(ioobject);
        	connection.setProducer(activity, ioobject);

        }

        rmActivities.put(opApp.getOperatorName(), activity);
        
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

    	// create activity corresponding processor
        Processor opProcessor = edits.createProcessor(activity.getConfiguration().getOperatorName());
        try {
            Edit e1 = edits.getAddProcessorEdit(df, opProcessor);
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
        	
        	// if the portmapping username is the processor name
            if (p.getUserName().equals(user.getLocalName())) {
            	
            	// go through the output ports of the producer
            	 for (String producer_outputPort : outputPortsMap.keySet()) {

            		 // if there is no file location set then skip
            		 if (outputPortsMap.get(producer_outputPort).getFileLocationAt(0) == null ||
                             outputPortsMap.get(producer_outputPort).getFileLocationAt(0).trim().isEmpty()) {
                         continue;
                     }
            		 
            		 ActivityOutputPort activityOutputPort = getOutputPort(producer, producer_outputPort);
                     
                     // get the port class and name
                     String outportClass = outputPortsMap.get(producer_outputPort).getPortClass();
                     String outportName = outputPortsMap.get(producer_outputPort).getPortName();
                     
                     // go through the input ports
                     for (String inputKey : inputPortsMap.keySet()) {
                    	 
                    	 ActivityInputPort ip = getInputPort(user, inputKey);
                         String inportClass  = inputPortsMap.get(inputKey).getPortClass();
                    	 String inportName  = inputPortsMap.get(inputKey).getPortName();
                    	 
                    	 if (!activityOutputPort.getName().equals("original")) {	// skip the original port
                    		 logger.info("outport-name : " + outportName + " inport-name : " + inportName);
                    		 logger.info("user-property : " + p.getUserProperty() + " producer-property : " + p.getProducerProperty());
                    		 
                    		 String outputInc = null;
                    		 if (p.getProducerProperty() != null){
                    			 outputInc = p.getProducerProperty().replace(" ", "_");
                              	 outputInc = outputInc.substring(1, outputInc.length() - 1);
                    		 }
               
                    		 
                    		 String inputInc = p.getUserProperty().replace(" ", "_");
                          	 inputInc = inputInc.substring(1, inputInc.length() - 1);
                          	 
                    		 if (inportName.equals(inputInc)) {
                    			
                    			 if ( outportName.equals(outputInc)) {
                       				 
                        			 logger.info("Creating data link between ports: " + activityOutputPort.getName() + " --> " + ip.getName());
                                     Edit e4 = Tools.getCreateAndConnectDatalinkEdit(df, activityOutputPort, ip);
                                     e4.doEdit();
                                     
                    			 } else if (outputInc == null){
                       				 
                        			 logger.info("Creating data link between ports: " + activityOutputPort.getName() + " --> " + ip.getName());
                                     Edit e4 = Tools.getCreateAndConnectDatalinkEdit(df, activityOutputPort, ip);
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

    private void connectFinalActivity(RapidMinerExampleActivity activity) {
        String activityName = activity.getConfiguration().getOperatorName();
        logger.info("Final Processors: " + activityName);
        int rand = (int)(Math.random() * 9999)+1000;
        for (Processor p : df.getProcessors()) {
            if (p.getLocalName().equals(activityName)) {
                for (String outP : activity.getConfiguration().getOutputPorts().keySet()) {

                    if (!outP.equals("original")) {
                        if (activity.getConfiguration().getOutputPorts().get(outP).getFileLocationAt(0) != null) {

                            ActivityOutputPort out = getOutputPort(p, outP);
                            String name = Tools.uniqueProcessorName(outP + "_" + activityName + "_" + rand, df);
                            DataflowOutputPort dfop= edits.createDataflowOutputPort( name , df);
                            try {
                                edits.getAddDataflowOutputPortEdit(df, dfop).doEdit();
                                Tools.getCreateAndConnectDatalinkEdit(df, out, dfop.getInternalInputPort()).doEdit();
                            } catch (EditException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void sortPreferences() {
        
		preferences = getPreferences();
        if (preferences != null) {
            CredentialManager credManager;
            try {
                credManager = CredentialManager.getInstance();
                username_password = credManager.getUsernameAndPasswordForService(URI.create(preferences.getBrowserServiceLocation()), true, null);

                preferences.setUsername(username_password.getUsername());
                preferences.setPassword(username_password.getPasswordAsString());
            } catch (CMException e) {
                e.printStackTrace();

            }
        }
        else {
            JOptionPane.showMessageDialog(new JFrame(),
                    new JLabel("<html>Please set the Rapid Analytics repository location <br> " +
                            " and flora location in the preferences panel</html>"));
        }
		
	}
	
    private RapidAnalyticsPreferences getPreferences() {

        RapidMinerPluginConfiguration config = RapidMinerPluginConfiguration.getInstance();
        String repos = config.getProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION);
        System.err.println("Got repository location: " + repos);
        if (repos.equals("")) {
            return null;
        }

        RapidAnalyticsPreferences pref = new RapidAnalyticsPreferences();
        pref.setRepositoryLocation(repos);
        return pref;

    }


	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public Plan getPlan() {
		return plan;
	}
}
