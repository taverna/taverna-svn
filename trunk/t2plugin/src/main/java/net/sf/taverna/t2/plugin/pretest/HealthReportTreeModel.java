package net.sf.taverna.t2.plugin.pretest;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workflowmodel.HealthReport;

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
		else if (parent instanceof HealthReport){
			return ((HealthReport)parent).getSubReports().get(index);
		}
		return null;
	}

	public int getChildCount(Object parent) {
		if (parent.equals(getRoot())) {
			return healthReports.size();
		}
		else if (parent instanceof HealthReport){
			return ((HealthReport)parent).getSubReports().size();
		}
		return 0;
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent.equals(getRoot())) {
			return healthReports.indexOf(child);
		}
		else if (parent instanceof HealthReport){
			return ((HealthReport)parent).getSubReports().indexOf(child);
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
		else if (node instanceof HealthReport){
			return ((HealthReport)node).getSubReports().size()==0;
		}
		return true;
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
		int [] childIndices = new int[]{healthReports.indexOf(report)};
		Object [] children = new Object[]{report};
		
		Object [] path = new Object[] {getRoot()};
		for (TreeModelListener listener : listeners.toArray(new TreeModelListener[]{})) {
			TreeModelEvent event = new TreeModelEvent(this,path,childIndices,children);
			listener.treeNodesInserted(event);
		}
	}

}
