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
 * Filename           $RCSfile: Utilities.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:48:39 $
 *               by   $Author: stain $
 * Created on 11-Jul-2006
 *****************************************************************/
package uk.org.mygrid.logbook.ui.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;

import uk.org.mygrid.logbook.ui.ProcessRunsTreeTable;

/**
 * @author dturi
 * @version $Id: Utilities.java,v 1.1 2007-12-14 12:48:39 stain Exp $
 */
public class Utilities {

    public static void exportRDF(String rdf) {
        if (rdf == null) {
            JOptionPane.showMessageDialog(null,
                    "No RDF retrieved - see logs for details",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            final JFileChooser fc = new JFileChooser();
            Preferences prefs = Preferences
                    .userNodeForPackage(ProcessRunsTreeTable.class);
            String curDir = prefs.get("currentDir", System
                    .getProperty("user.home"));
            fc.resetChoosableFileFilters();
            fc.setFileFilter(new ExtensionFileFilter(new String[] {
                    "rdf", "xml", "owl" }));
            fc.setCurrentDirectory(new File(curDir));
            int returnVal = fc
                    .showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                prefs.put("currentDir", fc.getCurrentDirectory()
                        .toString());
                File file = fc.getSelectedFile();
                PrintWriter out = new PrintWriter(new FileWriter(file));
                out.println(rdf);
                out.flush();
                out.close();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Problem saving RDF: \n" + ex.getMessage(),
                    "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

}
