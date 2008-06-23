package net.sf.taverna.t2.ui.menu.impl;

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.taverna.t2.workbench.design.actions.AddDataflowInputAction;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowOutputAction;
import net.sf.taverna.t2.workbench.design.actions.RenameDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RenameDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

public class ContextMenuFactory {

	public static JPopupMenu getContextMenu(Dataflow dataflow, Object dataflowObject, Component component) {
		JPopupMenu popupMenu = new JPopupMenu();

		if (dataflowObject instanceof Dataflow) {
			popupMenu.add(new JMenuItem(new AddDataflowInputAction(dataflow, component)));
			popupMenu.add(new JMenuItem(new AddDataflowOutputAction(dataflow, component)));
		} else if (dataflowObject instanceof Processor) {
			popupMenu.add(new JMenuItem(new RenameProcessorAction(dataflow, (Processor) dataflowObject, component)));
		} else if (dataflowObject instanceof DataflowInputPort) {
			popupMenu.add(new JMenuItem(new RenameDataflowInputPortAction(dataflow, (DataflowInputPort) dataflowObject, component)));		
		} else if (dataflowObject instanceof DataflowOutputPort) {
			popupMenu.add(new JMenuItem(new RenameDataflowOutputPortAction(dataflow, (DataflowOutputPort) dataflowObject, component)));		
		}
		return popupMenu;
	}

}
