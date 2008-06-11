package net.sf.taverna.t2.workbench.views.graph;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class GraphViewTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;

	private Edits edits = EditsRegistry.getEdits();
	
	private EditManager editManager = EditManager.getInstance();

	private GraphViewComponent graphViewComponent;
	
	public GraphViewTransferHandler(GraphViewComponent graphViewComponent) {
		this.graphViewComponent = graphViewComponent;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
	 */
	@Override
	public boolean canImport(JComponent component, DataFlavor[] dataFlavors) {
		for (DataFlavor dataFlavor : dataFlavors) {
			if (dataFlavor.getRepresentationClass().getName().endsWith("ConfigurationBean")) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
	 */
	@Override
	public boolean importData(JComponent component, Transferable transferable) {
		try {
			Object data = transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
			if (data instanceof List) {
				List<?> list = (List<?>) data;
				if (list.size() == 2) {
					Object object = list.get(1);
					if (object instanceof Activity) {
						Activity<?> activity = (Activity<?>) object;
						Dataflow dataflow = graphViewComponent.getDataflow();
						Edit<Dataflow> edit = edits.getAddProcessorEdit(dataflow, Tools.buildFromActivity(activity));
						editManager.doDataflowEdit(dataflow, edit);
					}
				}
				
			}
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
