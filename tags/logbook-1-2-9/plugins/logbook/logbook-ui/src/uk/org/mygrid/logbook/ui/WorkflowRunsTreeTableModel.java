package uk.org.mygrid.logbook.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.treetable.AbstractTreeTableModel;
import org.embl.ebi.escience.treetable.TreeTableModel;

import uk.org.mygrid.logbook.ui.util.Workflow;
import uk.org.mygrid.logbook.ui.util.WorkflowNode;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;

public class WorkflowRunsTreeTableModel extends AbstractTreeTableModel
		implements TreeTableModel {

	public static Logger logger = Logger
			.getLogger(WorkflowRunsTreeTableModel.class);

	String[] columns = { "Date", "ID", "Author" };

	static protected Class[] cTypes = { TreeTableModel.class, String.class,
			String.class, String.class };

	Vector<Workflow> workflowNodes;

	boolean failedworkflowFilter;

	boolean failedProcessFilter;

	boolean authorFilter;

	String authorFilterString;

	boolean dateFilter;

	Date startDate;

	Date endDate;

	boolean titleFilter = true;

	String titleFilterString;

	public void setTitleFilterString(String text) {

		titleFilterString = text;

	}

	public WorkflowRunsTreeTableModel() {
		super("root");

	}

	public WorkflowRunsTreeTableModel(Object root,
			Vector<Workflow> workflowNodes) {
		super(root);
		Object[] o = workflowNodes.toArray();
		Arrays.sort(o);
		workflowNodes = new Vector(Arrays.asList(o));
		this.workflowNodes = workflowNodes;
		reset();
	}

	public void setWorkflows(Vector<Workflow> workflowNodes) {
		this.workflowNodes = workflowNodes;
	}

	public void dateClicked() {
		if (dateFilter)
			dateFilter = false;
		else
			dateFilter = true;
		update();
	}

	public void reset() {
		((DefaultMutableTreeNode) getRoot()).removeAllChildren();
		for (int i = 0; i < workflowNodes.size(); i++) {
			Workflow workflow = workflowNodes.get(i);
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					workflow);
			Object[] workflowRuns = workflow.getWorkflowRuns();
			Arrays.sort(workflowRuns);
			for (int j = 0; j < workflowRuns.length; j++) {
				// System.out.println("LSID = " +
				// ((WorkflowRunNode)((WorkflowNode)workflowNodes.get(i)).workflowRuns[j]).LSID);
				Object workflowRun = workflowRuns[j];
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
						workflowRun);
				newNode.add(childNode);
			}
			((DefaultMutableTreeNode) getRoot()).add(newNode);
		}
		TreeNode[] parentPath = { (TreeNode) getRoot() };
		fireTreeStructureChanged(this, parentPath, null, null);
	}

	public void update() {
		reset();
		Enumeration workflowNodesEnumeration = ((DefaultMutableTreeNode) getRoot())
				.children();
		Vector parents = new Vector();
		Pattern p;
		Matcher m;
		while (workflowNodesEnumeration.hasMoreElements()) {
			DefaultMutableTreeNode workflow = (DefaultMutableTreeNode) workflowNodesEnumeration
					.nextElement();
			Enumeration workflowRunsEnumeration = workflow.children();
			Workflow workflowNode = (Workflow) workflow.getUserObject();
			int i = 0;
			List indices = new ArrayList();
			Vector children = new Vector();
			if (authorFilter) {
				p = Pattern.compile(authorFilterString);
				m = p.matcher(workflowNode.getAuthor());
				if (!m.find())
					parents.add(workflow);
			}
			// FIXME can't deal with null strings in the comparison!! this
			// applies to all the comparisons
			if (titleFilter) {
				if (titleFilterString != null && !titleFilterString.equals("")) {
					StringTokenizer s = new StringTokenizer(titleFilterString);
					if (workflowNode.getTitle() == null) {
						parents.add(workflow);
					} else {
						boolean match = false;
						while (s.hasMoreTokens()) {
							String arg = s.nextToken();
							try {
								p = Pattern.compile(arg.toLowerCase());
								m = p.matcher(workflowNode.getTitle()
										.toLowerCase());
								if (m.find())
									match = true;
							} catch (PatternSyntaxException pse) {
								logger.error(pse);
							}
						}
						if (!match)
							parents.add(workflow);
					}
				}
			}

			while (workflowRunsEnumeration.hasMoreElements()) {
				boolean remove = false;
				DefaultMutableTreeNode workflowRun = (DefaultMutableTreeNode) workflowRunsEnumeration
						.nextElement();
				WorkflowRun workflowRunNode = (WorkflowRun) workflowRun
						.getUserObject();
				if (failedProcessFilter
						&& workflowRunNode.isHasFailedProccess())
					remove = true;
				if (dateFilter) {
					if (workflowRunNode.getDate().before(startDate))
						remove = true;
					if (workflowRunNode.getDate().after(endDate))
						remove = true;
				}
				if (remove) {
					indices.add(new Integer(i));
					children.add(workflowRun);
					logger.debug("removing " + workflowRunNode.getLsid() + " "
							+ workflowRunNode.getDate());

					i++;
				}

				// now we have finished processing the workflows inform the
				// eventlisteners of changes
				// TODO need to check if children is null
				for (int k = 0; k < children.size(); k++) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) children
							.get(k)).getParent();
					((DefaultMutableTreeNode) children.get(k))
							.removeFromParent();
					if (parent.getChildCount() <= 0) {
						parents.add(parent);
					}
				}

				if (!workflowNodesEnumeration.hasMoreElements()) {
					for (int k = 0; k < parents.size(); k++) {
						((DefaultMutableTreeNode) parents.get(k))
								.removeFromParent();
					}
				}

				TreeNode[] parentPath = { workflow };
				// fireTreeNodesRemoved(this,parentPath,index,children);

				// TreeNode [] newpath = {(DefaultMutableTreeNode)getRoot()};
				fireTreeStructureChanged(this, parentPath, null, null);

			}
		}

	}

	/**
	 * A printing method for debugging
	 */
	public void printTree() {

		Enumeration workflowNodesEnumeration = ((DefaultMutableTreeNode) getRoot())
				.children();
		while (workflowNodesEnumeration.hasMoreElements()) {

			DefaultMutableTreeNode workflow = (DefaultMutableTreeNode) workflowNodesEnumeration
					.nextElement();
			Enumeration workflowRunsEnumeration = workflow.children();
			while (workflowRunsEnumeration.hasMoreElements()) {

				DefaultMutableTreeNode workflowRun = (DefaultMutableTreeNode) workflowRunsEnumeration
						.nextElement();
				WorkflowRun workflowRunNode = (WorkflowRun) workflowRun
						.getUserObject();
				System.out.println("--" + workflowRunNode.getLsid() + "...");

			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index) {
		DefaultMutableTreeNode theParent = (DefaultMutableTreeNode) parent;
		return theParent.getChildAt(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent) {
		DefaultMutableTreeNode theParent = (DefaultMutableTreeNode) parent;
		return theParent.getChildCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.treetable.TreeTableModel#isCellEditable(java.lang.Object,
	 *      int)
	 */
	public boolean isCellEditable(Object node, int column) {
		return getColumnClass(column) == TreeTableModel.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.treetable.TreeTableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columns.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.treetable.TreeTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return columns[column];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.treetable.TreeTableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int column) {
		return cTypes[column];
	}

	public DefaultMutableTreeNode[] getPath(MutableTreeNode node) {
		return getPathToRoot(node, 0);
	}

	public DefaultMutableTreeNode[] getPathToRoot(MutableTreeNode node,
			int depth) {
		DefaultMutableTreeNode[] retNodes;

		if (node == null) {
			if (depth == 0)
				return null;
			else
				retNodes = new DefaultMutableTreeNode[depth];

		} else {

			depth++;
			retNodes = getPathToRoot((DefaultMutableTreeNode) node.getParent(),
					depth);
			retNodes[retNodes.length - depth] = (DefaultMutableTreeNode) node;

		}

		return retNodes;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.treetable.TreeTableModel#getValueAt(java.lang.Object,
	 *      int)
	 */
	public Object getValueAt(Object node, int column) {

		if (node == this.getRoot()) {
			return "";
		}
		DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) node;

		WorkflowNode workflowNode = (WorkflowNode) theNode.getUserObject();

		switch (column) {
		case 1:
		    return workflowNode.getLsidSuffix();
        case 2:
            return workflowNode.getAuthor();
        case 3:
            return workflowNode.getDescription();
		default:
			// if (workflowRunNode.getCustomTitle() != null
			// && !workflowRunNode.getCustomTitle().equalsIgnoreCase(""))
			// return workflowRunNode.getCustomTitle();
			// else
			// return workflowRunNode.getTitle();
			return workflowNode.getDisplayDate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.treetable.TreeTableModel#setValueAt(java.lang.Object,
	 *      java.lang.Object, int)
	 */
	public void setValueAt(Object aValue, Object node, int column) {

		if (column == 0) {
			DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) node;
			WorkflowRun workflowRunNode = (WorkflowRun) theNode.getUserObject();
			workflowRunNode.setCustomTitle((String) aValue);

			// ((DefaultMutableTreeNode)this.getRoot()).getIndex(theNode);

			int[] indices = { theNode.getParent().getIndex(theNode) };
			TreeNode[] path = getPath((MutableTreeNode) theNode.getParent());
			TreeNode[] children = { theNode };
			fireTreeNodesChanged(this, path, indices, children);
		}

	}

}
