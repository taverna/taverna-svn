/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.palette;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * @author alanrw
 *
 */
public final class KindKeyList extends JList implements DragSourceListener,
DragGestureListener, DropTargetListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4989449280785296705L;
	/**
	 * 
	 */

	private int fromIndex = -1;
	private int toIndex = -1;

	public KindKeyList(DefaultListModel model) {
		super(model);
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY_OR_MOVE, this);
		new DropTarget(this, this);
	}

	public void dragGestureRecognized(DragGestureEvent arg0) {
		this.fromIndex = this.locationToIndex(arg0.getDragOrigin());
	}

	public void dragEnter(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragExit(DropTargetEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragOver(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void drop(DropTargetDropEvent arg0) {
		if (this.fromIndex != -1) {
			this.toIndex = this.locationToIndex(arg0.getLocation());
			DefaultListModel model = (DefaultListModel) this.getModel();
			Object o = model.getElementAt(this.fromIndex);
			model.removeElementAt(this.fromIndex);
			model.insertElementAt(o, this.toIndex);
		}
		this.fromIndex = -1;
		this.toIndex = -1;
	}

	public void dropActionChanged(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragDropEnd(DragSourceDropEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragEnter(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragExit(DragSourceEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dragOver(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dropActionChanged(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
