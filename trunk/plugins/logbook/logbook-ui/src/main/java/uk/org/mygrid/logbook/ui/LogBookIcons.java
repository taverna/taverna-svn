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
 * Filename           $RCSfile: LogBookIcons.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-04-02 16:24:37 $
 *               by   $Author: stain $
 * Created on 21 Nov 2006
 *****************************************************************/
package uk.org.mygrid.logbook.ui;

import javax.swing.ImageIcon;

public class LogBookIcons {

    public static ImageIcon levelsIcon, logBookIcon, rdfIcon, reloadIcon, rerunIcon;

    static {
        // Load the image files found in this package into the class.
        try {
            Class c = LogBookIcons.class;
            levelsIcon = new ImageIcon(c.getResource("icons/levels.png"));
            logBookIcon = new ImageIcon(c.getResource("icons/logbook.png"));
            rdfIcon = new ImageIcon(c.getResource("icons/rdf.gif"));
            reloadIcon = new ImageIcon(c.getResource("icons/reload.png"));
            rerunIcon = new ImageIcon(c.getResource("icons/rerun.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.toString());
        }
    }
}
