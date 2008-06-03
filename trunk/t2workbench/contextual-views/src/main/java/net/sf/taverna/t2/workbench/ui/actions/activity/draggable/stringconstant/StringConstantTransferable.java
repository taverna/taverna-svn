package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant;

import java.awt.datatransfer.DataFlavor;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.ActivityTransferable;

/**
 * Holds the Activity and the appropriate Config Bean for the dragged String Constant activity
 * 
 * @author Ian Dunlop
 * 
 */
public class StringConstantTransferable extends ActivityTransferable<StringConstantActivity, StringConstantConfigurationBean> {

	private DataFlavor dataFlavor;

	@Override
	public void createDataFlavor() {
		try {
			dataFlavor =  new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
					+ ";class=net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public DataFlavor getDataFlavor() {
		return dataFlavor;
	}

}
