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
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
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
	
	private DragSource dragSource;

	/**
	 * 
	 */
	private static final long serialVersionUID = 741115733679972895L;

	/**
	 * 
	 */
	public ActivitySubsetTree() {
		this.dragSource = DragSource.getDefaultDragSource();
		this.dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY, this);
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	/**
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
			PropertiedTreeNode<ProcessorFactoryAdapter> node = (PropertiedTreeNode<ProcessorFactoryAdapter>) dragSourcePath
					.getLastPathComponent();
			if (node instanceof PropertiedTreeObjectNode) {
				ProcessorFactoryAdapter adapter = ((PropertiedTreeObjectNode<ProcessorFactoryAdapter>) node)
						.getObject();
				ProcessorFactory pf = adapter.getTheFactory();
				Element el = pf.getXMLFragment();
				String name = pf.getName();
				FactorySpecFragment fsf = new FactorySpecFragment(el, name);
				Transferable t = new SpecFragmentTransferable(fsf);
				try {
					this.dragSource.startDrag(dge, DragSource.DefaultCopyDrop, t, this);
				} catch (InvalidDnDOperationException e) {
					e.printStackTrace();
					// TODO Figure out what causes this
				}
			}
		}
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
	 * @see javax.swing.JTree#getToolTipText(java.awt.event.MouseEvent)
	 */
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
				PropertiedTreeObjectNode<ProcessorFactoryAdapter> adapterNode = (PropertiedTreeObjectNode<ProcessorFactoryAdapter>) userObject;
				net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter adapter = adapterNode.getObject();
				ProcessorFactory pf = adapter.getTheFactory();
				result = getProcessorFactoryDescription(pf);
			}
		}
		return result;
	}
	
	/**
	 * @param pf
	 * @return
	 */
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
