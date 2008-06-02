package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;

import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

/**
 * Handles the dropping of a StringConstant onto a StringConstantActivity
 * 
 * @author Ian Dunlop
 * 
 */
public class StringConstantActivityDropTarget extends DropTarget {

	private JComponent component;
	private DataFlavor dataFlavor;
	private String value;

	/**
	 * Associate the component with a {@link DropTarget} to handle the action
	 * when something is dropped on it. Create the {@link DataFlavor} which this
	 * target will accept, in this case it is
	 * {@link StringConstantConfigurationBean}
	 * 
	 * @param component
	 */
	public StringConstantActivityDropTarget(JComponent component) {
		try {
			this.dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.component = component;

	}

	/**
	 * Get the activity and the bean from the transferable. Reconfigure the
	 * activity at the receiving object with the new bean
	 */
	@Override
	public synchronized void drop(DropTargetDropEvent dtde) {
		try {
			List transferData = (List) dtde.getTransferable().getTransferData(
					dataFlavor);
			StringConstantConfigurationBean bean = (StringConstantConfigurationBean) transferData
					.get(0);

			String value = bean.getValue();
			if (value != null) {
				setNewText(value);
				StringConstantTransferHandler th = (StringConstantTransferHandler) component
						.getTransferHandler();
				th.setBean(bean);
			}
			Activity activity = (Activity) transferData.get(1);
			if (activity != null) {
				StringConstantTransferHandler th = (StringConstantTransferHandler) component
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

	private void setNewText(String value) {
		this.value = value;
	}
}
