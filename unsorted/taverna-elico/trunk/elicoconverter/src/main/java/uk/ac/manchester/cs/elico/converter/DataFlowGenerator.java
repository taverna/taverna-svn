package uk.ac.manchester.cs.elico.converter;

import ch.uzh.ifi.ddis.ida.api.*;
import net.sf.taverna.t2.workflowmodel.*;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerParameterDescription;

import java.util.*;


/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS dFOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Simon Jupp<br>
 * Date: Jan 31, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Take a list of IDA plans and return a T2Flow representation
 */

 public class DataFlowGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DataFlowGenerator.class);

    private Dataflow df;
    private Edits edits;

//    public List<Edit<?>> getEditList() {
//        return editList;
//    }

//    private List<Edit<?>> editList = new ArrayList<Edit<?>>();

    private String workingLocation = "/demo/tempfiles/";

    private Map<String,Connection> connectionsByIOObjectId = new HashMap<String, Connection>();

    private Map<String, RapidMinerExampleActivity> rmActivity = new HashMap<String, RapidMinerExampleActivity>();

    private Map<String, Processor> processors = new HashMap<String, Processor>();

    private Map<RapidMinerExampleActivity, Set<RapidMinerExampleActivity>> producerToUser = new HashMap<RapidMinerExampleActivity, Set<RapidMinerExampleActivity>>();

    private Set<RapidMinerExampleActivity> terminatingOperators = new HashSet<RapidMinerExampleActivity>();

//    private ServiceDescription sd;

    public DataFlowGenerator() {

        this.edits = new EditsImpl();
        this.df = edits.createDataflow();
//        this.sd  = DataflowTemplateService.getServiceDescription();


    }

