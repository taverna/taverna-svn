package net.sf.taverna.t2.activities.stringconstant.query;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

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
				DataFlavor[] flavors = new DataFlavor[1];
				DataFlavor flavor = null;
				try {
					flavor = new DataFlavor(
							DataFlavor.javaJVMLocalObjectMimeType
									+ ";class=net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper");
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				flavors[1] = flavor;
				return flavors;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				DataFlavor thisFlavor = null;
				try {
					thisFlavor = new DataFlavor(
							DataFlavor.javaJVMLocalObjectMimeType
									+ ";class=net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper");
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return flavor.equals(flavor);
			}

		};
		return transferable;
	}

	public Icon getIcon() {
		return new ImageIcon(StringConstantActivityItem.class.getResource("/stringconstant.png"));
	}
	
	
}
