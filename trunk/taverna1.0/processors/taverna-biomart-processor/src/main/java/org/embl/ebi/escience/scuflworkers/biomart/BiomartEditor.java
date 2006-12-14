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
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-14 14:10:36 $
 *               by   $Author: davidwithers $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.biomart.martservice.MartServiceException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
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
					final BiomartConfigPanel component = new BiomartConfigPanel(bp);

					JFrame frame = new JFrame(component.getName());
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.getContentPane().add(component);
					frame.addWindowListener(new WindowAdapter() {
						public void windowClosed(WindowEvent e) {
							component.onDispose();
						}
					});
					if (component.getIcon() != null) {
						frame.setIconImage(component.getIcon().getImage());
					}
					frame.setSize(new Dimension(800, 600));
					frame.setVisible(true);

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
