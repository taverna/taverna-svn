package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell;

import java.awt.datatransfer.DataFlavor;

import javax.swing.JComponent;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.ActivityDropTarget;

public class BeanshellActivityDropTarget extends ActivityDropTarget<BeanshellActivity, BeanshellActivityConfigurationBean>{

	public BeanshellActivityDropTarget(JComponent component) {
		super(component);
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

}
