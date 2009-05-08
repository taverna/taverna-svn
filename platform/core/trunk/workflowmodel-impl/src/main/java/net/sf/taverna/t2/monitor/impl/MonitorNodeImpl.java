/**
 * 
 */
package net.sf.taverna.t2.monitor.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.NoSuchPropertyException;

/**
 * Simple implementation of MonitorNode extending DefaultMutableTreeNode for use
 * by the MonitorTreeModel
 * 
 * @author Tom Oinn
 * 
 */
class MonitorNodeImpl extends DefaultMutableTreeNode implements MonitorNode {

	private static final long serialVersionUID = -3173691158264611626L;
	private boolean expired = false;
	private ProcessIdentifier owningProcess;
	private Set<MonitorableProperty<?>> properties;
	private Object workflowObject;

	Date creationDate = new Date();

	MonitorNodeImpl(Object workflowObject, ProcessIdentifier owningProcess2,
			Set<MonitorableProperty<?>> properties) {
		super(null);
		this.properties = properties;
		this.workflowObject = workflowObject;
		this.owningProcess = owningProcess2;
	}

	@Override
	public Object getUserObject() {
		return this;
	}

	public void addMonitorableProperty(MonitorableProperty<?> newProperty) {
		properties.add(newProperty);
	}

	public void expire() {
		expired = true;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public ProcessIdentifier getOwningProcess() {
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

	public boolean hasExpired() {
		return this.expired;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getWorkflowObject().getClass().getSimpleName());
		sb.append(", ");
		sb.append(owningProcess.toString());
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
}