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
 * @author Tom Oinn
 */
public class UIUtils {	
	
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

}
