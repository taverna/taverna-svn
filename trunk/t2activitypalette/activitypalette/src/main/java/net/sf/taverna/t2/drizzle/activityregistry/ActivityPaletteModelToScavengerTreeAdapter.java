/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.Container;
import java.awt.Frame;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreeModel;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelperRegistry;

/**
 * A small class to enable an ActivityPaletteModel to appear like a ScavengerTree.
 * @author alanrw
 *
 */
public class ActivityPaletteModelToScavengerTreeAdapter implements ScavengerTree {
	
	private ActivityPaletteModel paletteModel;
	
	private ActivityPalettePanel representation;
	
	public ActivityPaletteModelToScavengerTreeAdapter(final ActivityPaletteModel paletteModel,
			final ActivityPalettePanel representation) {
		this.paletteModel = paletteModel;
		this.representation = representation;
	}

	public void addScavenger(Scavenger theScavenger) {
		this.paletteModel.addScavenger(theScavenger);
	}

	public void addScavengersFromModel() throws ScavengerCreationException {
		if (representation.getCurrentWorkflow() != null) {
			paletteModel.createScavengersFromModelThread(representation.getCurrentWorkflow());
		}
	}
	
	public Frame getContainingFrame() {
		Container result = this.representation;
		while ((result != null) && !(result instanceof Frame)) {
			result = result.getParent();
		}
		return (Frame) result;
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

	public void scavengingDone() {
		paletteModel.scavengingDone();
	}

	public void scavengingStarting(String message) {
		paletteModel.scavengingStarting(message);
	}

	public void setPopulating(boolean populating) {
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

	public void dragGestureRecognized(DragGestureEvent arg0) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	public void dropActionChanged(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
