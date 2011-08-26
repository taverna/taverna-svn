/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
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
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.TreeTableModelView;
import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.ProcessorSpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.treetable.JTreeTable;
import org.embl.ebi.escience.treetable.TreeTableModel;
import org.jdom.Element;

/**
 * A swing component that provides an expandable tree view of the constituent
 * components of a ScuflModel instance. This extends the normal scufl model
 * explorer to add a treetable view over the reliablity features.
 * 
 * @author Tom Oinn
 */
public class ScuflModelTreeTable extends JTreeTable implements
		ScuflModelEventListener, ScuflUIComponent, DropTargetListener,
		DragSourceListener, DragGestureListener {

	private static Logger logger = Logger.getLogger(ScuflModelTreeTable.class);

	// The ScuflModel that this is a view / controller over
	ScuflModel model = null;

	Processor lastInterestingProcessor = null;

	TreeTableModelView treeModel = new TreeTableModelView();

	/**
	 * Default constructor, creates a new ScuflModelExplorer that is not bound
	 * to any ScuflModel instance. Use the attachToModel method to actually show
	 * data in this component.
	 */
	public ScuflModelTreeTable() {
		super();
		// Set up the drag listener
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY_OR_MOVE, this);
		// Set up the drop listener
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		setModel(treeModel);
		TableColumnModel columnModel = getColumnModel();

		for (int i = 1; i < 6; i++) {
			TableColumn c = columnModel.getColumn(i);
			// c.sizeWidthToFit();
			c.setResizable(false);
			c.setMaxWidth(50);
			c.setMinWidth(50);
			c.setPreferredWidth(50);
		}
		setDefaultEditor(TreeTableModel.class,
				new ScuflModelTreeTableCellEditor());
		JCheckBox jcbox = new JCheckBox();
		jcbox.setOpaque(true);
		jcbox.setBackground(getSelectionBackground());
		jcbox.setHorizontalAlignment(SwingConstants.CENTER);
		setDefaultEditor(Boolean.class, new DefaultCellEditor(jcbox));
		setDefaultRenderer(Boolean.class, new TableCheckbox());
		// Attach the popup menu generator to the tree
		this.addMouseListener(new ScuflModelExplorerPopupHandler(this));
		// Show lines in the tree diagram
		this.tree.putClientProperty("JTree.lineStyle", "Angled");
		final ScuflModelExplorerRenderer renderer = new ScuflModelExplorerRenderer();
		this.tree.setCellRenderer(renderer);
		// this.addMouseMotionListener(new
		// ScuflModelExplorerDragHandler(this.tree));
		// this.setDragEnabled(true);
		// this.tree.setRowHeight(0);
		getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}
						ListSelectionModel lsm = (ListSelectionModel) e
								.getSource();
						if (lsm.isSelectionEmpty()) {
							renderer.setSignificant(null);
						} else {
							int selectedRow = lsm.getMinSelectionIndex();
							JTree tree = getTree();
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
									.getPathForRow(selectedRow)
									.getLastPathComponent();
							renderer.setSignificant(node.getUserObject());
						}
						repaint();
					}
				});
	}

	/**
	 * Recognize the drag gesture, allow if the tree column returns a processor
	 * node
	 */
	public void dragGestureRecognized(DragGestureEvent e) {
		Point p = e.getDragOrigin();
		int x = (int) p.getX();
		int y = (int) p.getY();
		// Transform drag start coordinates into those of the
		// tree component
		for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
			if (getColumnClass(counter) == TreeTableModel.class) {
				x = x - getCellRect(0, counter, true).x;
				break;
			}
		}
		// What node did the drag originate from?
		TreePath dragFromPath = tree.getPathForLocation(x, y);
		if (dragFromPath == null) {
			// No node dragged from
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) dragFromPath
				.getLastPathComponent();
		Object o = node.getUserObject();
		if (o instanceof Processor) {
			Processor dragSource = (Processor) o;
			// Is it an alternate?
			if (dragSource.getModel() == null) {
				return;
			}
			Element el = ProcessorHelper.elementForProcessor(dragSource);
			ProcessorSpecFragment psf = new ProcessorSpecFragment(el,
					dragSource.getName());
			Transferable t = new SpecFragmentTransferable(psf);
			e.startDrag(DragSource.DefaultCopyDrop, t, this);
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

	/**
	 * The editor class is aware that the processor nodes should be edited by
	 * using the getName method rather than toString to fetch their initial
	 * value
	 */
	public class ScuflModelTreeTableCellEditor extends DefaultCellEditor {

		public ScuflModelTreeTableCellEditor() {
			super(new TreeTableTextField());
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int r, int c) {
			Component component = super.getTableCellEditorComponent(table,
					value, isSelected, r, c);
			JTree t = getTree();
			boolean rv = t.isRootVisible();
			int offsetRow = rv ? r : r - 1;
			Rectangle bounds = t.getRowBounds(offsetRow);
			int offset = bounds.x;
			TreeCellRenderer tcr = t.getCellRenderer();
			if (tcr instanceof DefaultTreeCellRenderer) {
				Object node = t.getPathForRow(offsetRow).getLastPathComponent();
				boolean isExpanded = t.isExpanded(t.getPathForRow(offsetRow));
				boolean isLeaf = t.getModel().isLeaf(node);
				Component renderer = tcr.getTreeCellRendererComponent(t, node,
						true, isExpanded, isLeaf, offsetRow, true);
				Icon icon = ((JLabel) renderer).getIcon();
				// if (t.getModel().isLeaf(node))
				// icon = ((DefaultTreeCellRenderer)tcr).getLeafIcon();
				// else if (tree.isExpanded(offsetRow))
				// icon = ((DefaultTreeCellRenderer)tcr).getOpenIcon();
				// else
				// icon = ((DefaultTreeCellRenderer)tcr).getClosedIcon();
				if (icon != null) {
					offset += ((DefaultTreeCellRenderer) tcr).getIconTextGap()
							+ icon.getIconWidth();
				}
				Object uo = ((DefaultMutableTreeNode) node).getUserObject();
				if (uo instanceof Processor) {
					String currentName = ((Processor) uo).getName();
					((TreeTableTextField) getComponent()).setText(currentName);
				} else if (uo instanceof InputPort) {
					InputPort ip = (InputPort) uo;
					if (ip.hasDefaultValue()) {
						((TreeTableTextField) getComponent()).setText(ip
								.getDefaultValue());
					} else {
						((TreeTableTextField) getComponent()).setText("");
					}
				}
			}
			((TreeTableTextField) getComponent()).offset = offset;
			return component;
		}

		/**
		 * This is overridden to forward the event to the tree. This will return
		 * true if the click count >= 3, or the event is null.
		 */
		public boolean isCellEditable(EventObject e) {
			// Edit on double click rather than the default triple
			if (e instanceof MouseEvent) {
				MouseEvent me = (MouseEvent) e;
				if (me.getClickCount() == 1
						&& System.getProperties().getProperty(
								"taverna.osxpresent") != null) {
					for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
						if (getColumnClass(counter) == TreeTableModel.class) {
							MouseEvent newME = new MouseEvent(tree, me.getID(),
									me.getWhen(), me.getModifiers(), me.getX()
											- getCellRect(0, counter, true).x,
									me.getY(), me.getClickCount(), me
											.isPopupTrigger());
							logger.debug(newME);
							tree.dispatchEvent(newME);

							Point p = new Point(me.getX(), me.getY());
							int row = rowAtPoint(p);
							int column = columnAtPoint(p);
							if (column == 0) {
								boolean isExpanded = tree.isExpanded(tree
										.getPathForRow(row));
								if (isExpanded == false) {
									tree.expandPath(tree.getPathForRow(row));
								} else {
									tree.collapsePath(tree.getPathForRow(row));
								}
							}

							break;
						}
					}

				}
				if (me.getClickCount() >= 3) {
					return true;
				}
			}
			if (e == null) {
				return true;
			}
			return false;
		}

	}

	public void drop(DropTargetDropEvent e) {
		try {
			DataFlavor f = SpecFragmentTransferable.factorySpecFragmentFlavor;
			Transferable t = e.getTransferable();
			if (e.isDataFlavorSupported(f)) {
				// Have something of type factorySpecFragmentFlavor;
				FactorySpecFragment fsf = (FactorySpecFragment) t
						.getTransferData(f);
				// logger.debug("Drop of "+fsf.getFactoryNodeName());
				// Get the tree path which the drop has landed on, if there is
				// one.
				Point p = e.getLocation();
				int x = (int) p.getX();
				int y = (int) p.getY();
				// Transform drop coordinates to the coordinates of the JTree
				// contained
				// by the treetable
				for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
					if (getColumnClass(counter) == TreeTableModel.class) {
						x = x - getCellRect(0, counter, true).x;
						break;
					}
				}
				TreePath path = tree.getPathForLocation(x, y);
				if (path != null
						&& path.getPathCount() > 2
						&& ((DefaultMutableTreeNode) path.getPathComponent(2))
								.getUserObject() instanceof Processor) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
							.getPathComponent(2);
					logger.debug(node);
					Processor target = (Processor) node.getUserObject();
					Element wrapperElement = new Element("wrapper");
					wrapperElement.addContent(fsf.getElement());
					Processor alternateProcessor = ProcessorHelper
							.loadProcessorFromXML(wrapperElement, null,
									"alternate");
					AlternateProcessor ap = new AlternateProcessor(
							alternateProcessor);
					target.addAlternate(ap);
					// Top level processor nodes are always located at
					// Workflow/Processors/<NAME>
					// so if the path is length 3 or more, and the node at
					// position 3 is a processor
					// then we've been dragged into a processor and should
					// create an alternate.
				} else {
					logger.debug("No node under the drop.");
					// Just add the node to the model as a new processor
					String validName = model.getValidProcessorName(fsf
							.getFactoryNodeName());
					Element wrapperElement = new Element("wrapper");
					wrapperElement.addContent(fsf.getElement());

					Processor newProcessor = ProcessorHelper
							.loadProcessorFromXML(wrapperElement, model,
									validName);
					model.addProcessor(newProcessor);
				}
				e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			}
		} catch (Exception ex) {
			e.rejectDrop();
		}
	}

	public void dragEnter(DropTargetDragEvent e) {
		//
	}

	public void dragExit(DropTargetEvent e) {
		//
	}

	public void dragOver(DropTargetDragEvent e) {
		//
	}

	public void dropActionChanged(DropTargetDragEvent e) {
		//
	}

	/**
	 * Set the default expansion state, with all processors, data constraints
	 * and workflow source and sink ports show, but nothing else.
	 */
	public void setDefaultExpansionState() {
		synchronized (this.treeModel) {
			pathToSelect = null;
			expandAll(this.tree, new TreePath(this.treeModel.getRoot()), true);
			if (pathToSelect != null) {
				// this.tree.setSelectionPath(pathToSelect);
			}
		}
	}

	TreePath pathToSelect = null;

	private void expandAll(JTree tree, TreePath parent, boolean expand) {
		synchronized (this.treeModel) {
			// Traverse children
			// Ignores nodes who's userObject is a Processor type to
			// avoid overloading the UI with nodes at startup.
			TreeNode node = (TreeNode) parent.getLastPathComponent();
			if (node.getChildCount() >= 0
					&& (((DefaultMutableTreeNode) node).getUserObject() instanceof Processor == false)) {
				for (Enumeration e = node.children(); e.hasMoreElements();) {
					TreeNode n = (TreeNode) e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					if (((DefaultMutableTreeNode) n).getUserObject() instanceof Processor) {
						Processor p = (Processor) (((DefaultMutableTreeNode) n)
								.getUserObject());

						if (p == lastInterestingProcessor) {
							pathToSelect = path;
							expandAll(tree, path, expand);
						}
					} else {
						expandAll(tree, path, expand);
					}

				}
			}
			// Expansion or collapse must be done bottom-up
			if (expand) {
				tree.expandPath(parent);
			} else {
				tree.collapsePath(parent);
			}
		}
	}

	/**
	 * Bind this view onto a ScuflModel instance, this registers the view to
	 * receive events and thus keep up to date.
	 */
	public void attachToModel(ScuflModel theModel) {
		this.model = theModel;
		treeModel.attachToModel(theModel);
		theModel.addListener(this.treeModel);
		theModel.addListener(this);

		setDefaultExpansionState();
	}

	/**
	 * Unbind from the current model, does nothing if we're not bound to a
	 * model.
	 */
	public void detachFromModel() {
		if (this.model != null) {
			this.model.removeListener(this);
			this.model.removeListener(this.treeModel);
		}
	}

	/**
	 * Handle events from the model in order to keep up to date with any changes
	 * in state.
	 */
	public synchronized void receiveModelEvent(ScuflModelEvent event) {
		if (event.getSource() instanceof Processor) {
			lastInterestingProcessor = (Processor) (event.getSource());
		} else if (event.getSource() instanceof DataConstraint) {
			DataConstraint dc = (DataConstraint) (event.getSource());
			Port sourcePort = dc.getSource();
			// the way that xml splitters are added together with their
			// DataConstraint causes display problems
			// if its expanded immediately, so this suppresses this if its type
			// 'text/xml'.
			if (!sourcePort.getSyntacticType().equals("'text/xml'"))
				lastInterestingProcessor = sourcePort.getProcessor();
		}
		// ((AbstractTableModel)(super.getModel())).fireTableDataChanged();
		setDefaultExpansionState();
		repaint();
	}

	/**
	 * Return a preferred name for windows containing this component
	 */
	public String getName() {
		return "Scufl Model Explorer";
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowExplorer;
	}

	class TableCheckbox extends JCheckBox implements TableCellRenderer {
		TableCheckbox() {
			setHorizontalAlignment(SwingConstants.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value != null) {
				setSelected(((Boolean) value).booleanValue());
				if (isSelected) {
					setForeground(ScuflModelTreeTable.this
							.getSelectionForeground());
					setBackground(ScuflModelTreeTable.this
							.getSelectionBackground());
				} else {
					setForeground(ScuflModelTreeTable.this.getForeground());
					setBackground(ScuflModelTreeTable.this.getBackground());
				}
				return this;
			}
			JLabel label = new JLabel("");
			label.setOpaque(true);
			if (isSelected) {
				label.setForeground(ScuflModelTreeTable.this
						.getSelectionForeground());
				label.setBackground(ScuflModelTreeTable.this
						.getSelectionBackground());
			} else {
				label.setForeground(ScuflModelTreeTable.this.getForeground());
				label.setBackground(ScuflModelTreeTable.this.getBackground());
			}
			if (hasFocus) {
				label.setBorder(UIManager
						.getBorder("Table.focusCellHighlightBorder"));
			} else {
				label.setBorder(null);
			}
			return label;
		}
	}
}
