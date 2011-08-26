package net.sf.taverna.t2.ui.menu.impl;

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowInputAction;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowOutputAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDatalinkAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveMergeAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveProcessorAction;
import net.sf.taverna.t2.workbench.design.actions.RenameDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RenameDataflowOutputPortAction;
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
			popupMenu.add(new ShadedLabel("Merge : " + merge.getLocalName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RemoveMergeAction(dataflow, merge, component)));
		} else if (dataflowObject instanceof DataflowInputPort) {
			DataflowInputPort dataflowInputPort = (DataflowInputPort) dataflowObject;
			popupMenu.add(new ShadedLabel("Workflow Input : " + dataflowInputPort.getName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RenameDataflowInputPortAction(dataflow, dataflowInputPort, component)));		
			popupMenu.add(new JMenuItem(new RemoveDataflowInputPortAction(dataflow, dataflowInputPort, component)));		
		} else if (dataflowObject instanceof DataflowOutputPort) {
			DataflowOutputPort dataflowOutputPort = (DataflowOutputPort) dataflowObject;
			popupMenu.add(new ShadedLabel("Workflow Output : " + dataflowOutputPort.getName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RenameDataflowOutputPortAction(dataflow, dataflowOutputPort, component)));		
			popupMenu.add(new JMenuItem(new RemoveDataflowOutputPortAction(dataflow, dataflowOutputPort, component)));		
		} else if (dataflowObject instanceof Datalink) {
			popupMenu.add(new ShadedLabel("Link", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RemoveDatalinkAction(dataflow, (Datalink) dataflowObject, component)));		
		}
		return popupMenu;
	}

}
