/*
 * ServiceTree.java
 *
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

/**
 * 
 * @author alperp
 */

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;

@SuppressWarnings("serial")
public class ServiceTree extends JTree implements DragGestureListener,
		DragSourceListener {	

	/** Creates a new instance of ServiceTree */
	public ServiceTree(TreeModel model) {
		super(model);

		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY_OR_MOVE, this);

		FetaResultRenderer renderer = new FetaResultRenderer();
		this.setCellRenderer(renderer);
		setShowsRootHandles(false);
		this.addMouseListener(new ServiceTreePopupHandler(this));

	}	

	public void dragGestureRecognized(DragGestureEvent e) {

		// Get the node that was dragged
		Point p = e.getDragOrigin();

		TreePath dragSourcePath = getPathForLocation((int) p.getX(), (int) p
				.getY());
		if (dragSourcePath != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) dragSourcePath
					.getLastPathComponent();
			Object userObject = node.getUserObject();
			if (userObject instanceof BasicServiceModel) {
				org.jdom.Element el = ((BasicServiceModel) userObject)
						.getTavernaProcessorSpecAsElement();
				FactorySpecFragment fsf = new FactorySpecFragment(el, node
						.toString());
				Transferable t = new SpecFragmentTransferable(fsf);
				e.startDrag(DragSource.DefaultCopyDrop, t, this);
			}
		}

	}

	public void dragDropEnd(DragSourceDropEvent e) {
		//
	}

	public void dragEnter(DragSourceDragEvent e) {
		//
	}

	public void dragExit(DragSourceEvent e) {
		//
	}

	public void dragOver(DragSourceDragEvent e) {
		//
	}

	public void dropActionChanged(DragSourceDragEvent e) {
		//
	}

}
