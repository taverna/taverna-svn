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
 * Filename           $RCSfile: ProvenanceTestPopulator.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:52:51 $
 *               by   $Author: stain $
 * Created on 20-Jun-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;

import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

/**
 * Runs all workflows in {@link #DIRECTORIES}. Useful to populate test
 * databases for provenance.
 * 
 * @author dturi
 * @version $Id: ProvenanceGeneratorTest.java,v 1.1 2005/08/16 12:19:31 turid
 *          Exp $
 */
public class ProvenanceTestPopulator {

    public static final String[] DIRECTORIES = { "PaulFisher"
     , "TomOinn"
    // , "PeterLi"
    };

    public static void main(String[] args) throws LogBookException {
        WorkflowEnactorHelper.setMygridLSIDProvider();
        Properties configuration = ProvenanceConfigurator.getConfiguration();
        for (int j = 0; j < DIRECTORIES.length; j++) {
            File workflowsDirectory = new File("workbench/examples/"
                    + DIRECTORIES[j]);
            File[] workflows = workflowsDirectory.listFiles();
            for (int i = 0; i < workflows.length; i++) {
                try {
                    File workflow = workflows[i];
                    if (!workflow.isDirectory()
                            && workflow.getName().endsWith(".xml")) {
                        System.out.println("Running " + workflow);
                        InputStream scufl = workflow.toURL().openStream();
                        OldProvenanceGenerator provenanceGenerator = new OldProvenanceGenerator(
                                configuration);
                        WorkflowLauncher launcher = new WorkflowLauncher(scufl,
                                null);
                        launcher.execute(new HashMap(), provenanceGenerator);
                        scufl.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.exit(0);
    }

}
