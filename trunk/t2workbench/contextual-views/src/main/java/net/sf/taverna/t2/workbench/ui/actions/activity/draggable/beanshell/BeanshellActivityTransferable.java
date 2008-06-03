package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell;

import java.awt.datatransfer.DataFlavor;

import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.ActivityTransferable;

public class BeanshellActivityTransferable extends ActivityTransferable{
	
	private DataFlavor dataFlavor;

	@Override
	public DataFlavor getDataFlavor() {
		return dataFlavor;
	}
	
	public void createDataFlavor() {
		try {
			dataFlavor  = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
					+ ";class=net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
