package uk.org.mygrid.logbook.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.embl.ebi.escience.treetable.AbstractTreeTableModel;
import org.embl.ebi.escience.treetable.TreeTableModel;

import uk.org.mygrid.logbook.ui.util.ProcessRun;
import uk.org.mygrid.logbook.ui.util.ProcessRunWithIterationsImpl;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;

public class ProcessRunsTreeTableModel extends AbstractTreeTableModel implements
		TreeTableModel {

	// private ScuflModel scuflModel;

	// private int rows = 0;

	List<ProcessRun> processRuns;

	final String[] columnNames = { "Name", "Event End Time", "Event detail" };

	public ProcessRunsTreeTableModel(Object root) {
		super(new DefaultMutableTreeNode(root));

	}

	public ProcessRunsTreeTableModel(WorkflowRun w,
			List<ProcessRun> processRuns) {
		super(new DefaultMutableTreeNode(w));
		this.processRuns = processRuns;
		setData(w, processRuns);
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * public ProcessRun getProcessAt(int row) {
	 * 
	 * return processRuns.get(row); }
	 */
	public ProcessRun[] getProcessAt(int[] rows) {
		ProcessRun[] result = new ProcessRun[rows.length];
		for (int i = 0; i < rows.length; i++) {

			result[i] = processRuns.get(rows[i]);

		}

		return result;

	}

	/**
	 * public int getRowCount(){ if(processRuns != null) return
	 * processRuns.size(); else return 0; }
	 */

	public List<ProcessRun> getProcesses() {
		return processRuns;
	}

	public void setData(WorkflowRun w, List<ProcessRun> processes) {

		clear();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getRoot();

		if (w == null)
			root.setUserObject("");
		else
			root.setUserObject(w);

		if (processes == null) {
			TreeNode[] parentPath = { (TreeNode) getRoot() };
			fireTreeStructureChanged(this, parentPath, null, null);
			return;
		}

		this.processRuns = processes;
		for (int i = 0; i < processRuns.size(); i++) {
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					processRuns.get(i));

			if (processRuns.get(i) instanceof ProcessRunWithIterationsImpl) {

				ProcessRunWithIterationsImpl pI = (ProcessRunWithIterationsImpl) processRuns
						.get(i);
				List<ProcessRun> processIterations = pI.getIterations();
				if (processIterations != null)
					addIterations(newNode, processIterations);

			}

			((DefaultMutableTreeNode) getRoot()).add(newNode);
		}
		TreeNode[] parentPath = { (TreeNode) getRoot() };
		fireTreeStructureChanged(this, parentPath, null, null);

	}

	public DefaultMutableTreeNode addIterations(DefaultMutableTreeNode node,
			List<ProcessRun> processIterations) {

		for (int j = 0; j < processIterations.size(); j++) {

			ProcessRun p = processIterations.get(j);
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(p);
			if (processIterations.get(j) instanceof ProcessRunWithIterationsImpl) {

				ProcessRunWithIterationsImpl pI = (ProcessRunWithIterationsImpl) processIterations
						.get(j);
				List<ProcessRun> childProcessIterations = pI.getIterations();
				if (childProcessIterations != null)
					addIterations(childNode, childProcessIterations);

			}
			node.add(childNode);

		}

		return node;

	}

	public void clear() {
		((DefaultMutableTreeNode) getRoot()).removeAllChildren();
		this.processRuns = new ArrayList<ProcessRun>();

	}

	public Class getColumnClass(int c) {
		if (c == 0)
			return TreeTableModel.class;
		else
			return String.class;
	}

	/**
	 * public Object getValueAt(int row, int column) {
	 * 
	 * ProcessRun p = getProcessAt(row); Object result;
	 * 
	 * switch(column){
	 * 
	 * case 0: result = p.getIcon();break; case 1: result = p.getName();break;
	 * case 2: result = p.getDateString();break; case 4: result =
	 * p.getLSID();break; case 5: result = p.getDate();break;
	 * 
	 * case 3:
	 * 
	 * if (p.isFailed()){ result = "<html><font color=\"red\">ServiceFailed</font></html>";}
	 * else{ result = "<html><font color=#1C7366>ProcessCompleted</font></html>"; }
	 * if(p instanceof ProcessRunWithIterations){ result = p; } break; default:
	 * result = null; }
	 * 
	 * 
	 * return result; }
	 */
	public Object getValueAt(Object node, int column) {

		if (node == this.getRoot())
			return "";

		DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) node;

		ProcessRun p = (ProcessRun) theNode.getUserObject();

		Object result;

		switch (column) {

		case 0:
			result = p;
			break;

		case 1:
			result = p.getDateString();
			break;
		case 3:
			result = p.getLsid();
			break;
		case 4:
			result = p.getDate();
			break;

		case 2:

			if (p.isFailed()) {
				result = "<html><font color=\"red\">ServiceFailed</font></html>";
			} else {
				result = "<html><font color=#1C7366>ProcessCompleted</font></html>";
			}
			if (p instanceof ProcessRunWithIterationsImpl) {
				result = "<html><font color=\"purple\">Iterations</font></html>";
			}
			break;
		default:
			result = null;

		}

		return result;

	}

	public Object getChild(Object parent, int index) {
		DefaultMutableTreeNode theParent = (DefaultMutableTreeNode) parent;

		return theParent.getChildAt(index);
	}

	public int getChildCount(Object parent) {
		DefaultMutableTreeNode theParent = (DefaultMutableTreeNode) parent;

		return theParent.getChildCount();
	}

}