//    public DataFlowGenerator(Edits edits, Dataflow df) {
//        this.edits = edits;
//        this.df = df;
//        this.sd  = DataflowTemplateService.getServiceDescription();
//
//    }




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


            for (Parameter params : opAp.getParameters()) {

                logger.info("Param" + params.getInstClassID());
                logger.info("Param" + params.getInstID());
                logger.info("Param" + params.getInstName());
                logger.info("Param" + params.getInstTypeName());
                logger.info("Param" + params.getRoleID());
                logger.info("Param" + params.getRoleName());
            }

            for (IOObjectDescription desc: opAp.getProducedObject()) {

                logger.info("Produces: " + desc.getIOObjectID());
                logger.info("Produces: " + desc.getIOOLocation());
                logger.info("Produces: " + desc.getIOObjectType());
            }

            for (IOObjectDescription desc: opAp.getUsedObject()) {
                logger.info("Uses id: " + desc.getIOObjectID());
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


    public Dataflow getDataFlow (Plan p) {

        // get the basic types

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

                for (Connection.User activityUser : con.getUsers()) {
                    RapidMinerExampleActivity activity = rmActivity.get(activityUser.getOperator().getConfiguration().getOperatorName());
                    logger.info("Setting input location on " + activity.getConfiguration().getOperatorName() + " to " + fileLocation );
//                    activity.getConfiguration().setInputLocation(fileLocation);
                }
            }
            else {

                RapidMinerExampleActivity activity = rmActivity.get(con.getProducer().getConfiguration().getOperatorName());
//                activity.getConfiguration().setOutputLocation(fileLocation);
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

                for (Connection.User activityUser : con.getUsers()) {
                    logger.info("Setting producer to user on : " + IOObjectID +
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



                try {
                    connectProcessors (producer, processors.get(userId));
                } catch (EditException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }


            // handle the final output activity
            for (RapidMinerExampleActivity terminatingActivities :  terminatingOperators) {
                connectFinalActivity(terminatingActivities);
            }



        }

        return df;

    }

    private void connectFinalActivity(RapidMinerExampleActivity activity) {
        String activityName = activity.getConfiguration().getOperatorName();
        logger.info("Final Processors: " + activityName);
        for (Processor p : df.getProcessors()) {
            if (p.getLocalName().equals(activityName)) {
                ActivityOutputPort out = getOutputPort(p, "outputLocation");
                DataflowOutputPort dfop= edits.createDataflowOutputPort("out_" + activityName, df);
                try {
                    edits.getAddDataflowOutputPortEdit(df, dfop).doEdit();
                    Tools.getCreateAndConnectDatalinkEdit(df, out, dfop.getInternalInputPort()).doEdit();
                } catch (EditException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


            }
        }


    }


    public void connectProcessors(Processor producer, Processor user) throws EditException {

        logger.info("Connecting Processors: " + producer.getLocalName() + " --> " + user.getLocalName());

        ActivityOutputPort out = getOutputPort(producer, "outputLocation");
        ActivityInputPort in = getInputPort(user, "inputLocation");

        logger.info("Creating data link between: " + out.getName() + " --> " + in.getName());
        Edit e4 = Tools.getCreateAndConnectDatalinkEdit(df, out, in);
        e4.doEdit();
//        editList.add(e4);


    }

    public ActivityOutputPort getOutputPort (Processor p, String name) {

        for (Activity a : p.getActivityList()) {
            for (Object outPort : a.getOutputPorts()) {
                ActivityOutputPort x = (ActivityOutputPort) outPort;
                if (x.getName().equals(name)) {
                    return x;
                }
            }
        }
        return null;
//        return new NoOutputPortException();
    }

    public ActivityInputPort getInputPort (Processor p, String name) {

        for (Activity a : p.getActivityList()) {
            for (Object inPort : a.getInputPorts()) {
                ActivityInputPort x = (ActivityInputPort) inPort;
                if (x.getName().equals(name)) {
                    return x;
                }
            }
        }
        return null;
//        return new NoOutputPortException();
    }


    public Processor createProcessor (RapidMinerExampleActivity activity) {

        Processor opProcessor = edits.createProcessor(activity.getConfiguration().getOperatorName());
        try {
            Edit e1 = edits.getAddProcessorEdit(df, opProcessor);
            e1.doEdit();
//            editList.add(e1);

            Edit e2 = edits.getAddActivityEdit(opProcessor, activity);
            e2.doEdit();
//            editList.add(e2);

            Edit e3 = edits.getDefaultDispatchStackEdit(opProcessor);
            e3.doEdit();
//            editList.add(e3);



        } catch (EditException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        logger.info(opProcessor.getLocalName() + " processor created");

        return opProcessor;

    }

    public void processOperator (OperatorApplication opApp) throws ActivityConfigurationException {

        RapidMinerActivityConfigurationBean bean = new RapidMinerActivityConfigurationBean();

        bean.setIsParametersConfigured(true);
        bean.setOperatorName(opApp.getOperatorName());
        bean.setCallName(opApp.getAnnOperatorName());
//        bean.setIsExplicit(true);
        bean.setHasDescriptions(false);

        List<RapidMinerParameterDescription> paramDescription = new ArrayList<RapidMinerParameterDescription>();

        // set parameters
//        for (Parameter p : opApp.getParameters()) {
//        }

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

//        RapidAnalyticsPreferences prefs = new RapidAnalyticsPreferences();
//        prefs.setRepositoryLocation("http://rpc295.cs.man.ac.uk:8081");
//        prefs.setUsername("jupp");
//        prefs.setPassword("jupppwd");
//        RapidMinerExampleActivity activity = new RapidMinerExampleActivity(prefs);


        RapidMinerExampleActivity activity = new RapidMinerExampleActivity();


//        editList.add(edits.getConfigureActivityEdit(activity, bean));
        activity.configure(bean);

        // find uses IOobject, this needs to be turned into a location if it doesn't have one
        // store what it uses as the operator that produces what it uses will be connected
        // to this operator from it's outputLocation to the inputLocation

        for (IOObjectDescription ioobject : opApp.getUsedObject()) {

//				log("Used: "+ioobject.getRoleName()+", "+ioobject.getIOObjectID());
            Connection connection = getConnectionByIOObjectId(ioobject);
            connection.addUser(activity, ioobject.getRoleName());
            connection.setLocation(ioobject.getIOOLocation());

        }

        for (IOObjectDescription ioobject : opApp.getProducedObject()) {

//				log("Used: "+ioobject.getRoleName()+", "+ioobject.getIOObjectID());
            Connection connection = getConnectionByIOObjectId(ioobject);
            connection.setProducer(activity, ioobject.getIOOLocation());

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

}
