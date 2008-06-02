package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BeanshellActivityTransferHandler extends TransferHandler {
	
	private DataFlavor dataFlavor;
	private BeanshellActivityConfigurationBean bean;
	private Activity<?> activity;
	
	public BeanshellActivityTransferHandler(BeanshellActivityConfigurationBean bean, BeanshellActivity activity) {
		this.bean = bean;
		this.activity = activity;
	}
	

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		// FIXME do actual check of flavor
		return true;
	}


	@Override
	protected Transferable createTransferable(JComponent c) {
		BeanshellActivityTransferable transferable = new BeanshellActivityTransferable();
		transferable.setActivity(activity);
		transferable.setBean(bean);
		return transferable;
	}


	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		// TODO Auto-generated method stub
		super.exportAsDrag(comp, e, action);
	}


	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		// TODO Auto-generated method stub
		super.exportDone(source, data, action);
	}


	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		super.exportToClipboard(comp, clip, action);
	}


	@Override
	public int getSourceActions(JComponent c) {
		// TODO Auto-generated method stub
		return COPY_OR_MOVE;
	}


	@Override
	public Icon getVisualRepresentation(Transferable t) {
		// TODO Auto-generated method stub
		return super.getVisualRepresentation(t);
	}


	@Override
	public boolean importData(JComponent comp, Transferable t) {
		try {
			dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			bean = (BeanshellActivityConfigurationBean) t
					.getTransferData(dataFlavor);
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}


	public void setActivity(Activity<?> activity) {
		this.activity = activity;		
	}


	public void setBean(BeanshellActivityConfigurationBean bean) {
		this.bean = bean;
	}


	public Activity<?> getActivity() {
		return this.activity;
	}


	public BeanshellActivityConfigurationBean getBean() {
		return bean;
	}

}
