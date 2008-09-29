/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

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
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.ToolTipManager;

import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.jdom.Element;

/**
 * @author alanrw
 *
 */
public final class ActivitySubsetTable extends JTable implements
		DragGestureListener, DragSourceListener {
	
	private DragSource dragSource;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6721017677971353107L;

	/**
	 * 
	 */
	public ActivitySubsetTable() {
		this.dragSource = DragSource.getDefaultDragSource();
		this.dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	/**
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		if (dge == null) {
			throw new NullPointerException("dge cnanot be null"); //$NON-NLS-1$
		}
		Point l = dge.getDragOrigin();
		int dragSourceRow = rowAtPoint(l);
		if (dragSourceRow != -1) {
			ProcessorFactory pf = ((ActivitySubsetTableModel) getModel()).getRowObject(dragSourceRow).getTheFactory();
				Element el = pf.getXMLFragment();
				String name = pf.getName();
				FactorySpecFragment fsf = new FactorySpecFragment(el, name);
				Transferable t = new SpecFragmentTransferable(fsf);
				this.dragSource.startDrag(dge, DragSource.DefaultCopyDrop, t, this);
		}
	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
	 */
	public void dragDropEnd(DragSourceDropEvent dsde) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
	 */
	public void dragExit(DragSourceEvent dse) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dragOver(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
	 */
	@Override
	public String getToolTipText(MouseEvent evt) {
		if (evt == null) {
			throw new NullPointerException("evt cannot be null"); //$NON-NLS-1$
		}
		String result = "No description available"; //$NON-NLS-1$
		Point l = evt.getPoint();
		int dragSourceRow = rowAtPoint(l);
		if (dragSourceRow != -1) {
			ProcessorFactory pf = ((ActivitySubsetTableModel) getModel()).getRowObject(dragSourceRow).getTheFactory();
			result = ActivitySubsetTree.getProcessorFactoryDescription(pf);
		}
		return result;
	}
}
