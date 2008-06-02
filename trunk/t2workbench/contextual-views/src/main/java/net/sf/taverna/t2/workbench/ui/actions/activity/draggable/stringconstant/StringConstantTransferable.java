package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Holds the Activity and the appropriate Config Bean for the dragged activity
 * 
 * @author Ian Dunlop
 * 
 */
public class StringConstantTransferable implements Transferable {

	private DataFlavor dataFlavor;
	private DataFlavor[] flavors;
	private StringConstantConfigurationBean bean;
	private Activity<?> activity;

	/**
	 * Create the {@link DataFlavor} for this transferable, in this case it
	 * holds a {@link StringConstantConfigurationBean}
	 */
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

	/**
	 * The Config Bean that should be transferred during the drag/drop operation
	 * 
	 * @param bean
	 */
	public void setBean(StringConstantConfigurationBean bean) {
		this.bean = bean;
	}

	/**
	 * The activity that should be transferred during the drag/drop operation
	 * 
	 * @param activity
	 */
	public void setActivity(Activity<?> activity) {
		this.activity = activity;
	}

	/**
	 * Returns a {@link List} containing the config bean and the activity which
	 * are transferred during the drag/drop operation
	 */
	public List<Object> getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		// FIXME check that the data flavor is of the correct type
		List<Object> dataList = new ArrayList<Object>();
		dataList.add(bean);
		dataList.add(activity);
		return dataList;
	}

	/**
	 * What type of data is transferred during any drag/drop operations using
	 * this transferablr
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	/**
	 * Does this transferable support a particular type of {@link DataFlavor}
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {

		return (this.dataFlavor.equals((flavor)));
	}

	/**
	 * The config bean which is transfered by this transferable
	 * 
	 * @return
	 */
	public StringConstantConfigurationBean getBean() {
		return bean;
	}

	/**
	 * The activity which is transfered by this transferable
	 * 
	 * @return
	 */
	public Activity<?> getActivity() {
		return activity;
	}

}
