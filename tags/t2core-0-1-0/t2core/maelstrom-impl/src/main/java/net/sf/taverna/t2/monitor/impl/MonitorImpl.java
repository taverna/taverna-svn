package net.sf.taverna.t2.monitor.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.*;

import net.sf.taverna.t2.monitor.Monitor;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.NoSuchPropertyException;

/**
 * A relatively naive implementation of the Monitor interface which holds all
 * state in a tree model. Use getMonitor() to get the monitor singleton, all
 * workflows under a given JVM use the same instance in this implementation with
 * the root node of the monitor tree corresponding to the monitor itself.
 * <p>
 * Internally we use a default tree model with default mutable tree nodes where
 * the user object is set to instances of MonitorNode, with the exception of the
 * 'true' root of the tree in which it is set to the MonitorImpl itself
 * 
 * @author Tom Oinn
 * 
 */
public final class MonitorImpl implements Monitor {

	// ############################################################
	// # Static code here
	// ############################################################
	private static Monitor monitorSingleton = null;
	private static boolean isEnabled = false;
	private static long deregisterDelay = 1000;

	/**
	 * Very simple UI!
	 */
	public static void showMonitorFrame() {
		MonitorImpl m = (MonitorImpl) (MonitorImpl.getMonitor());
		final JTree tree = m.new AlwaysOpenJTree(m.monitorTree);
		final JScrollPane jsp = new JScrollPane(tree);
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(jsp);
		frame.pack();
		frame.setVisible(true);
		new javax.swing.Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				jsp.repaint();
			}
		}).start();
	}

	/**
	 * Returns a tree view over the monitor.
	 * 
	 * @return a tree view over the monitor
	 */
	public static JTree getJTree() {
		MonitorImpl m = (MonitorImpl) (MonitorImpl.getMonitor());
		return m.new AlwaysOpenJTree(m.monitorTree);
	}

	/**
	 * By default the monitor is linked to the code but disabled, mostly so our
	 * unit tests don't all suddenly change or require the monitor to be working
	 * correctly!
	 * 
	 * @param enable
	 */
	public static void enableMonitoring(boolean enable) {
		isEnabled = enable;
	}

	/**
	 * Nodes will be removed at least delayTime milliseconds after their initial
	 * deregistration request, this allows UI components to show nodes which
	 * would otherwise vanish almost instantaneously.
	 * 
	 * @param delayTime
	 *            time in milliseconds between the deregistration request and
	 *            attempt to actually remove the node in question
	 */
	public static void setNodeRemovalDelay(long delayTime) {
		deregisterDelay = delayTime;
	}

	/**
	 * Get the monitor singleton
	 * 
	 * @return
	 */
	public synchronized static Monitor getMonitor() {
		if (monitorSingleton == null) {
			monitorSingleton = new MonitorImpl();
		}
		return monitorSingleton;
	}

	// ############################################################
	// # Static code ends
	// ############################################################

	private DefaultTreeModel monitorTree;

	private java.util.Timer nodeRemovalTimer;

	private MonitorImpl() {
		monitorTree = new DefaultTreeModel(new DefaultMutableTreeNode(this));
		// Create the node removal timer as a daemon thread
		nodeRemovalTimer = new java.util.Timer(true);
	}

	/**
	 * Request the removal of the specified node from the monitor tree. In this
	 * particular case the removal task will be added to a timer and executed at
	 * some (slightly) later time as determined by the removalDelay property.
	 */
	public void deregisterNode(String[] owningProcess) {
		if (isEnabled) {
			// System.out.println("Remove node @" +
			// printProcess(owningProcess));
			final DefaultMutableTreeNode nodeToRemove = nodeAtProcessPath(
					owningProcess, -1);
			((MonitorNodeImpl) nodeToRemove.getUserObject()).expire();
			nodeRemovalTimer.schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					synchronized (monitorTree) {
						monitorTree.removeNodeFromParent(nodeToRemove);
					}
				}
			}, deregisterDelay);
		}
	}

	private String printProcess(String[] process) {
		StringBuffer sb = new StringBuffer();
		for (String part : process) {
			sb.append("{" + part + "}");
		}
		return sb.toString();
	}

	class MonitorNodeImpl implements MonitorNode {

		private Object workflowObject;
		private String[] owningProcess;
		private Set<MonitorableProperty<?>> properties;
		private boolean expired = false;

		MonitorNodeImpl(Object workflowObject, String[] owningProcess,
				Set<MonitorableProperty<?>> properties) {
			this.properties = properties;
			this.workflowObject = workflowObject;
			this.owningProcess = owningProcess;
		}

		public void expire() {
			expired = true;
		}

		public String[] getOwningProcess() {
			return owningProcess;
		}

		/**
		 * Return an unmodifiable copy of the property set
		 */
		public Set<? extends MonitorableProperty<?>> getProperties() {
			return Collections.unmodifiableSet(properties);
		}

		public Object getWorkflowObject() {
			return workflowObject;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(getWorkflowObject().getClass().getSimpleName());
			sb.append(", ");
			sb.append(owningProcess[owningProcess.length - 1]);
			sb.append(" : ");
			for (MonitorableProperty<?> prop : getProperties()) {
				int i = 0;
				for (String nameElement : prop.getName()) {
					sb.append(nameElement);
					i++;
					if (i < prop.getName().length) {
						sb.append(".");
					}
				}
				sb.append("=");
				try {
					sb.append(prop.getValue().toString());
				} catch (NoSuchPropertyException nspe) {
					sb.append("EXPIRED");
				}
				sb.append(" ");
			}
			return sb.toString();
		}

		Date creationDate = new Date();

		public Date getCreationDate() {
			return creationDate;
		}

		public void addMonitorableProperty(MonitorableProperty<?> newProperty) {
			properties.add(newProperty);

		}

		public boolean hasExpired() {
			return this.expired;
		}

	}

	/**
	 * Create a new node in the monitor
	 */
	public void registerNode(final Object workflowObject,
			final String[] owningProcess,
			final Set<MonitorableProperty<?>> properties) {
		if (isEnabled) {
			// System.out.println("Registering node "
			// + printProcess(owningProcess));
			// Create a new MonitorNode
			final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					new MonitorNodeImpl(workflowObject, owningProcess,
							properties));
			synchronized (monitorTree) {
				final MutableTreeNode parentNode = nodeAtProcessPath(
						owningProcess, owningProcess.length - 1);
				monitorTree.insertNodeInto(newNode, parentNode, monitorTree
						.getChildCount(parentNode));
			}
		}
	}

	/**
	 * Inject properties into an existing node
	 */
	public void addPropertiesToNode(String[] owningProcess,
			Set<MonitorableProperty<?>> newProperties) {
		try {
			DefaultMutableTreeNode node = nodeAtProcessPath(owningProcess, -1);
			MonitorNode mn = (MonitorNode) node.getUserObject();
			for (MonitorableProperty<?> prop : newProperties) {
				mn.addMonitorableProperty(prop);
			}
		} catch (IndexOutOfBoundsException ioobe) {
			// Fail silently here, the node wasn't found in the state tree
		}

		// TODO Auto-generated method stub

	}

	/**
	 * Return the node pointed to by the first 'limit' number of elements in the
	 * owning process string array. If limit is -1 then use owningProcess.length
	 * 
	 * @param owningProcess
	 * @param limit
	 * @return
	 */
	private DefaultMutableTreeNode nodeAtProcessPath(String[] owningProcess,
			int limit) throws IndexOutOfBoundsException {
		if (limit == -1) {
			limit = owningProcess.length;
		}
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) monitorTree
				.getRoot();
		for (int index = 0; index < limit && index < owningProcess.length; index++) {
			boolean found = false;
			for (int childIndex = 0; childIndex < monitorTree
					.getChildCount(currentNode)
					&& !found; childIndex++) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) monitorTree
						.getChild(currentNode, childIndex);
				MonitorNode childMonitorNode = (MonitorNode) childNode
						.getUserObject();
				if (childMonitorNode.getOwningProcess()[index]
						.equals(owningProcess[index])) {
					currentNode = childNode;
					found = true;
					// break;
				}
			}
			if (!found) {
				throw new IndexOutOfBoundsException(
						"Cannot locate node with process ID "
								+ printProcess(owningProcess));
			}
		}
		return currentNode;
	}

	class AlwaysOpenJTree extends JTree {

		private static final long serialVersionUID = -3769998854485605447L;

		public AlwaysOpenJTree(TreeModel newModel) {
			super(newModel);
			setRowHeight(18);
			setLargeModel(true);
			setEditable(false);
			setExpandsSelectedPaths(false);
			setDragEnabled(false);
			setScrollsOnExpand(false);
			setSelectionModel(EmptySelectionModel.sharedInstance());
			setCellRenderer(new DefaultTreeCellRenderer() {

				private static final long serialVersionUID = 7106767124654545039L;

				@Override
				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean sel, boolean expanded,
						boolean leaf, int row, boolean hasFocus) {
					super.getTreeCellRendererComponent(tree, value, sel,
							expanded, leaf, row, hasFocus);
					if (value instanceof DefaultMutableTreeNode) {
						Object o = ((DefaultMutableTreeNode) value)
								.getUserObject();
						if (o instanceof MonitorNode) {
							MonitorNode mn = (MonitorNode) o;
							if (mn.hasExpired()) {
								setEnabled(false);
							}
						}
					}
					return this;
				}
			});
		}

		@Override
		public void setModel(TreeModel model) {
			if (treeModel == model)
				return;
			if (treeModelListener == null)
				treeModelListener = new TreeModelHandler() {
					public void treeNodesInserted(final TreeModelEvent ev) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								TreePath path = ev.getTreePath();
								setExpandedState(path, true);
								fireTreeExpanded(path);
							}
						});
					}
					public void treeStructureChanged(final TreeModelEvent ev) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								TreePath path = ev.getTreePath();
								setExpandedState(path, true);
								fireTreeExpanded(path);
							}
						});
					}
				};
			if (model != null) {
				model.addTreeModelListener(treeModelListener);
			}
			TreeModel oldValue = treeModel;
			treeModel = model;
			firePropertyChange(TREE_MODEL_PROPERTY, oldValue, model);
		}

	}

}
