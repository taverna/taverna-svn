package uk.ac.manchester.cs.elico.converter;

import ch.uzh.ifi.ddis.ida.api.*;
import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import ch.uzh.ifi.ddis.ida.core.fact.Fact;
import com.rapid_i.elico.DataTableResponse;
import com.rapid_i.elico.MetaDataServicePortBindingStub;
import com.rapid_i.elico.MetaDataService_ServiceLocator;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;
import org.apache.axis.AxisFault;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Simon Jupp<br>
 * Date: Jan 12, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class TestConverter {

    MainGoal mainGoal;

    Task task;

    GoalFactory gFactory;

    OWLOntology ontology;




    public TestConverter () {

        IDAPreferences prefs = new IDAPreferences("/Applications/Unix/flora2",
                "/Users/simon/tmp/elico/flora2/");

        

        IDAInterface ida = IDAFactory.getIDA();


		try {

			ida.startPlanner(prefs);
            

            gFactory = ida.createEmptyGoalSpecification();

			// test main goal tree

			Tree<MainGoal> tMainGoal = ida.getMainGoals();
            getGoalChildren(tMainGoal);

            Tree<Task> tTask = ida.getTasks();
            getTaskChildren(tTask);


            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication ("jupp", "jupppwd".toCharArray());
                }
            };

            Authenticator.setDefault(auth);

            MetaDataService_ServiceLocator service = new MetaDataService_ServiceLocator();

            try {
                MetaDataServicePortBindingStub stub = new MetaDataServicePortBindingStub(new URL("http://rpc295.cs.man.ac.uk:8081/e-LICO/MetaDataService"), service);
                stub.setUsername("jupp");
                stub.setPassword("jupppwd");

                DataTableResponse response = stub.getOWLIndividualsFromRepository("http://example.owl", "test1", "/demo/example_data/Iris.ioo");

                System.out.println("Setting main goal: " + mainGoal.getGoalName() );
                String mainGoalID = gFactory.setMainGoal(mainGoal);

                if (response.getErrorMessage() != null) {
                    System.out.println("getting OWL error response: " + response.getErrorMessage());
                }
                else {
                    System.out.println("OWL response: " + response.getOwlXML());
                }
                URI uri = setMetaData(response.getOwlXML());

                System.out.println("URI for data : " + uri.toString());


                for (DataRequirement dr : mainGoal.getDataRequirement()) {
                    System.out.println("Data requirements:" + dr.getClassName() + " role: " + dr.getRoleName());

                    if (dr.getRoleName().equals("trainingData")
                            && dr.getClassName().equals("Data")) {

                        System.out.println("Adding last data requirment");
                        gFactory.addDataRequirement(mainGoalID, dr,
                                uri);
                    }
                }

                System.out.println("using task: " + task.getTaskName());
                gFactory.addUseTask(mainGoalID, task);

                for (Fact f : gFactory.getFacts()) {
                    System.out.println("Fact:" + f.toString() + " type: " + f.getFactType());
                }

                System.err.println("Getting plans");
//
                //ida.getCaseIndex()
                List<Plan> plans = ida.getPlans(task, gFactory.getFacts(), 10);
//
                System.err.println("Got plans");
                int x = 0;
                for (Plan p : plans) {

                    System.out.println("\n\nGot plan rank: " + p.getRank());
                    for (OperatorApplication opAp : p.getOperatorApplications()) {
                        processOperatorApplication(opAp);
                    }

                    DataFlowGenerator dfg = new DataFlowGenerator();
                    Dataflow df = dfg.getDataFlow(p);
                    XMLSerializer serializer = new XMLSerializerImpl();
                    Element serialized = null;
                    try {
                        serialized = serializer.serializeDataflow(df);

                        XMLOutputter outputter = new XMLOutputter();
                        String dfXML = outputter.outputString(serialized);
                        System.out.println("Writing new WF to file..." + "file:/Users/simon/tmp/idatest" + x + ".t2flow");
                        FileWriter fw = new FileWriter(new File(URI.create("file:/Users/simon/tmp/idatest" + x + ".t2flow")));

                        fw.write(dfXML); fw.close();
                    } catch (SerializationException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    
                    x++;

                }


            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (RemoteException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


            ida.shutDownPlanner();


        } catch (IDAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.exit(0);


    }

    private List<OperatorApplication> processOperatorApplication(OperatorApplication opAp) {
        List<OperatorApplication> basicOpApps = new ArrayList<OperatorApplication>();
        int x = 0;
        if (opAp.getOpType().name().equals("BASIC")) {

            basicOpApps.add(opAp);
            System.out.println("Type:" + opAp.getOpType().name());
            System.out.println("Operator Execution Name: " + opAp.getAnnOperatorName());

            System.out.println("id:" + opAp.getOperatorID());
            System.out.println("name:" + opAp.getOperatorName());
            System.out.println("Type name " + opAp.getOperatorTypeName());

            for (SimpleParameter sparams : opAp.getSimpleParameters()) {
                System.out.println("simple:" + sparams.getDataPropertyID());
                System.out.println("simple:" + sparams.getDataPropertyName());
                System.out.println("simple:" + sparams.getDataType());
                System.out.println("simple:" + sparams.getValue());
            }


            for (Parameter params : opAp.getParameters()) {

                System.out.println("Param" + params.getInstClassID());
                System.out.println("Param" + params.getInstID());
                System.out.println("Param" + params.getInstName());
                System.out.println("Param" + params.getInstTypeName());
                System.out.println("Param" + params.getRoleID());
                System.out.println("Param" + params.getRoleName());
            }

            for (IOObjectDescription desc: opAp.getProducedObject()) {

                System.out.println("Produces: " + desc.getIOObjectID());
                System.out.println("Produces: " + desc.getIOOLocation());
                System.out.println("Produces: " + desc.getIOObjectType());
            }

            for (IOObjectDescription desc: opAp.getUsedObject()) {
                System.out.println("Uses id: " + desc.getIOObjectID());
                System.out.println("Uses name: " + desc.getIOObjectName());
                System.out.println("Uses location: " + desc.getIOOLocation());
                System.out.println("Uses type: " + desc.getIOObjectType());
                System.out.println("Uses role: " + desc.getRoleName());
            }

            System.out.println("Generate dataflow:");

        }
        else {
            for (OperatorApplication steps : opAp.getSteps()) {
                basicOpApps.addAll(processOperatorApplication(steps));
            }
        }
        return basicOpApps;
    }

    private URI setMetaData(String OWLxml) {

        

        OntologyStreamReader reader = new OntologyStreamReader(OWLxml);
			OWLOntologyManager ontManager = OWLManager
					.createOWLOntologyManager();
			OWLDataFactory dFactory = ontManager.getOWLDataFactory();

			try {
				ontology = ontManager.loadOntology(reader);

				// adding the class assertions to the fact list
				for (OWLClassAssertionAxiom ax : ontology
						.getAxioms(AxiomType.CLASS_ASSERTION)) {
					if (ax.getDescription().isLiteral()) {
						gFactory.addClassFact(ax.getIndividual().getURI(), ax
								.getDescription().asOWLClass().getURI());
					}

				}

				// adding owl object property assertions to the fact list
				for (OWLObjectPropertyAssertionAxiom ax : ontology
						.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {

					gFactory.addObjectPropertyFact(ax.getSubject().getURI(), ax
							.getProperty().getNamedProperty().getURI(), ax
							.getObject().getURI());
				}

				// adding data property assertions to the fact list
				for (OWLDataPropertyAssertionAxiom ax : ontology
						.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
					if (!ax.getProperty().isAnonymous()) {
						if (ax.getObject().isTyped()) {
							OWLTypedConstant value = ax.getObject()
									.asOWLTypedConstant();
							if (value.getDataType().equals(
									dFactory.getIntegerDataType())) {
								gFactory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), Integer
										.parseInt(value.getLiteral()));
							} else if (value.getDataType().equals(
									dFactory.getDoubleDataType())) {
								gFactory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), Double
										.parseDouble(value.getLiteral()));
							} else if (value.getDataType().equals(
									dFactory.getFloatDataType())) {
								gFactory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), Float
										.parseFloat(value.getLiteral()));
							} else if (value.getDataType().equals(
									dFactory.getBooleanDataType())) {
								gFactory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), Boolean
										.parseBoolean(value.getLiteral()));
							} else {
								gFactory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), value
										.getLiteral());
							}
							// System.out.println(value.getDataType());

						} else {
							OWLUntypedConstant value = ax.getObject()
									.asOWLUntypedConstant();
							gFactory.addDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), value
									.getLiteral());
						}

					}
				}

				// adding negative object property assertions
				for (OWLNegativeObjectPropertyAssertionAxiom ax : ontology
						.getAxioms(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)) {

					gFactory.addNegativeObjectPropertyFact(ax.getSubject()
							.getURI(), ax.getProperty().getNamedProperty()
							.getURI(), ax.getObject().getURI());
				}

				// adding data property assertions to the fact list
				for (OWLNegativeDataPropertyAssertionAxiom ax : ontology
						.getAxioms(AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION)) {
					if (!ax.getProperty().isAnonymous()) {
						OWLTypedConstant value = ax.getObject()
								.asOWLTypedConstant();

						if (value.getDataType().equals(
								dFactory.getIntegerDataType())) {
							gFactory.addNegativeDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), Integer
									.parseInt(value.getLiteral()));
						} else if (value.getDataType().equals(
								dFactory.getDoubleDataType())) {
							gFactory.addNegativeDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), Double
									.parseDouble(value.getLiteral()));
						} else if (value.getDataType().equals(
								dFactory.getFloatDataType())) {
							gFactory.addNegativeDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), Float
									.parseFloat(value.getLiteral()));
						} else if (value.getDataType().equals(
								dFactory.getBooleanDataType())) {
							gFactory.addNegativeDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), Boolean
									.parseBoolean(value.getLiteral()));
						}

					} else {
						OWLUntypedConstant value = ax.getObject()
								.asOWLUntypedConstant();
						gFactory.addNegativeDataPropertyFact(ax.getSubject()
								.getURI(), ax.getProperty().asOWLDataProperty()
								.getURI(), value.getLiteral());
					}

				}


            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        OWLClass cls = getIOObjectClass();

        OWLIndividual ind = getIOObjectInd(cls);

        return ind.getURI();
    }

    private OWLIndividual getIOObjectInd(OWLClass cls) {
		for (OWLIndividual ind : ontology.getReferencedIndividuals()) {
			if (ind.getTypes(ontology).contains(cls))
				return ind;
		}
		return null;
	}

	private OWLClass getIOObjectClass() {
		for (OWLClass cls : ontology.getReferencedClasses()) {
			if (cls.toString().equals("DataTable"))
				return cls;
		}
		return null;
	}

    public void getGoalChildren (Tree<MainGoal> tMainGoal) {

        if (tMainGoal.getData().getGoalName().equals("PredictiveModelling")) {
            mainGoal = tMainGoal.getData();
        }
        System.out.println(tMainGoal.getData().getGoalName());
        for (Tree<MainGoal> child : tMainGoal.getChildren()) {
            getGoalChildren(child);
        }

    }

    public void getTaskChildren (Tree<Task> tTask) {

        if (tTask.getData().getTaskName().equals("DiscretizeAll")) {
            task = tTask.getData();
        }
        System.out.println(tTask.getData().getTaskName());
        for (Tree<Task> child : tTask.getChildren()) {
            getTaskChildren(child);
        }

    }

    public static void main(String[] args) {

        new TestConverter();

    }

}
