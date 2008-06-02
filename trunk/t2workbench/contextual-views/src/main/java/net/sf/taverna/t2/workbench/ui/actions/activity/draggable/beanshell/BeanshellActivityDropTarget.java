package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class BeanshellActivityDropTarget extends DropTarget {

	private JComponent component;
	private DataFlavor dataFlavor;

	public BeanshellActivityDropTarget(JComponent component) {
		this.component = component;
		try {
			this.dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void drop(DropTargetDropEvent dtde) {
		try {
			List transferData = (List) dtde.getTransferable().getTransferData(
					dataFlavor);

			BeanshellActivityConfigurationBean bean = (BeanshellActivityConfigurationBean) transferData
					.get(0);
			if (bean != null) {

				BeanshellActivityTransferHandler th = (BeanshellActivityTransferHandler) component
						.getTransferHandler();
				th.setBean(bean);
			}

			Activity activity = (Activity) transferData.get(1);
			if (activity != null) {
				BeanshellActivityTransferHandler th = (BeanshellActivityTransferHandler) component
						.getTransferHandler();
				th.setActivity(activity);
				activity.configure(bean);
			}
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
