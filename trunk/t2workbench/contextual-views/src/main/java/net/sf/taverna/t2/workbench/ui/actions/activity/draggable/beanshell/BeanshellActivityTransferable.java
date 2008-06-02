package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BeanshellActivityTransferable implements Transferable {

	private DataFlavor dataFlavor;
	private DataFlavor[] flavors;
	private BeanshellActivityConfigurationBean bean;
	private Activity<?> activity;

	public BeanshellActivityTransferable() {
		try {
			dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flavors = new DataFlavor[1];
		flavors[0] = dataFlavor;
	}

	public List<Object> getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		// FIXME check that the data flavor is of the correct type
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

	public BeanshellActivityConfigurationBean getBean() {
		return bean;
	}

	public void setBean(BeanshellActivityConfigurationBean bean) {
		this.bean = bean;
	}

	public Activity<?> getActivity() {
		return activity;
	}

	public void setActivity(Activity<?> activity) {
		this.activity = activity;
	}

}
