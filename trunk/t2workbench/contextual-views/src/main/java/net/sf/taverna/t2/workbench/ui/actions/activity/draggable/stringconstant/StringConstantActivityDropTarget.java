package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant;

import java.awt.datatransfer.DataFlavor;

import javax.swing.JComponent;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.ActivityDropTarget;

/**
 * Handles the dropping of a StringConstant onto a StringConstantActivity
 * 
 * @author Ian Dunlop
 * 
 */
public class StringConstantActivityDropTarget extends ActivityDropTarget<StringConstantActivity, StringConstantConfigurationBean> {

	public StringConstantActivityDropTarget(JComponent component) {
		super(component);
	}

	@Override
	public DataFlavor getDataFlavor() {
		// TODO Auto-generated method stub
		try {
			return  new DataFlavor(
						DataFlavor.javaJVMLocalObjectMimeType
								+ ";class=net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
