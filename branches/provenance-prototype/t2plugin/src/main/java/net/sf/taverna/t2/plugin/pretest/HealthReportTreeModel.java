package net.sf.taverna.t2.plugin.pretest;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

public class HealthReportTreeModel implements TreeModel {
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	private HealthReport overallReport = new HealthReport("Health Report","",Status.OK);
	
	public void addHealthReport(HealthReport report) {
		overallReport.getSubReports().add(report);
		fireNodeInserted(report);
		if (report.getStatus().compareTo(overallReport.getStatus())>0) {
			overallReport.setStatus(report.getStatus());
			fireRootChanged();
		}
	}

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public Object getChild(Object parent, int index) {
		return ((HealthReport)parent).getSubReports().get(index);
	}

	public int getChildCount(Object parent) {
		return ((HealthReport)parent).getSubReports().size();
	}

	public int getIndexOfChild(Object parent, Object child) {
		return ((HealthReport)parent).getSubReports().indexOf(child);
	}

	public Object getRoot() {
		return overallReport;
	}

	public boolean isLeaf(Object node) {
		return ((HealthReport)node).getSubReports().size()==0;
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		for (TreeModelListener listener : listeners.toArray(new TreeModelListener[]{})) {
			listener.treeNodesChanged(new TreeModelEvent(this,path));
		}
	}
	
	private void fireNodeInserted(HealthReport report) {
		int [] childIndices = new int[]{overallReport.getSubReports().indexOf(report)};
		Object [] children = new Object[]{report};
		
		Object [] path = new Object[] {getRoot()};
		for (TreeModelListener listener : listeners.toArray(new TreeModelListener[]{})) {
			TreeModelEvent event = new TreeModelEvent(this,path,childIndices,children);
			listener.treeNodesInserted(event);
		}
	}
	
	private void fireRootChanged() {
		for (TreeModelListener listener : listeners.toArray(new TreeModelListener[]{})) {
			TreeModelEvent event = new TreeModelEvent(this,new Object[]{getRoot()});
			listener.treeNodesChanged(event);
		}
	}

}
