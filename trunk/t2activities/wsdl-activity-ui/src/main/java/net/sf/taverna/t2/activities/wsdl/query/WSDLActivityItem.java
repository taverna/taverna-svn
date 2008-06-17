package net.sf.taverna.t2.activities.wsdl.query;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;

public class WSDLActivityItem implements ActivityItem {

	private String use;
	private String url;
	private String style;
	private String operation;

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getType() {
		return "SOAP";
	}

	@Override
	public String toString() {
		return operation;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
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
				WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
				bean.setOperation(operation);
				bean.setWsdl(url);
				WSDLActivity activity = new WSDLActivity();
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
