package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant;

import java.awt.datatransfer.DataFlavor;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.ActivityTransferHandler;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.ActivityTransferable;

/**
 * Handles transferring of {@link StringConstantActivity} during a drag and drop
 * action
 * 
 * @author Ian Dunlop
 * 
 */
public class StringConstantTransferHandler
		extends
		ActivityTransferHandler<StringConstantActivity, StringConstantConfigurationBean> {

	public StringConstantTransferHandler(StringConstantConfigurationBean bean,
			StringConstantActivity activity) {
		super(activity, bean);
	}

	public StringConstantTransferHandler() {
		super();
	}

	@Override
	public DataFlavor getDataFlavor() {
		try {
			return new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ActivityTransferable getTransferable() {
		return new StringConstantTransferable();
	}

}
