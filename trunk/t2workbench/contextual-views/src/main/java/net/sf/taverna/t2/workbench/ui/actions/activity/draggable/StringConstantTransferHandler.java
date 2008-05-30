package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class StringConstantTransferHandler extends TransferHandler {

	private DataFlavor dataFlavor;
	private StringConstantConfigurationBean bean;
	private Activity<?> activity;

	public StringConstantTransferHandler() {

	}

	public StringConstantTransferHandler(StringConstantConfigurationBean bean,
			Activity<?> activity) {
		this.bean = bean;
		this.activity = activity;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		System.out.println("can import");
		// FIXME should check that transferable is correct type
		return true;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		System.out.println("create transferable");
		StringConstantTransferable transferable = new StringConstantTransferable();
		transferable.setBean(bean);
		transferable.setActivity(activity);
		return transferable;
	}

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		System.out.println("export as drag");
		// TODO Auto-generated method stub
		super.exportAsDrag(comp, e, action);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		System.out.println("export done");
		// TODO Auto-generated method stub
		super.exportDone(source, data, action);
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		System.out.println("export to clipboard");
		// TODO Auto-generated method stub
		super.exportToClipboard(comp, clip, action);
	}

	@Override
	public int getSourceActions(JComponent c) {
		System.out.println("get source actions");
		// TODO Auto-generated method stub
		return COPY_OR_MOVE;
	}

	@Override
	public Icon getVisualRepresentation(Transferable t) {
		System.out.println("get visual rep");
		// TODO Auto-generated method stub
		return super.getVisualRepresentation(t);
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		System.out.println("import data");
		try {
			dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			bean = (StringConstantConfigurationBean) t
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

	public StringConstantConfigurationBean getBean() {
		return bean;
	}

	public void setBean(StringConstantConfigurationBean bean) {
		System.out.println("set bean");
		this.bean = bean;
	}

	public Activity<?> getActivity() {
		return activity;
	}

	public void setActivity(Activity<?> activity) {
		System.out.println("set activity " + ((StringConstantConfigurationBean)activity.getConfiguration()).getValue());
		this.activity = activity;
	}

}
