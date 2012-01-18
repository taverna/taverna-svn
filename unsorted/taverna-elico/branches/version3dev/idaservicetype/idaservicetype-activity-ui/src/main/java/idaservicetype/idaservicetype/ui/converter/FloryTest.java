package idaservicetype.idaservicetype.ui.converter;

import ch.uzh.ifi.ddis.ida.api.*;
import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import com.rapid_i.elico.DataTableResponse;
import com.rapid_i.elico.MetaDataServicePortBindingStub;
import com.rapid_i.elico.MetaDataService_ServiceLocator;
import org.apache.axis.AxisFault;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;/*
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
 * Date: Jan 14, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class FloryTest {

	private static final Logger logger = LoggerFactory.getLogger(FloryTest.class);

	private static final String URL = "http://rapid-i.dyndns.org:8080";

	private IDAInterface ida;

	private GoalFactory factory;

    MetaDataServicePortBindingStub stub;

    MetaDataService_ServiceLocator service;

	private OWLOntology ontology;

	public FloryTest() {
		try {
			ida = IDAFactory.getIDA();
			IDAPreferences prefs = new IDAPreferences(
					"/Applications/Unix/flora2",
					"/Users/simon/tmp/elico/flora2");

			// prefs.setBasicLocalConfiguration("/Users/floareaserban/reasoning/flora2",
			// "/Users/floareaserban/tmp/test");
			ida.startPlanner(prefs);

			factory = ida.createEmptyGoalSpecification();

            service = new MetaDataService_ServiceLocator();
            stub = new MetaDataServicePortBindingStub(new URL("http://rpc295.cs.man.ac.uk:8081/e-LICO/MetaDataService"), service);
            stub.setUsername("jupp");
            stub.setPassword("jupppwd");

//			Authenticator.setDefault(new Authenticator() {
//				@Override
//				protected PasswordAuthentication getPasswordAuthentication() {
//					return new PasswordAuthentication("jupp", "florypwd"
//							.toCharArray());
//				}
//			});
//
//			MetaDataService_Service repo;
//			try {
//				repo = new MetaDataService_Service(new URL(URL
//						+ "/e-LICO/MetaDataService?wsdl"), new QName(
//						"http://elico.rapid_i.com/", "MetaDataService"));
//				port = repo.getMetaDataServicePort();
//
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		} catch (IDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getCause().getMessage());
		} catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

	public void retrievePlans() {

		try {
			Tree<MainGoal> mgoals = ida.getMainGoals();
//			logger.info("children " + mgoals.getNumberOfChildren());
			if (mgoals != null)
				if (mgoals.hasChildren()) {
					MainGoal g = mgoals.getChildAt(0).getData();
//					logger.info("main goal "
//							+ mgoals.getChildAt(0).getNumberOfChildren());
					if (g != null) {
//						logger.info(g.getGoalName());

						String mainGoalID = factory.setMainGoal(g);

//						logger.info("before setting metadata");
						// call metadata service
						URI indURI = setMetaData();

						// add the ioobject to the goal
						if (indURI != null)
							for (DataRequirement dr : g.getDataRequirement()) {
//								logger.info(dr.getRoleName() + " "
//										+ dr.getPortClass() + " " + dr.getID());
								if (dr.getRoleName().equals("trainingData")
										&& dr.getClassName().equals("Data")) {

//									logger.info(dr.getRoleName());
									factory.addDataRequirement(mainGoalID, dr,
											indURI);
								}
							}

						Tree<Task> tasks = ida.getTasks();
						String taskID = null;
						Task t = null;
						if (tasks != null) {
//							logger.info("task not null "
//									+ tasks.getData().getTaskName() + " "
//									+ tasks.getNumberOfChildren());
							if (tasks.hasChildren()) {
//								logger.info(tasks.getData().getTaskName());
								for (Tree<Task> c : tasks.getChildren()) {
//									logger.info(c.getData().getTaskName());
									if (c.getData().getTaskName().equals(
											"DataMining")) {
										t = c.getData();
//										logger.info(t.getTaskName());
										break;
									}
								}

								// add the task that solves the maingoal
								taskID = factory.addUseTask(mainGoalID, t);

							}
						}

//						logger.info("before calling plans");
						// call the planner and retrieve plans
						if (t != null && taskID != null) {
							long itime = System.currentTimeMillis();
							List<Plan> plans = ida.getPlans(t, factory
									.getFacts(), 5);

							long ftime = System.currentTimeMillis();
//							logger.info("total time " + (ftime - itime) + " "
//									+ plans.size());

							for (Plan p : plans) {
//								logger.info("plan size="
//										+ p.getOperatorApplications().size());
								List<OperatorApplication> oaps = p
										.getOperatorApplications();
								for (OperatorApplication oap : oaps) {

//									logger.info("op " + oap.getOperatorID()
//											+ " " + oap.getOperatorName() + " "
//											+ oap.getOperatorTypeName() + " "
//											+ oap.getOperatorTypeID() + " "
//											+ oap.getAnnOperatorName());
//									logger.info("uses "
//											+ oap.getUsedObject().size());
//									logger.info("used objects");
									for (IOObjectDescription iod : oap
											.getUsedObject()) {
										printIOObjectDescription(iod);
									}
//									logger.info("produces "
//											+ oap.getProducedObject().size());
									for (IOObjectDescription iod : oap
											.getProducedObject()) {
										printIOObjectDescription(iod);
									}

//									logger.info("params "
//											+ oap.getParameters().size());
									for (Parameter param : oap.getParameters()) {
										printParameter(param);
									}
//									logger.info("simple params "
//											+ oap.getSimpleParameters().size());
									for (SimpleParameter sParam : oap
											.getSimpleParameters()) {
										printSimpleParameter(sParam);
									}

								}
								break;
							}

						}

					}

				}
			ida.shutDownPlanner();
		} catch (IDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void retrieveCases() {

		try {
			Tree<MainGoal> mgoals = ida.getMainGoals();
//			logger.info("children " + mgoals.getNumberOfChildren());
			if (mgoals != null)
				if (mgoals.hasChildren()) {
					MainGoal g = mgoals.getChildAt(0).getData();
//					logger.info("main goal "
//							+ mgoals.getChildAt(0).getNumberOfChildren());
					if (g != null) {
//						logger.info(g.getGoalName());

						String mainGoalID = factory.setMainGoal(g);

//						logger.info("before setting metadata");
						// call metadata service
						URI indURI = setMetaData();

						// add the ioobject to the goal
						if (indURI != null)
							for (DataRequirement dr : g.getDataRequirement()) {
//								logger.info(dr.getRoleName() + " "
//										+ dr.getPortClass() + " " + dr.getID());
								if (dr.getRoleName().equals("trainingData")
										&& dr.getClassName().equals("Data")) {

//									logger.info(dr.getRoleName());
									factory.addDataRequirement(mainGoalID, dr,
											indURI);
								}
							}

						Tree<Task> tasks = ida.getTasks();
						String taskID = null;
						Task t = null;
						if (tasks != null) {
//							logger.info("task not null "
//									+ tasks.getData().getTaskName() + " "
//									+ tasks.getNumberOfChildren());
							if (tasks.hasChildren()) {
//								logger.info(tasks.getData().getTaskName());
								for (Tree<Task> c : tasks.getChildren()) {
//									logger.info(c.getData().getTaskName());
									if (c.getData().getTaskName().equals(
											"DataMining")) {
										t = c.getData();
//										logger.info(t.getTaskName());
										break;
									}
								}

								// add the task that solves the maingoal
								taskID = factory.addUseTask(mainGoalID, t);

							}
						}

//						logger.info("before calling plans");
						// call the planner and retrieve plans
						if (t != null && taskID != null) {

//							 logger.info("before getting case");
							 List<WeightedFeatureValue> features = ida
							 .getCaseIndex(t, factory.getFacts());
							 for (int i = 0; i < features.size(); i++) {
							 WeightedFeatureValue wfv = features.get(i);
							 try {
//							 logger.info("feature " + wfv.getFeatureName()
//							 + " " + wfv.getFeatureWeight() + " "
//							 + wfv.getFeatureValueAsString() + " "
//							 + wfv.getFeatureValueAsInt() + " "
//							 + wfv.getFeatureValueAsDouble());
							 } catch(NumberFormatException e) {

							 }

							 }
//							 logger.info("after getting case");

						}

					}

				}
			ida.shutDownPlanner();
		} catch (IDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public URI setMetaData() {

        DataTableResponse tableResult = null;
        try {
            tableResult = stub.getOWLIndividualsFromRepository("http://example.owl", "test1", "/home/jupp/demo/Iris.ioo");
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//		DataTableResponse tableResult = port.getOWLIndividualsFromRepository(
//				"base", "prefix", "/groups/elico/templates/data/DemoData");
//		logger.info(tableResult.getOperatorName());
//		logger.info(tableResult.getOwlXML());

		if (tableResult.getOwlXML() != null) {
			OntologyStreamReader reader = new OntologyStreamReader(tableResult
					.getOwlXML());
			OWLOntologyManager ontManager = OWLManager
					.createOWLOntologyManager();
			OWLDataFactory dFactory = ontManager.getOWLDataFactory();

			try {
				ontology = ontManager.loadOntology(reader);

				// adding the class assertions to the fact list
				for (OWLClassAssertionAxiom ax : ontology
						.getAxioms(AxiomType.CLASS_ASSERTION)) {
					if (ax.getDescription().isLiteral()) {
						factory.addClassFact(ax.getIndividual().getURI(), ax
								.getDescription().asOWLClass().getURI());
					}

				}

				// adding owl object property assertions to the fact list
				for (OWLObjectPropertyAssertionAxiom ax : ontology
						.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {

					factory.addObjectPropertyFact(ax.getSubject().getURI(), ax
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
								factory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), Integer
										.parseInt(value.getLiteral()));
							} else if (value.getDataType().equals(
									dFactory.getDoubleDataType())) {
								factory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), Double
										.parseDouble(value.getLiteral()));
							} else if (value.getDataType().equals(
									dFactory.getFloatDataType())) {
								factory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), Float
										.parseFloat(value.getLiteral()));
							} else if (value.getDataType().equals(
									dFactory.getBooleanDataType())) {
								factory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), Boolean
										.parseBoolean(value.getLiteral()));
							} else {
								factory.addDataPropertyFact(ax.getSubject()
										.getURI(), ax.getProperty()
										.asOWLDataProperty().getURI(), value
										.getLiteral());
							}
							// System.out.println(value.getDataType());

						} else {
							OWLUntypedConstant value = ax.getObject()
									.asOWLUntypedConstant();
							factory.addDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), value
									.getLiteral());
						}

					}
				}

				// adding negative object property assertions
				for (OWLNegativeObjectPropertyAssertionAxiom ax : ontology
						.getAxioms(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)) {

					factory.addNegativeObjectPropertyFact(ax.getSubject()
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
							factory.addNegativeDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), Integer
									.parseInt(value.getLiteral()));
						} else if (value.getDataType().equals(
								dFactory.getDoubleDataType())) {
							factory.addNegativeDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), Double
									.parseDouble(value.getLiteral()));
						} else if (value.getDataType().equals(
								dFactory.getFloatDataType())) {
							factory.addNegativeDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), Float
									.parseFloat(value.getLiteral()));
						} else if (value.getDataType().equals(
								dFactory.getBooleanDataType())) {
							factory.addNegativeDataPropertyFact(ax.getSubject()
									.getURI(), ax.getProperty()
									.asOWLDataProperty().getURI(), Boolean
									.parseBoolean(value.getLiteral()));
						}

					} else {
						OWLUntypedConstant value = ax.getObject()
								.asOWLUntypedConstant();
						factory.addNegativeDataPropertyFact(ax.getSubject()
								.getURI(), ax.getProperty().asOWLDataProperty()
								.getURI(), value.getLiteral());
					}

				}

//				logger.info("facts " + factory.getFacts().size());
				OWLClass cls = getIOObjectClass();
//				logger.info(cls.toString());

				OWLIndividual ind = getIOObjectInd(cls);
//				logger.info(ind.toString());
				return ind.getURI();

			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
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

	private void printIOObjectDescription(IOObjectDescription iod) {
//		logger.info(iod.getIOObjectID() + " " + iod.getIOObjectName() + " "
//				+ iod.getIOObjectType() + " " + iod.getIOObjectTypeID() + " "
//				+ iod.getIOOLocation() + " " + iod.getRoleID() + " "
//				+ iod.getRoleName());
	}

	private void printParameter(Parameter param) {
//		logger.info(param.getInstClassID() + " " + param.getInstID() + " "
//				+ param.getInstName() + " " + param.getInstTypeName() + " "
//				+ param.getRoleID() + " " + param.getRoleName());
	}

	private void printSimpleParameter(SimpleParameter sParam) {
//		logger.info(sParam.getDataPropertyID() + " "
//				+ sParam.getDataPropertyName() + " "
//				+ sParam.getDataType().toString() + " " + sParam.getValue());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FloryTest ts = new FloryTest();
		ts.retrievePlans();
//		ts.retrieveCases();

	}

}

