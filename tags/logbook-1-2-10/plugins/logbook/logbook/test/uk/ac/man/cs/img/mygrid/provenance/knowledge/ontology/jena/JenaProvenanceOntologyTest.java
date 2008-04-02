/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: JenaProvenanceOntologyTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:37 $
 *               by   $Author: stain $
 * Created on 17-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;
import uk.org.mygrid.provenance.util.ProvenanceOntologyUtil;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author dturi
 * @version $Id: JenaProvenanceOntologyTest.java,v 1.1 2005/11/11 15:45:17 turid
 *          Exp $
 */
public class JenaProvenanceOntologyTest extends TestCase {

    public static final File INSTANCES_DIR = new File(System.getProperty(
            "instances.dir", "etc/ontology/instances"));

    public static String RUN = "urn:lsid:net.sf.taverna:wfInstance:4";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JenaProvenanceOntologyTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        TestUtils.setTestProperties();
        super.setUp();
    }

    public void testReasoning() throws Exception {
        JenaProvenanceOntology ontology = new JenaProvenanceOntology(new File(
                INSTANCES_DIR, "test.owl").toURI());
        OntModel ontModel = ontology.getOntModel();
        ontModel.write(System.out);
        ExtendedIterator instances = ontModel.getOntClass(
                ProvenanceVocab.DATA_OBJECT.getURI()).listInstances();
        int i = 0;
        while (instances.hasNext()) {
            Individual nextIndividual = (Individual) instances.next();
            System.out.println(nextIndividual.getURI());
            i++;
        }
        instances.close();
        assertEquals("expected runs", 2, i);

        // Model plain = ModelFactory.createModelForGraph( ontModel.getGraph()
        // );
        // plain.write( System.out, "RDF/XML" );
    }

    public void testAddersAndGetters() throws Exception {
        JenaProvenanceOntology provenanceOntology = new JenaProvenanceOntology();
        String experimenter = "urn:lsid:www.mygrid.org.uk:person:test";
        String organization = "urn:lsid:www.mygrid.org.uk:organization:test";
        provenanceOntology.addBelongsTo(experimenter, organization);
        String workflowRun = "urn:lsid:www.mygrid.org.uk:experimentinstance:test";
        provenanceOntology.addLaunchedBy(workflowRun, experimenter);
        String retrievedExperimenter = provenanceOntology
                .getExperimenter(workflowRun);
        System.out.println(retrievedExperimenter);
        assertEquals(experimenter, retrievedExperimenter);
        String experimenterOrganization = provenanceOntology
                .getExperimenterOrganization(workflowRun);
        assertEquals(organization, experimenterOrganization);
        String nestedWorkflowRun = "urn:lsid:www.mygrid.org.uk:experimentinstance:nestedTest";
        String processRunId = "urn:test:processRun";
        provenanceOntology.addProcessIsNestedWorkflow(processRunId,
                nestedWorkflowRun);
        provenanceOntology.addNestedWorkflow(workflowRun, nestedWorkflowRun);
        OntClass ontClass = provenanceOntology.model
                .getOntClass(ProvenanceVocab.NESTED_WORKFLOW_RUN.getURI());
        Iterator iterator = ontClass.listInstances();
        assertTrue("There is at least one NestedWorkflowRun", iterator
                .hasNext());
        Individual nestedWorkflowRunIndividual = (Individual) iterator.next();
        assertEquals(nestedWorkflowRun,
                ((nestedWorkflowRunIndividual).getURI()));
        ObjectProperty parentWorkflow = provenanceOntology.model
                .getObjectProperty(ProvenanceVocab.NESTED_RUN.getURI());
        Individual workflowRunIndividual = provenanceOntology.model
                .createIndividual(workflowRun, ProvenanceVocab.WORKFLOW_RUN);
        NodeIterator iter = provenanceOntology.model.listObjectsOfProperty(
                workflowRunIndividual, parentWorkflow);
        assertTrue("There is at least one value in range of parentWorkflow",
                iter.hasNext());
        RDFNode node = iter.nextNode();
        iter.close();
        assertEquals(nestedWorkflowRun, node.toString());

    }

    public void testGetWorkflowStartDate() {
        JenaMetadataService rdfRepository;
        try {
            rdfRepository = new JenaMetadataService(
                    ProvenanceConfigurator.getMetadataStoreConfiguration());
        Model instanceData = rdfRepository.retrieveGraphModel(RUN);
        ProvenanceOntology provenanceOntology = new JenaProvenanceOntology(
                instanceData);
        String workflowStartDate = provenanceOntology
                .getUnparsedWorkflowStartDate(RUN);
        Date date = ProvenanceOntologyUtil.parseDateTime(workflowStartDate);
        assertTrue("Workflow was run before", date.before(new Date()));
        System.out.println(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        System.out.println(calendar);
        } catch (Exception e) {
            System.err.println("Last test not valid, but normal: probably run does not exist");
        }
    }

    // public void testPrint() throws Exception {
    // NamedRDFGraphsRepository rdfRepository = new NamedRDFGraphsRepository();
    // Model instanceData =
    // rdfRepository.retrieveGraphModel("urn:lsid:www.mygrid.org.uk:experimentinstance:DJFFOA1ANW0");
    // ProvenanceOntology provenanceOntology = new
    // ProvenanceOntology(instanceData);
    // provenanceOntology.write(new File(INSTANCES_DIR, "changed.owl"));
    // }
    
    public void testAddEndTime() {
        JenaProvenanceOntology provenanceOntology = new JenaProvenanceOntology();
        provenanceOntology.addEndTime("urn:test", ProvenanceVocab.PROCESS_RUN);
        provenanceOntology.model.write(System.out, "N3");
    }

}
