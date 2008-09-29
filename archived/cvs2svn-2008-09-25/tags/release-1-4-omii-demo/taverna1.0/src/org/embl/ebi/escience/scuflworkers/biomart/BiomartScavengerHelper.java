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
 * Filename           $RCSfile: BiomartScavengerHelper.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-05-30 16:18:39 $
 *               by   $Author: sowen70 $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Helper for creating Biomart scavengers
 * 
 * @author David Withers
 */
public class BiomartScavengerHelper implements ScavengerHelper {

	public String getScavengerDescription() {
		return "Add new Biomart service...";
	}

	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final String baseURL = (String) JOptionPane.showInputDialog(
						null, "Enter the Biomart location", "Biomart location",
						JOptionPane.QUESTION_MESSAGE, null, null,
						"http://www.biomart.org/biomart");
				if (baseURL != null) {
					new Thread() {
						public void run() {
							if (s.getParentPanel()!=null) s.getParentPanel().startProgressBar("Adding BioMart scavenger");
							try {
								s.addScavenger(new BiomartScavenger(baseURL));
							} catch (ScavengerCreationException sce) {
								JOptionPane
										.showMessageDialog(null,
												"Unable to create scavenger!\n"
														+ sce.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							}
							if (s.getParentPanel()!=null) s.getParentPanel().stopProgressBar();
						}
					}.start();
				}
			}
		};
	}

}
