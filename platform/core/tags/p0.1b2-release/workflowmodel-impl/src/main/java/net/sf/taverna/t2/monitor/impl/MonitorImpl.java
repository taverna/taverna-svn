/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.monitor.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.monitor.Monitor;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.monitor.MonitorableProperty;

import org.apache.log4j.Logger;

/**
 * Monitor instance backed by a DefaultTreeModel where all nodes are
 * MonitorNodeImpl instances. MonitorNodeImpl is itself a subclass of
 * DefaultMutableTreeNode and overrides the getUserObject method to return
 * itself.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public class MonitorImpl extends DefaultTreeModel implements Monitor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8277297629781179943L;

	private static Logger logger = Logger.getLogger(MonitorImpl.class);

	private long nodeRemovalDelay = 2000;

	private java.util.Timer nodeRemovalTimer;

	/**
	 * Construct a new MonitorImpl
	 */
	public MonitorImpl() {
		super(null);
		setRoot(new MonitorNodeImpl(this, new ProcessIdentifier(),
				new HashSet<MonitorableProperty<?>>()));
		// Create the node removal timer as a daemon thread
		nodeRemovalTimer = new java.util.Timer(true);
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
	public void setNodeRemovalDelay(long delayTime) {
		nodeRemovalDelay = delayTime;
	}

	/**
	 * Return the node pointed to by the first 'limit' number of elements in the
	 * owning process string array. If limit is -1 then use owningProcess.length
	 * 
	 * @param owningProcess
	 * @param limit
	 * @return
	 */
	protected MonitorNodeImpl nodeAtProcessPath(String[] owningProcess,
			int limit) throws IndexOutOfBoundsException {
		if (limit == -1) {
			limit = owningProcess.length;
		}
		MonitorNodeImpl currentNode = (MonitorNodeImpl) this.getRoot();
		for (int index = 0; index < limit && index < owningProcess.length; index++) {
			boolean found = false;
			for (int childIndex = 0; childIndex < this
					.getChildCount(currentNode)
					&& !found; childIndex++) {
				MonitorNodeImpl childNode = (MonitorNodeImpl) this.getChild(
						currentNode, childIndex);
				MonitorNode childMonitorNode = (MonitorNode) childNode
						.getUserObject();
				if (childMonitorNode.getOwningProcess().asArray()[index]
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

	protected String printProcess(String[] process) {
		StringBuffer sb = new StringBuffer();
		for (String part : process) {
			sb.append("{" + part + "}");
		}
		return sb.toString();
	}

	/**
	 * Inject properties into an existing node
	 */
	public void addPropertiesToNode(ProcessIdentifier owningProcess,
			Set<MonitorableProperty<?>> newProperties) {
		String[] parts = owningProcess.asArray();
		try {
			MonitorNode node = nodeAtProcessPath(parts, -1);
			for (MonitorableProperty<?> prop : newProperties) {
				node.addMonitorableProperty(prop);
			}
		} catch (IndexOutOfBoundsException ioobe) {
			// Fail silently here, the node wasn't found in the state tree
			logger.warn("Could not add properties to unknown node "
					+ printProcess(parts));
		}
	}

	/**
	 * Request the removal of the specified node from the monitor tree. In this
	 * particular case the removal task will be added to a timer and executed at
	 * some (slightly) later time as determined by the removalDelay property.
	 */
	public void deregisterNode(ProcessIdentifier owningProcess) {
		// logger.debug("Remove node @" +
		// printProcess(owningProcess));
		String[] parts = owningProcess.asArray();
		final MonitorNodeImpl nodeToRemove = nodeAtProcessPath(parts, -1);
		long expiryTime = System.currentTimeMillis() + getNodeRemovalDelay();
		DeregistrationRequest request = new DeregistrationRequest(nodeToRemove,
				expiryTime);
		nodeToRemove.expire();
		synchronized (deregistrationQueue) {
			System.out.println("Removal request : " + nodeToRemove);
			deregistrationQueue.add(request);
		}
		nodeRemovalTimer.schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				synchronized (deregistrationQueue) {
					while (deregistrationQueue.peek() != null
							&& (deregistrationQueue.peek().requestTime < System
									.currentTimeMillis())) {
						try {
							removeNodeFromParent(deregistrationQueue.poll().node);
						} catch (RuntimeException ex) {
							//
						}
					}
				}
			}
		}, getNodeRemovalDelay() + 100);
	}

	private final BlockingQueue<DeregistrationRequest> deregistrationQueue = new LinkedBlockingQueue<DeregistrationRequest>();

	class DeregistrationRequest {
		public DeregistrationRequest(MonitorNodeImpl node, long time) {
			this.node = node;
			this.requestTime = time;
		}

		MonitorNodeImpl node;
		long requestTime;
	}

	/**
	 * Create a new node in the monitor
	 */
	public void registerNode(final Object workflowObject,
			final ProcessIdentifier owningProcess,
			final Set<MonitorableProperty<?>> properties) {
		// logger.debug("Registering node " + printProcess(owningProcess));

		// Create a new MonitorNode
		final MonitorNodeImpl newNode = new MonitorNodeImpl(workflowObject,
				owningProcess, properties);
		synchronized (this) {
			String[] parts = owningProcess.asArray();
			final MonitorNodeImpl parentNode = nodeAtProcessPath(parts,
					parts.length - 1);
			this.insertNodeInto(newNode, parentNode, this
					.getChildCount(parentNode));
		}
	}

	public long getNodeRemovalDelay() {
		return nodeRemovalDelay;
	}

	public void registerNode(Object workflowObject,
			ProcessIdentifier owningProcess) {
		registerNode(workflowObject, owningProcess,
				new HashSet<MonitorableProperty<?>>());
	}
}
