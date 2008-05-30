package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class StringConstantTransferable implements Transferable {

	private DataFlavor dataFlavor;
	private DataFlavor[] flavors;
	private StringConstantConfigurationBean bean;
	private Activity<?> activity;

	public StringConstantTransferable() {
		try {
			dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flavors = new DataFlavor[1];
		flavors[0] = dataFlavor;
	}

	public void setBean(StringConstantConfigurationBean bean) {
		this.bean = bean;
	}

	public void setActivity(Activity<?> activity) {
		this.activity = activity;
	}

	/**
	 * return an already created bean or a brand new empty one
	 */
	public List<Object> getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		List<Object> dataList = new ArrayList<Object>();
		dataList.add(bean);
		dataList.add(activity);
		return dataList;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {

		return (this.dataFlavor.equals((flavor)));
	}

	public StringConstantConfigurationBean getBean() {
		return bean;
	}

	public Activity<?> getActivity() {
		return activity;
	}

}
