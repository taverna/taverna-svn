/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.ui.menu.impl;

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowInputAction;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowOutputAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDatalinkAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveMergeAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveProcessorAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.Processor;

public class ContextMenuFactory {

	public static JPopupMenu getContextMenu(Dataflow dataflow, Object dataflowObject, Component component) {
		JPopupMenu popupMenu = new JPopupMenu();

		if (dataflowObject instanceof Dataflow) {
			popupMenu.add(new ShadedLabel("Workflow Inputs", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new AddDataflowInputAction(dataflow, component)));
			popupMenu.addSeparator();
			popupMenu.add(new ShadedLabel("Workflow Outputs", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new AddDataflowOutputAction(dataflow, component)));
		} else if (dataflowObject instanceof Processor) {
			Processor processor = (Processor) dataflowObject;
			popupMenu.add(new ShadedLabel("Processor : " + processor.getLocalName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RenameProcessorAction(dataflow, processor, component)));
			popupMenu.add(new JMenuItem(new RemoveProcessorAction(dataflow, processor, component)));
		} else if (dataflowObject instanceof Merge) {
			Merge merge = (Merge) dataflowObject;
			popupMenu.add(new ShadedLabel("Merge", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RemoveMergeAction(dataflow, merge, component)));
		} else if (dataflowObject instanceof DataflowInputPort) {
			DataflowInputPort dataflowInputPort = (DataflowInputPort) dataflowObject;
			popupMenu.add(new ShadedLabel("Workflow Input : " + dataflowInputPort.getName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new EditDataflowInputPortAction(dataflow, dataflowInputPort, component)));		
			popupMenu.add(new JMenuItem(new RemoveDataflowInputPortAction(dataflow, dataflowInputPort, component)));		
		} else if (dataflowObject instanceof DataflowOutputPort) {
			DataflowOutputPort dataflowOutputPort = (DataflowOutputPort) dataflowObject;
			popupMenu.add(new ShadedLabel("Workflow Output : " + dataflowOutputPort.getName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new EditDataflowOutputPortAction(dataflow, dataflowOutputPort, component)));		
			popupMenu.add(new JMenuItem(new RemoveDataflowOutputPortAction(dataflow, dataflowOutputPort, component)));		
		} else if (dataflowObject instanceof Datalink) {
			popupMenu.add(new ShadedLabel("Link", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RemoveDatalinkAction(dataflow, (Datalink) dataflowObject, component)));		
		}
		return popupMenu;
	}

}
