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
 * Filename           $RCSfile: WorkflowEditProcessorAction.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-05-21 09:50:41 $
 *               by   $Author: sowen70 $
 * Created on 26 Oct 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.workflow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet;
import org.embl.ebi.escience.scuflui.spi.ProcessorActionSPI;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

public class WorkflowEditProcessorAction implements ProcessorActionSPI {

	WorkflowProcessor processor;

	public ActionListener getListener(Processor processor) {
		this.processor = (WorkflowProcessor) processor;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAction();
			}
		};
	}

	private void handleAction() {
		ScuflModel model = processor.getInternalModelForEditing();
		ScuflModelSet.getInstance().addModel(model);
	}

	public boolean canHandle(Processor processor) {
		return (processor instanceof WorkflowProcessor);
	}

	public String getDescription() {
		return "Edit Nested Workflow";
	}

	public ImageIcon getIcon() {
		return ProcessorHelper.getIconForTagName("workflow");
	}

}
