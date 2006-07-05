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
 * Filename           $RCSfile: BiomartEditor.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:57 $
 *               by   $Author: davidwithers $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.biomart.martservice.MartServiceException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.UIUtils;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;

/**
 * An editor for a biomart Query object attached to a BiomartProcessor.
 * 
 * @author Tom Oinn
 * @author David Withers
 */
public class BiomartEditor implements ProcessorEditor {

	public ActionListener getListener(Processor theProcessor) {
		final BiomartProcessor bp = (BiomartProcessor) theProcessor;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					UIUtils.createFrame(bp.getModel(), new BiomartConfigPanel(
							bp), 100, 100, 600, 500);
				} catch (MartServiceException e) {
					JOptionPane.showMessageDialog(null,
							"Unable to create biomart query editor\n"
									+ e.getMessage(),
							"Problem creating biomart query editor",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}

	public String getEditorDescription() {
		return "Configure biomart query...";
	}

}
