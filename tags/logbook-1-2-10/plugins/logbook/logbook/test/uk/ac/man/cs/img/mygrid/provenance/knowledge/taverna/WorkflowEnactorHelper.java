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
 * Filename           $RCSfile: WorkflowEnactorHelper.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:52:57 $
 *               by   $Author: stain $
 * Created on 20-Jun-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.lsid.AssigningServiceClient;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;
import org.embl.ebi.escience.scuflui.EnactorInvocation;
import org.embl.ebi.escience.scuflui.shared.UIUtils;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j.TestLSIDProvider;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.rdf.model.Model;

import de.fuberlin.wiwiss.ng4j.NamedGraphSet;

/**
 * 
 * @author dturi
 * @version $Id: WorkflowEnactorHelper.java,v 1.1 2007-12-14 12:52:57 stain Exp $
 */
public class WorkflowEnactorHelper {

    private ProvenanceGenerator provenanceGenerator;

    private NamedGraphSet graphSet;

    private Properties configuration;

    /**
     * @throws IOException
     * @throws FileNotFoundException
     * 
     */
    public WorkflowEnactorHelper() throws FileNotFoundException, IOException {
        this(System.getProperties());
    }

    public WorkflowEnactorHelper(Properties configuration)
            throws FileNotFoundException, IOException {
        this.configuration = configuration;
        TestUtils.setTestProperties();
    }

    /**
     * @return Returns the provenanceGenerator.
     */
    public ProvenanceGenerator getProvenanceGenerator() {
        return provenanceGenerator;
    }

    /**
     * @return Returns the graphSet.
     */
    public NamedGraphSet getGraphSet() {
        return graphSet;
    }

    public ProvenanceGenerator executeAndDisplayWorkflow(String workflow,
            UserContext userContext, File outFile) throws Exception {
        provenanceGenerator = executeWorkflow(workflow, userContext);

        JenaMetadataService rdfRepository = new JenaMetadataService(configuration);
        // String retrievedGraph = rdfRepository.retrieveGraph(RUN);
        // System.out.println(retrievedGraph);
        Model instanceData = rdfRepository.retrieveGraphModel(TestUtils.RUN);
        TestUtils.writeOut(instanceData);
        new JenaProvenanceOntology(instanceData).writeForProtege(outFile);
        graphSet = rdfRepository.getGraphSet();
        return provenanceGenerator;
    }

    public ProvenanceGenerator executeWorkflow(String scuflWorkflow,
            UserContext userContext) throws Exception {
        return executeWorkflow(scuflWorkflow, new HashMap(), userContext);
    }

    public ProvenanceGenerator executeWorkflow(String scuflWorkflow,
            Map inputs, UserContext userContext) throws Exception {
        InputStream scufl = ClassLoader
                .getSystemResourceAsStream(scuflWorkflow);
        return executeWorkflow(scufl, inputs, userContext);
    }

    public ProvenanceGenerator executeWorkflow(URL scuflWorkflow, Map inputs,
            UserContext userContext) throws Exception {
        InputStream scufl = scuflWorkflow.openStream();
        return executeWorkflow(scufl, inputs, userContext);
    }

    public ProvenanceGenerator executeWorkflow(URL scuflWorkflow,
            UserContext userContext) throws Exception {
        return executeWorkflow(scuflWorkflow, new HashMap(), userContext);
    }

    public ProvenanceGenerator executeWorkflow(InputStream scufl, Map inputs,
            UserContext userContext) throws Exception {
        setStupidLSIDProvider();
        provenanceGenerator = ProvenanceGenerator.getInstance();
        provenanceGenerator.setConfiguration(configuration);
        provenanceGenerator.initialise();
        WorkflowLauncher launcher = new WorkflowLauncher(scufl, userContext);
        // Map outputs =
        launcher.execute(inputs, provenanceGenerator);
        scufl.close();
        return provenanceGenerator;
    }

    static public void executeWorkflow(String scuflWorkflow,
            UserContext userContext, WorkflowEventListener[] listeners)
            throws Exception {
        InputStream scufl = ClassLoader
                .getSystemResourceAsStream(scuflWorkflow);
        executeWorkflow(scufl, new HashMap(), userContext, listeners);
    }

    static public void executeWorkflow(String scuflWorkflow, Map inputs,
            UserContext userContext, WorkflowEventListener[] listeners)
            throws Exception {
        InputStream scufl = ClassLoader
                .getSystemResourceAsStream(scuflWorkflow);
        executeWorkflow(scufl, inputs, userContext, listeners);
    }

    static public void executeWorkflow(URL scuflWorkflow, Map inputs,
            UserContext userContext, WorkflowEventListener[] listeners)
            throws Exception {
        InputStream scufl = scuflWorkflow.openStream();
        executeWorkflow(scufl, inputs, userContext, listeners);
    }

    static public void executeWorkflow(URL scuflWorkflow,
            UserContext userContext, WorkflowEventListener[] listeners)
            throws Exception {
        executeWorkflow(scuflWorkflow, new HashMap(), userContext, listeners);
    }

    static public void executeWorkflow(InputStream scufl, Map inputs,
            UserContext userContext, WorkflowEventListener[] listeners)
            throws Exception {
        setStupidLSIDProvider();
        WorkflowLauncher launcher = new WorkflowLauncher(scufl, userContext);
        launcher.execute(inputs, listeners);
        scufl.close();
    }

    public static void setStupidLSIDProvider() {
        DataThing.SYSTEM_DEFAULT_LSID_PROVIDER = new TestLSIDProvider();
    }

    public static void setMygridLSIDProvider() {
        System
                .setProperty("taverna.lsid.asclient.endpoint",
                        "http://phoebus.cs.man.ac.uk:8081/authority/services/AssigningWebService");
        System
                .setProperty("taverna.lsid.asclient.ns.wfdefinition",
                        "operation");
        System.setProperty("taverna.lsid.asclient.ns.wfinstance",
                "experimentinstance");
        System.setProperty("taverna.lsid.asclient.ns.datathingleaf",
                "lsdocument");
        System.setProperty("taverna.lsid.asclient.ns.datathingcollection",
                "documentcollection");
        DataThing.SYSTEM_DEFAULT_LSID_PROVIDER = new AssigningServiceClient();
    }

    public void executeWorkflowInGUI(String scuflWorkflow,
            UserContext userContext) throws Exception {

        setStupidLSIDProvider();

        final ScuflModel model = new ScuflModel();
        InputStream scufl = ClassLoader
                .getSystemResourceAsStream(scuflWorkflow);
        XScuflParser.populate(scufl, model, null);
        EnactorProxy enactor = FreefluoEnactorProxy.getInstance();
        WorkflowInstance workflowInstance = enactor.compileWorkflow(model,
                new HashMap(), userContext);
        UIUtils.createFrame(model, new EnactorInvocation(workflowInstance),
                100, 100, 600, 400);
    }

    public void query(String query) throws Exception {
        System.out.println(query);
        JenaMetadataService rdfRepository = new JenaMetadataService(configuration);
        Iterator iterator = rdfRepository.query(query);
        while (iterator.hasNext()) {
            Map nextMap = (Map) iterator.next();
            System.out.println(nextMap);
        }
    }

//    public static void writeOut(CloseableIterator<Statement> instanceData) {
//        while (instanceData.hasNext())
//            System.out.println(instanceData.next().toString());
//        instanceData.close();
//    }

}