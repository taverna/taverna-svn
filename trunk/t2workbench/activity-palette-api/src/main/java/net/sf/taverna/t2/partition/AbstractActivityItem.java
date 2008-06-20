package net.sf.taverna.t2.partition;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.Icon;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;

import org.apache.log4j.Logger;

public abstract class AbstractActivityItem implements ActivityItem {

	private static Logger logger = Logger.getLogger(AbstractActivityItem.class);

	/**
	 * Compare the values that are shown in the activity palette and re-order
	 * alphabetically by the lower case representation
	 */
	public int compareTo(ActivityItem o) {
		if (toString().toLowerCase().compareTo(o.toString().toLowerCase()) > 0) {

			return 1;
		}
		return 0;
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
				wrapper.setActivity(getUnconfiguredActivity());
				wrapper.setBean(getConfigBean());
				wrapper.setName(AbstractActivityItem.this.toString());
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
					logger.error("Error casting Dataflavor", e);
				}
				flavors[0] = flavor;
				return flavors;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				DataFlavor thisFlavor = null;
				try {
					thisFlavor = new DataFlavor(
							DataFlavor.javaJVMLocalObjectMimeType
									+ ";class=net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper");
				} catch (ClassNotFoundException e) {
					logger.error("Error casting Dataflavor", e);
				}
				return flavor.equals(thisFlavor);
			}

		};
		return transferable;
	}

	public abstract Icon getIcon();

	protected abstract Object getConfigBean();

	protected abstract Activity<?> getUnconfiguredActivity();

}
