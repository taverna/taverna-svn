package net.sf.taverna.t2.monitor.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
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

	private Timer nodeRemovalTimer;

	private MonitorImpl() {
		monitorTree = new DefaultTreeModel(new DefaultMutableTreeNode(this));
		// Create the node removal timer as a daemon thread
		nodeRemovalTimer = new Timer(true);
	}

	/**
	 * Request the removal of the specified node from the monitor tree. In this
	 * particular case the removal task will be added to a timer and executed at
	 * some (slightly) later time as determined by the removalDelay property.
	 */
	public void deregisterNode(String[] owningProcess) {
		if (this.isEnabled) {
			final MutableTreeNode nodeToRemove = nodeAtProcessPath(
					owningProcess, -1);
			nodeRemovalTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					synchronized (monitorTree) {
						monitorTree.removeNodeFromParent(nodeToRemove);
					}
				}
			}, deregisterDelay);
		}
	}

	/**
	 * Create a new node in the monitor
	 */
	public void registerNode(final Object workflowObject,
			final String[] owningProcess,
			final Set<? extends MonitorableProperty<?>> properties) {
		if (this.isEnabled) {

			// Create a new MonitorNode
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					new MonitorNode() {
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
							sb.append(getWorkflowObject().toString());
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
					});
			synchronized (monitorTree) {
				MutableTreeNode parentNode = nodeAtProcessPath(owningProcess,
						owningProcess.length - 1);
				monitorTree.insertNodeInto(newNode, parentNode, monitorTree
						.getChildCount(parentNode));
			}
		}
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
			for (int childIndex = 0; childIndex < monitorTree
					.getChildCount(currentNode); childIndex++) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) monitorTree
						.getChild(currentNode, childIndex);
				MonitorNode childMonitorNode = (MonitorNode) childNode
						.getUserObject();
				if (childMonitorNode.getOwningProcess()[index] == owningProcess[index]) {
					currentNode = childNode;
					break;
				}
			}
			throw new IndexOutOfBoundsException(
					"Cannot locate node with process ID " + owningProcess);
		}
		return currentNode;
	}

}
