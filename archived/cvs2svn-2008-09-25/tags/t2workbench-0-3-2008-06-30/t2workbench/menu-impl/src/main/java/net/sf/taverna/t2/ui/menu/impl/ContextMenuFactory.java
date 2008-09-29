package net.sf.taverna.t2.ui.menu.impl;

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.taverna.t2.workbench.design.actions.AddDataflowInputAction;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowOutputAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDatalinkAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveProcessorAction;
import net.sf.taverna.t2.workbench.design.actions.RenameDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RenameDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Processor;

public class ContextMenuFactory {

	public static JPopupMenu getContextMenu(Dataflow dataflow, Object dataflowObject, Component component) {
		JPopupMenu popupMenu = new JPopupMenu();

		if (dataflowObject instanceof Dataflow) {
			popupMenu.add(new JMenuItem(new AddDataflowInputAction(dataflow, component)));
			popupMenu.add(new JMenuItem(new AddDataflowOutputAction(dataflow, component)));
		} else if (dataflowObject instanceof Processor) {
			popupMenu.add(new JMenuItem(new RenameProcessorAction(dataflow, (Processor) dataflowObject, component)));
			popupMenu.add(new JMenuItem(new RemoveProcessorAction(dataflow, (Processor) dataflowObject, component)));
		} else if (dataflowObject instanceof DataflowInputPort) {
			popupMenu.add(new JMenuItem(new RenameDataflowInputPortAction(dataflow, (DataflowInputPort) dataflowObject, component)));		
			popupMenu.add(new JMenuItem(new RemoveDataflowInputPortAction(dataflow, (DataflowInputPort) dataflowObject, component)));		
		} else if (dataflowObject instanceof DataflowOutputPort) {
			popupMenu.add(new JMenuItem(new RenameDataflowOutputPortAction(dataflow, (DataflowOutputPort) dataflowObject, component)));		
			popupMenu.add(new JMenuItem(new RemoveDataflowOutputPortAction(dataflow, (DataflowOutputPort) dataflowObject, component)));		
		} else if (dataflowObject instanceof Datalink) {
			popupMenu.add(new JMenuItem(new RemoveDatalinkAction(dataflow, (Datalink) dataflowObject, component)));		
		}
		return popupMenu;
	}

}
