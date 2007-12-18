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
 * Filename           $RCSfile: TestUtils.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:38 $
 *               by   $Author: stain $
 * Created on 26 Oct 2006
 *****************************************************************/
package uk.org.mygrid.provenance.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;

import com.hp.hpl.jena.rdf.model.Model;

public class TestUtils {

    static public void setTestProperties() throws FileNotFoundException,
            IOException {
        Properties props = new Properties();
        File log4j = new File("test-resource", "log4j.properties");
        InputStream in = new FileInputStream(log4j);
        props.load(in);
        in.close();
        PropertyConfigurator.configure(props);
    }

    public static String RUN = "urn:lsid:net.sf.taverna:wfInstance:4";
    public static final String LAB_1 = "urn:lsid:www.scientific_organisations.org:lab:l5678";
    public static final String LAB_2 = "urn:lsid:www.scientific_organisations.org:lab:l9999";
    public static final String TEST_PERSON_1 = "urn:lsid:www.people.org:person:p1234";
    public static final String TEST_PERSON_2 = "urn:lsid:www.people.org:person:p4321";
    public static void setPrefixes(Model model) {
        model.setNsPrefix("provenance", ProvenanceOntology.PROVENANCE_NS + "#");
        model.setNsPrefix("process", "urn:www.mygrid.org.uk/process#");
        model.setNsPrefix("processProperty",
                "urn:www.mygrid.org.uk/process_property#");
        model.setNsPrefix("processRun", "urn:www.mygrid.org.uk/process_run#");
    }
    public static void writeOut(Model instanceData) {
        setPrefixes(instanceData);
        instanceData.write(System.out, "N3");
    }

}
