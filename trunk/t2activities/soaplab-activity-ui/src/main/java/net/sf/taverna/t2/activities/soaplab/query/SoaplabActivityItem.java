package net.sf.taverna.t2.activities.soaplab.query;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;

public class SoaplabActivityItem implements ActivityItem {
	private String category;
	private String operation;
	private String url;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return "Soaplab";
	}

	@Override
	public String toString() {
		return this.operation;
	}

	/**
	 * Returns a {@link Transferable} containing an
	 * {@link ActivityAndBeanWrapper} with an {@link Activity} and its
	 * configuration bean inside. To get the Transferable you call
	 * {@link ActivityItem#getActivityTransferable()}, with this you call this
	 * method which returns an {@link ActivityAndBeanWrapper}. From the callers
	 * side you ask for a DataFlavor of
	 * {@link DataFlavor#javaJVMLocalObjectMimeType} of type
	 * {@link ActivityAndBeanWrapper} <code>
	 * new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper"));
	 * </code>
	 */
	public Transferable getActivityTransferable() {
		Transferable transferable = new Transferable() {

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				ActivityAndBeanWrapper wrapper = new ActivityAndBeanWrapper();
				SoaplabActivityConfigurationBean bean = new SoaplabActivityConfigurationBean();
				bean.setEndpoint(url + operation);
				SoaplabActivity activity = new SoaplabActivity();
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

	public Icon getIcon() {
		return new ImageIcon(SoaplabActivityItem.class.getResource("/soaplab.png"));
	}

}
