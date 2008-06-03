package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Contains the {@link Activity} and its config bean which are transferred
 * during a drag and drop operation
 * 
 * @author Ian Dunlop
 * 
 * @param <ActivityType>
 *            the class of Activity
 * @param <ConfigType>
 *            the configuration bean for the activity
 */
public abstract class ActivityTransferable<ActivityType, ConfigType> implements
		Transferable {

	private ActivityType activity;
	private ConfigType bean;
	private DataFlavor[] flavors;

	public ActivityTransferable() {
		createDataFlavor();
		flavors = new DataFlavor[1];
		flavors[0] = getDataFlavor();
	}

	/**
	 * The {@link DataFlavor} which is transferred across
	 */
	public abstract void createDataFlavor();

	/**
	 * Find out what {@link DataFlavor} this transferrable contains
	 * 
	 */
	public abstract DataFlavor getDataFlavor();

	/**
	 * Returns a List containing the configuration bean and the activity
	 */
	public List<Object> getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		// TODO Auto-generated method stub
		List<Object> dataList = new ArrayList<Object>();
		dataList.add(bean);
		dataList.add(activity);
		return dataList;
	}

	/**
	 * An array of 1 DataFlavor!
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(getDataFlavor());
	}

	/**
	 * Set the Activity which is transferred
	 * 
	 * @param activity
	 */
	public void setActivity(ActivityType activity) {
		this.activity = activity;
	}

	/**
	 * Set the bean for the activity which is transferred
	 * 
	 * @param bean
	 */
	public void setBean(ConfigType bean) {
		this.bean = bean;
	}

}
