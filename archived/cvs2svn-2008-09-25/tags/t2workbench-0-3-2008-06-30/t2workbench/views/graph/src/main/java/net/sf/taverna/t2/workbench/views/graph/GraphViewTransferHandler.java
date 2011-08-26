package net.sf.taverna.t2.workbench.views.graph;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.apache.log4j.Logger;

public class GraphViewTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger
	.getLogger(GraphViewTransferHandler.class);

	private Edits edits = EditsRegistry.getEdits();

	private EditManager editManager = EditManager.getInstance();

	private GraphViewComponent graphViewComponent;

	private DataFlavor activityDataFlavor;

	public GraphViewTransferHandler(GraphViewComponent graphViewComponent) {
		this.graphViewComponent = graphViewComponent;
		try {
			activityDataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType + ";class="
							+ ActivityAndBeanWrapper.class.getCanonicalName(),
					"Activity", getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			logger.warn("Could not find the class "
					+ ActivityAndBeanWrapper.class);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
	 *      java.awt.datatransfer.DataFlavor[])
	 */
	@Override
	public boolean canImport(JComponent component, DataFlavor[] dataFlavors) {
//		for (DataFlavor dataFlavor : dataFlavors) {
//			if (dataFlavor.equals(activityDataFlavor)) {
//				return true;
//			}
//		}
//		return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
	 *      java.awt.datatransfer.Transferable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(JComponent component, Transferable transferable) {
		try {
			Object data = transferable.getTransferData(activityDataFlavor);
			if (data instanceof ActivityAndBeanWrapper) {
				ActivityAndBeanWrapper activityAndBeanWrapper = (ActivityAndBeanWrapper) data;
				Activity activity = activityAndBeanWrapper.getActivity();
				Object bean = activityAndBeanWrapper.getBean();
				activity.configure(bean);
				Dataflow dataflow = graphViewComponent.getDataflow();
				Processor p = Tools.buildFromActivity(activity);
				String name=activityAndBeanWrapper.getName().replace(' ', '_');
				name=Tools.uniqueProcessorName(name,dataflow);
				Edit<Processor> renameProcessorEdit = edits.getRenameProcessorEdit(p, name);
				Edit<Dataflow> edit = edits.getAddProcessorEdit(dataflow, p);
				editManager.doDataflowEdit(dataflow, edit);
				editManager.doDataflowEdit(dataflow, renameProcessorEdit);
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
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
