package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

/**
 * 
 * Associate the component with a {@link DropTarget} to handle the action when
 * something is dropped on it. Create the {@link DataFlavor} which this target
 * will accept.
 * 
 * @author Ian Dunlop
 * 
 * @param <ActivityType>
 *            the class of activity the drop handles
 * @param <ConfigType>
 *            the configuration bean for the activity
 */
public abstract class ActivityDropTarget<ActivityType, ConfigType> extends
		DropTarget {

	private JComponent component;

	/**
	 * Handles the action when an {@link Activity} is dropped on a
	 * {@link JComponent}
	 * 
	 * @param component
	 *            the target for the drop
	 */
	public ActivityDropTarget(JComponent component) {
		this.component = component;
	}

	/**
	 * What type of objects the component accepts
	 * 
	 * @return the flavor of drop the component likes
	 */
	public abstract DataFlavor getDataFlavor();

	/**
	 * Sets the activity and configuration bean associated with the drop
	 * component to whatever was in the payload. If the drop was successful then
	 * add a mouse listener to the component so we can click on it and get the
	 * appropriate {@link ContextualView}
	 */
	@Override
	public synchronized void drop(DropTargetDropEvent dtde) {
		try {
			List transferData = (List) dtde.getTransferable().getTransferData(
					getDataFlavor());

			ConfigType bean = (ConfigType) transferData.get(0);
			if (bean != null) {

				ActivityTransferHandler th = (ActivityTransferHandler) component
						.getTransferHandler();
				th.setBean(bean);
			}

			Activity activity = (Activity) transferData.get(1);
			if (activity != null) {
				ActivityTransferHandler th = (ActivityTransferHandler) component
						.getTransferHandler();
				th.setActivity(activity);
				activity.configure(bean);
			}
			checkActivityMouseListenerPresent();
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

	/**
	 * Check for the presence of an {@link ActivityMouseListener}. If there is
	 * not one then add it. This {@link ActivityMouseListener} handles the pop
	 * up of the appropriate {@link ContextualView}. We may need to add one
	 * here because when the Activity draggable component is created we may not
	 * want it to pop up a {@link ContextualView} from its original position.
	 * eg. if it is in the activity palette
	 */
	private void checkActivityMouseListenerPresent() {
		MouseListener[] mouseListeners = component.getMouseListeners();
		if (mouseListeners.length == 0) {
			ActivityMouseListener mouseListener = new ActivityMouseListener(
					component);
			component.addMouseListener(mouseListener);
		}
		boolean activityMouseListenerPresent = false;
		for (MouseListener listener : mouseListeners) {
			if (listener instanceof ActivityMouseListener) {
				activityMouseListenerPresent = true;
				break;
			}
		}
		if (!activityMouseListenerPresent) {
			ActivityMouseListener mouseListener = new ActivityMouseListener(
					component);
			component.addMouseListener(mouseListener);
		}
	}

}
