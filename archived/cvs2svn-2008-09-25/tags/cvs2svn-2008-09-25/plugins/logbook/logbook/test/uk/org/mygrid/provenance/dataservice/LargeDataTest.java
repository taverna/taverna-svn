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
 * Filename           $RCSfile: LargeDataTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:52:58 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.implementation.SimpleUserContext;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.WorkflowEnactorHelper;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;

public class LargeDataTest extends ProvenanceGeneratorTests {

    public LargeDataTest() {
        super();
    }

    public void testExecution() throws Exception {
        File file = new File(
                "/home/dturi/eclipse-workspace/rdf-provenance/workbench/examples/PaulFisher/blast_simplifier_input.xml");
        Document inputDoc = new SAXBuilder(false).build(new FileReader(file));
        Map inputMap = DataThingXMLFactory.parseDataDocument(inputDoc);

        UserContext userContext = new SimpleUserContext(TEST_PERSON_1,
                "urn:lsid:www.scientific_organisations.org:lab:l5678",
                "urn:lsid:removeme", "zapp", "angelica");
        new WorkflowEnactorHelper(configuration).executeWorkflow(
                "PaulFisher/blast_simplifier.xml", inputMap, userContext);
    }

    protected void tearDown() throws Exception {
        // MetadataServiceFactory.getInstance(configuration).removeGraph(
        // RUN);
        super.tearDown();
    }

}
