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
 * Filename           $RCSfile: UserChangedDataTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:22 $
 *               by   $Author: stain $
 * Created on 06-Sep-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.query;

import junit.framework.TestCase;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

public class UserChangedDataTest extends TestCase {

    public static final String EXPERIMENT_TEST_CHANGED_DATA1 = "urn:lsid:net.taverna.sf:experiment:test_changed_data1";

    public static final String DATA_ITEM_OLD1 = "urn:lsid:net.taverna.sf:dataItem:old1";

    public static final String DATA_ITEM_CHANGED1 = "urn:lsid:net.taverna.sf:dataItem:changed1";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(UserChangedDataTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Test method for
     * 'uk.ac.man.cs.img.mygrid.provenance.knowledge.query.UserChangedData.workflowsWithDataChangedByExperimenter(String)'
     */
    public void testGetWorkflowsWithDataChanged() throws Exception {
        JenaProvenanceOntology provenanceOntology = new JenaProvenanceOntology();
        provenanceOntology.addChangedData(DATA_ITEM_CHANGED1, DATA_ITEM_OLD1);
        MetadataServiceFactory.getInstance(
                ProvenanceConfigurator.getMetadataStoreConfiguration())
                .storeModel(provenanceOntology.getInstanceData(),
                        EXPERIMENT_TEST_CHANGED_DATA1);
        UserChangedData userChangedData = new UserChangedData();
        System.out.println(userChangedData.toString());
    }

}
