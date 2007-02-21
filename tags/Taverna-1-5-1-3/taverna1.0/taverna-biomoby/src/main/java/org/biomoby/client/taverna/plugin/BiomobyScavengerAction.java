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
 * Filename           $RCSfile: BiomobyScavengerAction.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-30 16:05:52 $
 *               by   $Author: sowen70 $
 * Created on 30 Nov 2006
 *****************************************************************/
package org.biomoby.client.taverna.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.scavenger.spi.ScavengerActionSPI;

public class BiomobyScavengerAction implements ScavengerActionSPI {

	public boolean canHandle(Scavenger scavenger) {
		return scavenger instanceof BiomobyScavenger;
	}

	public String getDescription() {
		return "A example BioMoby Scavenger Action";
	}

	public ImageIcon getIcon() {
		return new ImageIcon(
                this
                .getClass()
                .getClassLoader()
                .getResource(
                        "org/biomoby/client/taverna/plugin/moby.png"));
	}

	public ActionListener getListener(final Scavenger scavenger) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Nothing yet implemented.");				
			}			
		};
	}

}
