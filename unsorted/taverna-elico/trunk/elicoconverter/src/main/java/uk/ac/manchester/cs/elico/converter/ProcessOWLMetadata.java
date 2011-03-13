package uk.ac.manchester.cs.elico.converter;

import ch.uzh.ifi.ddis.ida.api.GoalFactory;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.*;/*
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
 * Date: Feb 28, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class ProcessOWLMetadata {

    private OWLOntology ontology;

    private OWLOntologyManager ontManager;

    private OWLDataFactory dFactory;

    private GoalFactory gFactory;

    static String DATATABLE = "DataTable";

    public ProcessOWLMetadata(String owlXML, GoalFactory goalFactory) throws OWLOntologyCreationException {

        gFactory = goalFactory;
        OntologyStreamReader reader = new OntologyStreamReader(owlXML);
        ontManager = OWLManager
                .createOWLOntologyManager();
        dFactory = ontManager.getOWLDataFactory();

        ontology = ontManager.loadOntology(reader);

        parseOntology();
    }

    public GoalFactory getGoalFactory () {
        return gFactory;
    }

    private void parseOntology() {

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
    }

    public OWLIndividual getIOObjectInd(OWLClass cls) {
        for (OWLIndividual ind : ontology.getReferencedIndividuals()) {
            if (ind.getTypes(ontology).contains(cls))
                return ind;
        }
        return null;
    }

    public OWLClass getIOObjectClass() {
        for (OWLClass cls : ontology.getReferencedClasses()) {
            if (!cls.toString().equals("Thing"))
                return cls;
        }
        return null;
    }



}
