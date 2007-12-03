package net.sf.taverna.t2.plugin.pretest;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.ProcessorHealthReport;

public class HealthReportTreeModel implements TreeModel {
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	private List<HealthReport> healthReports = new ArrayList<HealthReport>();
	
	public void addHealthReport(HealthReport report) {
		healthReports.add(report);
		fireNodeInserted(report);
	}

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public Object getChild(Object parent, int index) {
		if (parent.equals(getRoot())) {
			return healthReports.get(index);
		}
		else if (parent instanceof ProcessorHealthReport){
			return ((ProcessorHealthReport)parent).getActivityHealthReports().get(index);
		}
		return null;
	}

	public int getChildCount(Object parent) {
		if (parent.equals(getRoot())) {
			return healthReports.size();
		}
		else if (parent instanceof ProcessorHealthReport){
			return ((ProcessorHealthReport)parent).getActivityHealthReports().size();
		}
		return 0;
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent.equals(getRoot())) {
			return healthReports.indexOf(child);
		}
		else if (parent instanceof ProcessorHealthReport){
			return ((ProcessorHealthReport)parent).getActivityHealthReports().indexOf(child);
		}
		return -1;
	}

	public Object getRoot() {
		return "Report";
	}

	public boolean isLeaf(Object node) {
		if (getRoot().equals(node)) {
			return healthReports.size()==0;
		}
		else {
			return (!(node instanceof ProcessorHealthReport));
		}
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
		Object [] path = new Object[] {getRoot(),report};
		for (TreeModelListener listener : listeners.toArray(new TreeModelListener[]{})) {
			TreeModelEvent event = new TreeModelEvent(this,path);
			listener.treeNodesInserted(event);
		}
	}

}
