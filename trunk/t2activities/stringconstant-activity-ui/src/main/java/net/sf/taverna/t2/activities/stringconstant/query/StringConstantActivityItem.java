package net.sf.taverna.t2.activities.stringconstant.query;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;

public class StringConstantActivityItem implements ActivityItem {

	public String getType() {
		return "String Constant";
	}

	@Override
	public String toString() {
		return getType();
	}
	/**
	 * Returns a {@link Transferable} containing an
	 * {@link ActivityAndBeanWrapper} with an {@link Activity} and its
	 * configuration bean inside. To get the Transferable you call
	 * {@link ActivityItem#getActivityTransferable()}, with this you call this
	 * method which returns an {@link ActivityAndBeanWrapper}. From the callers
	 * side you ask for a DataFlavor of
	 * {@link DataFlavor#javaJVMLocalObjectMimeType} of type
	 * {@link ActivityAndBeanWrapper} 
	 * <code>
	 * new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper"));
	 * </code>
	 */
	public Transferable getActivityTransferable() {
		Transferable transferable = new Transferable() {

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				ActivityAndBeanWrapper wrapper = new ActivityAndBeanWrapper();
				StringConstantConfigurationBean bean = new StringConstantConfigurationBean();
				bean.setValue("Add your own value here");
				StringConstantActivity activity = new StringConstantActivity();
				wrapper.setActivity(activity);
				wrapper.setBean(bean);
				return wrapper;

			}

			public DataFlavor[] getTransferDataFlavors() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				// TODO Auto-generated method stub
				// FIXME put in the actual data flavor
				return true;
			}

		};
		return transferable;
	}
	
	
}
