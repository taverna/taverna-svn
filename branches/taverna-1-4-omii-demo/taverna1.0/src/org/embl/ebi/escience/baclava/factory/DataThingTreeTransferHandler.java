/*
 * Created on Mar 18, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.embl.ebi.escience.baclava.factory;

import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.embl.ebi.escience.baclava.*;

/**
 * @author <a href="mailto:bleh">Kevin Glover </a>
 */
public class DataThingTreeTransferHandler implements DragGestureListener,
		DragSourceListener {
	private JTree tree;

	private DragSource dragSource; // dragsource

	// private static DefaultMutableTreeNode draggedNode;
	// private DefaultMutableTreeNode draggedNodeParent;

	public DataThingTreeTransferHandler(JTree tree, int action) {
		this.tree = tree;
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(tree, action, this);
	}

	/* Methods for DragSourceListener */
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// if (dsde.getDropSuccess() && dsde.getDropAction() ==
		// DnDConstants.ACTION_MOVE
		// && draggedNodeParent != null)
		// {
		// ((DefaultTreeModel)
		// tree.getModel()).nodeStructureChanged(draggedNodeParent);
		// }
	}

	private void setCursor(DragSourceDragEvent dsde) {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		} else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(
						DragSource.DefaultMoveDrop);
			} else {
				dsde.getDragSourceContext().setCursor(
						DragSource.DefaultMoveNoDrop);
			}
		}
	}

	public final void dragEnter(DragSourceDragEvent dsde) {
		setCursor(dsde);
	}

	public final void dragOver(DragSourceDragEvent dsde) {
		setCursor(dsde);
	}

	public final void dropActionChanged(DragSourceDragEvent dsde) {
		setCursor(dsde);
	}

	public final void dragExit(DragSourceEvent dse) {
		dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
	}

	/* Methods for DragGestureListener */
	public final void dragGestureRecognized(DragGestureEvent dge) {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			Object pathComponent = path.getLastPathComponent();
			if (pathComponent instanceof DataThingTreeNode) {
				DataThingTreeNode node = (DataThingTreeNode) pathComponent;
				DataThing dataThing = node.getNodeThing();
				dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, /*
																		 * image,
																		 * new
																		 * Point(0,
																		 * 0),
																		 */
				new TransferableDataThing(dataThing), this);

			}
		}
	}
}