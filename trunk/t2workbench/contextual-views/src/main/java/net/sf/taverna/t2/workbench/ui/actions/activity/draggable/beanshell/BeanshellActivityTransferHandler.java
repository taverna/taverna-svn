package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell;

import java.awt.datatransfer.DataFlavor;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.ActivityTransferHandler;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.ActivityTransferable;

public class BeanshellActivityTransferHandler extends ActivityTransferHandler<BeanshellActivity, BeanshellActivityConfigurationBean>{

	public BeanshellActivityTransferHandler(BeanshellActivityConfigurationBean bean,
			BeanshellActivity activity) {
		super(activity, bean);
	}

	@Override
	public DataFlavor getDataFlavor() {
		try {
			return new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
					+ ";class=net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean");
		} catch (ClassNotFoundException e) {
			//TODO sort out exceptions
		}
		return null;
	}

	@Override
	public ActivityTransferable getTransferable() {
		return new BeanshellActivityTransferable();
	}

}
