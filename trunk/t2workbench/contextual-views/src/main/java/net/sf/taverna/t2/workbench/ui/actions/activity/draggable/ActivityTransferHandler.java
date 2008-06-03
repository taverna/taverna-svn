package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Creates the {@link ActivityTransferable} which is sent during a drag and drop
 * {@link Activity} operation
 * 
 * @author Ian Dunlop
 * 
 * @param <ActivityType>
 *            the class of Activity being transferred
 * @param <ConfigType>
 *            the bean class for the activity
 */
public abstract class ActivityTransferHandler<ActivityType, ConfigType> extends
		TransferHandler {

	private ActivityType activity;
	private ConfigType bean;

	public ActivityTransferHandler(ActivityType activity, ConfigType bean) {
		this.activity = activity;
		this.bean = bean;
	}

	/**
	 * Gets the {@link ActivityTransferable} which is transferred across during
	 * an {@link Activity} drag and drop operation
	 * 
	 * @return
	 */
	public abstract ActivityTransferable getTransferable();

	/**
	 * The type of DataFlavor that is transferred across during the drag and
	 * drop operation. eg. new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType +
	 * ";class=net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean");
	 * 
	 * @return
	 */
	public abstract DataFlavor getDataFlavor();

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		// FIXME should check for the correct flavor
		return super.canImport(comp, transferFlavors);
	}

	/**
	 * Calls overridden abstract method {@link #getTransferable()} to create and
	 * get the type of transferrable and then sets it with the activity and bean
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		ActivityTransferable transferable = getTransferable();
		transferable.setActivity(activity);
		transferable.setBean(bean);
		return transferable;
	}

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		// TODO Auto-generated method stub
		super.exportAsDrag(comp, e, action);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		// TODO Auto-generated method stub
		super.exportDone(source, data, action);
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		super.exportToClipboard(comp, clip, action);
	}

	@Override
	public int getSourceActions(JComponent c) {
		// TODO Auto-generated method stub
		return COPY_OR_MOVE;
	}

	@Override
	public Icon getVisualRepresentation(Transferable t) {
		// TODO Auto-generated method stub
		return super.getVisualRepresentation(t);
	}

	/**
	 * Gets the config bean transferred across based on the DataFlavor required ({@link #getDataFlavor()}
	 * and casts it as the appropriate ConfigType. Sets the {@link #bean} as
	 * this object
	 */
	@Override
	public boolean importData(JComponent comp, Transferable t) {

		try {
			bean = (ConfigType) t.getTransferData(getDataFlavor());
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * the config bean for the activity
	 * 
	 * @param bean
	 */
	public void setBean(ConfigType bean) {
		this.bean = bean;
	}

	/**
	 * The activity being transferred
	 * 
	 * @param activity
	 */
	public void setActivity(ActivityType activity) {
		this.activity = activity;
	}

	/**
	 * Get the activity which is transferred
	 * 
	 * @return
	 */
	public ActivityType getActivity() {
		return activity;
	}

	/**
	 * Get the bean being transferred
	 * 
	 * @return
	 */
	public ConfigType getBean() {
		return bean;
	}

}
