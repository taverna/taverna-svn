package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

/**
 * Action for removing an input port from the dataflow.
 *
 * @author David Withers
 */
public class RemoveDataflowInputPortAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RemoveDataflowInputPortAction.class);

	private DataflowInputPort port;

	public RemoveDataflowInputPortAction(Dataflow dataflow, DataflowInputPort port, Component component) {
		super(dataflow, component);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Delete Input Port");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Set<? extends Datalink> datalinks = port.getInternalOutputPort().getOutgoingLinks();
			if (datalinks.isEmpty()) {
				editManager.doDataflowEdit(dataflow, edits.getRemoveDataflowInputPortEdit(dataflow, port));
			} else {
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				for (Datalink datalink : datalinks) {
					editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
				}
				editList.add(edits.getRemoveDataflowInputPortEdit(dataflow, port));
				editManager.doDataflowEdit(dataflow, new CompoundEdit(editList));
			}			
			dataflowSelectionModel.removeSelection(port);
		} catch (EditException e1) {
			logger.debug("Remove dataflow input port failed", e1);
		}
	}

}
