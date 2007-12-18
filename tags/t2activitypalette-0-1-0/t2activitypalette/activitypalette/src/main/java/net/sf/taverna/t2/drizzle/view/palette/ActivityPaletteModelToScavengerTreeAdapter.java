/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.palette;

import java.awt.Frame;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.HashSet;

import javax.swing.tree.TreeModel;

import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;

/**
 * A small class to enable an ActivityPaletteModel to appear like a ScavengerTree.
 * @author alanrw
 *
 */
public class ActivityPaletteModelToScavengerTreeAdapter implements ScavengerTree {
	
	private ActivityPaletteModel paletteModel;
	
	private ActivityPalettePanel representation;
	
	private HashSet<ScuflModel> seenModels;
	
	/**
	 * @param paletteModel
	 * @param representation
	 */
	public ActivityPaletteModelToScavengerTreeAdapter(final ActivityPaletteModel paletteModel,
			final ActivityPalettePanel representation) {
		if (paletteModel == null) {
			throw new NullPointerException("paletteModel cannot be null"); //$NON-NLS-1$
		}
		if (representation == null) {
			throw new NullPointerException("representation cannot be null"); //$NON-NLS-1$
		}
		this.paletteModel = paletteModel;
		this.representation = representation;
		this.seenModels = new HashSet<ScuflModel>();
	}

	/**
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTree#addScavenger(org.embl.ebi.escience.scuflui.workbench.Scavenger)
	 */
	public void addScavenger(Scavenger theScavenger) {
		if (theScavenger == null) {
			throw new NullPointerException("theScavenger cannot be null"); //$NON-NLS-1$
		}
		this.paletteModel.addScavenger(theScavenger);
	}

	/**
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTree#addScavengersFromModel()
	 */
	public void addScavengersFromModel() throws ScavengerCreationException {
		ScuflModel currentWorkflow = this.representation.getCurrentWorkflow();
		if ((currentWorkflow != null) && !this.seenModels.contains(currentWorkflow)){
			
			this.paletteModel.createScavengersFromModelThread(currentWorkflow);
			this.seenModels.add(currentWorkflow);
		}
	}
	
	public Frame getContainingFrame() {
		return this.representation.getContainingFrame();
	}

	public TreeModel getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNextCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isPopulating() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTree#scavengingDone()
	 */
	public void scavengingDone() {
		this.paletteModel.scavengingDone();
	}

	/**
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTree#scavengingStarting(java.lang.String)
	 */
	public void scavengingStarting(String message) {
		if (message == null) {
			throw new NullPointerException("message cannot be null"); //$NON-NLS-1$
		}
		this.paletteModel.scavengingStarting(message);
	}

	/**
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTree#setPopulating(boolean)
	 */
	public void setPopulating(boolean populating) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
	 */
	public void dragDropEnd(DragSourceDropEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dragEnter(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
	 */
	public void dragExit(DragSourceEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dragOver(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dropActionChanged(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragEnter(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragOver(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
