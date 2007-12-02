/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

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

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;

import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.jdom.Element;

/**
 * @author alanrw
 * 
 */
public final class ActivitySubsetTree extends JTree implements
		DragGestureListener, DragSourceListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 741115733679972895L;

	public ActivitySubsetTree(final TreeModel treeModel) {
		super(treeModel);
		if (treeModel == null) {
			throw new NullPointerException("treeModel cannot be null"); //$NON-NLS-1$
		}
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY_OR_MOVE, this);
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 */
	@SuppressWarnings("unchecked")
	public void dragGestureRecognized(DragGestureEvent dge) {
		if (dge == null) {
			throw new NullPointerException("dge cannot be null"); //$NON-NLS-1$
		}
		// Get the node that was dragged
		Point l = dge.getDragOrigin();
		TreePath dragSourcePath = getPathForLocation((int) l.getX(), (int) l
				.getY());
		if (dragSourcePath != null) {
			PropertiedTreeNode<ProcessorFactory> node = (PropertiedTreeNode<ProcessorFactory>) dragSourcePath
					.getLastPathComponent();
			if (node instanceof PropertiedTreeObjectNode) {
				ProcessorFactory pf = ((PropertiedTreeObjectNode<ProcessorFactory>) node)
						.getObject();
				Element el = pf.getXMLFragment();
				String name = pf.getName();
				FactorySpecFragment fsf = new FactorySpecFragment(el, name);
				Transferable t = new SpecFragmentTransferable(fsf);
				dge.startDrag(DragSource.DefaultCopyDrop, t, this);
			}
		}
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

	@SuppressWarnings("unchecked")
	@Override
	public String getToolTipText(MouseEvent evt) {
		if (evt == null) {
			throw new NullPointerException("evt cannot be null"); //$NON-NLS-1$
		}
		String result = "No description available"; //$NON-NLS-1$
		TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
		if (curPath != null) {
			Object userObject = curPath.getLastPathComponent();
			if (userObject instanceof PropertiedTreeObjectNode) {
				PropertiedTreeObjectNode<ProcessorFactory> poNode = (PropertiedTreeObjectNode<ProcessorFactory>) userObject;
				ProcessorFactory pf = poNode.getObject();
				result = getProcessorFactoryDescription(pf);
			}
		}
		return result;
	}
	
	public static String getProcessorFactoryDescription(final ProcessorFactory pf) {
		if (pf == null) {
			throw new NullPointerException("pf cannot be null"); //$NON-NLS-1$
		}
		String result = "No description available"; //$NON-NLS-1$
			String description = pf.getDescription();
			if (description == null) {
				try {
					description = pf.createProcessor("foo", null) //$NON-NLS-1$
							.getDescription();
					if ((description != null) && !description.equals("")) { //$NON-NLS-1$
						pf.setDescription(description);
					}
				} catch (Exception ex) {
					result = "Unable to fetch description"; //$NON-NLS-1$
				}
			}
			if ((description != null) && !description.equals("")) { //$NON-NLS-1$
				result = description;
			}
		return result;
	}
}
