/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.shared;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

/**
 * Contains utility methods to deal with opening windows and suchlike in a way
 * that makes no assumptions about the existance of a JDesktop pane.
 * <p>Also contains, from version 1.5 onwards, methods to set and notify
 * components of changes to the underlying set of named models.
 * @author Tom Oinn
 */
public class UIUtils {

	/**
	 * At any given time there are zero or more named model objects over
	 * which the workbench UI is acting. 
	 */
	private static Map<String,Object> modelMap = 
		new Hashtable<String,Object>();
	
	/**
	 * Used as a modelName for setModel() and getNamedModel() - notes
	 * the current active workflow in the GUI.
	 *  
	 */
	public final static String CURRENT_WORKFLOW = "currentWorkflow";

	/**
	 * Set this to be notified of changes to the model map
	 */
	public static ModelChangeListener DEFAULT_MODEL_LISTENER = null;
	
	/**
	 * Manipulate the current model map
	 * @param modelName name of the model to act on
	 * @param model null to destroy the model or a reference to the
	 * new model to set. If it didn't already exist a modelCreated
	 * event will be fired otherwise modelChanged is called.
	 */
	public synchronized static void setModel(String modelName, Object model) {
		if (! modelMap.containsKey(modelName)) {
			if (model == null) {
				// removal of unknown model
				return;
			}
			// Store new model
			modelMap.put(modelName, model);
			if (DEFAULT_MODEL_LISTENER != null) {
				DEFAULT_MODEL_LISTENER.modelCreated(modelName, model);
			}
			return;
		}
		if (model == null) {
			// Remove existing model
			modelMap.remove(modelName);
			if (DEFAULT_MODEL_LISTENER != null) {
				DEFAULT_MODEL_LISTENER.modelDestroyed(modelName);
			}
			return;
		}
		// Replace existing model
		Object oldModel = modelMap.get(modelName);
		if (oldModel == model) {
			// No change
			return;
		}
		modelMap.put(modelName, model);
		if (DEFAULT_MODEL_LISTENER != null) {
			DEFAULT_MODEL_LISTENER.modelChanged(modelName, oldModel, model);
		}
	}

	
	/**
	 * Register with the UIUtils static class to inform workbench like
	 * systems that the underlying map of named model objects has been
	 * altered in some way.
	 * @author Tom Oinn
	 */
	public interface ModelChangeListener {

		/**
		 * Called when the named model is updated
		 * @param modelName name of the model that changed
		 * @param newModel new model object
		 * @param oldModel old model object it replaces
		 */
		public void modelChanged(String modelName, Object oldModel, Object newModel);

		/**
		 * Called when the named model is removed from the
		 * model map
		 * @param modelName
		 */
		public void modelDestroyed(String modelName);

		/**
		 * Called when a new model is created or inserted into the
		 * model map under a previously absent key
		 * @param modelName name of the new model
		 * @param model the new model object
		 */
		public void modelCreated(String modelName, Object model);
	
	}
	
	
	
	public static FrameCreator DEFAULT_FRAME_CREATOR = new FrameCreator() {
		public void createFrame(ScuflModel targetModel,
				UIComponentSPI targetComponent, int posX, int posY,
				int sizeX, int sizeY) {
			final UIComponentSPI component = targetComponent;
			final ScuflModel model = targetModel;
			JFrame newFrame = new JFrame(component.getName());
			newFrame.getContentPane().setLayout(new BorderLayout());
			newFrame.getContentPane().add(
					new JScrollPane((JComponent) targetComponent),
					BorderLayout.CENTER);
			newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			newFrame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					component.onDispose();
				}
			});
			if (component.getIcon() != null) {
				newFrame.setIconImage(component.getIcon().getImage());
			}
			if (component instanceof WorkflowModelViewSPI) {
				((WorkflowModelViewSPI)component).attachToModel(model);
			}
			newFrame.setSize(sizeX, sizeY);
			newFrame.setLocation(posX, posY);
			newFrame.setVisible(true);
		}
	};

	/**
	 * Implement this interface and set the DEFAULT_FRAME_CREATOR field to
	 * change the behaviour of the windowing system used by the Taverna
	 * Workbench
	 */
	public interface FrameCreator {
		public void createFrame(ScuflModel targetModel,
				UIComponentSPI targetComponent, int posX, int posY,
				int sizeX, int sizeY);
	}

	/**
	 * Create a top level window using the configured default frame creator. For
	 * platforms such as Mac OS X where the expected windowing behaviour is
	 * different from the default desktop pane the default frame creator can be
	 * overridden to produce whatever top level window is required - the
	 * Workbench class contains code to do this in the case of both OS X and
	 * other window systems where the desktop pane is not required.
	 * <p>
	 * This method will handle the appropriate logic to bind to and unbind from
	 * a model when given an implementation of the ScuflUIComponent interface.
	 * It's worth noting that, in addition to implementing this interface, any
	 * object passed in as the target component must also be a subclass of
	 * JComponent!
	 */
	public static void createFrame(ScuflModel targetModel,
			UIComponentSPI targetComponent, int posX, int posY, int sizeX,
			int sizeY) {
		DEFAULT_FRAME_CREATOR.createFrame(targetModel, targetComponent, posX,
				posY, sizeX, sizeY);
	}

	/**
	 * As for the method above but allows a non-ScuflUIComponent JComponent.
	 * Internally this component is wrapped up in a trivial ScuflUIComponent
	 * which entirely ignores the workflow model settings.
	 */
	public static void createFrame(JComponent rawComponent, int posX, int posY,
			int width, int height) {
		UIComponentSPI p = new WrapperFrame(rawComponent);
		createFrame(null, p, posX, posY, width, height);
	}

	/**
	 * Trivial implementation of ScuflUIComponent to wrap a JComponent, ignores
	 * all model handling methods.
	 */
	@SuppressWarnings("serial")
	static class WrapperFrame extends JPanel implements UIComponentSPI {
		public WrapperFrame(JComponent component) {
			super(new BorderLayout());
			add(component, BorderLayout.CENTER);
		}

		public ImageIcon getIcon() {
			return null;
		}

		public String getName() {
			return "";
		}

		public void onDisplay() {
			//
		}

		public void onDispose() {
			//
		}
	}
	
	/**
	 * Determines, if possible, the parent window of an ActionEvent. Usually determined via the JPopupMenu invoker.
	 * This is useful for making JOptionPane dialogues modal when diplayed from menu item.
	 * 
	 * @param ae
	 * @return Component, or null if it cannot be determined.
	 */
	public static Component getActionEventParentWindow(ActionEvent ae) {
		Component parent = null;
		if (ae.getSource() instanceof Component) {
			Component source=(Component)ae.getSource();
			if (source.getParent() instanceof JPopupMenu) {
				parent = ((JPopupMenu)source.getParent()).getInvoker();
			}
		}
		return parent;		
	}
	
	public static Object getNamedModel(String string) {
		return modelMap.get(string);
	}

}
